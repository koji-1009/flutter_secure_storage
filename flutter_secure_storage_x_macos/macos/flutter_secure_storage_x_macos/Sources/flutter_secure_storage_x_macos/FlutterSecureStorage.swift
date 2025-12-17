//
//  FlutterSecureStorage.swift
//  flutter_secure_storage_macos
//
//  Created by Julian Steenbakker on 09/12/2022.
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
    synchronizable: Bool? = nil,
    accessibility: String? = nil,
    useDataProtectionKeyChain: Bool? = nil,
    returnData: Bool? = nil
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

    if let useDataProtectionKeyChain = useDataProtectionKeyChain,
      #available(macOS 10.15, *)
    {
      keychainQuery[kSecUseDataProtectionKeychain] = useDataProtectionKeyChain
    }

    if let returnData = returnData {
      keychainQuery[kSecReturnData] = returnData
    }
    return keychainQuery
  }

  internal func containsKey(
    key: String,
    groupId: String?,
    accountName: String?,
    synchronizable: Bool? = nil,
    accessibility: String? = nil,
    useDataProtectionKeyChain: Bool? = nil
  ) -> Result<Bool, OSSecError> {
    let keychainQuery = baseQuery(
      key: key,
      groupId: groupId,
      accountName: accountName,
      synchronizable: nil,
      accessibility: nil,
      useDataProtectionKeyChain: useDataProtectionKeyChain,
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
    synchronizable: Bool? = nil,
    accessibility: String? = nil,
    useDataProtectionKeyChain: Bool? = nil
  ) -> FlutterSecureStorageResponse {
    var keychainQuery = baseQuery(
      key: nil,
      groupId: groupId,
      accountName: accountName,
      synchronizable: nil,
      accessibility: nil,
      useDataProtectionKeyChain: useDataProtectionKeyChain,
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

  internal func read(
    key: String,
    groupId: String?,
    accountName: String?,
    synchronizable: Bool? = nil,
    accessibility: String? = nil,
    useDataProtectionKeyChain: Bool? = nil
  ) -> FlutterSecureStorageResponse {
    let keychainQuery = baseQuery(
      key: key,
      groupId: groupId,
      accountName: accountName,
      synchronizable: nil,
      accessibility: nil,
      useDataProtectionKeyChain: useDataProtectionKeyChain,
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

  internal func deleteAll(
    groupId: String?,
    accountName: String?,
    synchronizable: Bool? = nil,
    accessibility: String? = nil,
    useDataProtectionKeyChain: Bool? = nil
  ) -> FlutterSecureStorageResponse {
    let keychainQuery = baseQuery(
      key: nil,
      groupId: groupId,
      accountName: accountName,
      synchronizable: nil,
      accessibility: nil,
      useDataProtectionKeyChain: useDataProtectionKeyChain,
      returnData: nil
    )
    let status = SecItemDelete(keychainQuery as CFDictionary)

    if status == errSecItemNotFound {
      // deleteAll() deletes all items, so return nil if the items does not exist
      return FlutterSecureStorageResponse(status: errSecSuccess, value: nil)
    }

    return FlutterSecureStorageResponse(status: status, value: nil)
  }

  internal func delete(
    key: String,
    groupId: String?,
    accountName: String?,
    synchronizable: Bool? = nil,
    accessibility: String? = nil,
    useDataProtectionKeyChain: Bool? = nil
  ) -> FlutterSecureStorageResponse {
    let keychainQuery = baseQuery(
      key: key,
      groupId: groupId,
      accountName: accountName,
      synchronizable: nil,
      accessibility: nil,
      useDataProtectionKeyChain: useDataProtectionKeyChain,
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
    synchronizable: Bool? = nil,
    accessibility: String? = nil,
    useDataProtectionKeyChain: Bool? = nil
  ) -> FlutterSecureStorageResponse {
    guard let valueData = value.data(using: .utf8) else {
      return FlutterSecureStorageResponse(status: errSecParam, value: nil)
    }

    let deleteResult = delete(
      key: key,
      groupId: groupId,
      accountName: accountName,
      useDataProtectionKeyChain: useDataProtectionKeyChain
    )
    if let status = deleteResult.status, status != errSecSuccess {
      return deleteResult
    }

    var createQuery = baseQuery(
      key: key,
      groupId: groupId,
      accountName: accountName,
      synchronizable: synchronizable,
      accessibility: accessibility,
      useDataProtectionKeyChain: useDataProtectionKeyChain,
      returnData: nil
    )
    createQuery[kSecValueData] = valueData

    let addStatus = SecItemAdd(createQuery as CFDictionary, nil)
    return FlutterSecureStorageResponse(status: addStatus, value: nil)
  }
}

struct FlutterSecureStorageResponse {
  var status: OSStatus?
  var value: Any?
}

struct OSSecError: Error {
  var status: OSStatus
}
