package services

import org.scalatest.{BeforeAndAfter, FunSuite}
import services.builders.DockerImageCommandBuilder

class ImageServiceTest extends FunSuite with BeforeAndAfter with ImageBuilder {
  private var imageService: ImageService = _

  before {
    imageService = new ImageService(new TerminalCommander, new DockerImageCommandBuilder)
  }

  test("build hello-world 2.0.0 docker") {
    val imageName: String = "hello-world"
    val tag: String = "2.0.0"
    val dockerFilePath: String = new java.io.File(".").getCanonicalPath + "/src/test/resources/dockerfiles.success"
    withHelloWorldImage(imageService, tag) match {
      case Left(left) => fail(left)
      case Right(image) =>
        assert(image.id.nonEmpty)
        assert(image.name == imageName)
        assert(image.tag == tag)
    }
    imageService.createImage(imageName, tag, dockerFilePath) match {
      case Left(message) =>
        assert(message == s"Image already $imageName:$tag already exists")
        imageService.remove(imageName, tag) match {
          case Left(remove_message) => fail(remove_message)
          case Right(image) =>
            assert(image.name == imageName && image.tag == tag)
            imageService.images() match {
              case Left(left) => fail(left)
              case Right(list) => assert(!list.exists(x => x.name == imageName && x.tag == tag))
            }
        }
      case Right(_) => fail("Did not fail when images exists")
    }
  }
}

