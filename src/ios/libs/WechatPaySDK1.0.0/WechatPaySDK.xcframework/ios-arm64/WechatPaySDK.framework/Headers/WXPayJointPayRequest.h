//
//  WXPayJointPayRequest.h
//  WechatPaySDK
//

#import <Foundation/Foundation.h>
#import "WXPayRequest.h"

NS_ASSUME_NONNULL_BEGIN

/** 联合支付请求。字段、校验与 wire 取值形态均与 App 支付一致。 */
NS_SWIFT_NAME(WechatPayJointPayRequest)
@interface WXPayJointPayRequest : WXPayRequest

@property (nonatomic, copy, nullable) NSString *partnerId;
@property (nonatomic, copy, nullable) NSString *prepayId;
@property (nonatomic, copy, nullable) NSString *nonceStr;
/** 与 OpenSDK 一致不参与本地校验；0 原样交给微信。 */
@property (nonatomic, assign) UInt32 timeStamp;
@property (nonatomic, copy, nullable) NSString *package;
@property (nonatomic, copy, nullable) NSString *sign;

@end

NS_ASSUME_NONNULL_END
