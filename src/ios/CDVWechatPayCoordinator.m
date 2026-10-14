#import "CDVWechatPayCoordinator.h"

@implementation CDVWechatPayCoordinator

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
