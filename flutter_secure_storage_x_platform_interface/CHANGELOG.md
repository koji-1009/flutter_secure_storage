## 1.3.0

> Note: This release has breaking changes.

 - **BREAKING** **FEAT**: Update dart and flutter version. ([28413719](https://github.com/koji-1009/flutter_secure_storage/commit/28413719b1aff6159590305afa24601f1f784389))

## 1.2.0

 - **FEAT**: flutter_secure_storage_x. ([667cfeab](https://github.com/koji-1009/flutter_secure_storage/commit/667cfeab1a8c80c26cf9e31da37e5c98a22d5e39))

## 1.1.2
Adds onCupertinoProtectedDataAvailabilityChanged and isCupertinoProtectedDataAvailable via MethodChannelFlutterSecureStorage to prevent breaking changes.

## 1.1.1
Reverts onCupertinoProtectedDataAvailabilityChanged and isCupertinoProtectedDataAvailable.

## 1.1.0
Adds onCupertinoProtectedDataAvailabilityChanged and isCupertinoProtectedDataAvailable.

## 1.0.2
- Update Dart SDK Constraint to support <4.0.0 instead of <3.0.0.

## 1.0.1
- Migrated from flutter_lints to lint and applied suggestions.
- Remove pubspec.lock according to https://dart.dev/guides/libraries/private-files#pubspeclock

## 1.0.0
- Initial release. Contains the interface and an implementation based on method channels.
- Changed effective_dart to flutter_lints