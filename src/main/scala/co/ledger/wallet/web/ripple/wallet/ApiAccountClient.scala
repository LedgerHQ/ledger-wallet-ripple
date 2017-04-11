package co.ledger.wallet.web.ripple.wallet

import java.util.Date

import co.ledger.wallet.core.wallet.ripple.api.AbstractApiAccountClient
import co.ledger.wallet.core.wallet.ripple.api.AbstractApiAccountClient.{AccountSavedState, AccountSavedStateBatch}
import co.ledger.wallet.core.wallet.ripple.database.AccountRow
import co.ledger.wallet.web.ripple.core.utils.ChromePreferences
import co.ledger.wallet.web.ripple.services.SessionService
import org.ripple.api.RippleAPI

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
/**
  *
  * ApiAccountClient
  * ledger-wallet-ripple-chrome
  *
  * Created by Pierre Pollastri on 15/06/2016.
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
class ApiAccountClient(override val wallet: ApiWalletClient, override
protected val password: Option[String], accountRow: AccountRow, api: RippleAPI)
  extends AbstractApiAccountClient(wallet, accountRow, api) with
    IndexedDBBackedAccountClient {

  override protected def load(): Future[AccountSavedState] = {
    Future.successful({
      if (prefs.int("batch_count").isEmpty) {
        new AccountSavedState {
          override var batches: Array[AccountSavedStateBatch] = Array()
          override var lastSynchronizationDate: Long = new Date().getTime
          override var lastSynchronizationStatus: Int = 1
          override var batchSize: Int = 20
          override var index: Int = accountRow.index
        }
      } else {
        val serializedBatches = new ArrayBuffer[AccountSavedStateBatch]()
        for (i <- 0 until prefs.int("batch_count").get) {
          serializedBatches += new AccountSavedStateBatch {
            override var blockHash: String = prefs.string(s"${i}_block_hash").get
            override var blockHeight: Long = prefs.long(s"${i}_block_height").get
            override var index: Int = prefs.int(s"${i}_index").get
          }
        }
        new AccountSavedState {
          override var batches: Array[AccountSavedStateBatch] = serializedBatches.toArray
          override var lastSynchronizationDate: Long = prefs.long("last_sync_date").get
          override var lastSynchronizationStatus: Int = prefs.int("last_sync_status").get
          override var batchSize: Int = prefs.int("batch_size").get
          override var index: Int = ApiAccountClient.this.index
        }
      }
    })
  }

  override protected def save(state: AccountSavedState): Future[Unit] = {
    val editor = prefs.edit()
    editor.putInt("batch_count", state.batches.length)
          .putInt("batch_size", state.batchSize)
          .putLong("last_sync_date", state.lastSynchronizationDate)
          .putInt("last_sync_status", state.lastSynchronizationStatus)
    for (i <- state.batches.indices) {
      val batch = state.batches(i)
      editor.putInt(s"${i}_index", batch.index)
            .putString(s"${i}_block_hash", batch.blockHash)
            .putLong(s"${i}_block_height", batch.blockHeight)
    }
    editor.commit()
    Future.successful()
  }

  private def prefs = {
    val prefName = s"${wallet.name}_api_client_$index"
    new ChromePreferences(prefName)
  }

}
