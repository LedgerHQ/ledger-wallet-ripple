package co.ledger.wallet.core.device.ethereum

import java.util.UUID

import co.ledger.wallet.core.concurrent.FutureQueue
import co.ledger.wallet.core.device.Device
import co.ledger.wallet.core.utils.logs.{Loggable, Logger}
import co.ledger.wallet.core.utils.{BytesReader, BytesWriter, HexUtils}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

/**
  *
  * LedgerCommonApiInterface
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

trait LedgerCommonApiInterface extends Loggable {
  import LedgerCommonApiInterface._

  implicit val ec: ExecutionContext
  def device: Device

  val Ok = Array(0x9000)

  /**
    * Send an APDU to the chip
    *
    * @param cla Instruction class
    * @param ins Instruction
    * @param p1 First parameter
    * @param p2 Second parameter
    * @param lc Length of data to send
    * @param data Data to transfer
    * @param le Maximum length to received
    * @return
    */
  protected def sendApdu(cla: Int, ins: Int, p1: Int, p2: Int, lc: Int, data: Array[Byte], le: Int)
  : Future[CommandResult] = {
    val raw = new BytesWriter(6 + data.length)
      .writeByte(cla)
      .writeByte(ins)
      .writeByte(p1)
      .writeByte(p2)
      .writeByte(lc)
      .writeByteArray(data)
      .toByteArray
    sendApdu(raw)
  }

  /**
    * Send an APDU to the chip (sets lc to data length)
    *
    * @param cla
    * @param ins
    * @param p1
    * @param p2
    * @param data
    * @param le
    * @return
    */
  protected def sendApdu(cla: Int, ins: Int, p1: Int, p2: Int, data: Array[Byte], le: Int)
  : Future[CommandResult] = {
    sendApdu(cla, ins, p1, p2, data.length, data, le)
  }

  protected def sendApdu(cla: Int, ins: Int, p1: Int, p2: Int, lc: Int, le: Int):
  Future[CommandResult] ={
    sendApdu(cla, ins, p1, p2, lc, Array.empty[Byte], le)
  }

  protected def sendApdu(command: Array[Byte]): Future[CommandResult] = {
    device.readyForExchange flatMap {(_) =>
      Logger.v(s"=> ${HexUtils.bytesToHex(command)}")("APDU")
      device.exchange(command)
    } map {(result) =>
      Logger.v(s"<= ${HexUtils.bytesToHex(result)}")("APDU")
      new CommandResult(result)
    }
  }

  protected def sendApduSplit(cla: Int,
                              ins: Int,
                              p1: Int,
                              p2: Int,
                              data: Array[Byte],
                              acceptedSw: Array[Int]): Future[CommandResult] = {
    val apdus = new ArrayBuffer[Array[Byte]]()
    var offset = 0

    while (offset < data.length) {
      val blockLength = Math.min(255, data.length - offset)
      val apdu = new BytesWriter(blockLength + 5)
      apdu
        .writeByte(cla)
        .writeByte(ins)
        .writeByte(p1)
        .writeByte(p2)
        .writeByte(blockLength)
        .writeByteArray(data.slice(offset, offset + blockLength))
      apdus += apdu.toByteArray
      offset += blockLength
    }

    val promise = Promise[CommandResult]()
    def iterate(index: Int): Unit = {
      val apdu = apdus(index)
      sendApdu(apdu) onComplete {
        case Success(result) =>
          if (acceptedSw.contains(result.sw) && (index + 1) < apdus.length) {
            iterate(index + 1)
          } else {
            promise.success(result)
          }
        case Failure(ex) =>
          promise.failure(ex)
      }
    }
    iterate(0)
    promise.future
  }

  protected def sendApduSplit2(cla: Int,
                               ins: Int,
                               p1: Int,
                               p2: Int,
                               data: Array[Byte],
                               data2: Array[Byte],
                               acceptedSw: Array[Int]): Future[CommandResult] = {
    val apdus = new ArrayBuffer[Array[Byte]]()
    val maxBlockSize = 255 - data2.length
    var offset = 0

    while (offset < data.length) {
      val blockLength = Math.min(maxBlockSize, data.length - offset)
      val isLastBlock = (offset + blockLength) == data.length
      val apdu = new BytesWriter(blockLength + 5 + (if (isLastBlock) data2.length else 0))
      apdu
        .writeByte(cla)
        .writeByte(ins)
        .writeByte(p1)
        .writeByte(p2)
        .writeByte(blockLength + (if (isLastBlock) data2.length else 0))
        .writeByteArray(data.slice(offset, offset + blockLength))
      if (isLastBlock)
        apdu.writeByteArray(data2)
      apdus += apdu.toByteArray
      offset += blockLength
    }

    val promise = Promise[CommandResult]()
    def iterate(index: Int): Unit = {
      val apdu = apdus(index)
      sendApdu(apdu) onComplete {
        case Success(result) =>
          if (acceptedSw.contains(result.sw) && (index + 1) < apdus.length) {
            iterate(index + 1)
          } else {
            promise.success(result)
          }
        case Failure(ex) =>
          promise.failure(ex)
      }
    }
    iterate(0)
    promise.future
  }

  protected def matchErrorsAndThrow(result: CommandResult): Unit = matchErrors(result).get

  protected def matchErrors(result: CommandResult): Try[Unit] = {
    if (result.sw == 0x9000)
      Success()
    else {
      result.sw match {
        case 0x6700 => Failure(LedgerApiIncorrectLengthException())
        case 0x6982 => Failure(LedgerApiInvalidAccessRightException())
        case 0x6A80 => Failure(LedgerApiInvalidDataException())
        case 0x6A82 => Failure(LedgerApiFileNotFoundException())
        case 0x6B00 => Failure(LedgerApiInvalidParameterException())
        case 0x6D00 => Failure(LedgerApiNotImplementedException())
        case 0x6FAA => Failure(LedgerApiNeedUnplugException())
        case code: Int =>
          if ((code & 0x6F00) == 0x6F00)
            Failure(LedgerApiTechnicalProblemException(code))
          else
            Failure(LedgerApiUnknownErrorException(code))
      }
    }
  }

  /** *
    * Schedule a command to run on the device
    *
    * @param name Friendly name to display
    * @param handler The actual body of the method
    * @tparam T
    * @return
    */
  protected def $[T <: Any](name: String)(handler: => Future[T]): Future[T] = {
    val promise = Promise[T]()
    val fun = {() =>
      promise.completeWith(handler)
      promise.future
    }
    _tasks.enqueue(fun, name)
    promise.future
  }

  /** *
    * Same as [[LedgerCommonApiInterface.$()]] but caches the result
    *
    * @param name
    * @param handler
    * @tparam T
    * @return
    */
  protected def $$[T <: Any](name: String)(handler: => Future[T]): Future[T] = synchronized {
    if (!_resultCache.contains(name)) {
      _resultCache(name) = $(name)(handler)
      _resultCache(name).onFailure {
        case all => _resultCache.remove(name)
      }
    }
    _resultCache(name).asInstanceOf[Future[T]]
  }

  def cancelPendingCommands(): Unit = {
    _tasks.removeAll()
  }

  private[this] def _tasks = new FutureQueue[Any](ec) {
    override protected def onTaskFailed(name: String, cause: Throwable): Unit = {
      super.onTaskFailed(name, cause)
    }
  }

  private[this] val _resultCache = scala.collection.mutable.Map[String, Future[Any]]()
  private[this] var _restoreDeviceUuid: Option[UUID] = None
}

object LedgerCommonApiInterface {

  class CommandResult(result: Array[Byte]) {

    private val reader = new BytesReader(result)

    val data = reader.slice(0, reader.length - 2)

    reader.seek(reader.length - 2)

    val sw = (reader.readNextByte() & 0xFF) << 8 | (reader.readNextByte() & 0xFF)
  }

  class LedgerApiException(code: Int, msg: String)
    extends Exception(s"$msg - ${Integer.toHexString(code)}")
  case class LedgerApiIncorrectLengthException() extends
    LedgerApiException(0x6700, "Incorrect length")
  case class LedgerApiInvalidAccessRightException() extends
    LedgerApiException(0x6982, "Security status not satisfied (Bitcoin dongle is locked or invalid access rights)")
  case class LedgerApiInvalidDataException() extends
    LedgerApiException(0x6A80, "Invalid data")
  case class LedgerApiFileNotFoundException() extends
    LedgerApiException(0x6A82, "File not found")
  case class LedgerApiInvalidParameterException() extends
    LedgerApiException(0x6B00, "Incorrect parameter P1 or P2")
  case class LedgerApiNotImplementedException() extends
    LedgerApiException(0x6D00, "Not implemented")
  case class LedgerApiNeedUnplugException() extends
    LedgerApiException(0x6FAA, "You Ledger need to be unplugged")
  case class LedgerApiTechnicalProblemException(code: Int) extends
    LedgerApiException(code, "Technical problem (Internal error, please report)")
  case class LedgerApiUnknownErrorException(code: Int) extends
    LedgerApiException(code, "Unexpected status word")

  case class LedgerUnsupportedFirmwareException() extends
    Exception("This firmware is not supported by the application")

}