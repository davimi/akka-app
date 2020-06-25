package serialization

import api.{NetBalanceMonthly, TransactionProcessed}
import domain.Transaction
import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpecLike}
import spray.json._

class DomainJsonParserSpec
  extends WordSpecLike
  with Matchers {

  "The JSON parser" should {
    "serialize and deserialize its own output - NetBalanceMonthly" in {
      import AccountInsightsJsonParser._
      val data = Map("2018" -> Map("1" -> 1203.4, "2" -> 5467.09))
      val balance: NetBalanceMonthly = NetBalanceMonthly(data)
      val json = balance.toJson.toString
      val parsed = json.parseJson.convertTo[NetBalanceMonthly]
      parsed should equal(balance)
    }

    "serialize NetBalanceMonthly" in {
      import AccountInsightsJsonParser._
      val data = Map("2018" -> Map("1" -> 1203.4, "2" -> 5467.09))
      val balance: NetBalanceMonthly = NetBalanceMonthly(data)
      val expected = """{"data":{"2018":{"1":1203.4,"2":5467.09}}}"""
      val json = balance.toJson.toString
      expected should equal(json)
    }

    "serialize TransactionProcessed" in {
      import AccountInsightsJsonParser._
      val response = TransactionProcessed("trx123")
      val expected = """{"transactionId":"trx123"}"""
      val json = response.toJson.toString
      json should equal(expected)
    }

  }
}
