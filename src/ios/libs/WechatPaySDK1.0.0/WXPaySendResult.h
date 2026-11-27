//
//  WXPaySendResult.h
//  WechatPaySDK
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 发送结果码。

 取值与 Android PaySDK 的 @c SendResult 语义对齐，但只保留 iOS 这一端**真能判定**的那些：
 Android 的 @c CODE_WECHAT_SIGNATURE_INVALID（校验微信安装包签名）在 iOS 上没有对应能力，
 故不设此码。不设推测性判断是本 SDK 与 OpenSDK 行为一致的前提。
 */
typedef NS_ENUM(NSInteger, WXPaySendResultCode) {
    /** 本地投递原语正常返回。 */
    WXPaySendResultCodeOK = 0,
    /** 未调用 @c +[WXPayKit initWithConfig:]。集成错误，改代码。 */
    WXPaySendResultCodeNotInitialized = 1001,
    /** 请求参数不合法。集成错误，改代码。 */
    WXPaySendResultCodeInvalidParam = 1002,
    /** 微信未安装。引导用户安装。 */
    WXPaySendResultCodeWechatNotInstalled = 1004,
    /** 该能力当前不可用（本机微信版本不支持）。走降级链路。 */
    WXPaySendResultCodeCapabilityDisabled = 1006,
    /** 投递原语失败：URL 拼装失败、写通道失败，或 @c openURL 返回 NO。 */
    WXPaySendResultCodeLaunchBlocked = 1007,
    /** 未归类失败。 */
    WXPaySendResultCodeUnknown = 1099,
} NS_SWIFT_NAME(WechatPaySendResultCode);

/**
 发送结果。所有能力的 @c send 都通过 completion 回它。

 它只回答「请求发出去了吗」，不回答「用户付了钱吗」——与 OpenSDK @c +[WXApi sendReq:completion:]
 回调的那个 @c BOOL 同一水位。支付结果是另一个类型（@c WXPayAppPayResult 等），要在
 @c AppDelegate 的 URL 回调里用 @c +[WXPayKit handleOpenURL:handler:] 拿。

 **@c isSent 为 YES 时不要提示「支付成功」。** 它只说明 @c openURL 返回了成功，既不保证微信
 完成了处理，更不保证用户付了钱。属性名不叫 @c isSuccess 正是为了这条。
 */
NS_SWIFT_NAME(WechatPaySendResult)
@interface WXPaySendResult : NSObject

/** 请求是否已被 SDK 确认发出。等价于 @c code == WXPaySendResultCodeOK。 */
@property (nonatomic, readonly) BOOL isSent;
/** 见 @c WXPaySendResultCode。 */
@property (nonatomic, readonly) WXPaySendResultCode code;
/**
 排障用的可读描述，非空。

 措辞不构成兼容承诺，不要拿它做分支判断，也不要展示给终端用户。
 */
@property (nonatomic, copy, readonly) NSString *message;

+ (instancetype)ok;
+ (instancetype)failureWithCode:(WXPaySendResultCode)code message:(NSString *)message;

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
