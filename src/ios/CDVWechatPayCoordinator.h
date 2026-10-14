#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef BOOL (^CDVWechatCallbackHandler)(void);

/// Owns the single in-flight AppPay callback independently from OpenSDK callbacks.
@interface CDVWechatPayCoordinator : NSObject

- (BOOL)beginPaymentWithCallbackId:(NSString *)callbackId;
- (void)cancelPaymentWithCallbackId:(NSString *)callbackId;
- (nullable NSString *)consumePaymentCallbackId;

+ (BOOL)routePaySDK:(CDVWechatCallbackHandler)paySDK
            openSDK:(CDVWechatCallbackHandler)openSDK;

+ (NSDictionary *)payloadWithErrCode:(NSInteger)errCode
                              errStr:(nullable NSString *)errStr
                    fromAppQrPay:(BOOL)fromAppQrPay;

@end

NS_ASSUME_NONNULL_END
