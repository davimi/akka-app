package app

import com.typesafe.config.{ConfigValue, ConfigValueFactory}
import scala.language.implicitConversions

object Utils {
  implicit def toConfigValueConverter[T](value: T): ConfigValue = {
    ConfigValueFactory.fromAnyRef(value)
  }
}
