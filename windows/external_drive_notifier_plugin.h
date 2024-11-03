#ifndef FLUTTER_PLUGIN_EXTERNAL_DRIVE_NOTIFIER_PLUGIN_H_
#define FLUTTER_PLUGIN_EXTERNAL_DRIVE_NOTIFIER_PLUGIN_H_

#include <flutter/method_channel.h>
#include <flutter/plugin_registrar_windows.h>

#include <memory>

namespace external_drive_notifier {

class ExternalDriveNotifierPlugin : public flutter::Plugin {
 public:
  static void RegisterWithRegistrar(flutter::PluginRegistrarWindows *registrar);

  ExternalDriveNotifierPlugin();

  virtual ~ExternalDriveNotifierPlugin();

  // Disallow copy and assign.
  ExternalDriveNotifierPlugin(const ExternalDriveNotifierPlugin&) = delete;
  ExternalDriveNotifierPlugin& operator=(const ExternalDriveNotifierPlugin&) = delete;

  // Called when a method is called on this plugin's channel from Dart.
  void HandleMethodCall(
      const flutter::MethodCall<flutter::EncodableValue> &method_call,
      std::unique_ptr<flutter::MethodResult<flutter::EncodableValue>> result);
};

}  // namespace external_drive_notifier

#endif  // FLUTTER_PLUGIN_EXTERNAL_DRIVE_NOTIFIER_PLUGIN_H_
