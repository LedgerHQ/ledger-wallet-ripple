/**
  * Created by alix on 5/17/17.
  */
package object exceptions {

  case class RippleException() extends Exception("The Ripple Network is not responding")

  case class DisconnectedException() extends Exception("The Ripple Network stopped responding abruptly")

  case class MissingTagException() extends Exception("The recipient requires a destination tag")

  case class FeesException() extends Exception("The ripple API does not respond")

}
