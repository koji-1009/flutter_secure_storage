## 12.1.1

 - **DOCS**: Add detailed roadmap for Android, Windows, and Linux. ([58c39ca7](https://github.com/koji-1009/flutter_secure_storage/commit/58c39ca7fbe3db7e3f4bc59adccc4df183d095be))

## 12.1.0

 - **REFACTOR**: Remove platform package. ([4a2804b5](https://github.com/koji-1009/flutter_secure_storage/commit/4a2804b5fa1ba327dde91f87993413beedde21df))
 - **REFACTOR**: dart format. ([7bff643c](https://github.com/koji-1009/flutter_secure_storage/commit/7bff643c7c9392546a6a006328bdfaa6d6db71bd))
 - **FEAT**: Flutter 3.35. ([8fbc402f](https://github.com/koji-1009/flutter_secure_storage/commit/8fbc402fa57790553fe9ab9410ebc6d874b36f8f))

## 12.0.0

 - This major version update introduces several significant internal changes to enhance security and performance. On the Android platform, the encryption process, previously implemented independently, has been fully unified into the more secure Android-standard KeyStore system. Existing data saved by the app will be automatically migrated to the new encryption format upon first access. However, please note that this migration support is scheduled to be removed in a future version. Furthermore, Keychain access processing has been comprehensively improved on both iOS and macOS, leading to more efficient and stable operation.

 - **REFACTOR**: Update method channel names to use new domain. ([6109404d](https://github.com/koji-1009/flutter_secure_storage/commit/6109404d8b97f883746bb70cc4a76d18619f62f0))
 - **REFACTOR**: Update ios code. ([ccc9a22d](https://github.com/koji-1009/flutter_secure_storage/commit/ccc9a22deac5b20af5159d63c2fa5ad6a09cd7ad))
 - **FIX**: Modify secretKey. ([5eac7c24](https://github.com/koji-1009/flutter_secure_storage/commit/5eac7c2428330aaf8f5e290ccd1fb374aa4668d5))
 - **FIX**: deleteAll and write. ([3a79e85d](https://github.com/koji-1009/flutter_secure_storage/commit/3a79e85dc24db0031273a44bbaf3d1df4c25d2e3))
 - **FIX**: Remove old code. ([8cfeb9c9](https://github.com/koji-1009/flutter_secure_storage/commit/8cfeb9c96abbb8bdbd7ec4b923073bd7dc7b29f5))
 - **FEAT**: Update datastore-preferences. ([c3b507cc](https://github.com/koji-1009/flutter_secure_storage/commit/c3b507cc4e366b94f4144d74c318d8291e123e65))
 - **FEAT**: Add new KeyStore encryption. ([af1ed0ac](https://github.com/koji-1009/flutter_secure_storage/commit/af1ed0acc2918f77d8e15f1d03c460afd6667fd5))

## 11.3.0

 - **REFACTOR**: Code cleanup and improve error handling. ([16702f78](https://github.com/koji-1009/flutter_secure_storage/commit/16702f7825bb4f9b071f52d2fe6f1d9a49e5046f))
 - **FEAT**: Use Dispatchers.Main. ([c4c4b68f](https://github.com/koji-1009/flutter_secure_storage/commit/c4c4b68f19f5b71020a49923d4c2282e320a71f8))

## 11.2.0

 - **FIX**: Use stackTraceToString. ([69088e35](https://github.com/koji-1009/flutter_secure_storage/commit/69088e35ad087acfb3e9a2a4f6aa28a80d9eeb74))
 - **FEAT**: Kotlin Coroutines. ([55fe402f](https://github.com/koji-1009/flutter_secure_storage/commit/55fe402f9d0ef3f8c0ea4a95fc35df28892ea470))
 - **FEAT**: TargetSDK 35. ([139c723a](https://github.com/koji-1009/flutter_secure_storage/commit/139c723ab6ac25fa0736b372245113cf8c56f635))

## 11.1.0

 - **FEAT**: DataStore v1.1.7. ([2786c155](https://github.com/koji-1009/flutter_secure_storage/commit/2786c155575ea0816c07f1e73193ce293393c480))

## 11.0.0

> Note: This release has breaking changes.

 - **BREAKING** **FEAT**: Remove EncryptedSharedPreferences. ([54b48c7b](https://github.com/koji-1009/flutter_secure_storage/commit/54b48c7b726965d0c52a9784492a397edbf8ec1d))

## 10.2.3

 - Update a dependency to the latest release.

## 10.2.2

 - **FIX**: Fix linux implementation to pass test. ([869eddbc](https://github.com/koji-1009/flutter_secure_storage/commit/869eddbc09f200be104aa63f03aa196bb1040f8f))

## 10.2.1

 - Update a dependency to the latest release.

## 10.2.0

 - **REFACTOR**: Simplify nil check. ([5a0ad611](https://github.com/koji-1009/flutter_secure_storage/commit/5a0ad61156cb98ab10da68566beb26c734d88c81))
 - **REFACTOR**: Check by whether the value can be retrieved. ([d063698d](https://github.com/koji-1009/flutter_secure_storage/commit/d063698d0a18fbeaa6183020d01c1c0761c8372a))
 - **FEAT**: Use serial queue. ([0aeec0c7](https://github.com/koji-1009/flutter_secure_storage/commit/0aeec0c776e58188b72ae7889156973644e0a7df))

## 10.1.1

 - Update a dependency to the latest release.

## 10.1.0

 - **REFACTOR**: Code cleanup. ([00105189](https://github.com/koji-1009/flutter_secure_storage/commit/00105189eed63fd852a69a9383ac260db05b5696))
 - **FEAT**: Support WASM. ([1bc3da1c](https://github.com/koji-1009/flutter_secure_storage/commit/1bc3da1cba448761aa4a01fd2e9133eacd0dd7b1))

## 10.0.1

 - **DOCS**: Update README. ([06b8b28a](https://github.com/koji-1009/flutter_secure_storage/commit/06b8b28a8f787a5dbaa1ce21922161e2a5b70fee))

## 10.0.0

 - Change minimum Android SDK version to 23 and iOS version to 12.0. Android supports DataStore, iOS supports Swift Package Manager.

 - **REFACTOR**: minor fix. ([9f30fe4a](https://github.com/koji-1009/flutter_secure_storage/commit/9f30fe4adbd295b45da34f38d0c40b43760b3512))
 - **REFACTOR**: Code cleanup. ([6cbcd5da](https://github.com/koji-1009/flutter_secure_storage/commit/6cbcd5da2152bf73b03b84fff1952be681c016f7))
 - **REFACTOR**: Fix todo. ([29cf145d](https://github.com/koji-1009/flutter_secure_storage/commit/29cf145dde3ad152f9830456468d4861fabc365a))
 - **REFACTOR**: Remove unncessary if block ([#15](https://github.com/koji-1009/flutter_secure_storage/issues/15)). ([3f6ad6e9](https://github.com/koji-1009/flutter_secure_storage/commit/3f6ad6e91e927d5abcf974368151d0be8c867128))
 - **FIX**: integration test. ([e05ba8c6](https://github.com/koji-1009/flutter_secure_storage/commit/e05ba8c6c6b7b7c6c8a0a8216190e4d62c42deeb))
 - **FIX**: Fix deprecated members. ([8d9e5f41](https://github.com/koji-1009/flutter_secure_storage/commit/8d9e5f411af101cca97a119ca05d505e8c9424c8))
 - **FEAT**: Swift Package Manager. ([a9c0ec14](https://github.com/koji-1009/flutter_secure_storage/commit/a9c0ec14f88586523e0ce3289da10992bd453d23))
 - **FEAT**: DataStore. ([852e696a](https://github.com/koji-1009/flutter_secure_storage/commit/852e696a45f209cca18d201a18cb8e754fc038fc))
 - **FEAT**: Move kotlin dir. ([3d4402ec](https://github.com/koji-1009/flutter_secure_storage/commit/3d4402ecb56f41f4be1c26b1ac5d49ebe6b7599b))
 - **FEAT**: Convert to kotlin (plugin). ([71e4d46e](https://github.com/koji-1009/flutter_secure_storage/commit/71e4d46e954de9922ade0254c3c8d1e8d8b93e3b))
 - **FEAT**: Convert to kotlin (ciphers). ([5adb8964](https://github.com/koji-1009/flutter_secure_storage/commit/5adb8964f1199e3830b4b4246007ab9218ca0c99))
 - **FEAT**: Set min sdk 23. ([0417be2b](https://github.com/koji-1009/flutter_secure_storage/commit/0417be2be197e2427279f297c96b3eba4a0073aa))
 - **FEAT**: Update gradle files. ([5cfdb803](https://github.com/koji-1009/flutter_secure_storage/commit/5cfdb8033a17933cc51950b324d38ab4c0b4574c))
 - **FEAT**: Update example project by flutter 3.24. ([e0159fac](https://github.com/koji-1009/flutter_secure_storage/commit/e0159facf8129cfbb7ef86317afd8ca34aa1ee1c))

## 9.4.0

> Note: This release has breaking changes.

 - **REFACTOR**: fix deprecated. ([105f8024](https://github.com/koji-1009/flutter_secure_storage/commit/105f8024ac8b8b4865aa38aa56edc7b7709dc99b))
 - **REFACTOR**: dart lint fix. ([03f859de](https://github.com/koji-1009/flutter_secure_storage/commit/03f859deaa57d3a1d16fe514f9fc3776ba991735))
 - **BREAKING** **FEAT**: Update dart and flutter version. ([28413719](https://github.com/koji-1009/flutter_secure_storage/commit/28413719b1aff6159590305afa24601f1f784389))

## 9.3.0

 - **FEAT**: flutter_secure_storage_x. ([667cfeab](https://github.com/koji-1009/flutter_secure_storage/commit/667cfeab1a8c80c26cf9e31da37e5c98a22d5e39))

## 9.2.3
* [iOS] Fix for issue #711: The specified item already exists in the keychain.

## 9.2.2
[iOS, macOS] Fixed an issue which caused the readAll and deleteAll to not work properly.

## 9.2.1
* Fix async race condition bug in storage operations.
* [macOS] Return nil on macOS if key is not found

## 9.2.0
New Features:
* [iOS, macOS] Reintroduced isProtectedDataAvailable.
* Listener functionality via `FlutterSecureStorage().registerListener()`

Bugs Fixed:
* [iOS] Return nil on iOS read if key is not found
* [macOS] Also set kSecUseDataProtectionKeychain on read for macos.

## 9.1.1
Reverts new feature because of breaking changes.
* [iOS, macOS] Added isProtectedDataAvailable, A boolean value that indicates whether content protection is active.

## 9.1.0
New Features:
* [iOS, macOS] Added isProtectedDataAvailable, A boolean value that indicates whether content protection is active.

Improvements:
* [iOS, macOS] Use accessibility option for all operations
* [iOS, macOS] Added privacy manifest
* [iOS] Fixes error when no item exists
* [Linux] Fixed search with schemas fails in cold keyrings
* [Linux] Fixed erase called on null
* [Android] Fixed native Android stacktraces in PlatformExceptions
* [Android] Fixed exception when reading data after boot

## 9.0.0
Breaking changes:
* [Windows] Migrated to FFI with win32 package.

## 8.1.0
* [Android] Upgraded to Gradle 8.
* [Android] Fixed resetOnError not working.
* [Windows] Changed PathNotFoundException to FileSystemException to be backwards compatible with Flutter SDK 2.12.0.
* [Windows] Applied lint suggestions.
* [Linux] Remove and replace libjsoncpp1 dependency.
* [Linux, macOS, Windows, Web] Update Dart SDK Constraint to support <4.0.0 instead of <3.0.0.

## 9.0.0
Breaking changes:
* [Windows] Migrated to FFI with win32 package.

## 8.1.0
* [Android] Upgraded to Gradle 8.
* [Android] Fixed resetOnError not working.
* [Windows] Changed PathNotFoundException to FileSystemException to be backwards compatible with Flutter SDK 2.12.0.
* [Windows] Applied lint suggestions.
* [Linux] Remove and replace libjsoncpp1 dependency.
* [Linux, macOS, Windows, Web] Update Dart SDK Constraint to support <4.0.0 instead of <3.0.0.

## 8.0.0
Breaking changes:
* [macOS] The minimum macOS version supported is now 10.14.

Other changes:
* [Android] Fixed an issue when Encrypted Shared Preferences failed, the fallback would not handle the data correctly.
* [Windows] Write encrypted data to files instead of the windows credential system.
* [Linux] Fixed an issue with memory management.

## 7.0.2
[macOS] Fix issue with plugin name.

## 7.0.1
[Android] Reverted double initialization of the SharedPreferences because this will break mixed usage of secureSharedPreference on Android.

## 7.0.0
Breaking changes:
* [macOS] The minimum macOS version supported is now 10.13.

Other changes:
* [Android] Fixed double initialization of the SharedPreferences which caused containsKey and other functions to not work properly.
* [macOS] Upgraded codebase to swift which fixed containsKey always returning true.

## 6.1.0
* [iOS] (From 6.1.0-beta.1) Migrated from objective C to Swift. This also fixes issues with containsKey and possibly other issues.
* [Android] Upgrade security-crypto from 1.1.0-alpha03 to 1.1.0-alpha04
* [Android] Fix deprecation warnings.
* [All] Migrated from flutter_lints to lint and applied suggestions.

## 6.1.0-beta.1
* [iOS] Migrated from objective C to Swift. This also fixes issues with containsKey and possibly other issues.

## 6.0.0
* [Android] Upgrade to Android SDK 33.

## 5.1.2
This version reverts some breaking changes of update 5.1.0.
These changes will become available in version 6.0.0
* [Android] Revert upgrade to Android SDK 33.

## 5.1.1
* Example app dependencies updated
* Updated homepage

## 5.1.0
* [Android] You can now select your own key prefix or database name.
* [Android] Upgraded to Android SDK 33.
* [Android] You can now select the keyCipherAlgorithm and storageCipherAlgorithm.
* [Linux] Fixed an issue where no error was being reported if there was something wrong accessing the secret service.
* [macOS] Fixed an memory-leak.
* [macOS] You can now select the same options as for iOS.

## 5.0.2
* [Android] Fixed bug where sharedPreference object was not yet initialized.

## 5.0.1
* [Android] Added java 8 requirement for gradle build.

## 5.0.0
First stable release of flutter_secure_storage for multi-platform!
Please see all beta release notes for changes.

This first release also fixes several stability issues on Android regarding encrypted shared
preferences.

## [5.0.0-beta.5]
* [Linux, iOS & macOS] Add containsKey function.
* [Linux] Fix for use of undeclared identifier 'flutter_secure_storage_linux_plugin_register_with_registrar'

## [5.0.0-beta.4]
* [Windows] Fixed application crashing when key doesn't exists.
* [Web] Added prefix to local storage key when deleting, fixing items that wouldn't delete.

## [5.0.0-beta.3]
* [Android] Add possibility to reset data when an error occurs.
* [Windows] Add readAll, deleteAll and containsKey functions.
* [All] Refactor option defaults.

## [5.0.0-beta.2]
* [Android] Improved EncryptedSharedPreferences by not loading unused Cipher.
* [Android] Removed deprecated classes
* [Web] Improved containsKey function

## [5.0.0-beta.1]
Initial BETA support for macOS, web & Windows. Development is still ongoing so expect some functions to not work correctly!
Please read the readme.md for information about every platform.

* Migrated to a federated project structure. [#254](https://github.com/mogol/flutter_secure_storage/pull/257). Thanks [jhancock4d](https://github.com/jhancock4d)
* Added support for encrypted shared preferences on Android. [#259](https://github.com/mogol/flutter_secure_storage/pull/259)

## [4.2.1]
* Added kSecAttrSynchronizable support by setting IOSOptions.synchronizable  [#51](https://github.com/mogol/flutter_secure_storage/issues/51)
* Changed deprecated jcenter to mavenCentral [#246](https://github.com/mogol/flutter_secure_storage/pull/246)

## [4.2.0]
* Remove Strongbox for Android [225](https://github.com/mogol/flutter_secure_storage/pull/225). Thanks [JordyLangen](https://github.com/JordyLangen).

## [4.1.0]
* Add support for Linux [185](https://github.com/mogol/flutter_secure_storage/pull/185). Thanks [talhabalaj](https://github.com/talhabalaj)
* Improve first-time read speed on Android by not creating cipher when key is not present. Thanks [PieterAelse](https://github.com/PieterAelse)
* Make it possible to customize iOS account name(kSecAttrService). Thanks [klyver](https://github.com/klyver)

## [4.0.0]
* Introduce null-safety. Thanks [Steve Alexander](https://github.com/SteveAlexander)

## [3.3.5]
* Fix thread safety issues in android code to close [161](https://github.com/mogol/flutter_secure_storage/issues/161). Thanks [koskimas](https://github.com/koskimas)

## [3.3.4]
* Fix Android hanging UI on StorageCipher initialization [#116](https://github.com/mogol/flutter_secure_storage/issues/116) by [morrica](https://github.com/morrica)
* Fix crash only observed for v2 apps [#124](https://github.com/mogol/flutter_secure_storage/pull/124) by [lidongze91](https://github.com/lidongze91)
* Fix crash when generating keys in android with RTL locales [#132](https://github.com/mogol/flutter_secure_storage/pull/132) by [iassal](https://github.com/iassal)
* Fix returning the error as String rather than Exception [#134](https://github.com/mogol/flutter_secure_storage/issues/134) by [wytesk133](https://github.com/wytesk133)s
* Fix Android crash onDetachedFromEngine when init fails [#144](https://github.com/mogol/flutter_secure_storage/issues/144) by [iassal](https://github.com/iassal)
* Handle null value at write function [#95](https://github.com/mogol/flutter_secure_storage/issues/95) by [ewertonrp](https://github.com/ewertonrp)
*  Add support for containsKey [#139](https://github.com/mogol/flutter_secure_storage/issues/139) by [iassal](https://github.com/iassal)

## [3.3.3]
* Fix compatibility with non-AndroidX project. [AndroidX Migration](https://flutter.dev/docs/development/androidx-migration) is recommended.

## [3.3.2]
* Migrate to Android v2 embedder.
* Adds support for specifying [iOS Keychain Item Accessibility](https://developer.apple.com/documentation/security/keychain_services/keychain_items/restricting_keychain_item_accessibility?language=objc).

## [3.3.1+2]
* Fix iOS build warning [Issue 30](https://github.com/mogol/flutter_secure_storage/issues/30)

## [3.3.1+1]
* Fix Android Manifest error [Issue 77](https://github.com/mogol/flutter_secure_storage/issues/77) and [Issue 79](https://github.com/mogol/flutter_secure_storage/issues/79). Thanks [nate-eisner](https://github.com/nate-eisner).

## [3.3.1]
* Fix crash without [iOSOptions](https://github.com/mogol/flutter_secure_storage/issues/73).

## [3.3.0]
* Added groupId for iOS keychain sharing. Thanks [Maleandr](https://github.com/Maleandr).
* Fix Gradle version in `gradle-wrapper.properties`. Thanks [blasten](https://github.com/blasten).
* Added minimum sdk requirement on AndroidManifest. Thanks [lidongze91](https://github.com/lidongze91).

## [3.2.1]
* Fix Android 9.0 Pie [KeyStore exception](https://github.com/mogol/flutter_secure_storage/issues/46).

## [3.2.0]
* **Breaking change**. Migrate from the deprecated original Android Support Library to AndroidX. This shouldn't result in any functional changes, but it requires any Android apps using this plugin to [also migrate](https://developer.android.com/jetpack/androidx/migrate) if they're using the original support library. Thanks [I-am-original](https://github.com/I-am-original).
* Enable StrongBox on Android devices that support it. Thanks [bbedward](https://github.com/bbedward).

## [3.1.3]
* Fix Android 9.0 Pie KeyStore exception. Thanks [hacker1024](https://github.com/hacker1024)

## [3.1.2]
* Added recreating secretKey if its decoding failed. Fix for [unwrap key](https://github.com/mogol/flutter_secure_storage/issues/13). Thanks [hnvn](https://github.com/hnvn).

## [3.1.1]
* Suppress warning about unchecked operations when compiling for Android.

## [3.1.0]
* Added `readAll` and `deleteAll`.

## [3.0.0]
* **Breaking change**. Changed payloads encryption for Android from RSA to AES, AES secret key is encrypted with RSA.

## [2.0.0]````
* **Breaking change**. Changed key alias to fix Android 4.4.2 issue. The plugin isn't able to get previous stored data.

## [1.0.0]
* Bump version

## [0.0.1]

* Initial release
