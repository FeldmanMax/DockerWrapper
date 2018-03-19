package dwrapper.container.api

import dwrapper.models.api.Container
import services.{ContainerService, ImageService, TerminalCommander}
import services.builders.{DockerContainerCommandBuilder, DockerImageCommandBuilder}
import utils.Implicits

object DockerContainer {

  private lazy val containerService = new ContainerService(new TerminalCommander, new DockerContainerCommandBuilder,
    new ImageService(new TerminalCommander, new DockerImageCommandBuilder))

  def run(containerName: String,
          imageName: String,
          ports: Option[Map[String, String]])(implicit warmUp: () => Option[String] = Implicits.warmUp): Either[String, Container] = {
    val container: Either[String, Container] = containerService.run(containerName, imageName, ports)
    warmUp() match {
      case None => container
      case Some(value) => Left(value)
    }
  }

  def stop(container_id: String): Either[String, Container] = containerService.stop(container_id)

  def remove(container_id: String): Either[String, Container] = containerService.remove(container_id)

  def containers(): Either[String, List[Container]] = containerService.containers()

  def container(container_id: String): Either[String, Container] = containerService.container(container_id)
}
