import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_secure_storage_x_example/main.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('Secure Storage Example', (tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: ItemsWidget(),
      ),
    );
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

    await tester.enterText(textFieldFinder, title);
    await tester.pumpAndSettle();
    await tester.pump(const Duration(seconds: 3));

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
  bool timerDone = false;
  final timer = Timer(
    timeout,
    () => throw TimeoutException('Pump until has timed out'),
  );
  while (timerDone != true) {
    await tester.pump();

    final found = tester.any(finder);
    if (found) {
      timerDone = true;
    }
  }
  timer.cancel();
}
