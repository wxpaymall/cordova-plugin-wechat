#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef BOOL (^CDVWechatCallbackHandler)(void);

/// Routes SDK callbacks and normalizes PaySDK result payloads.
@interface CDVWechatPayCoordinator : NSObject

+ (BOOL)routePaySDK:(CDVWechatCallbackHandler)paySDK
            openSDK:(CDVWechatCallbackHandler)openSDK;

+ (NSDictionary *)payloadWithErrCode:(NSInteger)errCode
                              errStr:(nullable NSString *)errStr
                    fromAppQrPay:(BOOL)fromAppQrPay;

@end

NS_ASSUME_NONNULL_END
