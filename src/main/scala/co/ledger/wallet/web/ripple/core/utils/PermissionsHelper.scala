package co.ledger.wallet.web.ripple.core.utils

import chrome.permissions.APIPermission

import scala.concurrent.Future

/**
  *
  * PermissionsHelper
  * ledger-wallet-ripple-chrome
  *
  * Created by Pierre Pollastri on 10/05/2016.
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
object PermissionsHelper {
  import scala.concurrent.ExecutionContext.Implicits.global

  def has(permissionName: String) = chrome.permissions.Permissions.contains(APIPermission(permissionName, ""))
  def request(permissionName: String) = chrome.permissions.Permissions.request(APIPermission(permissionName, ""))

  def requestIfNecessary(permissionName: String) = has(permissionName) flatMap {(hasPermission) =>
    if (hasPermission)
      Future.successful(true)
    else
      request(permissionName)
  }

  /*
  class ledger.managers.Permissions extends EventEmitter

  request: (permissions, callback) ->
    if not permissions?
      callback?(no)
    if _.isString permissions
      permissions = {permissions: [permissions]}
    chrome.permissions.request permissions, (granted) =>
      callback?(granted)

  getAll: (callback) ->
    chrome.permissions.getAll (permissions) =>
      callback?(permissions)

  has: (permissions, callback) ->
    if not permissions?
      callback?(no)
    if _.isString permissions
      permissions = {permissions: [permissions]}
    chrome.permissions.contains permissions, (granted) =>
      callback?(granted)

  remove: (permissions, callback) ->
    if not permissions?
      callback?(no)
    if _.isString permissions
      permissions = {permissions: [permissions]}
    chrome.permissions.remove permissions, (removed) =>
      callback?(removed)

ledger.managers.permissions = new ledger.managers.Permissions()
   */

}
