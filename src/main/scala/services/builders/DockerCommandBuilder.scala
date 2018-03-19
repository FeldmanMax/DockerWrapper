package services.builders

import utils.Implicits.MapExtensions

trait ImageCommandBuilder {
  def createImage(imageName: String, tag: String, dockerPath: String, args: Option[Map[String, String]] = None): String
  def getImages: String = s"docker images"
  def removeImage(imageName: String, tag: String): String
}

class DockerImageCommandBuilder extends ImageCommandBuilder {
  def createImage(imageName: String, tag: String, dockerPath: String, args: Option[Map[String, String]]): String = {
    val argString: String = buildArguments(args)
    val command = s"docker build $argString -t $imageName:$tag -f $dockerPath/Dockerfile $dockerPath"
    command
  }

  def removeImage(imageName: String, tag: String): String = s"docker rmi $imageName:$tag -f"

  private def buildArguments(args: Option[Map[String, String]]): String = {
   args match {
      case None => ""
      case Some(arguments) => "--build-arg " + arguments.mapAsString(" ", "=")
    }
  }
}


trait ContainerCommandBuilder {
  def list(): String
  def run(containerName: String, imageName: String, ports: Option[Map[String, String]]): String
  def stop(container_id: String): String
  def remove(container_id: String): String
}

class DockerContainerCommandBuilder extends ContainerCommandBuilder {
  def run(containerName: String, imageName: String, ports: Option[Map[String, String]]): String = {
    s"docker run -d -i --name $containerName ${buildPorts(ports)} $imageName"
  }

  def list(): String = s"docker container ls -a"
  def stop(container_id: String): String = s"docker stop $container_id"
  def remove(container_id: String): String = s"docker rm $container_id -f"

  private def buildPorts(ports: Option[Map[String, String]]): String = {
    ports match {
      case None => ""
      case Some(data) => " -p " + data.mapAsString(" -p ", ":")
    }
  }
}