package co.ledger.wallet.web.ripple.components

import biz.enef.angulate.{Component, ComponentDef, Scope}
import biz.enef.angulate.Module.RichModule

import scala.scalajs.js

/**
  *
  * Selector
  * ledger-wallet-ripple-chrome
  *
  * Created by Pierre Pollastri on 29/07/2016.
  *
  * The MIT License (MIT)
  *
  * Copyright (c) 2016 Ledger
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITEDbing.fr TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  */
@Component(ComponentDef(
  selector = "selector",
  templateUrl = "templates/components/selector.html",
  bind = js.Dictionary(
    "values" -> "@",
    "selected" -> "@",
    "changed" -> "@"
  )
))
class Selector($scope: Scope) {

  def values: js.Dictionary[String] = _values
  def values_=(v: js.Dictionary[String]) = {
    _values = js.Dynamic.global.JSON.parse(v).asInstanceOf[js.Dictionary[String]]
    if (_selected == null)
      _selected = _values.head._1
  }

  def select(key: String) = {
    _selected = key
    if (_onSelectionChanged != null)
      $scope.$parent.$eval(_onSelectionChanged, js.Dynamic.literal(value = key))
  }
  def selected = _selected
  def selected_=(key: String) = _selected = key
  var _selected: String = null
  def isSelected(key: String) = _selected == key

  def changed: String = _onSelectionChanged
  def changed_=(fun: String) =  {
    println(fun.getClass)
    _onSelectionChanged = fun
  }
  private var _onSelectionChanged: String = null

  private var _values: js.Dictionary[String] = js.Dictionary()

}

object Selector {
  def init(module: RichModule) = module.componentOf[Selector]
}