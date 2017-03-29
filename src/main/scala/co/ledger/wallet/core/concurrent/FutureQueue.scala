package co.ledger.wallet.core.concurrent

/**
  *
  * FutureQueue
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

import scala.concurrent.Future

class FutureQueue[A](executionContext: scala.concurrent.ExecutionContext) {

  private implicit val ec = executionContext

  /** *
    * Enqueue a new task to be execute once every task enqueued previously are executed
    * @param task
    */
  def enqueue(task: () => Future[A], name: String = "Task"): Unit = synchronized {
    _tasks.enqueue(Task(task, name))
    dequeue()
  }

  private def dequeue(): Boolean = synchronized {
    if (_currentTask.isEmpty && _tasks.nonEmpty) {
      val task = _tasks.dequeue()
      ec.execute(new Runnable {
        override def run(): Unit = {
          task.fun() andThen {
            case result =>
              //result.failed.foreach(onTaskFailed(task.name, _))
              //result.foreach(onTaskSucceeded(task.name, _))
              synchronized(_currentTask = None)
              dequeue()
          }
        }
      })
      true
    } else {
      false
    }
  }

  protected def onTaskSucceeded(name: String, result: A): Unit = {

  }

  protected def onTaskFailed(name: String, cause: Throwable): Unit = {

  }

  /** *
    * Remove all enqueued task
    * @return The future of the currently executed task
    */
  def removeAll(): Future[AnyRef] = synchronized {
    _tasks.clear()
    _currentTask.getOrElse(Future.successful(null)).map(_.asInstanceOf[AnyRef])
  }

  private[this] val _tasks = scala.collection.mutable.Queue[Task]()
  private[this] var _currentTask: Option[Future[A]] = None

  private case class Task(fun: () => Future[A], name: String)
}