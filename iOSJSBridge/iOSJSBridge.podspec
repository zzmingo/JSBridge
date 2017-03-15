#
# Be sure to run `pod lib lint iOSJSBridge.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'iOSJSBridge'
  s.version          = '0.1.0'
  s.summary          = 'A short description of iOSJSBridge.'
  s.description      = 'A JSBridge for iOS WKWebView'

  s.homepage         = 'https://github.com/zzmingo/iOSJSBridge'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'mingo' => 'zhang.zhengming@uneed.com' }
  s.source           = { :git => 'https://github.com/mingo/iOSJSBridge.git', :tag => s.version.to_s }

  s.ios.deployment_target = '8.0'

  s.source_files = 'iOSJSBridge/Classes/**/*'
  s.dependency 'SwiftyJSON'

end
