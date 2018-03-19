package services

import dwrapper.container.api.DockerContainer
import dwrapper.image.api.DockerImage
import dwrapper.models.api.{Container, Image}
import org.scalatest.{BeforeAndAfter, FunSuite}
import services.builders.{DockerContainerCommandBuilder, DockerImageCommandBuilder}

class ContainerServiceTest extends FunSuite with BeforeAndAfter with TCommanderBuilder with ImageBuilder {

  private var containerService: ContainerService = _
  private val imageName: String = "image/web-api"
  private val tag: String = "2.0.0"

  before {
    containerService = new ContainerService(
      new TerminalCommander(),
      new DockerContainerCommandBuilder(),
      new ImageService(
        new TerminalCommander(),
        new DockerImageCommandBuilder()
      )
    )
  }

  test("No containers") {
    val cmdContainerResult: String = "CONTAINER ID        IMAGE                 COMMAND             CREATED             STATUS              PORTS                  NAMES"
    val cmdImageResult: String = "REPOSITORY            TAG                 IMAGE ID            CREATED             SIZE"
    val containerServiceMock: ContainerService = new ContainerService(getCommanderToArray(cmdContainerResult),
      new DockerContainerCommandBuilder(),
      new ImageService(getCommanderToArray(cmdImageResult),
        new DockerImageCommandBuilder()))
    containerServiceMock.containers() match {
      case Left(left) => fail(left)
      case Right(containers) => assert(containers.lengthCompare(0) == 0)
    }
  }

  test("get one container") {
    val cmdContainerResult: String = "CONTAINER ID        IMAGE                 COMMAND             CREATED             STATUS              PORTS                  NAMES\ncc01ec7cdc87        image/web_api:1.0.0   \"sbt run\"           4 seconds ago       Up 21 seconds       0.0.0.0:80->9000/tcp   run-web-api-1.0.0"
    val cmdImageResult: String = "REPOSITORY            TAG                 IMAGE ID            CREATED             SIZE\nimage/web_api          1.0.0              171b1344c09f        7 days ago          499MB"
    val containerServiceMock: ContainerService = new ContainerService(getCommanderToArray(cmdContainerResult),
      new DockerContainerCommandBuilder(),
      new ImageService(getCommanderToArray(cmdImageResult),
        new DockerImageCommandBuilder()))
    containerServiceMock.containers() match {
      case Left(left) => fail(left)
      case Right(containers) =>
        assert(containers.lengthCompare(1) == 0)
        val container: Container = containers.head
        assert(container.id == "cc01ec7cdc87")
        assert(container.image.name == "image/web_api")
        assert(container.image.id == "171b1344c09f")
        assert(container.command == "\"sbt run\"")
        assert(container.status == "Up 21 seconds")
    }
  }

  test("flow test - hello world docker") {
    val imageService: ImageService = new ImageService(new TerminalCommander(), new DockerImageCommandBuilder())
    withHelloWorldImage(imageService, "1.0.0") match {
      case Left(left) => fail(left)
      case Right(image) =>
        val containerName: String = "hello-run-1.0.0"
        containerService.run(containerName, image.nameWithTag, None) match {
          case Left(left) => fail(left)
          case Right(container) => assert(container.image.nameWithTag == image.nameWithTag)
            containerService.remove(container.id) match {
              case Left(left) => fail(left)
              case Right(removedContainer) => assert(removedContainer.id == container.id)
                imageService.remove(image.name, image.tag) match {
                  case Left(left) => fail(left)
                  case Right(deletedImage) => assert(deletedImage.id == image.id)
                }
            }
        }
    }
  }

  test("load image - api response") {
    val imageService: ImageService = new ImageService(new TerminalCommander(), new DockerImageCommandBuilder())
    val dockerFilePath: String = "/Users/maksik1/IdeaProjects/ApiResponse"
    getImage(imageService, imageName, tag, dockerFilePath, None) match {
      case Left(left) => fail(left)
      case Right(image) => assert(image.name == imageName)
    }
  }

  test("load api response docker") {
    val myImage: Image = Image("", imageName, tag, "", "")
    val container_name: String = s"web-api-$tag"

    (DockerImage.image(myImage.name, myImage.tag) match { // Get the Image
      case Left(_) => DockerImage.createImage(myImage.name, myImage.tag, "/Users/maksik1/IdeaProjects/ApiResponse", None)
      case Right(image) => Right(image)
    }) match {                        // Start a Container from the Image
      case Left(left) => fail(left)
      case Right(image) => DockerContainer.run(container_name, image.nameWithTag, Some(Map("80" -> "9000"))) match {
        case Left(left) => fail(left)
        case Right(container) => assert(container.image.id == image.id)
          DockerContainer.stop(container.id) match {  //  Stop of the Container
            case Left(left) => fail(left)
            case Right(stopped_container) => assert(container.id == stopped_container.id)
              DockerContainer.remove(stopped_container.id) match {
                case Left(left) => fail(left)
                case Right(removed_container) => assert(stopped_container.id == removed_container.id)
                  DockerContainer.container(removed_container.id) match {
                    case Left(left) => assert(left == s"Container ${removed_container.id} does not exist")
                    case Right(_) => fail(s"Container ${removed_container.id} was not deleted")
                  }
              }
          }
      }
    }
  }
}
