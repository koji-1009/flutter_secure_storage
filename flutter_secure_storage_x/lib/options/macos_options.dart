part of '../flutter_secure_storage_x.dart';

/// Specific options for macOS platform.
class MacOsOptions extends AppleOptions {
  const MacOsOptions({
    super.groupId,
    super.accountName,
    super.accessibility,
    super.synchronizable,
    bool useDataProtectionKeyChain = true,
  }) : _useDataProtectionKeyChain = useDataProtectionKeyChain;

  static const MacOsOptions defaultOptions = MacOsOptions();

  final bool _useDataProtectionKeyChain;

  MacOsOptions copyWith({
    String? groupId,
    String? accountName,
    KeychainAccessibility? accessibility,
    bool? synchronizable,
    bool? useDataProtectionKeyChain,
  }) =>
      MacOsOptions(
        groupId: groupId ?? _groupId,
        accountName: accountName ?? _accountName,
        accessibility: accessibility ?? _accessibility,
        synchronizable: synchronizable ?? _synchronizable,
        useDataProtectionKeyChain:
            useDataProtectionKeyChain ?? _useDataProtectionKeyChain,
      );

  @override
  Map<String, String> toMap() => <String, String>{
        ...super.toMap(),
        'useDataProtectionKeyChain': '$_useDataProtectionKeyChain',
      };
}
