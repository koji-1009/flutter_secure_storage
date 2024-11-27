#include <flutter_linux/flutter_linux.h>

#include "include/flutter_secure_storage_x_linux/flutter_secure_storage_x_linux_plugin.h"

// This file exposes some plugin internals for unit testing. See
// https://github.com/flutter/flutter/issues/88724 for current limitations
// in the unit-testable API.

// Handles the write method call
FlMethodResponse *write(const gchar *key, const gchar *value);

// Handles the read method call
FlMethodResponse *read(const gchar *key);

// Handles the readAll method call
FlMethodResponse *read_all();

// Handles the delete method call
FlMethodResponse *delete_it(const gchar *key);

// Handles the deleteAll method call
FlMethodResponse *delete_all();

// Handles the containsKey method call
FlMethodResponse *contains_key(const gchar *key);
