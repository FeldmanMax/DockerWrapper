package utils

object Implicits {
  implicit class MapExtensions[A, B](data: Map[A, B]) {
    def mapAsString(separator: String, keyValueSeparator: String): String = data.map { case (key,value) =>
      s"$key$keyValueSeparator$value" } mkString separator
  }

  implicit val warmUp: () => Option[String] = () => None
}
