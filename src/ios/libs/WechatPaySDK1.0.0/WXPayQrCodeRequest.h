#import "WXPayRequest.h"

NS_ASSUME_NONNULL_BEGIN
NS_SWIFT_NAME(WechatPayQrCodeRequest)
@interface WXPayQrCodeRequest : WXPayRequest
@property (nonatomic, copy, nullable) NSString *codeContent;
@property (nonatomic, copy, nullable) NSString *extraMessage;
@end
NS_ASSUME_NONNULL_END
