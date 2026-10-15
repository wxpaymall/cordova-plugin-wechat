//
//  WechatPaySDK.h
//  WechatPaySDK
//
//  伞头文件：商户只需 #import <WechatPaySDK/WechatPaySDK.h>。
//

#import <Foundation/Foundation.h>

FOUNDATION_EXPORT double WechatPaySDKVersionNumber;
FOUNDATION_EXPORT const unsigned char WechatPaySDKVersionString[];

#import "WXPayKit.h"
#import "WXPayConfig.h"
#import "WXPayLogger.h"
#import "WXPayResultHandler.h"
#import "WXPaySendResult.h"
#import "WXPayCapability.h"
#import "WXPayRequest.h"
#import "WXPayResult.h"
#import "WXPayAppPayRequest.h"
#import "WXPayAppPayResult.h"
#import "WXPayJointPayRequest.h"
#import "WXPayJointPayResult.h"
#import "WXPayOfflineRequest.h"
#import "WXPayOfflineResult.h"
#import "WXPayQrCodeRequest.h"
#import "WXPayQrCodeResult.h"
#import "WXPayNontaxRequest.h"
#import "WXPayNontaxResult.h"
#import "WXPayInsuranceRequest.h"
#import "WXPayInsuranceResult.h"
#import "WXPaySceneRequest.h"
#import "WXPaySceneResult.h"
