package co.ledger.wallet.web.ethereum.core.event

import co.ledger.wallet.core.device.utils.{EventEmitter, EventReceiver}

/**
  *
  * JsEventEmitter
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 30/05/2016.
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
class JsEventEmitter extends EventEmitter {
  override def emit(event: AnyRef): Unit = {
    for (receiver <- _receivers) {
      receiver.receive(event)
    }
  }

  override def register(receiver: EventReceiver): Unit = {
    _receivers += receiver
  }

  override def unregister(receiver: EventReceiver): Unit = {
    _receivers -= receiver
  }

  private val _receivers = scala.collection.mutable.Set[EventReceiver]()
}
