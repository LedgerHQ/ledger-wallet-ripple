package co.ledger.wallet.web.ripple.content

import java.util.Date

import co.ledger.wallet.core.wallet.ripple.{Block, Ether, Transaction}
import co.ledger.wallet.web.ripple.core.database.Model

/**
  *
  * TransactionModel
  * ledger-wallet-ripple-chrome
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
class TransactionModel extends Model("transaction") {
  val hash = string("hash").unique().index().encrypted()
  val receivedAt = date("receivedAt").index()
  val value = string("value")
  val gas = string("gas")
  val gasUsed = string("gasUsed")
  val gasPrice = string("gasPrice")
  val cumulativeGasUsed = string("cumulativeGasUsed")
  val from = string("from").encrypted()
  val to = string("to").encrypted()
  val blockHash = string("blockHash").encrypted()
  val blockHeight = long("blockHeight").encrypted()
  val nonce = string("nonce").index()
  val data = string("data")

  def proxy: Transaction = {
    new Transaction {
      override def gas: Ether = Ether(TransactionModel.this.gas().get)

      override def nonce: BigInt = BigInt(TransactionModel.this.nonce().get, 16)

      override def gasPrice: Ether = Ether(TransactionModel.this.gas().get)

      override def block: Option[Block] = None

      override def from: String = TransactionModel.this.from().get

      override def data: String = TransactionModel.this.data().get

      override def hash: String = TransactionModel.this.hash().get

      override def to: String = TransactionModel.this.to().get

      override def value: Ether = Ether(TransactionModel.this.gas().get)

      override def cumulativeGasUsed: Ether = Ether(TransactionModel.this.gas().get)

      override def gasUsed: Ether = Ether(TransactionModel.this.gas().get)

      override def receivedAt: Date = new Date(TransactionModel.this.receivedAt().get.getTime().toLong)
    }
  }
}
