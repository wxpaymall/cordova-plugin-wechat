//
//  WXPayLogger.h
//  WechatPaySDK
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, WXPayLogLevel) {
    WXPayLogLevelVerbose = 2,
    WXPayLogLevelDebug   = 3,
    WXPayLogLevelInfo    = 4,
    WXPayLogLevelWarn    = 5,
    WXPayLogLevelError   = 6,
    /** 关闭全部日志输出。 */
    WXPayLogLevelNone    = 100,
} NS_SWIFT_NAME(WechatPayLogLevel);

/**
 日志出口。商户实现后通过 @c +[WXPayKit setLogger:] 注入，即可把 SDK 内部日志接到自有
 日志体系。不注入时默认写 @c NSLog。
 */
NS_SWIFT_NAME(WechatPayLogger)
@protocol WXPayLogger <NSObject>

/**
 @param level   日志级别
 @param tag     形如 @c PaySDK.Launcher
 @param message 已脱敏文本，不含 sign、token 与完整 prepayId
 */
- (void)logWithLevel:(WXPayLogLevel)level
                 tag:(NSString *)tag
             message:(NSString *)message;

@end

NS_ASSUME_NONNULL_END
