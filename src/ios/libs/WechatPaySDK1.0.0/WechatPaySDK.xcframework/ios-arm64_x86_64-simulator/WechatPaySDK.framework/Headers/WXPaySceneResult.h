#import "WXPayResult.h"

NS_ASSUME_NONNULL_BEGIN

NS_SWIFT_NAME(WechatPaySceneResult)
@interface WXPaySceneResult : NSObject <WXPayResult>

@property (nonatomic, readonly) NSInteger errCode;
@property (nonatomic, copy, readonly, nullable) NSString *sceneType;
@property (nonatomic, copy, readonly, nullable) NSString *extMessage;
@property (nonatomic, copy, readonly, nullable) NSString *transaction;

- (instancetype)initWithErrCode:(NSInteger)errCode
                      sceneType:(nullable NSString *)sceneType
                     extMessage:(nullable NSString *)extMessage
                    transaction:(nullable NSString *)transaction NS_DESIGNATED_INITIALIZER;

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
