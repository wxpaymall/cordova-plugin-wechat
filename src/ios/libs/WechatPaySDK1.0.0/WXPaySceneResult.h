#import "WXPayResult.h"

NS_ASSUME_NONNULL_BEGIN

NS_SWIFT_NAME(WechatPaySceneResult)
@interface WXPaySceneResult : NSObject <WXPayResult>

@property (nonatomic, readonly) NSInteger errCode;
@property (nonatomic, copy, readonly, nullable) NSString *errMessage;
@property (nonatomic, copy, readonly, nullable) NSString *sceneType;
@property (nonatomic, copy, readonly, nullable) NSString *extMessage;
@property (nonatomic, copy, readonly, nullable) NSString *transaction;
/// iOS 微信不回 openId（同 iOS OpenBusinessView），恒为空；保留字段与 Android、鸿蒙同形。
@property (nonatomic, copy, readonly, nullable) NSString *openId;

- (instancetype)initWithErrCode:(NSInteger)errCode
                     errMessage:(nullable NSString *)errMessage
                      sceneType:(nullable NSString *)sceneType
                     extMessage:(nullable NSString *)extMessage
                    transaction:(nullable NSString *)transaction
                         openId:(nullable NSString *)openId NS_DESIGNATED_INITIALIZER;

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
