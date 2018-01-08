/**
  * Created by alix on 5/17/17.
  */
package object exceptions {

  case class RippleException() extends Exception("The Ripple Network is not responding")

  case class DisconnectedException() extends Exception("Connection lost to the Ripple Network")

  case class MissingTagException() extends Exception("The recipient requires a destination tag")

  case class FeesException() extends Exception("The Ripple API does not respond")

  case class UnfundedException() extends Exception("The payment exceeds the balance capacity")

  case class UnknownException(val name: String) extends Exception("Unknown exception occured")

  case class SelfSendException() extends Exception("Sending funds to self")
}
