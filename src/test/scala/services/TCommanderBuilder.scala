package services

import services.parsers.ResponseParser

trait TCommanderBuilder {

  def getCommanderToArray(retValue: String): TCommander = {
    new TCommander {
      override def run[R](command: String, parser: String => Either[String, R]): Either[TerminalCommandResponse, R] = {
        Right(ResponseParser.asArray(retValue, "\n").asInstanceOf[R])
      }

      override def executeRun[R](command: String, parser: String => Either[String, R]): Either[String, R] = {
        run[R](command, parser) match {
          case Left(left) => Left(left.toString)
          case Right(result) => Right(result)
        }
      }
    }
  }
}
