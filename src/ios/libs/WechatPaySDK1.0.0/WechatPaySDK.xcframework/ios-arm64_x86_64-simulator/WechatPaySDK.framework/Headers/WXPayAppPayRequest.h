//
//  WXPayAppPayRequest.h
//  WechatPaySDK
//

#import <Foundation/Foundation.h>
#import "WXPayRequest.h"

NS_ASSUME_NONNULL_BEGIN

/**
 App 支付请求。字段与取值形态对齐 iOS OpenSDK 的 @c PayReq，均由商户后端产出、经 App 中继。

 这里没有 @c appId：iOS 的 App 支付把它放在拉起 URL 的路径段上（@c weixin://app/<appId>/），
 由 @c WXPayConfig.appId 提供。Android 的同名请求带 appId，是那一端的 wire 形态不同，
 不是本端漏了字段。

 也没有 @c signType：微信 iOS 侧只接受 @c SHA1，OpenSDK 在拼串时写死，PaySDK 保持一致。
 */
NS_SWIFT_NAME(WechatPayAppPayRequest)
@interface WXPayAppPayRequest : WXPayRequest

/** 商家向财付通申请的商家 id。必填。 */
@property (nonatomic, copy, nullable) NSString *partnerId;
/** 预支付订单号。必填。 */
@property (nonatomic, copy, nullable) NSString *prepayId;
/** 随机串，防重发。必填。 */
@property (nonatomic, copy, nullable) NSString *nonceStr;
/**
 时间戳，防重发。

 @c validate 不检查它：OpenSDK 拼 extraUrl 时也不检查，0 会原样进 wire 交给微信判。
 新增这道拦截会让本来能走通（或本来由微信给出明确错误）的请求变成本地失败。
 */
@property (nonatomic, assign) UInt32 timeStamp;
/** 商家根据财付通文档填写的数据和签名。必填，通常为 @c Sign=WXPay。 */
@property (nonatomic, copy, nullable) NSString *package;
/** 商家根据微信开放平台文档对数据做的签名。必填。 */
@property (nonatomic, copy, nullable) NSString *sign;

@end

NS_ASSUME_NONNULL_END
