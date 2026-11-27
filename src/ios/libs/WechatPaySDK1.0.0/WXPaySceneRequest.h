#import "WXPayRequest.h"

NS_ASSUME_NONNULL_BEGIN

NS_SWIFT_NAME(WechatPaySceneRequest)
@interface WXPaySceneRequest : WXPayRequest

@property (nonatomic, copy, nullable) NSString *sceneType;
@property (nonatomic, copy, nullable) NSString *query;
@property (nonatomic, copy, nullable) NSString *extInfo;
@property (nonatomic, copy, nullable) NSString *transaction;
@property (nonatomic, copy, nullable) NSString *openId;

@end

NS_ASSUME_NONNULL_END
