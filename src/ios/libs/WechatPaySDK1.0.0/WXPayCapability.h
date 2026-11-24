//
//  WXPayCapability.h
//  WechatPaySDK
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 PaySDK 在 iOS 上公开支持的支付能力。

 只有 App 支付、联合支付、支付场景三条，与鸿蒙一致。离线付款码、二维码、非税、医保这些
 OpenSDK 老能力不在 PaySDK 里，需要的继续用 OpenSDK。2–5 曾分给那四条，已经空出，不要复用。
 */
typedef NS_ENUM(NSInteger, WXPayCapability) {
    /** App 支付。 */
    WXPayCapabilityAppPay = 0,
    /** 联合支付。 */
    WXPayCapabilityJointPay = 1,
    /**
     打开微信支付独立进程里的一个页面。

     与 OpenSDK 的 @c WXOpenBusinessViewReq（微信先享后付那条）不是同一条能力：
     api name、回包路径都不同，两者互不认领。
     */
    WXPayCapabilityPayScene = 6,
} NS_SWIFT_NAME(WechatPayCapability);

NS_ASSUME_NONNULL_END
