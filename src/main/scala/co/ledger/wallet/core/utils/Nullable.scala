package co.ledger.wallet.core.utils

/**
  * Created by alix on 4/6/17.
  */
class Nullable[T](v : Option[T]) {
  def value = {
    v
  }
}

object Nullable {
  def apply[T](v: Option[T]): Nullable[T] = {
    Nullable[T](v)
  }
}