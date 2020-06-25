package domain

case class AccountHistory(name: String, transactions: Vector[Transaction]) {

  def addTransaction(trx: Transaction) = AccountHistory(name, (this.transactions :+ trx).sorted)
}
