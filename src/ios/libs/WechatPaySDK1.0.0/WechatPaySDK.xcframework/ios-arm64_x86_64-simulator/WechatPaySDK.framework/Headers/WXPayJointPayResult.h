//
//  WXPayJointPayResult.h
//  WechatPaySDK
//

#import <Foundation/Foundation.h>
#import "WXPayResult.h"

NS_ASSUME_NONNULL_BEGIN

/** 联合支付回包；字段语义与 OpenSDK 的 WXJointPayResp 一致。 */
NS_SWIFT_NAME(WechatPayJointPayResult)
@interface WXPayJointPayResult : NSObject <WXPayResult>

@property (nonatomic, readonly) NSInteger errCode;
@property (nonatomic, copy, readonly, nullable) NSString *errMessage;
@property (nonatomic, copy, readonly, nullable) NSString *returnKey;
@property (nonatomic, readonly) BOOL isSuccess;
@property (nonatomic, readonly) BOOL isCancel;

- (instancetype)initWithErrCode:(NSInteger)errCode
                     errMessage:(nullable NSString *)errMessage
                      returnKey:(nullable NSString *)returnKey NS_DESIGNATED_INITIALIZER;

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
