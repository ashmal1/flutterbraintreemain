#import "FlutterBraintreePlugin.h"
#import <flutter_braintree_betc/flutter_braintree_betc-Swift.h>

@implementation FlutterBraintreePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    [FlutterBraintreeCustomPlugin registerWithRegistrar:registrar];
    [FlutterBraintreeDropInPlugin registerWithRegistrar:registrar];
}
@end
