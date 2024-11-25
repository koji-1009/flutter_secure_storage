#include <libsecret/secret.h>
#include <memory>

#include "FHashTable.hpp"
#include "json.hpp"

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
    nlohmann::json json;
    for (const auto &pair : value)
    {
      json[pair.first] = pair.second;
    }
    const std::string output = json.dump(0);
    g_autoptr(GError) err = nullptr;

    bool result = secret_password_storev_sync(
        &the_schema, m_attributes.getGHashTable(), nullptr, label.c_str(),
        output.c_str(), nullptr, &err);

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
      auto json_value = nlohmann::json::parse(result);
      for (auto &el : json_value.items())
      {
        value[el.key()] = el.value();
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
