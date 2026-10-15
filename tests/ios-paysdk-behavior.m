#import <Foundation/Foundation.h>
#import "CDVWechatPayCoordinator.h"

static void Assert(BOOL condition, NSString *message) {
    if (!condition) {
        NSLog(@"ASSERTION FAILED: %@", message);
        exit(1);
    }
}

int main(void) {
    @autoreleasepool {
        __block NSInteger payCalls = 0;
        __block NSInteger openCalls = 0;
        BOOL handled = [CDVWechatPayCoordinator routePaySDK:^{
            payCalls += 1;
            return NO;
        } openSDK:^{
            openCalls += 1;
            return YES;
        }];
        Assert(handled && payCalls == 1 && openCalls == 1,
               @"PaySDK false must fall back to OpenSDK");

        handled = [CDVWechatPayCoordinator routePaySDK:^{
            payCalls += 1;
            return YES;
        } openSDK:^{
            openCalls += 1;
            return YES;
        }];
        Assert(handled && payCalls == 2 && openCalls == 1,
               @"PaySDK true must suppress OpenSDK fallback");

        NSDictionary *payload =
            [CDVWechatPayCoordinator payloadWithErrCode:0
                                                errStr:@""
                                      fromAppQrPay:YES];
        Assert([payload[@"fromAppQrPay"] boolValue],
               @"fromAppQrPay must pass through as true");
        Assert(payload[@"prepayId"] == nil,
               @"iOS callback payload must not invent a prepayId");
    }
    return 0;
}
