package co.ledger.wallet.web.ripple.components

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Component, ComponentDef}

import scala.scalajs.js

/**
  * Created by pollas_p on 02/05/2016.
  */
@Component(ComponentDef(
  selector = "counter",  // component name (i.e. the HTML tag)
  template = """<h1>{{count}}</h1> <button ng-click="inc()">+</button> <button ng-click="dec()">-</button> <button ng-click="toto()">toto</button>""",
  // - or -
  // templateUrl = "counter.html"
  bind = js.Dictionary(
    "init" -> "@"  // assign the value of the DOM attribute 'init' to the class property with the same name
  )
))
class Counter {

  var count = 0

  def init = count
  // called with the value of the DOM attribute 'init'
  def init_=(s: String) = count = s.toInt

  def inc() = count += 1

  def dec() = count -= 1

  def toto() = count += 10

}

object Counter {
  def init(module: RichModule) = {
    module.componentOf[Counter]
  }
}