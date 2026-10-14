#import "CDVWechatPayCoordinator.h"

@interface CDVWechatPayCoordinator ()
@property (nonatomic, copy, nullable) NSString *paymentCallbackId;
@end

@implementation CDVWechatPayCoordinator

- (BOOL)beginPaymentWithCallbackId:(NSString *)callbackId
{
    @synchronized (self) {
        if (self.paymentCallbackId != nil) {
            return NO;
        }
        self.paymentCallbackId = callbackId;
        return YES;
    }
}

- (void)cancelPaymentWithCallbackId:(NSString *)callbackId
{
    @synchronized (self) {
        if ([self.paymentCallbackId isEqualToString:callbackId]) {
            self.paymentCallbackId = nil;
        }
    }
}

- (NSString *)consumePaymentCallbackId
{
    @synchronized (self) {
        NSString *callbackId = self.paymentCallbackId;
        self.paymentCallbackId = nil;
        return callbackId;
    }
}

+ (BOOL)routePaySDK:(CDVWechatCallbackHandler)paySDK
            openSDK:(CDVWechatCallbackHandler)openSDK
{
    if (paySDK()) {
        return YES;
    }
    return openSDK();
}

+ (NSDictionary *)payloadWithErrCode:(NSInteger)errCode
                              errStr:(NSString *)errStr
                    fromAppQrPay:(BOOL)fromAppQrPay
{
    return @{
        @"errCode": @(errCode),
        @"errStr": errStr ?: @"",
        @"fromAppQrPay": @(fromAppQrPay),
    };
}

@end
