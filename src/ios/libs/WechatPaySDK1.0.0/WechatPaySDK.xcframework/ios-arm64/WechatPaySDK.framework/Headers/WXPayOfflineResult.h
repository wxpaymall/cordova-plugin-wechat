#import "WXPayResult.h"

NS_ASSUME_NONNULL_BEGIN
NS_SWIFT_NAME(WechatPayOfflineResult)
@interface WXPayOfflineResult : NSObject <WXPayResult>
@property (nonatomic, readonly) NSInteger errCode;
@property (nonatomic, copy, readonly, nullable) NSString *errMessage;
- (instancetype)initWithErrCode:(NSInteger)errCode
                     errMessage:(nullable NSString *)errMessage NS_DESIGNATED_INITIALIZER;
- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;
@end
NS_ASSUME_NONNULL_END
