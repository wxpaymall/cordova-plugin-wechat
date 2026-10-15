//
//  WXPayCapability.h
//  WechatPaySDK
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 PaySDK 在 iOS 上公开支持的支付能力。

 取值范围以 iOS OpenSDK 已有的支付能力为基线，再加 PaySDK 独有的 @c WXPayCapabilityPayScene。
 Android 有而这里没有的（企业红包）是因为 iOS 微信侧从来没有实现过那条协议，不是本 SDK 的取舍。
 */
typedef NS_ENUM(NSInteger, WXPayCapability) {
    /** App 支付。 */
    WXPayCapabilityAppPay = 0,
    /** 联合支付。 */
    WXPayCapabilityJointPay = 1,
    /** 跳转微信离线付款码；微信回跳时返回 @c WXPayOfflineResult。 */
    WXPayCapabilityOfflinePay = 2,
    /** 二维码支付。 */
    WXPayCapabilityQrCodePay = 3,
    /** 非税支付。本质是打开商户给定 URL 的微信内 H5。 */
    WXPayCapabilityNontaxPay = 4,
    /** 医保支付。与非税同构，只差微信侧的一个类型号。 */
    WXPayCapabilityPayInsurance = 5,
    /**
     打开微信支付独立进程里的一个页面。

     与 OpenSDK 的 @c WXOpenBusinessViewReq（微信先享后付那条）不是同一条能力：
     api name、回包路径都不同，两者互不认领。
     */
    WXPayCapabilityPayScene = 6,
} NS_SWIFT_NAME(WechatPayCapability);

NS_ASSUME_NONNULL_END
