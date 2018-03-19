package services

import dwrapper.models.api.Image
import logger.ApplicationLogger
import services.builders.ImageCommandBuilder
import services.parsers.ResponseParser

class ImageService(val commander: TCommander,
                   val commandBuilder: ImageCommandBuilder) {
  def createImage(imageName: String,
                  tag: String,
                  dockerPath: String,
                  args: Option[Map[String, String]] = None): Either[String, Image] = {
    ApplicationLogger.trace(s"createImage -> imageName: $imageName, tag: $tag, dockerPath: $dockerPath, IF YOU NEED THE ARGS AS WELL - ADD IT LATER")
    image(imageName, tag) match {
      case Right(_) => ApplicationLogger.errorLeft(s"Image already $imageName:$tag already exists")
      case Left(_) =>
        commander.executeRun[Array[String]](commandBuilder.createImage(imageName, tag, dockerPath, args),
          (str: String) => { Right(ResponseParser.asArray(str, "\n"))}).right.flatMap { response => getBuildImageResponse(imageName, tag, response)
        }
      }
  }


  def images(): Either[String, List[Image]] = {
    commander.executeRun[Array[String]](commandBuilder.getImages,
                                       (str: String) => { Right(ResponseParser.asArray(str, "\n"))} ).right.flatMap {getImagesResponse}
  }

  def remove(imageName: String, tag: String): Either[String, Image] = {
    ApplicationLogger.trace(s"remove -> imageName: $imageName, tag: $tag")
    image(imageName, tag).right.flatMap { image_model =>
      commander.run[String](commandBuilder.removeImage(imageName, tag),
        (response: String) => { Right(ResponseParser.asIs(response)) }) match {
        case Left(terminalCommandResponse) => ApplicationLogger.errorLeft(terminalCommandResponse.toString)
        case Right(_) =>
          image(imageName, tag) match {
            case Right(_) => ApplicationLogger.errorLeft(s"Image $imageName:$tag was not removed")
            case Left(_) => Right(image_model)
          }
      }
    }
  }

  def image(name: String, tag: String): Either[String, Image] = {
    images().right.flatMap { images =>
      images.find(x => x.name + ":" + x.tag == s"$name:$tag") match {
        case None => Left(s"Image could not be found $name:$tag")
        case Some(image) => Right(image)
      }
    }
  }

  def image(fullName: String): Either[String, Image] = {
    val nameToTag: Array[String] = fullName.split(":")
    if(nameToTag.length > 2)  Left(s"Unable to read the $fullName")
    else                      image(nameToTag(0), nameToTag(1))
  }

  private def getImagesResponse(data: Array[String]): Either[String, List[Image]] = {
    val result = data.tail.map(parseImage)
    if(result.exists(x=>x.isLeft))  Left(result.filter(x => x.isLeft).map(x => x.left.get) mkString "\n")
    else                            Right(result.map(x=>x.right.get).toList)
  }

  private def getBuildImageResponse(imageName: String, tag: String, response: Array[String]): Either[String, Image] = {
    (for {
      _ <- getLineByPrefix("Successfully built", response, getImageId).right
      _ <- getLineByPrefix("Successfully tagged", response, getTag).right
    } yield image(imageName, tag)).right.flatMap { image => image.right.flatMap { i => Right(i) }}
  }

  private def getLineByPrefix(prefix: String, data: Array[String], parser: String => Either[String, String]): Either[String, String] = {
    try{
      val line: String = data(data.indexWhere(str => str.contains(prefix))).trim
      if(line.isEmpty || line.length == prefix.length)  Left(s"Prefix could not be found $prefix")
      else                                          parser(line)
    }
    catch {
      case ex: Exception => Left(ex.getMessage)
    }
  }

  private def getImageId(line: String): Either[String, String] = Right("Image Id: " + line.replace("Successfully built", "").trim)
  private def getTag(line: String): Either[String, String] = Right("Repository Id: " + line.replace("Successfully tagged", "").trim)
  private def parseImage(line: String): Either[String, Image] = {
    val info: Array[String] = line.split("  ").filter(x=>x.nonEmpty).map(x=>x.trim)
    Right(Image(info(ImageResponseOrder.ID),
                info(ImageResponseOrder.NAME),
                info(ImageResponseOrder.TAG),
                info(ImageResponseOrder.TIMESTAMP),
                info(ImageResponseOrder.SIZE)))
  }
}


object ImageResponseOrder {
  val NAME: Int = 0
  val TAG: Int = 1
  val ID: Int = 2
  val TIMESTAMP: Int = 3
  val SIZE: Int = 4
}