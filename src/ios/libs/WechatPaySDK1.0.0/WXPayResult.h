//
//  WXPayResult.h
//  WechatPaySDK
//

#import <Foundation/Foundation.h>
#import "WXPayCapability.h"

NS_ASSUME_NONNULL_BEGIN

/** 支付回包结果的稳定领域契约。 */
NS_SWIFT_NAME(WechatPayResult)
@protocol WXPayResult <NSObject>

@property (nonatomic, readonly) WXPayCapability capability;

@end

NS_ASSUME_NONNULL_END
