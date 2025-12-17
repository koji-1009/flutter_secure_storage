#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint flutter_secure_storage_macos.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'flutter_secure_storage_x_macos'
  s.version          = '6.1.1'
  s.summary          = 'flutter_secure_storage_x'
  s.description      = <<-DESC
flutter_secure_storage_x Plugin for macOS
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files     = 'flutter_secure_storage_x_macos/Sources/flutter_secure_storage_x_macos/**/*.swift'
  s.dependency 'FlutterMacOS'

  s.platform = :osx, '10.14'
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES' }
  s.swift_version = '5.0'
  s.resource_bundles = {'flutter_secure_storage_x_macos' => ['flutter_secure_storage_x_macos/Sources/flutter_secure_storage_x_macos/PrivacyInfo.xcprivacy']}
end
