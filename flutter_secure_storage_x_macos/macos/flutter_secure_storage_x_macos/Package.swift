// swift-tools-version: 5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
  name: "flutter_secure_storage_x_macos",
  platforms: [
    .macOS("10.14")
  ],
  products: [
    .library(name: "flutter-secure-storage-x-macos", targets: ["flutter_secure_storage_x_macos"])
  ],
  dependencies: [],
  targets: [
    .target(
      name: "flutter_secure_storage_x_macos",
      dependencies: [],
      resources: [
        .process("PrivacyInfo.xcprivacy")
      ]
    )
  ]
)
