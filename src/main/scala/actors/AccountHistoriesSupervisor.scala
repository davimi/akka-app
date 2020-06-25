package actors

import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy}
import messages._
import api.{AccountCreated, NotOkResponse}

object AccountHistoriesSupervisor {
  def props = Props(new AccountHistoriesSupervisor)
  def name = "accountHistoriesSupervisor"
}

class   AccountHistoriesSupervisor
  extends Actor
  with ActorLogging {

  override def receive: Receive = {
    case CreateAccountManager(accountNr) =>
      context.child(accountNr)
        .fold {
          createAccountManager(accountNr)
          log.info("Account {} created", accountNr)
          sender ! AccountCreated(accountNr)
        } { _ =>
          sender ! NotOkResponse((NOK("Account exists already")))
        }

    case GetTransactionsByMonth(accountNr) =>
      context.child(accountNr) match {
        case Some(child) => child.forward(GetTransactionsByMonth(accountNr))
        case None =>
          log.warning(s"Account number not found for getting balance")
          sender ! NotOkResponse(NOK("No account exists to get balance"))
      }

    case trxMsg @ ProcessTransaction(accountNr, trx) =>
      context.child(accountNr) match {
        case Some(child) =>
          child.forward(trxMsg)
        case None =>
          log.warning(s"Account number not found for processing transaction: ${trx.accountNumber}")
          sender ! NotOkResponse(NOK("No account exists for transaction"))
      }
  }

  private[actors] def createAccountManager(accountNr: String): ActorRef =
    context.actorOf(AccountHistoryManager.props(accountNr), accountNr)
}
