import 'dart:io' show Platform;
import 'dart:math' show Random;

import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage_x/flutter_secure_storage_x.dart';

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
  State<ItemsWidget> createState() => ItemsWidgetState();
}

class ItemsWidgetState extends State<ItemsWidget> {
  final _storage = const FlutterSecureStorage();

  late final TextEditingController _accountNameController;
  final _items = <_SecItem>[];

  @override
  void initState() {
    super.initState();

    _accountNameController = TextEditingController(
      text: 'flutter_secure_storage_service',
    );
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
          if (!kIsWeb && Platform.isIOS)
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
          builder: (context) => _EditItemWidget(item.value),
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
        final key = await _displayTextInputDialog(
          context: context,
          key: item.key,
        );
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
        final key = await _displayTextInputDialog(
          context: context,
          key: item.key,
        );
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

  Future<String> _displayTextInputDialog({
    required BuildContext context,
    required String key,
  }) async {
    final controller = TextEditingController(
      text: key,
    );
    await showDialog<void>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Check if key exists'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('OK'),
          ),
        ],
        content: TextField(
          controller: controller,
        ),
      ),
    );
    final result = controller.text;
    controller.dispose();

    return result;
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

class _EditItemWidget extends StatefulWidget {
  const _EditItemWidget(this.text);

  final String text;

  @override
  State<_EditItemWidget> createState() => _EditItemWidgetState();
}

class _EditItemWidgetState extends State<_EditItemWidget> {
  late final TextEditingController _controller;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(
      text: widget.text,
    );
  }

  @override
  void dispose() {
    _controller.dispose();

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Edit item'),
      content: TextField(
        key: const Key('title_field'),
        controller: _controller,
        autofocus: true,
      ),
      actions: [
        TextButton(
          key: const Key('cancel'),
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cancel'),
        ),
        TextButton(
          key: const Key('save'),
          onPressed: () => Navigator.of(context).pop(_controller.text),
          child: const Text('Save'),
        ),
      ],
    );
  }
}

typedef _SecItem = ({
  String key,
  String value,
});
