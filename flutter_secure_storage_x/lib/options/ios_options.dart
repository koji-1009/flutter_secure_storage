part of '../flutter_secure_storage_x.dart';

/// Specific options for iOS platform.
class IOSOptions extends AppleOptions {
  const IOSOptions({
    super.groupId,
    super.accountName,
    super.accessibility,
    super.synchronizable,
  });

  static const IOSOptions defaultOptions = IOSOptions();

  IOSOptions copyWith({
    String? groupId,
    String? accountName,
    KeychainAccessibility? accessibility,
    bool? synchronizable,
  }) =>
      IOSOptions(
        groupId: groupId ?? _groupId,
        accountName: accountName ?? _accountName,
        accessibility: accessibility ?? _accessibility,
        synchronizable: synchronizable ?? _synchronizable,
      );
}
