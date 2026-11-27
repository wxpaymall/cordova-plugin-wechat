//
//  WXPayRequest.h
//  WechatPaySDK
//

#import <Foundation/Foundation.h>
#import "WXPayCapability.h"

NS_ASSUME_NONNULL_BEGIN

/**
 支付请求的抽象基类。

 不要直接实例化，也不要在 SDK 外派生子类：@c WXPayKit 按具体类型派发，未知子类一律
 判成 @c WXPaySendResultCodeInvalidParam。
 */
NS_SWIFT_NAME(WechatPayRequest)
@interface WXPayRequest : NSObject

/** 本请求对应的能力。子类必须覆写。 */
@property (nonatomic, readonly) WXPayCapability capability;

/**
 返回第一个非法字段名；全部合法返回 nil。

 与 OpenSDK 的参数自检同口径：只判必填项是否为空、长度是否越界，不做业务语义校验。
 */
- (nullable NSString *)validate;

@end

NS_ASSUME_NONNULL_END
