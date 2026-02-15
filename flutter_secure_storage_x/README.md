# flutter_secure_storage_x

[![pub package](https://img.shields.io/pub/v/flutter_secure_storage_x.svg)](https://pub.dev/packages/flutter_secure_storage_x)
[![flutter_secure_storage_x](https://github.com/koji-1009/flutter_secure_storage/actions/workflows/flutter.yml/badge.svg)](https://github.com/koji-1009/flutter_secure_storage/actions/workflows/flutter.yml)
[![integration test](https://github.com/koji-1009/flutter_secure_storage/actions/workflows/flutter_drive.yml/badge.svg)](https://github.com/koji-1009/flutter_secure_storage/actions/workflows/flutter_drive.yml)

## Philosophy

`flutter_secure_storage_x` is designed with a focus on long-term stability and maintainability for the most common use cases.

Rather than offering an exhaustive feature set, we aim to provide a minimal, transparent layer that respects and utilizes the native security primitives provided by the OS (such as **Android KeyStore**, **Jetpack DataStore**, and **Apple Keychain**).

### Note on Specialized Requirements

Specialized features, such as biometric authentication, require holistic design—from checking prerequisites and handling failure recovery to balancing user experience (UX) with strict security requirements.

For these specialized use cases or applications requiring extreme security levels, we recommend that developers implement their own native code. This ensures the implementation is fully optimized and tailored to the specific needs of the application, rather than relying on generalized library options.

For more detailed information and usage examples, please refer to the example app.

## Roadmap & History

### Android

The following table outlines the evolution of storage and encryption on Android, and the recommended migration path.

| Version           | Storage Backend                                         | Encryption                                                | Migration Status                                                                                                             |
|:----------------- |:------------------------------------------------------- |:--------------------------------------------------------- |:---------------------------------------------------------------------------------------------------------------------------- |
| **v10**           | _SharedPreferences_ (Default) <br> DataStore (Opt-in)   | Custom Implementation <br> & _EncryptedSharedPreferences_ | **Migration Bridge.** Essential for v9 users. Ensures data accessibility before _EncryptedSharedPreferences_ removal in v11. |
| **v11**           | _SharedPreferences_ (Default) <br> DataStore (Opt-in)   | Custom Implementation <br> (RSA/AES)                      | **Stabilization.** Removed unstable EncryptedSharedPreferences.                                                              |
| **v12**           | _SharedPreferences_ (Default) <br> DataStore (Opt-in)   | **Android KeyStore** <br> (OS Standard)                   | **Modernization.** Migrates from custom implementation to OS-standard KeyStore. Safe upgrade from v11.                       |
| **v13** (Planned) | **DataStore (Default)** <br> SharedPreferences (Legacy) | **Android KeyStore**                                      | **Transition.** DataStore becomes default. Encryption is fully managed by the OS.                                            |
| **v14** (Planned) | **DataStore ONLY**                                      | **Android KeyStore**                                      | **Finalization.** SharedPreferences support is completely removed.                                                           |

### Summary

* **v10 & v11**: Focus on stability and preparing for **DataStore**.
* **v12 (Current)**: Unifies encryption to the standard **Android KeyStore**. All legacy custom implementations (RSA/AES) are removed to improve security and maintainability.
* **v13 (Planned)**: **DataStore** becomes the default backend.
* **v14 (Planned)**: **DataStore** becomes the only supported backend. **SharedPreferences** support is removed.

> **Note**: Direct upgrades from v12 or earlier to v14 will not be supported. Users must upgrade to v13 first to migrate their data.

### Windows & Linux

**Planned for v15+**.
The current priority is to resolve stability issues on Android (v12-v14). Major updates for Windows and Linux will be addressed in v15 or later.

* **Windows**: [Issue #88](https://github.com/koji-1009/flutter_secure_storage/issues/88)
* **Linux**: [Issue #87](https://github.com/koji-1009/flutter_secure_storage/issues/87)

## Platform Implementation

Please note that this table represents the functions implemented in this repository and it is possible that changes haven't yet been released on pub.dev

|         | read               | write              | delete             | containsKey        | readAll            | deleteAll          | isCupertinoProtectedDataAvailable | onCupertinoProtectedDataAvailabilityChanged |
| ------- | ------------------ | ------------------ | ------------------ | ------------------ | ------------------ | ------------------ | --------------------------------- | ------------------------------------------- |
| Android | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |                                   |                                             |
| iOS     | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark:                | :white_check_mark:                          |
| Windows | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |                                   |                                             |
| Linux   | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |                                   |                                             |
| macOS   | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark:                | :white_check_mark: (on macOS 12 and newer)  |
| Web     | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |                                   |                                             |

## Getting Started

If not already present, call `WidgetsFlutterBinding.ensureInitialized()` in your `main()` function before interacting with the `MethodChannel`. See this issue for more information: [https://github.com/mogol/flutter_secure_storage/issues/336](https://github.com/mogol/flutter_secure_storage/issues/336)

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

If your application requires background access to the storage, you must configure this explicitly. This allows fetching secure values while the app is backgrounded by specifying `first_unlock` or `first_unlock_this_device`. The default, if not specified, is `unlocked`.

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

_Note_: By default, Android backs up data on Google Drive. This can cause an exception: `java.security.InvalidKeyException: Failed to unwrap key`.
You need to:

* [disable autobackup](https://developer.android.com/guide/topics/data/autobackup#EnablingAutoBackup), [details](https://github.com/mogol/flutter_secure_storage/issues/13#issuecomment-421083742)
* [exclude sharedprefs](https://developer.android.com/guide/topics/data/autobackup#IncludingFiles) `FlutterSecureStorage` used by the plugin, [details](https://github.com/mogol/flutter_secure_storage/issues/43#issuecomment-471642126)

DataStore support has been available since v10.0.0. When using DataStore, set the options as follows.

```dart
AndroidOptions _getAndroidOptions() => const AndroidOptions(
  dataStore: true,
);
```

### Web

The web implementation uses WebCrypto and should be considered experimental. It only works on secure contexts (HTTPS or localhost). Feedback is welcome to help improve it.

The intent is for the browser to create the private key, and as a result, the encrypted strings in `localStorage` are not portable to other browsers or other machines and will only work on the same domain.

**It is VERY important that you have HTTP Strict Transport Security enabled and the proper headers applied to your responses, or you could be subject to a JavaScript hijacking attack.**

Please see:

* [https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Strict-Transport-Security](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Strict-Transport-Security)
* [https://www.netsparker.com/blog/web-security/http-security-headers/](https://www.netsparker.com/blog/web-security/http-security-headers/)

#### WASM support

Supported from v2.0.0.

#### application-specific key option

On the web, all keys are stored in `localStorage`. This package offers an option to wrap these stored keys with an application-specific key to make analysis more difficult.

```dart
final _storage = const FlutterSecureStorageX(
  webOptions: WebOptions(
    wrapKey: '${your_application_specific_key}',
    wrapKeyIv: '${your_application_specific_iv}',
  ),
);
```

This option encrypts the key stored in `localStorage` with [WebCrypto wrapKey](https://developer.mozilla.org/en-US/docs/Web/API/SubtleCrypto/wrapKey). It is decrypted with [WebCrypto unwrapKey](https://developer.mozilla.org/en-US/docs/Web/API/SubtleCrypto/unwrapKey) when used.
Generating and managing application-specific keys requires careful attention from developers. See ([https://github.com/mogol/flutter_secure_storage/issues/726](https://github.com/mogol/flutter_secure_storage/issues/726)) for more information.

### Linux

You need `libsecret-1-dev` and `libjsoncpp-dev` on your machine to build the project, and `libsecret-1-0` and `libjsoncpp1` to run the application (add them as dependencies after packaging your app). If you are using Snapcraft to build the project, use the following:

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

In addition to `libsecret`, you also need a keyring service. For this, you can use either `gnome-keyring` (for GNOME users), `ksecretsservice` (for KDE users), or other lightweight providers like [`secret-service`](https://github.com/yousefvand/secret-service).

### macOS

You also need to add the Keychain Sharing capability to your macOS runner. To achieve this, please add the following in _both_ `macos/Runner/DebugProfile.entitlements` and `macos/Runner/Release.entitlements` (you need to change both files).

```xml
<key>keychain-access-groups</key>
<array/>
```

If your application is configured to use App Groups, you will need to add the name of the App Group to the `keychain-access-groups` argument above. Failure to do so will result in values appearing to be written successfully but never actually being written at all. For example, if your app has an App Group named "aoeu", then the value above would instead read:

```xml
<key>keychain-access-groups</key>
<array>
	<string>$(AppIdentifierPrefix)aoeu</string>
</array>
```

If you are configuring this value through Xcode, then the string you set in the Keychain Sharing section would simply read "aoeu", with Xcode appending the `$(AppIdentifierPrefix)` when it saves the configuration.

### Windows

You need the C++ ATL libraries installed along with the rest of the Visual Studio Build Tools. Download them from [here](https://visualstudio.microsoft.com/downloads/?q=build+tools) and ensure the C++ ATL component under 'Optional' is also installed.

## Integration Tests

Run the following command from `flutter_secure_storage_x/example` directory:

```shell
flutter test integration_test
```

This command runs tests on the currently active device.
