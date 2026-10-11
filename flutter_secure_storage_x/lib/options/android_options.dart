part of '../flutter_secure_storage_x.dart';

class AndroidOptions extends Options {
  const AndroidOptions({this.resetOnError = false});

  /// When an error is detected, automatically reset all data. This will prevent
  /// fatal errors regarding an unknown key however keep in mind that it will
  /// PERMANENTLY erase the data when an error occurs.
  ///
  /// Defaults to false.
  final bool resetOnError;

  static const AndroidOptions defaultOptions = AndroidOptions();

  @override
  Map<String, String> toMap() => <String, String>{
    'resetOnError': '$resetOnError',
  };

  AndroidOptions copyWith({bool? resetOnError}) =>
      AndroidOptions(resetOnError: resetOnError ?? this.resetOnError);
}
