package serialization

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import api.{_}
import domain.Transaction
import messages.NOK
import spray.json.{RootJsonFormat, _}

object AccountInsightsJsonParser extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val printer: PrettyPrinter.type = PrettyPrinter

  implicit val netBalanceMonthlyJsonProtocol: RootJsonFormat[NetBalanceMonthly] = rootFormat(jsonFormat1(NetBalanceMonthly.apply))

  implicit val accountCreatedJsonProtocol: RootJsonFormat[AccountCreated] = rootFormat(jsonFormat1(AccountCreated.apply))

  implicit val NOKProtocol: RootJsonFormat[NOK] = rootFormat(jsonFormat1(NOK.apply))
  implicit val notOkResponseJsonProtocol: RootJsonFormat[NotOkResponse] = rootFormat(jsonFormat1(NotOkResponse.apply))

  implicit val transactionProcessedJsonProtocol: RootJsonFormat[TransactionProcessed] = rootFormat(jsonFormat1(TransactionProcessed.apply))

  implicit val transactionJsonProtocol: RootJsonFormat[Transaction] = rootFormat(jsonFormat4(Transaction.apply))
}