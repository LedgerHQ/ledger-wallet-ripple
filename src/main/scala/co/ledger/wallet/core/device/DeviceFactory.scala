/**
  *
  * DeviceConnectionManager
  * Ledger wallet
  *
  * Created by Pierre Pollastri on 15/01/16.
  *
  * The MIT License (MIT)
  *
  * Copyright (c) 2015 Ledger
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
package co.ledger.wallet.core.device

import co.ledger.wallet.core.device.DeviceFactory.ScanRequest
import co.ledger.wallet.core.device.DeviceManager.ConnectivityType

import scala.concurrent.{ExecutionContext, Future, Promise}

trait DeviceFactory {

  /***
    * Check if the android device is compatible with the technology (may block the current thread)
    *
    * @return true if compatible false otherwise
    */
  def isCompatible: Boolean

  /***
    * Check if service is enabled (may block the current thread)
    *
    * @return true if enabled false otherwise
    */
  def isEnabled: Boolean

  /***
    * Check if the manager has enough permissions to run (may block the current thread)
    *
    * @return true if the manager has all required permissions false otherwise
    */
  def hasPermissions: Boolean

  def requestPermission(): Future[Unit]

  def requestScan(): ScanRequest

  def connectivityType: ConnectivityType
}

object DeviceFactory {
  val DefaultScanDuration = 10000L
  val InfiniteScanDuration = -1L

  trait DeviceScanCallback {

  }

  trait ScanRequest {
    def start(): Unit = {
      if (_promise.isCompleted || _isStarted)
        throw new IllegalStateException("Request already completed")
      _isStarted = true
      onStart()
      if (duration != InfiniteScanDuration) {
        /*
        new Handler().postDelayed(new Runnable {
          override def run(): Unit = stop()
        }, duration)
        */
      }
    }
    def onStart(): Unit
    def onStop(): Unit

    def stop(): Unit = {
      if (!_promise.isCompleted) {
        notifyEnd()
        onStop()
        _callback = None
        _ec = None
      }

    }

    private[this] var _duration = DefaultScanDuration
    def duration = _duration
    def duration_=(duration: Long) = _duration = duration

    def onScanUpdate(callback: PartialFunction[ScanUpdate, Unit])(implicit ec: ExecutionContext): Unit = {
      _callback = Some(callback)
      _ec = Some(ec)
    }

    def future: Future[Array[Device]] = _promise.future

    protected def notifyDeviceDiscovered(device: Device): Unit = {
      if (!_devices.contains(device)) {
        _devices = _devices :+ device
        _callback.foreach({ (callback) =>
          _ec.get.execute(new Runnable {
            override def run(): Unit = callback(DeviceDiscovered(device))
          })

        })
      }
    }

    protected def notifyDeviceLost(device: Device): Unit = {
      if (_devices.contains(device)) {
        _devices = _devices.filter(_ != device)
        _callback foreach { (callback) =>
          _ec.get.execute(new Runnable {
            override def run(): Unit = callback(DeviceLost(device))
          })
        }
      }
    }

    protected def notifyEnd(): Unit = {
      _promise.success(_devices)
    }

    protected def notifyFailure(throwable: Throwable): Unit = {
      _promise.failure(throwable)
      stop()
    }

    private[this] val _promise: Promise[Array[Device]] = Promise()
    private[this] var _callback: Option[PartialFunction[ScanUpdate, Unit]] = None
    private[this] var _ec: Option[ExecutionContext] = None
    private[this] var _devices: Array[Device] = Array()
    private[this] var _isStarted = false
  }

  trait ScanUpdate
  case class DeviceDiscovered(device: Device) extends ScanUpdate
  case class DeviceLost(device: Device) extends ScanUpdate

  class ScanException(msg: String) extends Exception(msg)
  case class ScanAlreadyStartedException() extends ScanException("Scan already started")
  case class ScanFailedApplicationRegistrationException() extends ScanException("Scan failed " +
    "application registration")
  case class ScanInternalErrorException() extends ScanException("Internal error")
  case class ScanUnsupportedFeatureException() extends ScanException("Unsupported feature")

}