//
//  WXPayKit.h
//  WechatPaySDK
//

#import <Foundation/Foundation.h>
#import "WXPayConfig.h"
#import "WXPayLogger.h"
#import "WXPayResultHandler.h"
#import "WXPaySendResult.h"
#import "WXPayCapability.h"
#import "WXPayRequest.h"

NS_ASSUME_NONNULL_BEGIN

/**
 SDK 全局入口：初始化、发送、日志、环境查询、回包转发。

 **能力放在请求对象里，不放在方法名上。** 所有能力共用 @c send:completion: 一个入口，与
 OpenSDK 的 @c +[WXApi sendReq:completion:] 同一形状。

 **所有方法永不抛出**，也都只能在主线程调用（内部要访问 @c UIApplication）。
 */
NS_SWIFT_NAME(WechatPay)
@interface WXPayKit : NSObject

/**
 初始化 SDK。在 @c application:didFinishLaunchingWithOptions: 里调用一次即可。

 对位 OpenSDK 的 @c +[WXApi registerApp:universalLink:]，但**不做**它那次「向微信发一条
 注册请求」的动作——那条请求服务于分享 / 登录的注册态，支付链路不依赖它。

 重复调用以最后一次为准。
 */
+ (void)initWithConfig:(WXPayConfig *)config NS_SWIFT_NAME(setUp(config:));

/**
 把一笔请求发给微信。

 @c completion 只说明请求有没有发给微信，**不说明用户付没付钱**——与 OpenSDK 的
 @c +[WXApi sendReq:completion:] 同一水位。判断成功用 @c WXPaySendResult.isSent，
 它刻意不叫 @c isSuccess：回包结果类型上那个 @c isSuccess 才是「付款成功」。

 底层 @c -[UIApplication openURL:options:completionHandler:] 是异步的，所以这里没有同步
 返回值。@c completion 在主线程调用，可能同步（参数校验失败等前置分支）也可能异步
 （实际拉起），**不要依赖它的调用时机**。

 App 支付的结果在 @c AppDelegate 的 URL 回调里，用 @c handleOpenURL:handler: 拿。
 */
+ (void)send:(nullable WXPayRequest *)req
  completion:(nullable void (^)(WXPaySendResult *result))completion;

/**
 把微信回跳的 URL 交给 SDK。认领了返回 @c YES。

 返回 @c NO 表示这条 URL 不属于 PaySDK，此时**应当**再交给 OpenSDK 的
 @c +[WXApi handleOpenURL:delegate:]——两个 SDK 同时集成时分享、登录的回跳也会落在同一个
 入口，PaySDK 不认领它们。

 在 @c application:openURL:options: 里调用。
 */
+ (BOOL)handleOpenURL:(nullable NSURL *)url
              handler:(nullable id<WXPayResultHandler>)handler;

/**
 Universal Link 版本的回包入口，在
 @c application:continueUserActivity:restorationHandler: 里调用。

 语义与 @c handleOpenURL:handler: 相同。
 */
+ (BOOL)handleUniversalLink:(nullable NSUserActivity *)userActivity
                    handler:(nullable id<WXPayResultHandler>)handler;

/** 本机是否安装了微信。 */
+ (BOOL)isWechatInstalled;

/**
 本机微信是否支持某条能力。

 只使用可靠探针（scheme 探测）；探不到的一律返回 @c YES 交给微信侧判，不做推测性拦截。
 */
+ (BOOL)supports:(WXPayCapability)capability;

/** 接管 SDK 内部日志。传 nil 恢复默认的 @c NSLog 输出。可在 init 之前调用。 */
+ (void)setLogger:(nullable id<WXPayLogger>)logger;

/** 默认 @c WXPayLogLevelInfo。传 @c WXPayLogLevelNone 关闭日志。 */
+ (void)setLogLevel:(WXPayLogLevel)level;

/** PaySDK 版本号，形如 @c 1.0.0。 */
+ (NSString *)sdkVersion;

@end

NS_ASSUME_NONNULL_END
