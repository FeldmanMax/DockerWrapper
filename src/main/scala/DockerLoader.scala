package dwrapper.image.traits

import dwrapper.container.api.DockerContainer
import dwrapper.image.api.DockerImage
import dwrapper.models.api.{Container, Image}
import utils.Implicits

trait DockerLoader {

  def loadContainer(container_name: String,
                    image_name: String,
                    tag: String,
                    dockerFilePath: String,
                    options: Option[Map[String, String]],
                    warmUp: () => Option[String] = Implicits.warmUp): Either[String, Container] = {
    getImage(image_name, tag, dockerFilePath).right.flatMap { image =>
      DockerContainer.run(container_name, image.nameWithTag, options)(warmUp)
    }.right.flatMap { container =>
      warmUp() match {
        case Some(error) => Left(error)
        case None => Right(container)
      }
    }
  }

  def removeContainer(container_id: String): Either[String, Container] = {
    DockerContainer.remove(container_id)
  }

  private def getImage(imageName: String, tag: String, dockerFilePath: String): Either[String, Image] = {
    DockerImage.image(imageName, tag) match {
      case Left(_) =>       DockerImage.createImage(imageName, tag, dockerFilePath, None)
      case Right(image) =>  Right(image)
    }
  }

}

trait DockerExecutor extends DockerLoader {
  def dockerExecute(container_name: String,
                    image_name: String,
                    tag: String,
                    dockerFilePath: String,
                    options: Option[Map[String, String]],
                    remove_after_action: Boolean = true)(action: () => Option[String],
                                                         warmUp: () => Option[String]): Option[String] = {
    loadContainer(container_name, image_name, tag, dockerFilePath, options, warmUp).right.flatMap { container =>
      val action_result: String = action().getOrElse("")
      val removal_result: String = if(remove_after_action)
        removeContainer(container.id) match {
                                              case Left(left) => s"Removal Container Errors \n $left"
                                              case Right(_) => ""
                                            }
      else ""

      val errorMessage: String = toErrorMessage(action_result, removal_result)
      if(errorMessage.isEmpty)  Right("")
      else                      Left(errorMessage)
    } match {
      case Left(left) => Option(left)
      case Right(_) => None
    }
  }

  private def toErrorMessage(action_result: String, removal_result: String): String = {
    val action_error: String = if(action_result.isEmpty) "" else s"Internal Errors \n $action_result \n"
    val removel_error: String = if(removal_result.isEmpty) "" else s"Removal Containers Errors \n $removal_result \n"
    action_error + removel_error
  }
}
