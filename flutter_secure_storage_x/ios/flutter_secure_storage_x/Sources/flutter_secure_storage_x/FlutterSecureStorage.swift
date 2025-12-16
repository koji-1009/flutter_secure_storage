//
//  FlutterSecureStorageManager.swift
//  flutter_secure_storage
//
//  Created by Julian Steenbakker on 22/08/2022.
//

import Foundation

class FlutterSecureStorage {
  private func parseAccessibleAttr(accessibility: String) -> CFString {
    switch accessibility {
    case "passcode":
      return kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly
    case "unlocked":
      return kSecAttrAccessibleWhenUnlocked
    case "unlocked_this_device":
      return kSecAttrAccessibleWhenUnlockedThisDeviceOnly
    case "first_unlock":
      return kSecAttrAccessibleAfterFirstUnlock
    case "first_unlock_this_device":
      return kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
    default:
      return kSecAttrAccessibleWhenUnlocked
    }
  }

  private func baseQuery(
    key: String?,
    groupId: String?,
    accountName: String?,
    synchronizable: Bool?,
    accessibility: String?,
    returnData: Bool?
  ) -> [CFString: Any] {
    var keychainQuery: [CFString: Any] = [
      kSecClass: kSecClassGenericPassword
    ]

    if let accessibility = accessibility {
      keychainQuery[kSecAttrAccessible] = parseAccessibleAttr(
        accessibility: accessibility
      )
    }

    if let key = key {
      keychainQuery[kSecAttrAccount] = key
    }

    if let groupId = groupId {
      keychainQuery[kSecAttrAccessGroup] = groupId
    }

    if let accountName = accountName {
      keychainQuery[kSecAttrService] = accountName
    }

    switch synchronizable {
    case .some(let value):
      keychainQuery[kSecAttrSynchronizable] = value
    case .none:
      keychainQuery[kSecAttrSynchronizable] = kSecAttrSynchronizableAny
    }

    if let returnData = returnData {
      keychainQuery[kSecReturnData] = returnData
    }

    return keychainQuery
  }

  internal func containsKey(key: String, groupId: String?, accountName: String?)
    -> Result<Bool, OSSecError>
  {
    // The accessibility parameter has no influence on uniqueness.
    let keychainQuery = baseQuery(
      key: key,
      groupId: groupId,
      accountName: accountName,
      synchronizable: nil,
      accessibility: nil,
      returnData: false
    )
    let status = SecItemCopyMatching(keychainQuery as CFDictionary, nil)

    switch status {
    case errSecSuccess:
      return .success(true)
    case errSecItemNotFound:
      return .success(false)
    default:
      return .failure(OSSecError(status: status))
    }
  }

  internal func readAll(
    groupId: String?,
    accountName: String?,
    synchronizable: Bool?,
    accessibility: String?
  ) -> FlutterSecureStorageResponse {
    var keychainQuery = baseQuery(
      key: nil,
      groupId: groupId,
      accountName: accountName,
      synchronizable: synchronizable,
      accessibility: accessibility,
      returnData: true
    )

    keychainQuery[kSecMatchLimit] = kSecMatchLimitAll
    keychainQuery[kSecReturnAttributes] = true

    var ref: AnyObject?
    let status = SecItemCopyMatching(keychainQuery as CFDictionary, &ref)

    if status == errSecItemNotFound {
      // readAll() returns all elements, so return nil if the items does not exist
      return FlutterSecureStorageResponse(status: errSecSuccess, value: nil)
    }

    guard status == noErr else {
      return FlutterSecureStorageResponse(status: status, value: nil)
    }

    guard let items = ref as? [[CFString: Any]] else {
      return FlutterSecureStorageResponse(status: errSecSuccess, value: [:])
    }

    var results: [String: String] = [:]
    for item in items {
      guard let key = item[kSecAttrAccount] as? String,
        let data = item[kSecValueData] as? Data,
        let value = String(data: data, encoding: .utf8)
      else {
        continue
      }
      results[key] = value
    }

    return FlutterSecureStorageResponse(status: errSecSuccess, value: results)
  }

  internal func read(key: String, groupId: String?, accountName: String?)
    -> FlutterSecureStorageResponse
  {
    let keychainQuery = baseQuery(
      key: key,
      groupId: groupId,
      accountName: accountName,
      synchronizable: nil,
      accessibility: nil,
      returnData: true
    )

    var ref: AnyObject?
    let status = SecItemCopyMatching(keychainQuery as CFDictionary, &ref)

    if status == errSecItemNotFound {
      return FlutterSecureStorageResponse(status: errSecSuccess, value: nil)
    }

    guard status == noErr, let data = ref as? Data else {
      return FlutterSecureStorageResponse(status: status, value: nil)
    }

    let value = String(data: data, encoding: .utf8)
    return FlutterSecureStorageResponse(status: status, value: value)
  }

  internal func deleteAll(groupId: String?, accountName: String?)
    -> FlutterSecureStorageResponse
  {
    let keychainQuery = baseQuery(
      key: nil,
      groupId: groupId,
      accountName: accountName,
      synchronizable: nil,
      accessibility: nil,
      returnData: nil
    )
    let status = SecItemDelete(keychainQuery as CFDictionary)

    if status == errSecItemNotFound {
      // deleteAll() deletes all items, so return nil if the items does not exist
      return FlutterSecureStorageResponse(status: errSecSuccess, value: nil)
    }

    return FlutterSecureStorageResponse(status: status, value: nil)
  }

  internal func delete(key: String, groupId: String?, accountName: String?)
    -> FlutterSecureStorageResponse
  {
    let keychainQuery = baseQuery(
      key: key,
      groupId: groupId,
      accountName: accountName,
      synchronizable: nil,
      accessibility: nil,
      returnData: nil
    )
    let status = SecItemDelete(keychainQuery as CFDictionary)

    if status == errSecItemNotFound {
      return FlutterSecureStorageResponse(status: errSecSuccess, value: nil)
    }

    return FlutterSecureStorageResponse(status: status, value: nil)
  }

  internal func write(
    key: String,
    value: String,
    groupId: String?,
    accountName: String?,
    synchronizable: Bool?,
    accessibility: String?
  ) -> FlutterSecureStorageResponse {
    var keychainQuery = baseQuery(
      key: key,
      groupId: groupId,
      accountName: accountName,
      synchronizable: synchronizable,
      accessibility: accessibility,
      returnData: nil
    )

    let result = read(key: key, groupId: groupId, accountName: accountName)
    if result.status == errSecSuccess && result.value != nil {
      // Delete the entry and create a new one in the next step.
      _ = delete(key: key, groupId: groupId, accountName: accountName)
    }

    // Entry does not exist or was deleted, create a new entry.
    keychainQuery[kSecValueData] = value.data(using: String.Encoding.utf8)

    let status = SecItemAdd(keychainQuery as CFDictionary, nil)

    return FlutterSecureStorageResponse(status: status, value: nil)
  }
}

struct FlutterSecureStorageResponse {
  var status: OSStatus?
  var value: Any?
}

struct OSSecError: Error {
  var status: OSStatus
}
