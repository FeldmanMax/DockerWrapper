package services

import dwrapper.models.api.Image

trait ImageBuilder {

  def getImage(imageService: ImageService, name: String, tag: String, dockerFilePath: String, mapping: Option[Map[String, String]]): Either[String, Image] = {
    imageService.createImage(name, tag, dockerFilePath, mapping) match {
      case Left(left) =>
        if(left.contains("already exists")) imageService.image(name, tag)
        else Left(left)
      case Right(image) => Right(image)
    }
  }

  def withHelloWorldImage(imageService: ImageService, tag: String): Either[String, Image] = {
    val imageName: String = "hello-world"
    val dockerFilePath: String = new java.io.File(".").getCanonicalPath + "/src/test/resources/dockerfiles.success"
    getImage(imageService, imageName, tag, dockerFilePath, None)
  }
}
