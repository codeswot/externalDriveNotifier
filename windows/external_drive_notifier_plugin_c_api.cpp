#include "include/external_drive_notifier/external_drive_notifier_plugin_c_api.h"

#include <flutter/plugin_registrar_windows.h>

#include "external_drive_notifier_plugin.h"

void ExternalDriveNotifierPluginCApiRegisterWithRegistrar(
    FlutterDesktopPluginRegistrarRef registrar) {
  external_drive_notifier::ExternalDriveNotifierPlugin::RegisterWithRegistrar(
      flutter::PluginRegistrarManager::GetInstance()
          ->GetRegistrar<flutter::PluginRegistrarWindows>(registrar));
}
