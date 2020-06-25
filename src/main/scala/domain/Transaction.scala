package domain

case class Transaction(transactionId: String, amount: Double, accountNumber: String, time: Long) extends Ordered[Transaction] {
  override def compare(that: Transaction): Int = (this.time - that.time).toInt
}