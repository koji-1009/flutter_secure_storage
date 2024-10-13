// swift-tools-version: 5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
  name: "flutter_secure_storage_x",
  platforms: [
    .iOS("12.0")
  ],
  products: [
    .library(name: "flutter-secure-storage-x", targets: ["flutter_secure_storage_x"])
  ],
  dependencies: [],
  targets: [
    .target(
      name: "flutter_secure_storage_x",
      dependencies: [],
      resources: [
        .process("PrivacyInfo.xcprivacy")
      ]
    )
  ]
)
