## 1.4.2

 - **REFACTOR**: Update method channel names to use new domain. ([6109404d](https://github.com/koji-1009/flutter_secure_storage/commit/6109404d8b97f883746bb70cc4a76d18619f62f0))

## 1.4.1

 - **FIX**: Fix linux implementation to pass test. ([869eddbc](https://github.com/koji-1009/flutter_secure_storage/commit/869eddbc09f200be104aa63f03aa196bb1040f8f))

## 1.4.0

 - **REFACTOR**: Code cleanup. ([00105189](https://github.com/koji-1009/flutter_secure_storage/commit/00105189eed63fd852a69a9383ac260db05b5696))
 - **FEAT**: Support WASM. ([1bc3da1c](https://github.com/koji-1009/flutter_secure_storage/commit/1bc3da1cba448761aa4a01fd2e9133eacd0dd7b1))

## 1.3.1

 - **DOCS**: Update README. ([06b8b28a](https://github.com/koji-1009/flutter_secure_storage/commit/06b8b28a8f787a5dbaa1ce21922161e2a5b70fee))

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