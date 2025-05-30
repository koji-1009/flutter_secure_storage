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
    key: String?, groupId: String?, accountName: String?, synchronizable: Bool?,
    accessibility: String?, returnData: Bool?
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

    if let synchronizable = synchronizable {
      keychainQuery[kSecAttrSynchronizable] = synchronizable
    }

    if let returnData = returnData {
      keychainQuery[kSecReturnData] = returnData
    }
    return keychainQuery
  }

  internal func containsKey(key: String, groupId: String?, accountName: String?) -> Result<
    Bool, OSSecError
  > {
    // The accessibility parameter has no influence on uniqueness.
    func queryKeychain(synchronizable: Bool) -> OSStatus {
      let keychainQuery = baseQuery(
        key: key, groupId: groupId, accountName: accountName, synchronizable: synchronizable,
        accessibility: nil, returnData: false)
      return SecItemCopyMatching(keychainQuery as CFDictionary, nil)
    }

    let statusSynchronizable = queryKeychain(synchronizable: true)
    if statusSynchronizable == errSecSuccess {
      return .success(true)
    } else if statusSynchronizable != errSecItemNotFound {
      return .failure(OSSecError(status: statusSynchronizable))
    }

    let statusNonSynchronizable = queryKeychain(synchronizable: false)
    switch statusNonSynchronizable {
    case errSecSuccess:
      return .success(true)
    case errSecItemNotFound:
      return .success(false)
    default:
      return .failure(OSSecError(status: statusNonSynchronizable))
    }
  }

  internal func readAll(
    groupId: String?, accountName: String?, synchronizable: Bool?, accessibility: String?
  ) -> FlutterSecureStorageResponse {
    var keychainQuery = baseQuery(
      key: nil, groupId: groupId, accountName: accountName, synchronizable: synchronizable,
      accessibility: accessibility, returnData: true)

    keychainQuery[kSecMatchLimit] = kSecMatchLimitAll
    keychainQuery[kSecReturnAttributes] = true

    var ref: AnyObject?
    let status = SecItemCopyMatching(keychainQuery as CFDictionary, &ref)

    if status == errSecItemNotFound {
      // readAll() returns all elements, so return nil if the items does not exist
      return FlutterSecureStorageResponse(status: errSecSuccess, value: nil)
    }

    var results: [String: String] = [:]

    if status == noErr {
      (ref as! NSArray).forEach { item in
        let key: String = (item as! NSDictionary)[kSecAttrAccount] as! String
        let value: String =
          String(data: (item as! NSDictionary)[kSecValueData] as! Data, encoding: .utf8) ?? ""
        results[key] = value
      }
    }

    return FlutterSecureStorageResponse(status: status, value: results)
  }

  internal func read(key: String, groupId: String?, accountName: String?)
    -> FlutterSecureStorageResponse
  {
    // Function to retrieve a value considering the synchronizable parameter.
    func readValue(synchronizable: Bool?) -> FlutterSecureStorageResponse {
      let keychainQuery = baseQuery(
        key: key, groupId: groupId, accountName: accountName, synchronizable: synchronizable,
        accessibility: nil, returnData: true)

      var ref: AnyObject?
      let status = SecItemCopyMatching(
        keychainQuery as CFDictionary,
        &ref
      )

      // Return nil if the key is not found.
      if status == errSecItemNotFound {
        return FlutterSecureStorageResponse(status: errSecSuccess, value: nil)
      }

      var value: String? = nil

      if status == noErr, let data = ref as? Data {
        value = String(data: data, encoding: .utf8)
      }

      return FlutterSecureStorageResponse(status: status, value: value)
    }

    // First, query without synchronizable, then with synchronizable if no value is found.
    let responseWithoutSynchronizable = readValue(synchronizable: nil)
    return responseWithoutSynchronizable.value != nil
      ? responseWithoutSynchronizable : readValue(synchronizable: true)
  }

  internal func deleteAll(groupId: String?, accountName: String?) -> FlutterSecureStorageResponse {
    let keychainQuery = baseQuery(
      key: nil, groupId: groupId, accountName: accountName, synchronizable: nil, accessibility: nil,
      returnData: nil)
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
      key: key, groupId: groupId, accountName: accountName, synchronizable: nil, accessibility: nil,
      returnData: true)
    let status = SecItemDelete(keychainQuery as CFDictionary)

    // Return nil if the key is not found
    if status == errSecItemNotFound {
      return FlutterSecureStorageResponse(status: errSecSuccess, value: nil)
    }

    return FlutterSecureStorageResponse(status: status, value: nil)
  }

  internal func write(
    key: String, value: String, groupId: String?, accountName: String?, synchronizable: Bool?,
    accessibility: String?
  ) -> FlutterSecureStorageResponse {
    var keychainQuery = baseQuery(
      key: key, groupId: groupId, accountName: accountName, synchronizable: synchronizable,
      accessibility: accessibility, returnData: nil)

    let result = read(key: key, groupId: groupId, accountName: accountName)
    if result.status == errSecSuccess && result.value != nil {
      // Entry exists, try to update it. Change of kSecAttrAccessible not possible via update.
      let update: [CFString: Any?] = [
        kSecValueData: value.data(using: String.Encoding.utf8),
        kSecAttrSynchronizable: synchronizable,
      ]

      let status = SecItemUpdate(keychainQuery as CFDictionary, update as CFDictionary)
      if status == errSecSuccess {
        return FlutterSecureStorageResponse(status: status, value: nil)
      }

      // Update failed, possibly due to different kSecAttrAccessible.
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
