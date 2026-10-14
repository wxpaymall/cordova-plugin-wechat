//
//  WXPayResultHandler.h
//  WechatPaySDK
//

#import <Foundation/Foundation.h>
#import "WXPayAppPayResult.h"
#import "WXPayInsuranceResult.h"
#import "WXPayJointPayResult.h"
#import "WXPayNontaxResult.h"
#import "WXPayOfflineResult.h"
#import "WXPayQrCodeResult.h"
#import "WXPaySceneResult.h"

NS_ASSUME_NONNULL_BEGIN

/**
 接收微信回包的 handler，对位 OpenSDK 的 @c WXApiDelegate。

 回包以 URL 形式落在你的 @c AppDelegate（@c application:openURL:options: 与
 @c application:continueUserActivity:restorationHandler:），在那里把 URL 交给
 @c +[WXPayKit handleOpenURL:handler:]，SDK 解析、上报之后**在调用线程同步**回调这里。

 方法都是 @c \@optional，只实现你接入的那条能力即可；**没实现的能力，回包会被丢弃**。

 使用它有两条**义务**，不是建议：

 1. **按订单标识自行分派。** 回包落点只有一个，且不绑定发起它的那次 @c send：用户连续下单、
    App 被回收后由微信冷启动拉起时，到手的结果未必是当前页面那一笔。iOS 的支付回包里能用于
    对账的只有 @c returnKey，发起时就要把它与自己的订单号一起**落盘**，只存内存的映射会随
    进程一起消失。这与 OpenSDK 要求商户在 @c onResp 里自行过滤是同一件事。
 2. **同一条 URL 不要交给同一个 SDK 两次。** 重复交付会让同一笔结果被处理两遍，上报也会多
    一条。**但同时接 PaySDK 与 OpenSDK 是安全的**：把 URL 先交给
    @c +[WXPayKit handleOpenURL:handler:]，它返回 @c NO 再交给 @c +[WXApi handleOpenURL:delegate:]，
    最多只有一条链会认领它。

 **客户端回包不可信**：它只用于驱动界面，落账一律以你自己的服务端订单状态为准。
 */
NS_SWIFT_NAME(WechatPayResultHandler)
@protocol WXPayResultHandler <NSObject>

@optional

/** App 支付回包。 */
- (void)onAppPayResult:(WXPayAppPayResult *)result;

/** 联合支付回包。 */
- (void)onJointPayResult:(WXPayJointPayResult *)result;

- (void)onOfflinePayResult:(WXPayOfflineResult *)result;
- (void)onQrCodePayResult:(WXPayQrCodeResult *)result;
- (void)onNontaxPayResult:(WXPayNontaxResult *)result;
- (void)onInsurancePayResult:(WXPayInsuranceResult *)result;
- (void)onScenePayResult:(WXPaySceneResult *)result;

@end

NS_ASSUME_NONNULL_END
