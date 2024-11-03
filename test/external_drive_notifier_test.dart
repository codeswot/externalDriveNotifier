import 'package:flutter_test/flutter_test.dart';
import 'package:external_drive_notifier/external_drive_notifier.dart';
import 'package:external_drive_notifier/external_drive_notifier_platform_interface.dart';
import 'package:external_drive_notifier/external_drive_notifier_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockExternalDriveNotifierPlatform
    with MockPlatformInterfaceMixin
    implements ExternalDriveNotifierPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final ExternalDriveNotifierPlatform initialPlatform = ExternalDriveNotifierPlatform.instance;

  test('$MethodChannelExternalDriveNotifier is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelExternalDriveNotifier>());
  });

  test('getPlatformVersion', () async {
    ExternalDriveNotifier externalDriveNotifierPlugin = ExternalDriveNotifier();
    MockExternalDriveNotifierPlatform fakePlatform = MockExternalDriveNotifierPlatform();
    ExternalDriveNotifierPlatform.instance = fakePlatform;

    expect(await externalDriveNotifierPlugin.getPlatformVersion(), '42');
  });
}
