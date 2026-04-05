// ignore_for_file: constant_identifier_names, deprecated_member_use_from_same_package

part of '../flutter_secure_storage_x.dart';

class AndroidOptions extends Options {
  const AndroidOptions({
    this.resetOnError = false,
    @Deprecated('This option will be removed in v15. '
        'If you use a custom prefix, keep it during v14 so that '
        'existing data can be automatically migrated on first launch. '
        'After migration, the prefix is no longer used.')
    this.preferencesKeyPrefix,
  });

  /// When an error is detected, automatically reset all data. This will prevent
  /// fatal errors regarding an unknown key however keep in mind that it will
  /// PERMANENTLY erase the data when an error occurs.
  ///
  /// Defaults to false.
  final bool resetOnError;

  /// The prefix for a key. If not provided, the default prefix will be used
  /// for migrating legacy data on first launch.
  ///
  /// If you use a custom prefix, keep this value during v14 to ensure
  /// migration completes. Changing prefix at runtime is not supported.
  /// This option will be removed in v15.
  @Deprecated('This option will be removed in v15. '
      'If you use a custom prefix, keep it during v14 so that '
      'existing data can be automatically migrated on first launch. '
      'After migration, the prefix is no longer used.')
  final String? preferencesKeyPrefix;

  static const AndroidOptions defaultOptions = AndroidOptions();

  @override
  Map<String, String> toMap() => <String, String>{
    'resetOnError': '$resetOnError',
    'preferencesKeyPrefix': preferencesKeyPrefix ?? '',
  };

  AndroidOptions copyWith({
    bool? resetOnError,
    @Deprecated('This option will be removed in v15. '
        'Keep custom prefix during v14 for migration.')
    String? preferencesKeyPrefix,
  }) => AndroidOptions(
    resetOnError: resetOnError ?? this.resetOnError,
    preferencesKeyPrefix: preferencesKeyPrefix ?? this.preferencesKeyPrefix,
  );
}
