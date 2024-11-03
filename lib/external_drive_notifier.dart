import 'dart:async';
import 'package:flutter/services.dart';

// TODO: cleanup, write tests
class ExternalDriveNotifier {
  static const MethodChannel _channel = MethodChannel('external_drive_notifier');
  static const EventChannel _eventChannel = EventChannel('external_drive_notifier_events');

  /// Stream of external drive events
  static Stream<DriveEvent> get driveEvents {
    return _eventChannel.receiveBroadcastStream().map((dynamic event) {
      final Map<String, dynamic> map = Map<String, dynamic>.from(event);
      return DriveEvent(
        type: map['event'] as String,
        path: map['path'] as String?,
        drives: map['drives']?.cast<String>(),
      );
    });
  }

  /// Get list of available external drives
  static Future<List<String>> getExternalDrives() async {
    final List<dynamic> drives = await _channel.invokeMethod('getExternalDrives');
    return drives.cast<String>();
  }
}

class DriveEvent {
  final String type;
  final String? path;
  final List<String>? drives;

  DriveEvent({
    required this.type,
    this.path,
    this.drives,
  });

  factory DriveEvent.fromMap(Map<String, dynamic> map) {
    return DriveEvent(
      type: map['event'] as String,
      path: map['path'] as String?,
      drives: map['drives']?.cast<String>(),
    );
  }

  @override
  String toString() => 'DriveEvent(type: $type, path: $path, drives: $drives)';
}
