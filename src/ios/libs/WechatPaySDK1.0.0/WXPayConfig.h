//
//  WXPayConfig.h
//  WechatPaySDK
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 SDK 全局配置（传给 @c +[WXPayKit initWithConfig:]）。

 对位 OpenSDK 的 @c +[WXApi registerApp:universalLink:]：两个字段一一对应，含义不变。
 与 OpenSDK 同时集成时各自 register 自己的 appId，互不共享这份配置。
 */
NS_SWIFT_NAME(WechatPayConfig)
@interface WXPayConfig : NSObject <NSCopying>

/**
 应用 appId。

 它同时是拉起微信的 URL 路径段（@c weixin://app/<appId>/...）和回包 URL 的认领依据
 （@c <appId>://pay?...）。**App 支付的请求对象里没有 appId**，用的就是这一个。
 */
@property (nonatomic, copy, readonly) NSString *appId;

/**
 在开放平台登记的 Universal Link，必须以 @c / 结尾。

 缺了它 iOS 12+ 上无法走 UL 通道，且微信回跳找不到落点：@c send 会直接失败，
 与 OpenSDK 未注册 UL 时的行为一致。
 */
@property (nonatomic, copy, readonly) NSString *universalLink;

/** @c appId 或 @c universalLink 传 nil 时收成空串，由发送链路判成参数错误，不抛出。 */
- (instancetype)initWithAppId:(nullable NSString *)appId
                universalLink:(nullable NSString *)universalLink NS_DESIGNATED_INITIALIZER;

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
