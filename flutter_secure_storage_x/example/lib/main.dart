import 'dart:math' show Random;

import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage_x/flutter_secure_storage_x.dart';
import 'package:platform/platform.dart';

void main() {
  runApp(
    const MaterialApp(
      home: ItemsWidget(),
    ),
  );
}

enum _Actions {
  deleteAll,
  isProtectedDataAvailable,
}

enum _ItemActions {
  delete,
  edit,
  containsKey,
  read,
}

class ItemsWidget extends StatefulWidget {
  const ItemsWidget({super.key});

  @override
  State<ItemsWidget> createState() => _ItemsWidgetState();
}

class _ItemsWidgetState extends State<ItemsWidget> {
  final _storage = const FlutterSecureStorage();
  final _accountNameController = TextEditingController(
    text: 'flutter_secure_storage_service',
  );

  final _items = <_SecItem>[];

  @override
  void initState() {
    super.initState();

    _accountNameController.addListener(_readAll);
    Future(() async {
      await _readAll();
    });
  }

  @override
  void dispose() {
    _accountNameController.removeListener(_readAll);
    _accountNameController.dispose();

    super.dispose();
  }

  Future<void> _readAll() async {
    final all = await _storage.readAll(
      iOptions: _getIOSOptions(),
      aOptions: _getAndroidOptions(),
    );
    setState(() {
      _items
        ..clear()
        ..addAll(all.entries.map((e) => (key: e.key, value: e.value)))
        ..sort((a, b) => int.parse(a.key).compareTo(int.parse(b.key)));
    });
  }

  Future<void> _deleteAll() async {
    await _storage.deleteAll(
      iOptions: _getIOSOptions(),
      aOptions: _getAndroidOptions(),
    );
    await _readAll();
  }

  Future<void> _isProtectedDataAvailable() async {
    final scaffoldMessenger = ScaffoldMessenger.of(context);
    final result = await _storage.isCupertinoProtectedDataAvailable();

    scaffoldMessenger.showSnackBar(
      SnackBar(
        content: Text('Protected data available: $result'),
        backgroundColor: result != null && result ? Colors.green : Colors.red,
      ),
    );
  }

  Future<void> _addNewItem() async {
    await _storage.write(
      key: DateTime.timestamp().microsecondsSinceEpoch.toString(),
      value: _randomValue(),
      iOptions: _getIOSOptions(),
      aOptions: _getAndroidOptions(),
    );
    await _readAll();
  }

  IOSOptions _getIOSOptions() => IOSOptions(
        accountName: _getAccountName(),
      );

  AndroidOptions _getAndroidOptions() => const AndroidOptions(
        dataStore: true,
      );

  String? _getAccountName() =>
      _accountNameController.text.isEmpty ? null : _accountNameController.text;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
        actions: [
          IconButton(
            key: const Key('add_random'),
            onPressed: () async {
              await _addNewItem();
            },
            icon: const Icon(Icons.add),
          ),
          PopupMenuButton<_Actions>(
            key: const Key('popup_menu'),
            onSelected: (action) async {
              switch (action) {
                case _Actions.deleteAll:
                  await _deleteAll();
                case _Actions.isProtectedDataAvailable:
                  await _isProtectedDataAvailable();
              }
            },
            itemBuilder: (context) => [
              const PopupMenuItem(
                key: Key('delete_all'),
                value: _Actions.deleteAll,
                child: Text('Delete all'),
              ),
              const PopupMenuItem(
                key: Key('is_protected_data_available'),
                value: _Actions.isProtectedDataAvailable,
                child: Text('IsProtectedDataAvailable'),
              ),
            ],
          ),
        ],
      ),
      body: CustomScrollView(
        slivers: [
          if (!kIsWeb && const LocalPlatform().isIOS)
            SliverToBoxAdapter(
              child: Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: TextFormField(
                    controller: _accountNameController,
                    decoration: const InputDecoration(
                      labelText: 'kSecAttrService',
                    ),
                  ),
                ),
              ),
            ),
          SliverList.builder(
            itemCount: _items.length,
            itemBuilder: (context, index) => ListTile(
              trailing: PopupMenuButton(
                key: Key('popup_row_$index'),
                onSelected: (action) async {
                  await _performAction(
                    context: context,
                    action: action,
                    item: _items[index],
                  );
                },
                itemBuilder: (context) => [
                  PopupMenuItem(
                    value: _ItemActions.delete,
                    child: Text(
                      'Delete',
                      key: Key('delete_row_$index'),
                    ),
                  ),
                  PopupMenuItem(
                    value: _ItemActions.edit,
                    child: Text(
                      'Edit',
                      key: Key('edit_row_$index'),
                    ),
                  ),
                  PopupMenuItem(
                    value: _ItemActions.containsKey,
                    child: Text(
                      'Contains Key',
                      key: Key('contains_row_$index'),
                    ),
                  ),
                  PopupMenuItem(
                    value: _ItemActions.read,
                    child: Text(
                      'Read',
                      key: Key('contains_row_$index'),
                    ),
                  ),
                ],
              ),
              title: Text(
                _items[index].value,
                key: Key('title_row_$index'),
              ),
              subtitle: Text(
                _items[index].key,
                key: Key('subtitle_row_$index'),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _performAction({
    required BuildContext context,
    required _ItemActions action,
    required _SecItem item,
  }) async {
    switch (action) {
      case _ItemActions.delete:
        await _storage.delete(
          key: item.key,
          iOptions: _getIOSOptions(),
          aOptions: _getAndroidOptions(),
        );
        await _readAll();
      case _ItemActions.edit:
        final result = await showDialog<String>(
          context: context,
          builder: (context) => _EditTextInputDialog(item.value),
        );
        if (result == null) {
          return;
        }

        await _storage.write(
          key: item.key,
          value: result,
          iOptions: _getIOSOptions(),
          aOptions: _getAndroidOptions(),
        );
        await _readAll();
      case _ItemActions.containsKey:
        final scaffoldMessenger = ScaffoldMessenger.of(context);
        final key = await showDialog<String>(
          context: context,
          builder: (context) => _DisplayTextInputDialog(item.key),
        );
        if (key == null) {
          return;
        }

        final result = await _storage.containsKey(
          key: key,
          iOptions: _getIOSOptions(),
          aOptions: _getAndroidOptions(),
        );
        scaffoldMessenger.showSnackBar(
          SnackBar(
            content: Text('Contains Key: $result, key checked: $key'),
            backgroundColor: result ? Colors.green : Colors.red,
          ),
        );
      case _ItemActions.read:
        final scaffoldMessenger = ScaffoldMessenger.of(context);
        final key = await showDialog<String>(
          context: context,
          builder: (context) => _DisplayTextInputDialog(item.key),
        );
        if (key == null) {
          return;
        }

        final result = await _storage.read(
          key: key,
          iOptions: _getIOSOptions(),
          aOptions: _getAndroidOptions(),
        );
        scaffoldMessenger.showSnackBar(
          SnackBar(
            content: Text('value: $result'),
          ),
        );
    }
  }

  String _randomValue() {
    final rand = Random();
    final codeUnits = List<int>.generate(
      20,
      (_) => rand.nextInt(26) + 65,
      growable: false,
    );

    return String.fromCharCodes(codeUnits);
  }
}

class _EditTextInputDialog extends StatefulWidget {
  const _EditTextInputDialog(this.value);

  final String value;

  @override
  State<_EditTextInputDialog> createState() => _EditTextInputDialogState();
}

class _EditTextInputDialogState extends State<_EditTextInputDialog> {
  late final TextEditingController _editTextDialogController;

  @override
  void initState() {
    super.initState();
    _editTextDialogController = TextEditingController(
      text: widget.value,
    );
  }

  @override
  void dispose() {
    _editTextDialogController.dispose();

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Edit item'),
      content: TextField(
        key: const Key('title_field'),
        controller: _editTextDialogController,
        autofocus: true,
      ),
      actions: [
        TextButton(
          key: const Key('cancel'),
          onPressed: () {
            Navigator.of(context).pop();
          },
          child: const Text('Cancel'),
        ),
        TextButton(
          key: const Key('save'),
          onPressed: () {
            final value = _editTextDialogController.text;
            Navigator.of(context).pop(value);
          },
          child: const Text('Save'),
        ),
      ],
    );
  }
}

class _DisplayTextInputDialog extends StatefulWidget {
  const _DisplayTextInputDialog(this.value);

  final String value;

  @override
  State<_DisplayTextInputDialog> createState() =>
      _DisplayTextInputDialogState();
}

class _DisplayTextInputDialogState extends State<_DisplayTextInputDialog> {
  late final TextEditingController _displayTextDialogController;

  @override
  void initState() {
    super.initState();
    _displayTextDialogController = TextEditingController(
      text: widget.value,
    );
  }

  @override
  void dispose() {
    _displayTextDialogController.dispose();

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Check if key exists'),
      actions: [
        TextButton(
          onPressed: () {
            final value = _displayTextDialogController.text;
            Navigator.of(context).pop(value);
          },
          child: const Text('OK'),
        ),
      ],
      content: TextField(
        controller: _displayTextDialogController,
      ),
    );
  }
}

typedef _SecItem = ({
  String key,
  String value,
});
