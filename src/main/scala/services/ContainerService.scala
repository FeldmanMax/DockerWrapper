package services

import dwrapper.models.api.Container
import services.builders.ContainerCommandBuilder
import services.parsers.ResponseParser

class ContainerService(val commander: TCommander,
                       val commandBuilder: ContainerCommandBuilder,
                       val imageService: ImageService) {
  def run(containerName: String,
          imageName: String,
          ports: Option[Map[String, String]]): Either[String, Container] = {
    commander.executeRun[Container](commandBuilder.run(containerName, imageName, ports), startParser)
  }

  def stop(container_id: String): Either[String, Container] = container(container_id).right.flatMap { _ => stopImpl(container_id) }

  def remove(container_id: String): Either[String, Container] = {
    stop(container_id).right.flatMap { removedContainer =>
      commander.run[Array[String]](commandBuilder.remove(container_id), (str: String) => { Right(ResponseParser.asArray(str, "\n")) }) match {
        case Left(left) =>  Left(left.toString)
        case Right(_) =>    Right(removedContainer)
      }
    }.right.flatMap { removedContainer =>
      if(container(container_id).isRight)   Left(s"Count not remove container $container_id")
      else                                  Right(removedContainer)
    }
  }

  def containers(): Either[String, List[Container]] = {
    commander.executeRun[Array[String]](commandBuilder.list(), (str: String) => { Right(ResponseParser.asArray(str, "\n")) })
      .right.flatMap { containersParser }
  }

  def container(container_id: String): Either[String, Container] = {
    containers().right.flatMap { containers =>
      containers.find(x => x.id == container_id) match {
        case None => Left(s"Container $container_id does not exist")
        case Some(container) => Right(container)
      }
    }
  }

  private def stopImpl(container_id: String): Either[String, Container] = {
    commander.executeRun[Array[String]](commandBuilder.stop(container_id),
                                       (str: String) => { Right(ResponseParser.asArray(str, "\n")) }).right.flatMap {
      _ => container(container_id)
    }
  }

  private def containersParser(containers: Array[String]): Either[String, List[Container]] = {
    val result: List[Either[String, Container]] = containers.tail.map(parseContainer).toList
    if(result.exists(x=>x.isLeft))  Left(result.filter(x => x.isLeft).map(x => x.left.get) mkString "\n")
    else                            Right(result.map(x=>x.right.get))
  }

  private def startParser(container_id: String): Either[String, Container] = container(container_id.take(12))

  private def parseContainer(line: String): Either[String, Container] = {
    val info: Array[String] = line.split("  ").filter(x=>x.nonEmpty).map(x=>x.trim)
    imageService.image(info(ContainerResponseOrder.IMAGE)).right.flatMap { image =>
      try {
        Right(Container(info(ContainerResponseOrder.ID),
          image,
          info(ContainerResponseOrder.COMMAND),
          info(ContainerResponseOrder.STATUS)))
      }
      catch {
        case ex: Exception => Left(ex.getMessage)
      }
    }
  }
}

object ContainerResponseOrder {
  val ID: Int = 0
  val IMAGE: Int = 1
  val COMMAND: Int = 2
  val CREATED: Int = 3
  val STATUS: Int = 4
}
