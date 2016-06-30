package co.ledger.wallet.core.wallet.ethereum.database

import co.ledger.wallet.core.wallet.ethereum.{Block, Operation, Transaction, Wallet}

import scala.concurrent.Future

/**
  *
  * AbstractDatabaseBackedWalletClient
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
trait DatabaseBackedWalletClient extends Wallet {

  /**
    * Insert or update a new block in the database
    * @param block
    * @return
    */
  def putBlock(block: Block): Future[Unit]

  /**
    * Insert or update a transaction in the database
    * @param transaction
    * @return
    */
  def putTransaction(transaction: Transaction): Future[Unit]
  def putTransactions(transactions: Array[Transaction]): Future[Unit]
  /**
    *
    * @param accountRow
    * @return
    */
  protected def putAccount(accountRow: AccountRow): Future[Unit]

  def putOperation(operation: Operation): Future[Unit]
  def putOperations(operation: Array[Operation]): Future[Unit]

  def startDatabaseTransaction(): Unit
  def commitDatabaseTransaction(): Unit

  protected def queryTransaction(hash: String): Future[Array[Transaction]]
  protected def queryTransactions(hashes: Array[String]): Future[Array[Transaction]]
  protected def queryAccounts(from: Int, to: Int): Future[Array[AccountRow]]
  protected def queryLastBlock(): Future[Block]
}
