package co.ledger.wallet.core.utils

/**
  *
  * DerivationPath
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 14/06/2016.
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
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  */
import java.security.InvalidParameterException

import scala.annotation.tailrec

class DerivationPath(p: DerivationPath, val childNum: Long) {

  val parent = if (p == null) this else p
  val depth: Int = parent.depth + 1
  val length = depth + 1

  def /(child: DerivationPath): DerivationPath = {
    new DerivationPath(this, child.childNum)
  }

  def ++(child: DerivationPath): DerivationPath = {
    var path = this
    for (i <- 0 to child.depth) {
      path = new DerivationPath(path, child(i).get.childNum)
    }
    path
  }

  def apply(depth: Int): Option[DerivationPath] = {
    var root = this
    while (root.parent != DerivationPath.Root && root.depth > depth) {
      root = root.parent
    }
    if (root == DerivationPath.Root)
      None
    else
      Some(root)
  }

  def isHardened = childNum >= 0x80000000L

  lazy val index = if (isHardened) childNum - 0x80000000L else childNum

  override def toString: String = {
    if (parent == this)
      "m"
    else
      s"${parent.toString}/${if (isHardened) index + "'" else index}"
  }
}
object DerivationPath {
  object Root extends DerivationPath(null, -1) {
    override val depth = -1
  }
  def apply(path: String): DerivationPath = {
    @tailrec
    def parse(parts: Array[String], offset: Int, node: DerivationPath): DerivationPath = {
      if (offset >= parts.length)
        node
      else if (parts(offset) == "m" && offset == 0)
        parse(parts, offset + 1, Root)
      else {
        val num = parts(offset).takeWhile(_.isDigit)
        if (num.length == 0)
          throw new InvalidParameterException(s"Unable to parse path $path")
        val hardened = parts(offset).endsWith("'") || parts(offset).endsWith("h")
        val childNum = num.toLong + (if (hardened) 0x80000000L else 0L)
        parse(parts, offset + 1, new DerivationPath(node, childNum))
      }
    }
    parse(path.split('/'), 0, Root)
  }
  object dsl {
    implicit class DPInt(val num: Int) {
      def h: DerivationPath = new DerivationPath(Root, num.toLong + 0x80000000L)
    }
    implicit def Int2DerivationPath(num: Int): DerivationPath = new DerivationPath(Root, num)
  }

}