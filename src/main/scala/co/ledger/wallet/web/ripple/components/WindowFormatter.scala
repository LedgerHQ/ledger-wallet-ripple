package co.ledger.wallet.web.ripple.components

import org.scalajs.dom

import scala.scalajs.js

/**
  * Created by alix on 4/19/17.
  */
object WindowManager {
  def open(url: String) = {
    /*val width = 600
    val height = 1200
    js.Dynamic.global.chrome.app.window.create(url, js.Dynamic.literal(outerBounds = js.Dynamic.literal(width = width,
      height = height,
      left = (dom.window.screen.availWidth - width),
      top = (dom.window.screen.availHeight - height))))*/
    js.Dynamic.global.gui.Shell.openExternal(url)
  }
}
//