package services.parsers

object ResponseParser {
  def asIs(str: String): String = str
  def asArray(str: String, separator: String): Array[String] = str.split(separator).filter(x=>x.nonEmpty)
}
