# flutter_secure_storage_x Monorepo

[![style: lint](https://img.shields.io/badge/style-flutter_lints-4BC0F5.svg)](https://pub.dev/packages/flutter_lints)
[![flutter_secure_storage_x](https://github.com/koji-1009/flutter_secure_storage/actions/workflows/flutter.yml/badge.svg)](https://github.com/koji-1009/flutter_secure_storage/actions/workflows/flutter.yml)
[![flutter_secure_storage_x](https://github.com/koji-1009/flutter_secure_storage/actions/workflows/flutter_drive.yml/badge.svg)](https://github.com/koji-1009/flutter_secure_storage/actions/workflows/flutter_drive.yml)

This repository is a fork of the popular [flutter_secure_storage](https://pub.dev/packages/flutter_secure_storage) package. The original project aims to provide a comprehensive set of features and options to cover a wide range of needs.

In contrast, this fork takes a more focused approach. The philosophy is to ensure long-term stability and maintainability by offering a minimal, robust API for the most common encrypted storage use cases. This pragmatic direction provides a simple and reliable solution for developers who prioritize these aspects.

This monorepo contains the following packages:

- `flutter_secure_storage_x`: The main package.
- `flutter_secure_storage_x_platform_interface`: The platform interface.
- `flutter_secure_storage_x_web`: Web implementation.
- `flutter_secure_storage_x_linux`: Linux implementation.
- `flutter_secure_storage_x_macos`: macOS implementation.
- `flutter_secure_storage_x_windows`: Windows implementation.

## Contributing

This project uses [melos](https://melos.invertase.dev/) to manage the monorepo.

To contribute, you need to initialize the workspace after cloning the repo:

```shell
# Install melos if you haven't already
dart pub global activate melos

# Clone the repository
git clone https://github.com/koji-1009/flutter_secure_storage.git
cd flutter_secure_storage

# Bootstrap the workspace
melos bootstrap
```

After that, everything should be set up for development.

### Running Tests

To run integration tests for the main package, navigate to the `flutter_secure_storage_x/example` directory and run:

```shell
flutter test integration_test
```