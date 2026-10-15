#import "WXPayResult.h"

NS_ASSUME_NONNULL_BEGIN
NS_SWIFT_NAME(WechatPayNontaxResult)
@interface WXPayNontaxResult : NSObject <WXPayResult>
@property (nonatomic, readonly) NSInteger errCode;
@property (nonatomic, copy, readonly, nullable) NSString *orderId;
- (instancetype)initWithErrCode:(NSInteger)errCode
                        orderId:(nullable NSString *)orderId NS_DESIGNATED_INITIALIZER;
- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;
@end
NS_ASSUME_NONNULL_END
