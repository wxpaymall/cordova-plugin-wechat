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

/**
 是否启用码上付，默认 YES，见 @c -configWithAppQrPayEnabled: 。

 开启只表示商户允许；这一笔是否真的展码，还要看后台放量和本机条件。
 */
@property (nonatomic, readonly) BOOL appQrPayEnabled;

/** @c appId 或 @c universalLink 传 nil 时收成空串，由发送链路判成参数错误，不抛出。 */
- (instancetype)initWithAppId:(nullable NSString *)appId
                universalLink:(nullable NSString *)universalLink;

- (instancetype)initWithAppId:(nullable NSString *)appId
                universalLink:(nullable NSString *)universalLink
              appQrPayEnabled:(BOOL)appQrPayEnabled NS_DESIGNATED_INITIALIZER;

/**
 返回一份只改了码上付开关的新配置，其余配置项原样保留，本对象不变。

 码上付：本机未安装微信时，App 支付改为展示一张付款二维码，由另一台已登录微信的设备扫码代付。
 默认开启。关闭后 App 支付在微信不可用时按原有逻辑返回失败，SDK 不再展码。
 只能关、不能强开，放量仍由后台在签发时决定。

 @code
 [WXPayKit initWithConfig:[[[WXPayConfig alloc] initWithAppId:@"wxYOUR_APP_ID"
                                                universalLink:@"https://example.com/app/"]
                              configWithAppQrPayEnabled:NO]];
 @endcode
 */
- (WXPayConfig *)configWithAppQrPayEnabled:(BOOL)enabled
    NS_SWIFT_NAME(withAppQrPayEnabled(_:));

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
