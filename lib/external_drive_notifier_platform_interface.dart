import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'external_drive_notifier_method_channel.dart';

abstract class ExternalDriveNotifierPlatform extends PlatformInterface {
  /// Constructs a ExternalDriveNotifierPlatform.
  ExternalDriveNotifierPlatform() : super(token: _token);

  static final Object _token = Object();

  static ExternalDriveNotifierPlatform _instance = MethodChannelExternalDriveNotifier();

  /// The default instance of [ExternalDriveNotifierPlatform] to use.
  ///
  /// Defaults to [MethodChannelExternalDriveNotifier].
  static ExternalDriveNotifierPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ExternalDriveNotifierPlatform] when
  /// they register themselves.
  static set instance(ExternalDriveNotifierPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('getPlatformVersion() has not been implemented.');
  }

  /// Method to fetch the list of currently connected drives.
  Future<List<String>> getConnectedDrives() {
    throw UnimplementedError('getConnectedDrives() has not been implemented.');
  }

  /// Stream to listen for USB insert/eject events.
  Stream<Map<String, dynamic>> get usbEventStream {
    throw UnimplementedError('usbEventStream has not been implemented.');
  }
}
