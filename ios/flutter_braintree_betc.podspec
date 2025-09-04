#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'flutter_braintree_betc'
  s.version          = '2.0.7'
  s.summary          = 'A Flutter plugin for Braintree'
  s.description      = <<-DESC
  A Flutter plugin that wraps the native Braintree Drop-In UI SDKs.
                       DESC
  s.homepage         = 'https://github.com/BetchaGlobal/FlutterBraintree'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'BETCHA INA' => '' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  s.dependency 'BraintreeDropIn', '9.10.0'
  s.dependency 'Braintree/PayPal', '~> 5.23'
  s.dependency 'Braintree/ApplePay', '~> 5.23'
  s.ios.deployment_target = '12.0'
  s.swift_version = '5.0'
end
