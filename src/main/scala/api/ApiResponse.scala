package api

import messages.NOK

trait ApiResponse
trait AccountAnalysis

case class NetBalanceMonthly(data: Map[String, Map[String, Double]]) extends ApiResponse with AccountAnalysis

case class AccountCreated(accountNr: String) extends ApiResponse

case class TransactionProcessed(transactionId: String) extends ApiResponse

case class NotOkResponse(notOk: NOK) extends ApiResponse