package dwrapper.image.api

import dwrapper.models.api.Image
import services.{ImageService, TerminalCommander}
import services.builders.DockerImageCommandBuilder

object DockerImage {

  private lazy val imageService = new ImageService(new TerminalCommander, new DockerImageCommandBuilder)
  def createImage(imageName: String, tag: String, dockerPath: String,
                  args: Option[Map[String, String]]): Either[String, Image] = imageService.createImage(imageName, tag, dockerPath, args)
  def images(): Either[String, List[Image]] =                                 imageService.images()
  def remove(imageName: String, tag: String): Either[String, Image] =         imageService.remove(imageName, tag)
  def image(name: String, tag: String): Either[String, Image] =               imageService.image(name, tag)
  def image(full_name: String): Either[String, Image] =                       imageService.image(full_name)
}
