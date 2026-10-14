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
        CDVWechatPayCoordinator *coordinator = [[CDVWechatPayCoordinator alloc] init];
        __block NSString *openSDKCallbackId = @"share-callback";

        Assert([coordinator beginPaymentWithCallbackId:@"pay-callback"],
               @"first payment must reserve its own callback");
        openSDKCallbackId = @"auth-callback";
        Assert(![coordinator beginPaymentWithCallbackId:@"second-pay-callback"],
               @"second in-flight payment must be rejected");
        Assert([[coordinator consumePaymentCallbackId] isEqualToString:@"pay-callback"],
               @"payment result must target only the payment callback");
        Assert([openSDKCallbackId isEqualToString:@"auth-callback"],
               @"payment result must not clear or overwrite OpenSDK callback state");
        Assert([coordinator consumePaymentCallbackId] == nil,
               @"cold-start result without a callback must be dropped");

        Assert([coordinator beginPaymentWithCallbackId:@"cancelled-pay"],
               @"payment slot must be reusable after consuming a result");
        [coordinator cancelPaymentWithCallbackId:@"other-pay"];
        Assert(![coordinator beginPaymentWithCallbackId:@"still-rejected"],
               @"a mismatched cancellation must not clear the active payment");
        [coordinator cancelPaymentWithCallbackId:@"cancelled-pay"];
        Assert([coordinator beginPaymentWithCallbackId:@"next-pay"],
               @"matching send failure must release the payment slot");
        Assert([[coordinator consumePaymentCallbackId] isEqualToString:@"next-pay"],
               @"released slot must not receive an earlier result");

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
