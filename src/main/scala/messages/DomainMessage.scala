package messages

import akka.actor.ActorRef
import domain.Transaction

sealed trait DomainMessage
sealed trait AccountHistorySupervisorMessage extends DomainMessage
sealed trait AccountHistoryManagerMessage extends DomainMessage

case class CreateAccountManager(accountNumber: String) extends AccountHistorySupervisorMessage

case class ProcessTransaction(accountNumber: String, transaction: Transaction) extends AccountHistoryManagerMessage

case class GetTransactionsByMonth(accountNumber: String) extends AccountHistoryManagerMessage

case object OK extends DomainMessage
case class NOK(msg: String) extends DomainMessage