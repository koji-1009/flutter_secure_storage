## 3.3.0

> Note: This release has breaking changes.

 - **REFACTOR**: dart lint fix. ([03f859de](https://github.com/koji-1009/flutter_secure_storage/commit/03f859deaa57d3a1d16fe514f9fc3776ba991735))
 - **BREAKING** **FEAT**: Update dart and flutter version. ([28413719](https://github.com/koji-1009/flutter_secure_storage/commit/28413719b1aff6159590305afa24601f1f784389))

## 3.2.0

 - **FEAT**: flutter_secure_storage_x. ([667cfeab](https://github.com/koji-1009/flutter_secure_storage/commit/667cfeab1a8c80c26cf9e31da37e5c98a22d5e39))

## 3.1.2
Reverts onCupertinoProtectedDataAvailabilityChanged and isCupertinoProtectedDataAvailable.

## 3.1.1
Updated flutter_secure_storage_platform_interface to latest version.

## 3.1.0
Fixed CompanyName and CompanyProduct on Windows are ignored when the lang-charset in the Runner.rc file is not 040904e4

## 3.0.0
- Migrated to win32 package replacing C.
- Changed PathNotFoundException to FileSystemException to be backwards compatible with Flutter SDK 2.12.0
- Applied lint suggestions

## 2.1.1
Revert changes made in version 2.1.0 due to breaking changes.
These changes will be republished under a new major version number 3.0.0.

## 2.1.0
- Changed PathNotFoundException to FileSystemException to be backwards compatible with Flutter SDK 2.12.0
- Applied lint suggestions

## 2.0.0
Write encrypted data to files instead of the windows credential system.

## 1.1.3
Updated flutter_secure_storage_platform_interface to latest version.

## 1.1.2
- Silently ignore errors when deleting keys that don't exist

## 1.1.1
- Fix application crash when key doesn't exists.

## 1.1.0
Features
- Add readAll, deleteAll and containsKey functions.

Bugfixes
- Fix implementation of delete operation to allow null value.

## 1.0.0
- Initial Windows implementation
