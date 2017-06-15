/**
  * Created by alix on 5/17/17.
  */
package object exceptions {

  case class RippleException() extends Exception("The Ripple Network is not responding")

  case class DisconnectedException() extends Exception("The Ripple Network stopped responding abruptly")

}
