import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'external_drive_notifier_platform_interface.dart';

/// An implementation of [ExternalDriveNotifierPlatform] that uses method channels.
class MethodChannelExternalDriveNotifier extends ExternalDriveNotifierPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('external_drive_notifier/methods');

  /// The event channel used to receive USB drive events from the native platform.
  @visibleForTesting
  final eventChannel = const EventChannel('external_drive_notifier/events');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  /// Gets the list of currently connected USB drives.
  @override
  Future<List<String>> getConnectedDrives() async {
    final List<dynamic> drives = await methodChannel.invokeMethod('getConnectedDrives');
    return drives.cast<String>();
  }

  /// Listens to USB drive events as a stream.
  @override
  Stream<Map<String, dynamic>> get usbEventStream {
    return eventChannel.receiveBroadcastStream().map((dynamic event) {
      return Map<String, dynamic>.from(event);
    });
  }
}
