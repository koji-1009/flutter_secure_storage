# flutter_secure_storage_x

[![style: lint](https://img.shields.io/badge/style-flutter_lints-4BC0F5.svg)](https://pub.dev/packages/flutter_lints)
[![pub package](https://img.shields.io/pub/v/flutter_secure_storage_x.svg)](https://pub.dev/packages/flutter_secure_storage_x)
[![flutter_secure_storage_x](https://github.com/koji-1009/flutter_secure_storage/actions/workflows/flutter.yml/badge.svg)](https://github.com/koji-1009/flutter_secure_storage/actions/workflows/flutter.yml)
[![flutter_secure_storage_x](https://github.com/koji-1009/flutter_secure_storage/actions/workflows/flutter_drive.yml/badge.svg)](https://github.com/koji-1009/flutter_secure_storage/actions/workflows/flutter_drive.yml)

A Flutter plugin to store data in secure storage:

- [Keychain](https://developer.apple.com/library/content/documentation/Security/Conceptual/keychainServConcepts/01introduction/introduction.html#//apple_ref/doc/uid/TP30000897-CH203-TP1) is used for iOS
- AES encryption is used for Android. AES secret key is encrypted with RSA and RSA key is stored in [KeyStore](https://developer.android.com/training/articles/keystore.html).

For more information see the example app.
- [`libsecret`](https://wiki.gnome.org/Projects/Libsecret) is used for Linux.

## Important notice for Android

DataStore support has been available since v10.0.0. When using DataStore, set the options as follows.

```dart
AndroidOptions _getAndroidOptions() => const AndroidOptions(
  dataStore: true,
);
```

## Important notice for Web

flutter_secure_storage only works on HTTPS or localhost environments. [Please see this issue for more information.](https://github.com/mogol/flutter_secure_storage/issues/320#issuecomment-976308930)

### WASM support

Supported from v2.0.0.

```yaml
dependency_overrides:
  flutter_secure_storage_x_web: ^2.0.0
```

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

If not present already, please call WidgetsFlutterBinding.ensureInitialized() in your main before you do anything with the MethodChannel. [Please see this issue  for more info.](https://github.com/mogol/flutter_secure_storage/issues/336)

```dart
import 'package:flutter_secure_storage_x/flutter_secure_storage_x.dart';

// Create storage
final storage = FlutterSecureStorage();

// Read value
String value = await storage.read(key: key);

// Read all values
Map<String, String> allValues = await storage.readAll();

// Delete value
await storage.delete(key: key);

// Delete all
await storage.deleteAll();

// Write value
await storage.write(key: key, value: value);
```

This allows us to be able to fetch secure values while the app is backgrounded, by specifying first_unlock or first_unlock_this_device. The default if not specified is unlocked.
An example:

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

### Configure Android version

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

_Note_ By default Android backups data on Google Drive. It can cause exception java.security.InvalidKeyException:Failed to unwrap key.
You need to

- [disable autobackup](https://developer.android.com/guide/topics/data/autobackup#EnablingAutoBackup), [details](https://github.com/mogol/flutter_secure_storage/issues/13#issuecomment-421083742)
- [exclude sharedprefs](https://developer.android.com/guide/topics/data/autobackup#IncludingFiles) `FlutterSecureStorage` used by the plugin, [details](https://github.com/mogol/flutter_secure_storage/issues/43#issuecomment-471642126)

### Configure Web Version

Flutter Secure Storage uses an experimental implementation using WebCrypto. Use at your own risk at this time. Feedback welcome to improve it. The intent is that the browser is creating the private key, and as a result, the encrypted strings in local_storage are not portable to other browsers or other machines and will only work on the same domain.

**It is VERY important that you have HTTP Strict Forward Secrecy enabled and the proper headers applied to your responses or you could be subject to a javascript hijack.**

Please see:

- https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Strict-Transport-Security
- https://www.netsparker.com/blog/web-security/http-security-headers/

#### application-specific key option

On the web, all keys are stored in LocalStorage. flutter_secure_storage has an option for the web to wrap this stored key with an application-specific key to make it more difficult to analyze.

```dart
final _storage = const FlutterSecureStorage(
  webOptions: WebOptions(
    wrapKey: '${your_application_specific_key}',
    wrapKeyIv: '${your_application_specific_iv}',
  ),
);
```

This option encrypts the key stored in LocalStorage with [WebCrypto wrapKey](https://developer.mozilla.org/en-US/docs/Web/API/SubtleCrypto/wrapKey). It is decrypted with [WebCrypto unwrapKey](https://developer.mozilla.org/en-US/docs/Web/API/SubtleCrypto/unwrapKey) when used.
Generating and managing application-specific keys requires careful attention from developers. See (https://github.com/mogol/flutter_secure_storage/issues/726) for more information.

### Configure Linux Version

You need `libsecret-1-dev` and `libjsoncpp-dev` on your machine to build the project, and `libsecret-1-0` and `libjsoncpp1` to run the application (add it as a dependency after packaging your app). If you using snapcraft to build the project use the following

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

### Configure macOS Version

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

### Configure Windows Version

You need the C++ ATL libraries installed along with the rest of Visual Studio Build Tools. Download them from [here](https://visualstudio.microsoft.com/downloads/?q=build+tools) and make sure the C++ ATL under optional is installed as well.

## Integration Tests

Run the following command from `example` directory

```shell
flutter drive --target=test_driver/app.dart
```

## Contributing

If you want to contribute, you need to initialise the workspace after cloning the repo with `melos` like this:

```shell
flutter pub get
melos bootstrap
```

After that, everything should be set up and working!
