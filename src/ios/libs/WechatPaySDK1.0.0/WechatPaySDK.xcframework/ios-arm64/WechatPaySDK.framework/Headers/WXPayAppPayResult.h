//
//  WXPayAppPayResult.h
//  WechatPaySDK
//

#import <Foundation/Foundation.h>
#import "WXPayResult.h"

NS_ASSUME_NONNULL_BEGIN

/** 释义与 OpenSDK @c WXErrCode 逐字一致，不缩窄微信的原始语义。 */
typedef NS_ENUM(NSInteger, WXPayErrCode) {
    /** 成功。 */
    WXPayErrCodeSuccess = 0,
    /** 一般错误。 */
    WXPayErrCodeCommon = -1,
    /** 用户取消。 */
    WXPayErrCodeUserCancel = -2,
    /** 被屏蔽所有操作，可能由于签名不正确或无权限。 */
    WXPayErrCodeBan = -6,
} NS_SWIFT_NAME(WechatPayErrCode);

/**
 App 支付的**回包**结果，形状对齐 OpenSDK 的 @c PayResp。

 它只从一个地方来：@c WXPayKit 在 @c handleOpenURL:handler: 里回调的
 @c WXPayResultHandler.onAppPayResult:。@c send 不返回它——那一步给的是 @c WXPaySendResult，
 只回答「请求发出去了吗」。

 **客户端回包不可信。** @c isSuccess 用来驱动界面，落账一律以你自己的服务端订单状态为准。

 回包落点只有一个，所有笔的结果都从这里来，**必须自行分派**，不能假设「收到回包 = 我当前
 这笔」。这与 OpenSDK 要求商户在 @c onResp 里自行过滤是同一件事。iOS 的支付回包里没有
 @c prepayId，用于对账的只有 @c returnKey，发起时就要把它与自己的订单号一起落盘。
 */
NS_SWIFT_NAME(WechatPayAppPayResult)
@interface WXPayAppPayResult : NSObject <WXPayResult>

/** 微信原样返回的 @c ret。上面四个常量之外的值原样透传，不做归一。 */
@property (nonatomic, readonly) NSInteger errCode;
/** 微信原样返回的 @c notifyStr，可能为空。不要解析它，也不要展示给终端用户。 */
@property (nonatomic, copy, readonly, nullable) NSString *errMessage;
/** 财付通返回给商家的信息。 */
@property (nonatomic, copy, readonly, nullable) NSString *returnKey;

/** 等价于 @c errCode == WXPayErrCodeSuccess。**客户端结果不可信，落账一律以服务端为准。** */
@property (nonatomic, readonly) BOOL isSuccess;
/** 等价于 @c errCode == WXPayErrCodeUserCancel。 */
@property (nonatomic, readonly) BOOL isCancel;
/** 是否来自 SDK 码上付页面的本地回包；该回包只用于驱动商户查单。 */
@property (nonatomic, readonly) BOOL fromAppQrPay NS_SWIFT_NAME(isFromAppQrPay);

- (instancetype)initWithErrCode:(NSInteger)errCode
                     errMessage:(nullable NSString *)errMessage
                      returnKey:(nullable NSString *)returnKey NS_DESIGNATED_INITIALIZER;

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
