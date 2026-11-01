#include <flutter_linux/flutter_linux.h>
#include <libsecret/secret.h>

#include <cstring>
#include <map>
#include <memory>
#include <stdexcept>
#include <string>

#include "FHashTable.hpp"

#define secret_autofree _GLIB_CLEANUP(secret_cleanup_free)
static inline void secret_cleanup_free(gchar **p) { secret_password_free(*p); }

class SecretStorage
{
  FHashTable m_attributes;
  std::string label;
  SecretSchema the_schema;

public:
  const char *getLabel() { return label.c_str(); }
  void setLabel(const char *label) { this->label = label; }

  SecretStorage(const char *_label = "default") : label(_label)
  {
    the_schema = {label.c_str(),
                  SECRET_SCHEMA_NONE,
                  {
                      {"account", SECRET_SCHEMA_ATTRIBUTE_STRING},
                  }};
  }

  void addAttribute(const char *key, const char *value)
  {
    m_attributes.insert(key, value);
  }

  bool addItem(const char *key, const char *value)
  {
    auto root = readFromKeyring();
    root[key] = value;
    return storeToKeyring(root);
  }

  std::string getItem(const char *key)
  {
    auto root = readFromKeyring();
    if (root.find(key) == root.end())
    {
      return "";
    }

    return root[key];
  }

  void deleteItem(const char *key)
  {
    auto root = readFromKeyring();
    if (root.empty())
    {
      return;
    }

    root.erase(key);
    storeToKeyring(root);
  }

  bool deleteKeyring()
  {
    return storeToKeyring({});
  }

  bool storeToKeyring(const std::map<std::string, std::string> &value)
  {
    g_autoptr(FlValue) json = fl_value_new_map();
    for (const auto &pair : value)
    {
      fl_value_set_string_take(json, pair.first.c_str(),
                               fl_value_new_string(pair.second.c_str()));
    }

    g_autoptr(FlJsonMessageCodec) codec = fl_json_message_codec_new();
    g_autoptr(GError) encode_err = nullptr;
    g_autofree gchar *output = fl_json_message_codec_encode(codec, json, &encode_err);
    if (output == nullptr)
    {
      throw std::runtime_error(std::string("storeToKeyring: ") + encode_err->message);
    }

    g_autoptr(GError) err = nullptr;

    bool result = secret_password_storev_sync(
        &the_schema, m_attributes.getGHashTable(), nullptr, label.c_str(),
        output, nullptr, &err);

    if (err)
    {
      throw std::runtime_error("storeToKeyring: " + std::string(err->message));
    }

    return result;
  }

  std::map<std::string, std::string> readFromKeyring()
  {
    warmupKeyring();

    std::map<std::string, std::string> value;
    g_autoptr(GError) err = nullptr;

    secret_autofree gchar *result = secret_password_lookupv_sync(
        &the_schema, m_attributes.getGHashTable(), nullptr, &err);

    if (err)
    {
      throw std::runtime_error(std::string("readFromKeyring: ") + err->message);
    }
    if (result != NULL && strcmp(result, "") != 0)
    {
      g_autoptr(FlJsonMessageCodec) codec = fl_json_message_codec_new();
      g_autoptr(GError) decode_err = nullptr;
      g_autoptr(FlValue) json = fl_json_message_codec_decode(codec, result, &decode_err);
      if (json == nullptr)
      {
        throw std::runtime_error(std::string("readFromKeyring: ") + decode_err->message);
      }

      // Older versions stored "null" after deleteAll.
      if (fl_value_get_type(json) == FL_VALUE_TYPE_NULL)
      {
        return value;
      }
      if (fl_value_get_type(json) != FL_VALUE_TYPE_MAP)
      {
        throw std::runtime_error("readFromKeyring: Stored data is not a JSON object");
      }

      for (size_t i = 0; i < fl_value_get_length(json); i++)
      {
        FlValue *k = fl_value_get_map_key(json, i);
        FlValue *v = fl_value_get_map_value(json, i);
        if (fl_value_get_type(k) != FL_VALUE_TYPE_STRING ||
            fl_value_get_type(v) != FL_VALUE_TYPE_STRING)
        {
          throw std::runtime_error("readFromKeyring: Stored value is not a string");
        }
        value[fl_value_get_string(k)] = fl_value_get_string(v);
      }
    }
    return value;
  }

private:
  // Search with schemas fails in cold keyrings.
  // https://gitlab.gnome.org/GNOME/gnome-keyring/-/issues/89
  //
  // Note that we're not using the workaround mentioned in the above issue. Instead, we're using
  // a workaround as implemented in http://crbug.com/660005. Reason being that with the lookup
  // approach we can't distinguish whether the keyring was actually unlocked or whether the user
  // cancelled the password prompt.
  void warmupKeyring()
  {
    g_autoptr(GError) err = nullptr;

    FHashTable attributes;
    attributes.insert("explanation", "Because of quirks in the gnome libsecret API, "
                                     "flutter_secret_storage needs to store a dummy entry to guarantee that "
                                     "this keyring was properly unlocked. More details at http://crbug.com/660005.");

    const gchar *dummy_label = "FlutterSecureStorage Control";

    // Store a dummy entry without `the_schema`.
    bool success = secret_password_storev_sync(
        NULL, attributes.getGHashTable(), nullptr, dummy_label,
        "The meaning of life", nullptr, &err);

    if (err)
    {
      throw std::runtime_error(std::string("warmupKeyring: ") + err->message);
    }

    if (!success)
    {
      throw std::runtime_error("warmupKeyring: Failed to unlock the keyring");
    }
  }
};
