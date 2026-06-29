import 'dart:async';
import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage_x/flutter_secure_storage_x.dart';
import 'package:flutter_secure_storage_x_example/main.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';

void main() {
  final binding = IntegrationTestWidgetsFlutterBinding.ensureInitialized();
  binding.testTextInput.register();

  testWidgets('Secure Storage Example', (tester) async {
    await tester.pumpWidget(const MaterialApp(home: ItemsWidget()));
    await tester.pumpAndSettle();

    final pageObject = HomePageObject(tester);

    await pageObject.deleteAll();
    pageObject.hasNoRow(0);

    await pageObject.addRandom();
    // Wait for the row to be added, as this action depends on the storage IO
    await pumpUntilFound(tester, find.byKey(const Key('title_row_0')));
    pageObject.hasRow(0);
    await pageObject.addRandom();
    // Wait for the row to be added, as this action depends on the storage IO
    await pumpUntilFound(tester, find.byKey(const Key('title_row_1')));
    pageObject.hasRow(1);

    await pageObject.editRow('Row 0', 0);
    pageObject.rowHasTitle('Row 0', 0);
    await pageObject.editRow('Row 1', 1);
    pageObject.rowHasTitle('Row 1', 1);

    await pageObject.deleteRow(1);
    pageObject.hasNoRow(1);
    pageObject.rowHasTitle('Row 0', 0);
    await pageObject.deleteRow(0);
    pageObject.hasNoRow(0);

    await pageObject.isProtectedDataAvailable();
    await pageObject.deleteAll();
  });

  // API-level golden tests (see helpers/groups at the bottom of this file).
  // Android is covered by the common suite (DataStore, the default backend).
  _commonTests();
  _appleTests();
  _webTests();
}

class HomePageObject {
  const HomePageObject(this.tester);

  final WidgetTester tester;

  Finder get _addRandomButtonFinder => find.byKey(const Key('add_random'));

  Finder get _deleteAllButtonFinder => find.byKey(const Key('delete_all'));

  Finder get _popUpMenuButtonFinder => find.byKey(const Key('popup_menu'));

  Finder get _isProtectedDataAvailableButtonFinder =>
      find.byKey(const Key('is_protected_data_available'));

  Future<void> deleteAll() async {
    expect(_popUpMenuButtonFinder, findsOneWidget);
    await tester.tap(_popUpMenuButtonFinder);
    await tester.pumpAndSettle();

    expect(_deleteAllButtonFinder, findsOneWidget);
    await tester.tap(_deleteAllButtonFinder);
    await tester.pumpAndSettle();
  }

  Future<void> addRandom() async {
    expect(_addRandomButtonFinder, findsOneWidget);
    await tester.tap(_addRandomButtonFinder);
    await tester.pumpAndSettle();
  }

  Future<void> editRow(String title, int index) async {
    final popupRow = find.byKey(Key('popup_row_$index'));
    expect(popupRow, findsOneWidget);
    await tester.tap(popupRow);
    await tester.pumpAndSettle();

    final editRow = find.byKey(Key('edit_row_$index'));
    expect(editRow, findsOneWidget);
    await tester.tap(editRow);
    await tester.pumpAndSettle();

    final textFieldFinder = find.byKey(const Key('title_field'));
    expect(textFieldFinder, findsOneWidget);
    await tester.tap(textFieldFinder);
    await tester.pumpAndSettle();

    await tester.enterText(textFieldFinder, '');
    await tester.pumpAndSettle();
    await tester.enterText(textFieldFinder, title);
    await tester.pumpAndSettle();
    expect(find.text(title), findsOneWidget);

    final saveButtonFinder = find.byKey(const Key('save'));
    expect(saveButtonFinder, findsOneWidget);
    await tester.tap(saveButtonFinder);
    await tester.pumpAndSettle();
    await tester.pump(const Duration(seconds: 3));
  }

  void rowHasTitle(String title, int index) {
    final titleRow = find.byKey(Key('title_row_$index'));
    expect(titleRow, findsOneWidget);
    expect((titleRow.evaluate().single.widget as Text).data, equals(title));
  }

  void hasRow(int index) {
    expect(find.byKey(Key('title_row_$index')), findsOneWidget);
  }

  Future<void> deleteRow(int index) async {
    final popupRow = find.byKey(Key('popup_row_$index'));
    expect(popupRow, findsOneWidget);
    await tester.tap(popupRow);
    await tester.pumpAndSettle();

    final deleteRow = find.byKey(Key('delete_row_$index'));
    expect(deleteRow, findsOneWidget);
    await tester.tap(deleteRow);
    await tester.pumpAndSettle();
    await tester.pump(const Duration(seconds: 3));
  }

  void hasNoRow(int index) {
    expect(find.byKey(Key('title_row_$index')), findsNothing);
  }

  Future<void> isProtectedDataAvailable() async {
    expect(_popUpMenuButtonFinder, findsOneWidget);
    await tester.tap(_popUpMenuButtonFinder);
    await tester.pumpAndSettle();

    expect(_isProtectedDataAvailableButtonFinder, findsOneWidget);
    await tester.tap(_isProtectedDataAvailableButtonFinder);
    await tester.pumpAndSettle();
  }
}

Future<void> pumpUntilFound(
  WidgetTester tester,
  Finder finder, {
  Duration timeout = const Duration(seconds: 30),
}) async {
  final end = DateTime.now().add(timeout);

  do {
    if (DateTime.now().isAfter(end)) {
      throw TimeoutException('Pump until has timed out');
    }
    await tester.pump(const Duration(milliseconds: 100));
  } while (!tester.any(finder));
}

// ---------------------------------------------------------------------------
// API-level golden tests for `FlutterSecureStorage`.
//
// These drive the public API directly (not the example UI) to pin the current
// observable behavior across public methods, options, and edge cases — a
// regression net for the current secure-storage implementations.
//
// They live in this file (rather than a separate one) because CI runs the
// `integration_test` directory as a whole, and launching multiple suites per
// run fails to start the app on desktop platforms.
//
// Assertions encode the *current* behavior. Platform-divergent cases (e.g. the
// empty-string contract, which returns '' on some backends and null on others,
// and concurrent-write safety, which only the serial-queue Apple backend
// guarantees) are deliberately kept out of the common suite.
// ---------------------------------------------------------------------------

const _storage = FlutterSecureStorage();

Future<void> _write(String key, String? value) =>
    _storage.write(key: key, value: value);
Future<String?> _read(String key) => _storage.read(key: key);
Future<bool> _contains(String key) => _storage.containsKey(key: key);
Future<void> _delete(String key) => _storage.delete(key: key);
Future<Map<String, String>> _readAll() => _storage.readAll();
Future<void> _deleteAll() => _storage.deleteAll();

/// Platform-agnostic behavior — runs on every platform.
void _commonTests() {
  group('Secure storage API — common golden behavior', () {
    setUp(_deleteAll);
    tearDown(_deleteAll);

    testWidgets('write then read returns the same value', (_) async {
      await _write('k', 'v');
      expect(await _read('k'), 'v');
    });

    testWidgets('read of a missing key returns null', (_) async {
      expect(await _read('missing'), isNull);
    });

    testWidgets('containsKey reflects write and delete', (_) async {
      expect(await _contains('k'), isFalse);
      await _write('k', 'v');
      expect(await _contains('k'), isTrue);
      await _delete('k');
      expect(await _contains('k'), isFalse);
    });

    testWidgets('overwriting a key keeps only the latest value', (_) async {
      await _write('k', 'first');
      await _write('k', 'second');
      expect(await _read('k'), 'second');
      expect(await _readAll(), {'k': 'second'});
    });

    testWidgets('write(null) deletes the key', (_) async {
      await _write('k', 'v');
      await _write('k', null);
      expect(await _read('k'), isNull);
      expect(await _contains('k'), isFalse);
    });

    testWidgets('delete of a missing key is a no-op', (_) async {
      await _delete('missing'); // must not throw
      expect(await _read('missing'), isNull);
    });

    testWidgets('readAll returns every stored pair', (_) async {
      await _write('a', '1');
      await _write('b', '2');
      await _write('c', '3');
      expect(await _readAll(), {'a': '1', 'b': '2', 'c': '3'});
    });

    testWidgets('readAll on empty storage returns an empty map', (_) async {
      expect(await _readAll(), isEmpty);
    });

    testWidgets('deleteAll clears all keys', (_) async {
      await _write('a', '1');
      await _write('b', '2');
      await _deleteAll();
      expect(await _readAll(), isEmpty);
    });

    testWidgets('deleteAll on empty storage is a no-op', (_) async {
      await _deleteAll(); // must not throw
      expect(await _readAll(), isEmpty);
    });

    testWidgets('round-trips multibyte / emoji values (UTF-8)', (_) async {
      const value = '日本語テキスト 🔐 こんにちは';
      await _write('unicode', value);
      expect(await _read('unicode'), value);
    });

    testWidgets('round-trips a large value', (_) async {
      final value = base64.encode(
        List<int>.generate(16 * 1024, (i) => i % 256),
      );
      await _write('large', value);
      expect(await _read('large'), value);
    });

    testWidgets('round-trips values with newlines and quotes', (_) async {
      const value = 'line1\nline2\tTabbed\r\n"quoted" \\backslash';
      await _write('special', value);
      expect(await _read('special'), value);
    });

    testWidgets('round-trips keys with spaces and unicode', (_) async {
      const key = 'key with spaces / 日本語 / 🔑';
      await _write(key, 'v');
      expect(await _read(key), 'v');
      expect((await _readAll())[key], 'v');
    });

    testWidgets('handles many keys', (_) async {
      for (var i = 0; i < 50; i++) {
        await _write('k$i', 'v$i');
      }
      final all = await _readAll();
      expect(all.length, 50);
      expect(all['k0'], 'v0');
      expect(all['k49'], 'v49');
    });
  });
}

/// iOS / macOS — Keychain-specific options.
void _appleTests() {
  final isApple =
      !kIsWeb &&
      (defaultTargetPlatform == TargetPlatform.iOS ||
          defaultTargetPlatform == TargetPlatform.macOS);

  const service = AppleOptions.defaultAccountName;

  IOSOptions ios({
    KeychainAccessibility accessibility = KeychainAccessibility.unlocked,
    String accountName = service,
    bool synchronizable = false,
  }) => IOSOptions(
    accessibility: accessibility,
    accountName: accountName,
    synchronizable: synchronizable,
  );

  MacOsOptions mac({
    KeychainAccessibility accessibility = KeychainAccessibility.unlocked,
    String accountName = service,
    bool synchronizable = false,
    bool useDataProtectionKeyChain = true,
  }) => MacOsOptions(
    accessibility: accessibility,
    accountName: accountName,
    synchronizable: synchronizable,
    useDataProtectionKeyChain: useDataProtectionKeyChain,
  );

  Future<void> write(
    String key,
    String? value, {
    KeychainAccessibility accessibility = KeychainAccessibility.unlocked,
    String accountName = service,
    bool synchronizable = false,
  }) => _storage.write(
    key: key,
    value: value,
    iOptions: ios(
      accessibility: accessibility,
      accountName: accountName,
      synchronizable: synchronizable,
    ),
    mOptions: mac(
      accessibility: accessibility,
      accountName: accountName,
      synchronizable: synchronizable,
    ),
  );

  Future<String?> read(String key, {String accountName = service}) =>
      _storage.read(
        key: key,
        iOptions: ios(accountName: accountName),
        mOptions: mac(accountName: accountName),
      );

  Future<Map<String, String>> readAll({String accountName = service}) =>
      _storage.readAll(
        iOptions: ios(accountName: accountName),
        mOptions: mac(accountName: accountName),
      );

  Future<void> deleteAll({String accountName = service}) => _storage.deleteAll(
    iOptions: ios(accountName: accountName),
    mOptions: mac(accountName: accountName),
  );

  group(
    'Apple Keychain — platform options',
    () {
      setUp(() => deleteAll());
      tearDown(() => deleteAll());

      for (final accessibility in const [
        KeychainAccessibility.unlocked,
        KeychainAccessibility.unlocked_this_device,
        KeychainAccessibility.first_unlock,
        KeychainAccessibility.first_unlock_this_device,
      ]) {
        testWidgets('round-trips with accessibility=${accessibility.name}', (
          _,
        ) async {
          await write('acc', 'v', accessibility: accessibility);
          expect(await read('acc'), 'v');
        });
      }

      testWidgets('different accountName values are isolated', (_) async {
        const a = 'service.a';
        const b = 'service.b';
        try {
          await write('shared', 'fromA', accountName: a);
          await write('shared', 'fromB', accountName: b);

          expect(await read('shared', accountName: a), 'fromA');
          expect(await read('shared', accountName: b), 'fromB');
          expect(await readAll(accountName: a), {'shared': 'fromA'});
        } finally {
          await deleteAll(accountName: a);
          await deleteAll(accountName: b);
        }
      });

      testWidgets('round-trips a synchronizable item locally', (_) async {
        // Reads use kSecAttrSynchronizableAny, so it is readable locally
        // regardless of iCloud sign-in.
        await write('sync', 'v', synchronizable: true);
        expect(await read('sync'), 'v');
      });

      testWidgets('concurrent writes all persist (serial queue)', (_) async {
        await Future.wait([for (var i = 0; i < 20; i++) write('c$i', 'v$i')]);
        final all = await readAll();
        expect(all.length, 20);
        for (var i = 0; i < 20; i++) {
          expect(all['c$i'], 'v$i');
        }
      });

      testWidgets('isCupertinoProtectedDataAvailable returns a bool', (
        _,
      ) async {
        final available = await _storage.isCupertinoProtectedDataAvailable();
        expect(available, isNotNull);
        expect(available, isA<bool>());
      });

      testWidgets(
        'macOS: round-trips with useDataProtectionKeyChain=false',
        (_) async {
          await _storage.write(
            key: 'dp',
            value: 'v',
            mOptions: mac(useDataProtectionKeyChain: false),
          );
          expect(
            await _storage.read(
              key: 'dp',
              mOptions: mac(useDataProtectionKeyChain: false),
            ),
            'v',
          );
          await _storage.delete(
            key: 'dp',
            mOptions: mac(useDataProtectionKeyChain: false),
          );
        },
        skip: defaultTargetPlatform != TargetPlatform.macOS,
      );
    },
    skip: !isApple ? 'Apple (iOS/macOS) only' : false,
  );
}

/// Web — localStorage namespace (publicKey) options.
void _webTests() {
  WebOptions web({String publicKey = 'FlutterSecureStorage'}) =>
      WebOptions(publicKey: publicKey);

  Future<void> write(String key, String? value, {required String publicKey}) =>
      _storage.write(
        key: key,
        value: value,
        webOptions: web(publicKey: publicKey),
      );

  Future<String?> read(String key, {required String publicKey}) =>
      _storage.read(
        key: key,
        webOptions: web(publicKey: publicKey),
      );

  Future<Map<String, String>> readAll({required String publicKey}) =>
      _storage.readAll(webOptions: web(publicKey: publicKey));

  Future<void> deleteAll({required String publicKey}) =>
      _storage.deleteAll(webOptions: web(publicKey: publicKey));

  group('Web — platform options', () {
    const a = 'ns.a';
    const b = 'ns.b';

    setUp(() async {
      await deleteAll(publicKey: a);
      await deleteAll(publicKey: b);
    });
    tearDown(() async {
      await deleteAll(publicKey: a);
      await deleteAll(publicKey: b);
    });

    testWidgets('round-trips under a custom publicKey namespace', (_) async {
      await write('k', 'v', publicKey: a);
      expect(await read('k', publicKey: a), 'v');
    });

    testWidgets('different publicKey namespaces are isolated', (_) async {
      // The web implementation namespaces by publicKey (localStorage key
      // prefix); dbName is currently unused.
      await write('k', 'fromA', publicKey: a);
      await write('k', 'fromB', publicKey: b);

      expect(await read('k', publicKey: a), 'fromA');
      expect(await read('k', publicKey: b), 'fromB');
      expect(await readAll(publicKey: a), {'k': 'fromA'});
    });
  }, skip: !kIsWeb ? 'Web only' : false);
}
