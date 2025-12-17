# flutter_secure_storage_x

[![style: lint](https://img.shields.io/badge/style-flutter_lints-4BC0F5.svg)](https://pub.dev/packages/flutter_lints)
[![pub package](https://img.shields.io/pub/v/flutter_secure_storage_x.svg)](https://pub.dev/packages/flutter_secure_storage_x)
[![flutter_secure_storage_x](https://github.com/koji-1009/flutter_secure_storage/actions/workflows/flutter.yml/badge.svg)](https://github.com/koji-1009/flutter_secure_storage/actions/workflows/flutter.yml)
[![flutter_secure_storage_x](https://github.com/koji-1009/flutter_secure_storage/actions/workflows/flutter_drive.yml/badge.svg)](https://github.com/koji-1009/flutter_secure_storage/actions/workflows/flutter_drive.yml)

This package is a fork of the popular [flutter_secure_storage](https://pub.dev/packages/flutter_secure_storage) package. The original project aims to provide a comprehensive set of features and options to cover a wide range of needs.

This fork builds upon the original work with a more focused approach. The philosophy is to ensure long-term stability and maintainability by offering a minimal, robust API for the most common encrypted storage use cases. This pragmatic direction provides a simple and reliable solution for developers who prioritize these aspects.

The plugin utilizes platform-specific secure storage mechanisms:

- On iOS, [Keychain](https://developer.apple.com/library/content/documentation/Security/Conceptual/keychainServConcepts/01introduction/introduction.html#//apple_ref/doc/uid/TP30000897-CH203-TP1) is used.
- On Android, AES encryption with [KeyStore](https://developer.android.com/training/articles/keystore.html) is used.
- For Linux, [`libsecret`](https://wiki.gnome.org/Projects/Libsecret) is utilized.

For more detailed information and usage examples, please refer to the example app.

## Platform Implementation

Please note that this table represents the functions implemented in this repository and it is possible that changes haven't yet been released on pub.dev

|         | read               | write              | delete             | containsKey        | readAll            | deleteAll          | isCupertinoProtectedDataAvailable | onCupertinoProtectedDataAvailabilityChanged |
|---------|--------------------|--------------------|--------------------|--------------------|--------------------|--------------------|-----------------------------------|---------------------------------------------|
| Android | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |                                   |
| iOS     | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark:                | :white_check_mark:                          |
| Windows | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |                                   |
| Linux   | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |                                   |
| macOS   | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark:                | :white_check_mark: (on macOS 12 and newer)  |
| Web     | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |                                   |

## Getting Started

If not present already, please call `WidgetsFlutterBinding.ensureInitialized()` in your `main` before you do anything with the MethodChannel. [Please see this issue for more info.](https://github.com/mogol/flutter_secure_storage/issues/336)

```dart
import 'package:flutter_secure_storage_x/flutter_secure_storage_x.dart';

// Create storage
final storage = FlutterSecureStorage();

// Read value
String? value = await storage.read(key: key);

// Read all values
Map<String, String> allValues = await storage.readAll();

// Delete value
await storage.delete(key: key);

// Delete all
await storage.deleteAll();

// Write value
await storage.write(key: key, value: value);
```

## Important Considerations

### Background Access

By default, this package configures both iOS Keychain and Android KeyStore to only allow data access when the device is unlocked. This is a security feature to protect sensitive data.

This means that if your app is launched in the background (e.g., by a push notification or background fetch) while the device is locked, any attempts to access the secure storage will fail.

If your application requires background access to the storage, you must configure this explicitly. This allows us to be able to fetch secure values while the app is backgrounded, by specifying `first_unlock` or `first_unlock_this_device`. The default if not specified is `unlocked`.

**iOS:**
Use the `iOptions` parameter with an appropriate `KeychainAccessibility` value, such as `KeychainAccessibility.first_unlock_this_device`.

An example using `KeychainAccessibility.first_unlock`:
```dart
final options = IOSOptions(
  accessibility: KeychainAccessibility.first_unlock,
);
await storage.write(
  key: key,
  value: value,
  iOptions: options,
);
```

**Android:**
The default Android settings generally allow background access as long as the device has been unlocked once since booting. No special configuration is typically needed for this use case.

## Platform-specific configuration

### Android

In `[project]/android/app/build.gradle` set `minSdkVersion` to >= 23.

```groovy
android {
    ...

    defaultConfig {
        ...
        minSdkVersion 23
        ...
    }

}
```

_Note_: By default Android backups data on Google Drive. It can cause exception `java.security.InvalidKeyException:Failed to unwrap key`.
You need to:

- [disable autobackup](https://developer.android.com/guide/topics/data/autobackup#EnablingAutoBackup), [details](https://github.com/mogol/flutter_secure_storage/issues/13#issuecomment-421083742)
- [exclude sharedprefs](https://developer.android.com/guide/topics/data/autobackup#IncludingFiles) `FlutterSecureStorage` used by the plugin, [details](https://github.com/mogol/flutter_secure_storage/issues/43#issuecomment-471642126)

DataStore support has been available since v10.0.0. When using DataStore, set the options as follows.

```dart
AndroidOptions _getAndroidOptions() => const AndroidOptions(
  dataStore: true,
);
```

### Web

The web implementation uses WebCrypto and should be considered experimental. It only works on secure contexts (HTTPS or localhost). Feedback is welcome to help improve it.

The intent is that the browser creates the private key, and as a result, the encrypted strings in `local_storage` are not portable to other browsers or other machines and will only work on the same domain.

**It is VERY important that you have HTTP Strict Forward Secrecy enabled and the proper headers applied to your responses or you could be subject to a javascript hijack.**

Please see:

- https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Strict-Transport-Security
- https://www.netsparker.com/blog/web-security/http-security-headers/

#### WASM support

Supported from v2.0.0.

#### application-specific key option

On the web, all keys are stored in LocalStorage. This package has an option for the web to wrap this stored key with an application-specific key to make it more difficult to analyze.

```dart
final _storage = const FlutterSecureStorageX(
  webOptions: WebOptions(
    wrapKey: '${your_application_specific_key}',
    wrapKeyIv: '${your_application_specific_iv}',
  ),
);
```

This option encrypts the key stored in LocalStorage with [WebCrypto wrapKey](https://developer.mozilla.org/en-US/docs/Web/API/SubtleCrypto/wrapKey). It is decrypted with [WebCrypto unwrapKey](https://developer.mozilla.org/en-US/docs/Web/API/SubtleCrypto/unwrapKey) when used.
Generating and managing application-specific keys requires careful attention from developers. See (https://github.com/mogol/flutter_secure_storage/issues/726) for more information.

### Linux

You need `libsecret-1-dev` and `libjsoncpp-dev` on your machine to build the project, and `libsecret-1-0` and `libjsoncpp1` to run the application (add it as a dependency after packaging your app). If you using snapcraft to build the project use the following:

```yaml
parts:
  uet-lms:
    source: .
    plugin: flutter
    flutter-target: lib/main.dart
    build-packages:
      - libsecret-1-dev
      - libjsoncpp-dev
    stage-packages:
      - libsecret-1-0
      - libjsoncpp-dev
```

Apart from `libsecret` you also need a keyring service, for that you need either `gnome-keyring` (for Gnome users) or `ksecretsservice` (for KDE users) or other light provider like [`secret-service`](https://github.com/yousefvand/secret-service).

### macOS

You also need to add Keychain Sharing as capability to your macOS runner. To achieve this, please add the following in *both* your `macos/Runner/DebugProfile.entitlements` *and* `macos/Runner/Release.entitlements` (you need to change both files).

```xml
<key>keychain-access-groups</key>
<array/>
```

If you have set your application up to use App Groups then you will need to add the name of the App Group to the `keychain-access-groups` argument above. Failure to do so will result in values appearing to be written successfully but never actually being written at all. For example if your app has an App Group named "aoeu" then your value for above would instead read:

```xml
<key>keychain-access-groups</key>
<array>
	<string>$(AppIdentifierPrefix)aoeu</string>
</array>
```

If you are configuring this value through XCode then the string you set in the Keychain Sharing section would simply read "aoeu" with XCode appending the `$(AppIdentifierPrefix)` when it saves the configuration.

### Windows

You need the C++ ATL libraries installed along with the rest of Visual Studio Build Tools. Download them from [here](https://visualstudio.microsoft.com/downloads/?q=build+tools) and make sure the C++ ATL under optional is installed as well.

## Integration Tests

Run the following command from `flutter_secure_storage_x/example` directory:

```shell
flutter test integration_test
```
This command runs tests on the currently active device.