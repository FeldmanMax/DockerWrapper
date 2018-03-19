package dwrapper.models.api

case class Image(id: String, name: String, tag: String, created: String, size: String) {
  def nameWithTag: String = s"$name:$tag"
}
