#include <cstring>

#include <flutter_linux/flutter_linux.h>
#include <gtk/gtk.h>

#include "include/flutter_secure_storage_x_linux/flutter_secure_storage_x_linux_plugin.h"
#include "include/Secret.hpp"

#include "flutter_secure_storage_x_linux_plugin_private.h"

#define FLUTTER_SECURE_STORAGE_X_LINUX_PLUGIN(obj)                                     \
  (G_TYPE_CHECK_INSTANCE_CAST((obj), flutter_secure_storage_x_linux_plugin_get_type(), \
                              FlutterSecureStorageXLinuxPlugin))

struct _FlutterSecureStorageXLinuxPlugin
{
  GObject parent_instance;
};

G_DEFINE_TYPE(FlutterSecureStorageXLinuxPlugin, flutter_secure_storage_x_linux_plugin, g_object_get_type())

// Called when a method call is received from Flutter.
static void flutter_secure_storage_x_linux_plugin_handle_method_call(
    FlutterSecureStorageXLinuxPlugin *self,
    FlMethodCall *method_call)
{
  g_autoptr(FlMethodResponse) response = nullptr;

  const auto *method = fl_method_call_get_name(method_call);
  auto *args = fl_method_call_get_args(method_call);

  auto *key = fl_value_lookup_string(args, "key");
  auto *value = fl_value_lookup_string(args, "value");
  auto *keyString = key == nullptr ? nullptr : fl_value_get_string(key);
  auto *valueString = value == nullptr ? nullptr : fl_value_get_string(value);

  try
  {
    if (strcmp(method, "write") == 0)
    {
      if (keyString == nullptr || valueString == nullptr)
      {
        response = FL_METHOD_RESPONSE(
            fl_method_error_response_new(
                "Bad arguments",
                "Key or Value was null",
                nullptr));
      }
      else
      {
        response = write(keyString, valueString);
      }
    }
    else if (strcmp(method, "read") == 0)
    {
      if (keyString == nullptr)
      {
        response = FL_METHOD_RESPONSE(
            fl_method_error_response_new(
                "Bad arguments",
                "Key was null",
                nullptr));
      }
      else
      {
        response = read(keyString);
      }
    }
    else if (strcmp(method, "readAll") == 0)
    {
      response = read_all();
    }
    else if (strcmp(method, "delete") == 0)
    {
      if (keyString == nullptr)
      {
        response = FL_METHOD_RESPONSE(
            fl_method_error_response_new(
                "Bad arguments",
                "Key was null",
                nullptr));
      }
      else
      {
        response = delete_it(keyString);
      }
    }
    else if (strcmp(method, "deleteAll") == 0)
    {
      response = delete_all();
    }
    else if (strcmp(method, "containsKey") == 0)
    {
      if (keyString == nullptr)
      {
        response = FL_METHOD_RESPONSE(
            fl_method_error_response_new(
                "Bad arguments",
                "Key was null",
                nullptr));
      }
      else
      {
        response = contains_key(keyString);
      }
    }
    else
    {
      response = FL_METHOD_RESPONSE(fl_method_not_implemented_response_new());
    }
  }
  catch (const std::exception &e)
  {
    response = FL_METHOD_RESPONSE(
        fl_method_error_response_new(
            "Libsecret error",
            e.what(),
            nullptr));
  }

  fl_method_call_respond(method_call, response, nullptr);
}

static SecretStorage keyring;

FlMethodResponse *write(const gchar *key, const gchar *value)
{
  keyring.addItem(key, value);
  return FL_METHOD_RESPONSE(fl_method_success_response_new(nullptr));
}

FlMethodResponse *read(const gchar *key)
{
  auto str = keyring.getItem(key);
  if (str == "")
  {
    return FL_METHOD_RESPONSE(fl_method_success_response_new(nullptr));
  }

  g_autoptr(FlValue) result = fl_value_new_string(str.c_str());
  return FL_METHOD_RESPONSE(fl_method_success_response_new(result));
}

FlMethodResponse *read_all()
{
  auto data = keyring.readFromKeyring();
  if (data.empty())
  {
    return FL_METHOD_RESPONSE(fl_method_success_response_new(nullptr));
  }

  g_autoptr(FlValue) response = fl_value_new_map();
  for (auto each : data)
  {
    fl_value_set_string_take(
        response,
        each.first.c_str(),
        fl_value_new_string(each.second.c_str()));
  }
  return FL_METHOD_RESPONSE(fl_method_success_response_new(response));
}

FlMethodResponse *delete_it(const gchar *key)
{
  keyring.deleteItem(key);
  return FL_METHOD_RESPONSE(fl_method_success_response_new(nullptr));
}

FlMethodResponse *delete_all()
{
  keyring.deleteKeyring();
  return FL_METHOD_RESPONSE(fl_method_success_response_new(nullptr));
}

FlMethodResponse *contains_key(const gchar *key)
{
  auto data = keyring.readFromKeyring();
  g_autoptr(FlValue) result = fl_value_new_bool(data.find(key) != data.end());
  return FL_METHOD_RESPONSE(fl_method_success_response_new(result));
}

static void flutter_secure_storage_x_linux_plugin_dispose(GObject *object)
{
  G_OBJECT_CLASS(flutter_secure_storage_x_linux_plugin_parent_class)->dispose(object);
}

static void flutter_secure_storage_x_linux_plugin_class_init(FlutterSecureStorageXLinuxPluginClass *klass)
{
  G_OBJECT_CLASS(klass)->dispose = flutter_secure_storage_x_linux_plugin_dispose;
}

static void flutter_secure_storage_x_linux_plugin_init(FlutterSecureStorageXLinuxPlugin *self) {}

static void method_call_cb(FlMethodChannel *channel, FlMethodCall *method_call,
                           gpointer user_data)
{
  FlutterSecureStorageXLinuxPlugin *plugin = FLUTTER_SECURE_STORAGE_X_LINUX_PLUGIN(user_data);
  flutter_secure_storage_x_linux_plugin_handle_method_call(plugin, method_call);
}

void flutter_secure_storage_x_linux_plugin_register_with_registrar(FlPluginRegistrar *registrar)
{
  FlutterSecureStorageXLinuxPlugin *plugin = FLUTTER_SECURE_STORAGE_X_LINUX_PLUGIN(
      g_object_new(flutter_secure_storage_x_linux_plugin_get_type(), nullptr));

  g_autoptr(FlStandardMethodCodec) codec = fl_standard_method_codec_new();
  g_autoptr(FlMethodChannel) channel =
      fl_method_channel_new(fl_plugin_registrar_get_messenger(registrar),
                            "plugins.it_nomads.com/flutter_secure_storage",
                            FL_METHOD_CODEC(codec));
  fl_method_channel_set_method_call_handler(channel, method_call_cb,
                                            g_object_ref(plugin),
                                            g_object_unref);

  auto *label = g_strdup_printf("%s/FlutterSecureStorage", APPLICATION_ID);
  auto *account = g_strdup_printf("%s.secureStorage", APPLICATION_ID);
  keyring.setLabel(label);
  keyring.addAttribute("account", account);

  g_object_unref(plugin);
}
