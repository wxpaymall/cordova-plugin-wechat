package xu.li.cordova.wechat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.webkit.URLUtil;

import com.tencent.mm.opensdk.modelbiz.ChooseCardFromWXCardPackage;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessView;
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessWebview;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.opensdk.modelmsg.WXEmojiObject;
import com.tencent.mm.opensdk.modelmsg.WXFileObject;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject;
import com.tencent.mm.opensdk.modelmsg.WXMusicObject;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.mm.opensdk.utils.ILog;
import com.tencent.mm.paysdk.PayConfig;
import com.tencent.mm.paysdk.WechatPay;
import com.tencent.mm.paysdk.model.AppPayRequest;
import com.tencent.mm.paysdk.model.SendResult;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class Wechat extends CordovaPlugin {

    public static final String TAG = "Cordova.Plugin.Wechat";

    public static final String PREFS_NAME = "Cordova.Plugin.Wechat";
    public static final String WXAPPID_PROPERTY_KEY = "wechatappid";
    public static final String WECHAT_SIGNATURE_CHECK_PROPERTY_KEY = "WECHAT_SIGNATURE_CHECK";

    public static final String ERROR_WECHAT_NOT_INSTALLED = "未安装微信";
    public static final String ERROR_WECHAT_SIGNATURE_INVALID = "微信安装包签名不可信";
    public static final String ERROR_INVALID_PARAMETERS = "参数格式错误";
    public static final String ERROR_SEND_REQUEST_FAILED = "发送请求失败";
    public static final String ERROR_WECHAT_RESPONSE_COMMON = "普通错误";
    public static final String ERROR_WECHAT_RESPONSE_USER_CANCEL = "用户点击取消并返回";
    public static final String ERROR_WECHAT_RESPONSE_SENT_FAILED = "发送失败";
    public static final String ERROR_WECHAT_RESPONSE_AUTH_DENIED = "授权失败";
    public static final String ERROR_WECHAT_RESPONSE_UNSUPPORT = "微信不支持";
    public static final String ERROR_WECHAT_RESPONSE_UNKNOWN = "未知错误";

    public static final String EXTERNAL_STORAGE_IMAGE_PREFIX = "external://";
    public static final int REQUEST_CODE_ENABLE_PERMISSION = 55433;
    public static final String ANDROID_WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";

    public static final String KEY_ARG_MESSAGE = "message";
    public static final String KEY_ARG_SCENE = "scene";
    public static final String KEY_ARG_TEXT = "text";
    public static final String KEY_ARG_MESSAGE_TITLE = "title";
    public static final String KEY_ARG_MESSAGE_DESCRIPTION = "description";
    public static final String KEY_ARG_MESSAGE_THUMB = "thumb";
    public static final String KEY_ARG_MESSAGE_MEDIA = "media";
    public static final String KEY_ARG_MESSAGE_MEDIA_TYPE = "type";
    public static final String KEY_ARG_MESSAGE_MEDIA_WEBPAGEURL = "webpageUrl";
    public static final String KEY_ARG_MESSAGE_MEDIA_IMAGE = "image";
    public static final String KEY_ARG_MESSAGE_MEDIA_TEXT = "text";
    public static final String KEY_ARG_MESSAGE_MEDIA_MUSICURL = "musicUrl";
    public static final String KEY_ARG_MESSAGE_MEDIA_MUSICDATAURL = "musicDataUrl";
    public static final String KEY_ARG_MESSAGE_MEDIA_VIDEOURL = "videoUrl";
    public static final String KEY_ARG_MESSAGE_MEDIA_FILE = "file";
    public static final String KEY_ARG_MESSAGE_MEDIA_EMOTION = "emotion";
    public static final String KEY_ARG_MESSAGE_MEDIA_EXTINFO = "extInfo";
    public static final String KEY_ARG_MESSAGE_MEDIA_URL = "url";
    public static final String KEY_ARG_MESSAGE_MEDIA_USERNAME = "userName";
    public static final String KEY_ARG_MESSAGE_MEDIA_MINIPROGRAMTYPE = "miniprogramType";
    public static final String KEY_ARG_MESSAGE_MEDIA_MINIPROGRAM = "miniProgram";
    public static final String KEY_ARG_MESSAGE_MEDIA_PATH = "path";
    public static final String KEY_ARG_MESSAGE_MEDIA_WITHSHARETICKET = "withShareTicket";
    public static final String KEY_ARG_MESSAGE_MEDIA_HDIMAGEDATA = "hdImageData";
    public static final String KEY_ARG_MESSAGE_MEDIA_BUSINESSTYPE = "businessType";
    public static final String KEY_ARG_MESSAGE_MEDIA_QUERY = "query";

    /**
     * UAT 要在同一批用例里覆盖两条支付链路，由平台下发的下单参数逐笔指定本次用哪个 SDK 发起。
     *
     * <p>只有显式传 {@code opensdk} 才切到 OpenSDK 老通道，缺失与未知值一律走 PaySDK——商城仍是
     * 商户接入的对照组，不带这个参数时代码路径与没有这个开关时逐字一致。
     *
     * <p>真实 UAT 下单回的是 {@code mmpay.fun://openview?urlb64=...}，H5 再调
     * {@code sendPaymentRequest} 时 JSON 里没有这个键。脚本把开关挂在 scheme 的 query 上，
     * native 从拉起 Intent 读取；{@code datab64} 那条深链若 JSON 里带了这个键，仍然优先生效。
     *
     * <p>商城 UI 上没有、也不应该有任何选择入口：这个键只可能来自 UAT 构造的拉起 URL。
     */
    public static final String KEY_UAT_APPPAY_SDK = "uat_wxpaymall_apppay_sdk";
    public static final String APPPAY_SDK_OPENSDK = "opensdk";

    public static final int TYPE_WECHAT_SHARING_APP = 1;
    public static final int TYPE_WECHAT_SHARING_EMOTION = 2;
    public static final int TYPE_WECHAT_SHARING_FILE = 3;
    public static final int TYPE_WECHAT_SHARING_IMAGE = 4;
    public static final int TYPE_WECHAT_SHARING_MUSIC = 5;
    public static final int TYPE_WECHAT_SHARING_VIDEO = 6;
    public static final int TYPE_WECHAT_SHARING_WEBPAGE = 7;
    public static final int TYPE_WECHAT_SHARING_MINI = 8;

    public static final int SCENE_SESSION = 0;
    public static final int SCENE_TIMELINE = 1;
    public static final int SCENE_FAVORITE = 2;

    public static final int MAX_THUMBNAIL_SIZE = 320;

    protected static CallbackContext currentCallbackContext;
    protected static IWXAPI wxAPI;
    protected static String appId;
    protected static CordovaPreferences wx_preferences;
    private static Wechat instance;
    private static Activity cordovaActivity;
    private static String extinfo;
    /**
     * 本笔支付从拉起 Intent 上读到的 SDK 选择。新的带 data 的 Intent 没带这个 query 就清空，
     * 避免上一笔 {@code opensdk} 污染下一笔缺省支付。没有 data 的 Intent（微信回跳之类）
     * 不动它，免得 H5 调 {@code sendPaymentRequest} 之前被清掉。
     */
    private static String uatAppPaySdkFromLaunch = "";

    @Override
    protected void pluginInitialize() {

        super.pluginInitialize();

        String id = getAppId(preferences);

        // save app id
        saveAppId(cordova.getActivity(), id);

        // 必须先于 initWXAPI：getWxAPI 是静态的，只能从 wx_preferences 取验签开关，而它一旦
        // 用默认值（打开）建出 IWXAPI 就会被静态缓存住——对着非官方签名的微信会 registerApp
        // 失败，连 isWXAppInstalled 都返回 false。
        wx_preferences = preferences;

        // init api
        initWXAPI();

        // 支付走 PaySDK：init 一次，之后每笔调 WechatPay.send。回包落点仍是
        // .wxapi.WXPayEntryActivity，与接 OpenSDK 时一样，见 EntryActivity。
        WechatPay.init(
                cordova.getActivity().getApplicationContext(),
                new PayConfig(id, checkWechatSignature(preferences))
        );

        // 保存引用
        instance = this;
        cordovaActivity = cordova.getActivity();
        captureUatAppPaySdkFromIntent(cordova.getActivity().getIntent());
        if (extinfo != null) {
            transmitLaunchFromWX(extinfo);
        }

        Log.d(TAG, "plugin initialized.");
    }

    /**
     * 商城的 release 包不签商户证书，装到测试机上必须能把校验关掉；商户接入时保持默认打开。
     * OpenSDK 与 PaySDK 两侧都吃这个开关，不然一侧起得来另一侧起不来。
     */
    private static boolean checkWechatSignature(CordovaPreferences prefs) {
        return prefs == null
                || prefs.getBoolean(WECHAT_SIGNATURE_CHECK_PROPERTY_KEY, true);
    }

    protected void initWXAPI() {
        IWXAPI api = getWxAPI(cordova.getActivity());
        if (wx_preferences == null) {
            wx_preferences = preferences;
        }
        if (api != null) {
            api.registerApp(getAppId(preferences));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        cordovaActivity = null;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // LaunchMyApp 不 setIntent，getIntent() 在热启动时仍是上一笔。必须从参数读。
        captureUatAppPaySdkFromIntent(intent);
    }

    /**
     * 从拉起虚拟商城的 Intent URI query 取出 {@link #KEY_UAT_APPPAY_SDK}。
     * 有 data 的 Intent 没带这个键就清空缓存；没有 data 的 Intent 不动缓存。
     */
    private void captureUatAppPaySdkFromIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        Uri data = intent.getData();
        if (data == null) {
            return;
        }
        String fromUri = data.getQueryParameter(KEY_UAT_APPPAY_SDK);
        uatAppPaySdkFromLaunch = fromUri == null ? "" : fromUri;
        if (!uatAppPaySdkFromLaunch.isEmpty()) {
            Log.d(TAG, "captured " + KEY_UAT_APPPAY_SDK + "=" + uatAppPaySdkFromLaunch
                    + " from launch intent");
        }
    }

    /**
     * JSON 里有键用 JSON（datab64 深链还能用）；没有就用拉起 Intent 上挂的 query。
     */
    private String resolveUatAppPaySdk(JSONObject params) {
        String fromParams = params.optString(KEY_UAT_APPPAY_SDK);
        if (fromParams != null && !fromParams.isEmpty()) {
            return fromParams;
        }
        return uatAppPaySdkFromLaunch == null ? "" : uatAppPaySdkFromLaunch;
    }

    /**
     * Get weixin api
     *
     * @param ctx
     * @return
     */
    public static IWXAPI getWxAPI(Context ctx) {
        if (wxAPI == null) {
            String appId = getSavedAppId(ctx);

            if (!appId.isEmpty()) {
                wxAPI = WXAPIFactory.createWXAPI(
                        ctx,
                        appId,
                        checkWechatSignature(wx_preferences)
                );

                // 获取微信客户端版本
                int clientVersion = wxAPI.getWXAppSupportAPI();
                Log.d(TAG, "clientVersion: " + clientVersion);

                // 设置日志监听
                wxAPI.setLogImpl(new ILog() {
                    @Override
                    public void v(String tag, String msg) {
                        Log.v(TAG, "v: " + tag + " " + msg);
                    }
                    @Override
                    public void d(String tag, String msg) {
                        Log.d(TAG, "d: " + tag + " " + msg);
                    }
                    @Override
                    public void i(String tag, String msg) {
                        Log.i(TAG, "i: " + tag + " " + msg);
                    }
                    @Override
                    public void w(String tag, String msg) {
                        Log.w(TAG, "w: " + tag + " " + msg);
                    }
                    @Override
                    public void e(String tag, String msg) {
                        Log.e(TAG, "e: " + tag + " " + msg);
                    }
                });
            }
        }

        return wxAPI;
    }

    public static void transmitLaunchFromWX(String extinfo) {
        if (instance == null) {
            Log.w(Wechat.TAG, "instance is null.");
            Wechat.extinfo = extinfo;
            return;
        }
        Wechat.extinfo = null;

        JSONObject data = getLaunchFromWXObject(extinfo);
        String format = "javascript:cordova.fireDocumentEvent('%s', %s);";
        final String js = String.format(format, "wechat.launchFromWX", data);
        if (cordovaActivity != null) {
            cordovaActivity.runOnUiThread(() -> instance.webView.loadUrl(js));
        } else {
            Log.w(Wechat.TAG, "cordovaActivity is null.");
        }
    }

    private static JSONObject getLaunchFromWXObject(String extinfo) {
        JSONObject data = new JSONObject();
        try {
            data.put("extinfo", extinfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, String.format("%s is called. Callback ID: %s.", action, callbackContext.getCallbackId()));

        if (action.equals("share")) {
            return share(args, callbackContext);
        } else if (action.equals("sendAuthRequest")) {
            return sendAuthRequest(args, callbackContext);
        } else if (action.equals("sendPaymentRequest")) {
            return sendPaymentRequest(args, callbackContext);
        } else if (action.equals("entrustAppSignContract")){
            return entrustAppSignContract(args, callbackContext);
        } else if (action.equals("isWXAppInstalled")) {
            return isInstalled(callbackContext);
        } else if (action.equals("chooseInvoiceFromWX")) {
            return chooseInvoiceFromWX(args, callbackContext);
        } else if (action.equals("openMiniProgram")) {
            return openMiniProgram(args, callbackContext);
        } else if (action.equals("openBusinessView")) {
            return openBusinessView(args, callbackContext);
        }

        return false;
    }

    protected boolean share(CordovaArgs args, final CallbackContext callbackContext)
            throws JSONException {
        final IWXAPI api = getWxAPI(cordova.getActivity());

        // check if installed
        if (!api.isWXAppInstalled()) {
            callbackContext.error(ERROR_WECHAT_NOT_INSTALLED);
            return true;
        }

        // check if # of arguments is correct
        final JSONObject params;
        try {
            params = args.getJSONObject(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        final SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction();

        if (params.has(KEY_ARG_MESSAGE)) {
            //小程序卡片单独构建
            JSONObject message = params.getJSONObject(KEY_ARG_MESSAGE);
            if (message.has(KEY_ARG_MESSAGE_MEDIA)) {
                JSONObject media = message.getJSONObject(KEY_ARG_MESSAGE_MEDIA);
                int type = media.has(KEY_ARG_MESSAGE_MEDIA_TYPE) ? media
                        .getInt(KEY_ARG_MESSAGE_MEDIA_TYPE) : TYPE_WECHAT_SHARING_MINI;
                if (type == TYPE_WECHAT_SHARING_MINI) {
                    req.transaction = buildTransaction(KEY_ARG_MESSAGE_MEDIA_MINIPROGRAM);
                }
            }
        }

        if (params.has(KEY_ARG_SCENE)) {
            switch (params.getInt(KEY_ARG_SCENE)) {
                case SCENE_FAVORITE:
                    req.scene = SendMessageToWX.Req.WXSceneFavorite;
                    break;
                case SCENE_TIMELINE:
                    req.scene = SendMessageToWX.Req.WXSceneTimeline;
                    break;
                case SCENE_SESSION:
                    req.scene = SendMessageToWX.Req.WXSceneSession;
                    break;
                default:
                    req.scene = SendMessageToWX.Req.WXSceneTimeline;
            }
        } else {
            req.scene = SendMessageToWX.Req.WXSceneTimeline;
        }

        // run in background
        cordova.getThreadPool().execute(new Runnable() {

            @Override
            public void run() {
                try {
                    req.message = buildSharingMessage(params);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to build sharing message.", e);

                    // clear callback context
                    currentCallbackContext = null;

                    // send json exception error
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
                }

                if (api.sendReq(req)) {
                    Log.i(TAG, "Message has been sent successfully.");
                } else {
                    Log.i(TAG, "Message has been sent unsuccessfully.");

                    // clear callback context
                    currentCallbackContext = null;

                    // send error
                    callbackContext.error(ERROR_SEND_REQUEST_FAILED);
                }
            }
        });

        // send no result
        sendNoResultPluginResult(callbackContext);

        return true;
    }

    protected boolean sendAuthRequest(CordovaArgs args, CallbackContext callbackContext) {
        final IWXAPI api = getWxAPI(cordova.getActivity());

        final SendAuth.Req req = new SendAuth.Req();
        try {
            req.scope = args.getString(0);
            req.state = args.getString(1);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());

            req.scope = "snsapi_userinfo";
            req.state = "wechat";
        }

        if (api.sendReq(req)) {
            Log.i(TAG, "Auth request has been sent successfully.");

            // send no result
            sendNoResultPluginResult(callbackContext);
        } else {
            Log.i(TAG, "Auth request has been sent unsuccessfully.");

            // send error
            callbackContext.error(ERROR_SEND_REQUEST_FAILED);
        }

        return true;
    }

    protected boolean sendPaymentRequest(CordovaArgs args, CallbackContext callbackContext) {

        // check if # of arguments is correct
        final JSONObject params;
        try {
            params = args.getJSONObject(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        // 只有精确等于 opensdk 才走 OpenSDK，其它（没配 / paysdk / 拼错）一律 PaySDK。
        if (APPPAY_SDK_OPENSDK.equals(resolveUatAppPaySdk(params))) {
            return sendPaymentRequestByOpenSdk(params, callbackContext);
        }

        final AppPayRequest req;

        try {
            // final String appid = params.getString("appid");
            // final String savedAppid = getSavedAppId(cordova.getActivity());
            // if (!savedAppid.equals(appid)) {
            //     this.saveAppId(cordova.getActivity(), appid);
            // }
            req = AppPayRequest.builder()
                    .appId(getAppId(preferences))
                    .partnerId(params.has("mch_id") ? params.getString("mch_id") : params.getString("partnerid"))
                    .prepayId(params.has("prepay_id") ? params.getString("prepay_id") : params.getString("prepayid"))
                    .nonceStr(params.has("nonce") ? params.getString("nonce") : params.getString("noncestr"))
                    .timeStamp(params.getString("timestamp"))
                    .sign(params.getString("sign"))
                    .packageValue(params.has("package") ? params.getString("package") : "Sign=WXPay")
                    .build();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());

            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        // 支付由 PaySDK 承接，其余能力仍走 OpenSDK。同步返回只说明请求发没发出去，结果稍后
        // 由微信回跳 .wxapi.WXPayEntryActivity 送达，见 EntryActivity。
        SendResult sendResult = WechatPay.send(req);

        if (sendResult.isSent()) {
            Log.i(TAG, "Payment request has been sent successfully.");

            // send no result
            sendNoResultPluginResult(callbackContext);
        } else {
            Log.i(TAG, "Payment request has been sent unsuccessfully: " + sendResult);

            // send error
            callbackContext.error(describeSendFailure(sendResult));
        }

        return true;
    }

    /**
     * 用 OpenSDK 发起 App 支付，供 UAT 逐笔覆盖老通道，见 {@link #KEY_UAT_APPPAY_SDK}。
     *
     * <p>参数取法与 PaySDK 分支逐字对齐（两套下单 JSON 的字段名都兼容），appId 同样取
     * {@code getAppId(preferences)}——同 appid、同包名、同签名，不涉及开放平台另行登记。
     *
     * <p>回包落在 {@code .wxapi.WXPayEntryActivity}，与 PaySDK 分支同一个落点，并且<b>同样由
     * PaySDK 认领</b>：两边组出的 wire 逐字相同（{@code _wxapi_payreq_*} 加
     * {@code COMMAND_PAY_BY_WX}），PaySDK 无从分辨也就不做分辨，这条链路走不到 OpenSDK 的
     * {@code onResp}。结果不受影响——errCode 原样透传不做归一，失败文案与 {@code onResp} 同码值
     * 分支逐字一致，JS 侧也只把成功回包写进日志、不读字段。见 EntryActivity。
     *
     * <p>代价是回包日志分不出发起方，UAT 要认某笔走了哪条链路，只能看下面那行发起日志。
     */
    private boolean sendPaymentRequestByOpenSdk(JSONObject params, CallbackContext callbackContext) {
        final PayReq req = new PayReq();

        try {
            req.appId = getAppId(preferences);
            req.partnerId = params.has("mch_id") ? params.getString("mch_id") : params.getString("partnerid");
            req.prepayId = params.has("prepay_id") ? params.getString("prepay_id") : params.getString("prepayid");
            req.nonceStr = params.has("nonce") ? params.getString("nonce") : params.getString("noncestr");
            req.timeStamp = params.getString("timestamp");
            req.sign = params.getString("sign");
            req.packageValue = params.has("package") ? params.getString("package") : "Sign=WXPay";
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());

            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        final IWXAPI api = getWxAPI(cordova.getActivity());

        if (api != null && api.sendReq(req)) {
            Log.i(TAG, "Payment request has been sent successfully by OpenSDK.");

            // send no result
            sendNoResultPluginResult(callbackContext);
        } else {
            Log.i(TAG, "Payment request has been sent unsuccessfully by OpenSDK.");

            // send error
            callbackContext.error(ERROR_SEND_REQUEST_FAILED);
        }

        return true;
    }

    /**
     * 把拉起失败翻成给 JS 的错误文案。
     *
     * <p>{@code getMessage()} 只进日志：它是排障用的可读描述，措辞不构成兼容承诺。
     */
    private static String describeSendFailure(SendResult sendResult) {
        switch (sendResult.getCode()) {
            case SendResult.CODE_WECHAT_NOT_INSTALLED:
                return ERROR_WECHAT_NOT_INSTALLED;
            case SendResult.CODE_WECHAT_SIGNATURE_INVALID:
                return ERROR_WECHAT_SIGNATURE_INVALID;
            case SendResult.CODE_INVALID_PARAM:
                return ERROR_INVALID_PARAMETERS;
            default:
                return ERROR_SEND_REQUEST_FAILED;
        }
    }

    protected boolean entrustAppSignContract(CordovaArgs args, CallbackContext callbackContext){
        // check if # of arguments is correct
        final JSONObject params;
        try {
            params = args.getJSONObject(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }    
        WXOpenBusinessWebview.Req req = new WXOpenBusinessWebview.Req();
        try {
            String preEntrustwebId = params.has("pre_entrustweb_id") ? params.getString("pre_entrustweb_id") : params.getString("pre_entrustweb_id");
            req.businessType = 12; // 固定值
            HashMap<String, String> queryInfo = new HashMap<>();
            queryInfo.put("pre_entrustweb_id", preEntrustwebId);
            req.queryInfo = queryInfo;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());

            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        final IWXAPI api = getWxAPI(cordova.getActivity());

        if (api.sendReq(req)) {
            Log.i(TAG, "Payment request has been sent successfully.");

            // send no result
            sendNoResultPluginResult(callbackContext);
        } else {
            Log.i(TAG, "Payment request has been sent unsuccessfully.");

            // send error
            callbackContext.error(ERROR_SEND_REQUEST_FAILED);
        }

        return true;
    }

    protected boolean chooseInvoiceFromWX(CordovaArgs args, CallbackContext callbackContext) {

        final IWXAPI api = getWxAPI(cordova.getActivity());

        // check if # of arguments is correct
        final JSONObject params;
        try {
            params = args.getJSONObject(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        ChooseCardFromWXCardPackage.Req req = new ChooseCardFromWXCardPackage.Req();

        try {
            req.appId = getAppId(preferences);
            req.cardType = "INVOICE";
            req.signType = params.getString("signType");
            req.cardSign = params.getString("cardSign");
            req.nonceStr = params.getString("nonceStr");
            req.timeStamp = params.getString("timeStamp");
            req.canMultiSelect = "1";
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());

            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        if (api.sendReq(req)) {
            Log.i(TAG, "Invoice request has been sent successfully.");

            // send no result
            sendNoResultPluginResult(callbackContext);
        } else {
            Log.i(TAG, "Invoice request has been sent unsuccessfully.");

            // send error
            callbackContext.error(ERROR_SEND_REQUEST_FAILED);
        }

        return true;
    }

    protected boolean isInstalled(CallbackContext callbackContext) {
        final IWXAPI api = getWxAPI(cordova.getActivity());

        if (!api.isWXAppInstalled()) {
            callbackContext.success(0);
        } else {
            callbackContext.success(1);
        }

        return true;
    }


    protected WXMediaMessage buildSharingMessage(JSONObject params)
            throws JSONException {
        Log.d(TAG, "Start building message.");

        // media parameters
        WXMediaMessage.IMediaObject mediaObject = null;
        WXMediaMessage wxMediaMessage = new WXMediaMessage();

        if (params.has(KEY_ARG_TEXT)) {
            WXTextObject textObject = new WXTextObject();
            textObject.text = params.getString(KEY_ARG_TEXT);
            mediaObject = textObject;
            wxMediaMessage.description = textObject.text;
        } else {
            JSONObject message = params.getJSONObject(KEY_ARG_MESSAGE);
            JSONObject media = message.getJSONObject(KEY_ARG_MESSAGE_MEDIA);

            wxMediaMessage.title = message.getString(KEY_ARG_MESSAGE_TITLE);
            wxMediaMessage.description = message.getString(KEY_ARG_MESSAGE_DESCRIPTION);

            // thumbnail
            Bitmap thumbnail = getThumbnail(message, KEY_ARG_MESSAGE_THUMB);
            if (thumbnail != null) {
                wxMediaMessage.setThumbImage(thumbnail);
                thumbnail.recycle();
            }

            // check types
            int type = media.has(KEY_ARG_MESSAGE_MEDIA_TYPE) ? media
                    .getInt(KEY_ARG_MESSAGE_MEDIA_TYPE) : TYPE_WECHAT_SHARING_WEBPAGE;

            switch (type) {
                case TYPE_WECHAT_SHARING_APP:
                    WXAppExtendObject appObject = new WXAppExtendObject();
                    appObject.extInfo = media.getString(KEY_ARG_MESSAGE_MEDIA_EXTINFO);
                    appObject.filePath = media.getString(KEY_ARG_MESSAGE_MEDIA_URL);
                    mediaObject = appObject;
                    break;

                case TYPE_WECHAT_SHARING_EMOTION:
                    WXEmojiObject emoObject = new WXEmojiObject();
                    InputStream emoji = getFileInputStream(media.getString(KEY_ARG_MESSAGE_MEDIA_EMOTION));
                    if (emoji != null) {
                        try {
                            emoObject.emojiData = Util.readBytes(emoji);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    mediaObject = emoObject;
                    break;

                case TYPE_WECHAT_SHARING_FILE:
                    WXFileObject fileObject = new WXFileObject();
                    InputStream file = getFileInputStream(media.getString(KEY_ARG_MESSAGE_MEDIA_FILE));
                    if (file != null) {
                        try {
                            fileObject.fileData = Util.readBytes(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    mediaObject = fileObject;
                    break;

                case TYPE_WECHAT_SHARING_IMAGE:
                    Bitmap image = getBitmap(message.getJSONObject(KEY_ARG_MESSAGE_MEDIA), KEY_ARG_MESSAGE_MEDIA_IMAGE, 0);
                    // give some tips to user           
                    if (image != null) {
                        mediaObject = new WXImageObject(image);
                        image.recycle();
                    }
                    break;

                case TYPE_WECHAT_SHARING_MUSIC:
                    WXMusicObject musicObject = new WXMusicObject();
                    musicObject.musicUrl = media.getString(KEY_ARG_MESSAGE_MEDIA_MUSICURL);
                    musicObject.musicDataUrl = media.getString(KEY_ARG_MESSAGE_MEDIA_MUSICDATAURL);
                    mediaObject = musicObject;
                    break;

                case TYPE_WECHAT_SHARING_VIDEO:
                    WXVideoObject videoObject = new WXVideoObject();
                    videoObject.videoUrl = media.getString(KEY_ARG_MESSAGE_MEDIA_VIDEOURL);
                    mediaObject = videoObject;
                    break;

                case TYPE_WECHAT_SHARING_MINI:
                    WXMiniProgramObject miniProgramObj = new WXMiniProgramObject();
                    try {
                        miniProgramObj.webpageUrl = media.getString(KEY_ARG_MESSAGE_MEDIA_WEBPAGEURL); // 兼容低版本的网页链接
                        miniProgramObj.miniprogramType = media.getInt(KEY_ARG_MESSAGE_MEDIA_MINIPROGRAMTYPE);// 正式版:0，测试版:1，体验版:2
                        miniProgramObj.userName = media.getString(KEY_ARG_MESSAGE_MEDIA_USERNAME);     // 小程序原始id
                        miniProgramObj.path = media.getString(KEY_ARG_MESSAGE_MEDIA_PATH);            //小程序页面路径
                        miniProgramObj.withShareTicket = media.getBoolean(KEY_ARG_MESSAGE_MEDIA_WITHSHARETICKET); // 是否使用带shareTicket的分享
                        wxMediaMessage = new WXMediaMessage(miniProgramObj);
                        wxMediaMessage.title = message.getString(KEY_ARG_MESSAGE_TITLE);                    // 小程序消息title
                        wxMediaMessage.description = message.getString(KEY_ARG_MESSAGE_DESCRIPTION);               // 小程序消息desc
                        wxMediaMessage.thumbData = Util.readBytes(getFileInputStream(media.getString(KEY_ARG_MESSAGE_MEDIA_HDIMAGEDATA))); // 小程序消息封面图片，小于128k
                        return wxMediaMessage;
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                    break;

                case TYPE_WECHAT_SHARING_WEBPAGE:
                default:
                    mediaObject = new WXWebpageObject(media.getString(KEY_ARG_MESSAGE_MEDIA_WEBPAGEURL));
            }
        }

        wxMediaMessage.mediaObject = mediaObject;

        return wxMediaMessage;
    }

    private String buildTransaction() {
        return String.valueOf(System.currentTimeMillis());
    }

    private String buildTransaction(final String type) {
        return type + System.currentTimeMillis();
    }

    protected Bitmap getThumbnail(JSONObject message, String key) {
        return getBitmap(message, key, MAX_THUMBNAIL_SIZE);
    }

    protected Bitmap getBitmap(JSONObject message, String key, int maxSize) {
        Bitmap bmp = null;
        String url = null;

        try {
            if (!message.has(key)) {
                return null;
            }

            url = message.getString(key);

            // get input stream
            InputStream inputStream = getFileInputStream(url);
            if (inputStream == null) {
                return null;
            }

            // decode it
            // @TODO make sure the image is not too big, or it will cause out of memory
            BitmapFactory.Options options = new BitmapFactory.Options();
            bmp = BitmapFactory.decodeStream(inputStream, null, options);

            // scale
            if (maxSize > 0 && (options.outWidth > maxSize || options.outHeight > maxSize)) {

                Log.d(TAG, String.format("Bitmap was decoded, dimension: %d x %d, max allowed size: %d.",
                        options.outWidth, options.outHeight, maxSize));

                int width = 0;
                int height = 0;

                if (options.outWidth > options.outHeight) {
                    width = maxSize;
                    height = width * options.outHeight / options.outWidth;
                } else {
                    height = maxSize;
                    width = height * options.outWidth / options.outHeight;
                }

                Bitmap scaled = Bitmap.createScaledBitmap(bmp, width, height, true);
                bmp.recycle();

                int length = scaled.getRowBytes() * scaled.getHeight();

                if (length > (maxSize / 10) * 1024) {
                    scaled = compressImage(scaled, (maxSize / 10));
                }

                bmp = scaled;
            }

            inputStream.close();

        } catch (JSONException e) {
            bmp = null;
            e.printStackTrace();
        } catch (IOException e) {
            bmp = null;
            e.printStackTrace();
        }

        return bmp;
    }


    /**
     * compress bitmap by quility
     */
    protected Bitmap compressImage(Bitmap image, Integer maxSize) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 90;

        while (baos.toByteArray().length / 1024 > maxSize) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    /**
     * Get input stream from a url
     */
    protected InputStream getFileInputStream(String url) {
        InputStream inputStream = null;
        try {

            if (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) {

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    if (!cordova.hasPermission(ANDROID_WRITE_EXTERNAL_STORAGE)) {
                        cordova.requestPermission(this, REQUEST_CODE_ENABLE_PERMISSION, ANDROID_WRITE_EXTERNAL_STORAGE);
                    }
                }

                File file = Util.downloadAndCacheFile(webView.getContext(), url);

                if (file == null) {
                    Log.d(TAG, String.format("File could not be downloaded from %s.", url));
                    return null;
                }

                // url = file.getAbsolutePath();
                inputStream = new FileInputStream(file);

                Log.d(TAG, String.format("File was downloaded and cached to %s.", file.getAbsolutePath()));

            } else if (url.startsWith("data:image")) {  // base64 image

                String imageDataBytes = url.substring(url.indexOf(",") + 1);
                byte imageBytes[] = Base64.decode(imageDataBytes.getBytes(), Base64.DEFAULT);
                inputStream = new ByteArrayInputStream(imageBytes);

                Log.d(TAG, "Image is in base64 format.");

            } else if (url.startsWith(EXTERNAL_STORAGE_IMAGE_PREFIX)) { // external path

                url = Environment.getExternalStorageDirectory().getAbsolutePath() + url.substring(EXTERNAL_STORAGE_IMAGE_PREFIX.length());
                inputStream = new FileInputStream(url);

                Log.d(TAG, String.format("File is located on external storage at %s.", url));

            } else if (!url.startsWith("/")) { // relative path

                inputStream = cordova.getActivity().getApplicationContext().getAssets().open(url);

                Log.d(TAG, String.format("File is located in assets folder at %s.", url));

            } else {

                inputStream = new FileInputStream(url);

                Log.d(TAG, String.format("File is located at %s.", url));

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return inputStream;
    }

    public static String getAppId(CordovaPreferences f_preferences) {
        if (appId == null) {
            if (f_preferences != null) {
                appId = f_preferences.getString(WXAPPID_PROPERTY_KEY, "");
            } else if (wx_preferences != null) {
                appId = wx_preferences.getString(WXAPPID_PROPERTY_KEY, "");
            }
        }

        return appId;
    }

    /**
     * Get saved app id
     *
     * @param ctx
     * @return
     */
    public static String getSavedAppId(Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(WXAPPID_PROPERTY_KEY, "");
    }

    /**
     * Save app id into SharedPreferences
     *
     * @param ctx
     * @param id
     */
    public static void saveAppId(Context ctx, String id) {
        if (id != null && id.isEmpty()) {
            return;
        }

        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(WXAPPID_PROPERTY_KEY, id);
        editor.commit();
    }

    public static CallbackContext getCurrentCallbackContext() {
        return currentCallbackContext;
    }

    private void sendNoResultPluginResult(CallbackContext callbackContext) {
        // save current callback context
        currentCallbackContext = callbackContext;

        // send no result and keep callback
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    protected boolean openMiniProgram(CordovaArgs args, CallbackContext callbackContext) {
        currentCallbackContext = callbackContext;
        String appId = getAppId(preferences);
        ; // 填应用AppId
        IWXAPI api = WXAPIFactory.createWXAPI(cordova.getActivity(), appId);

        final JSONObject params;
        try {
            params = args.getJSONObject(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
        try {
            req.userName = params.getString(KEY_ARG_MESSAGE_MEDIA_USERNAME); // 填小程序原始id
            req.path = params.getString(KEY_ARG_MESSAGE_MEDIA_PATH);                  //拉起小程序页面的可带参路径，不填默认拉起小程序首页
            req.miniprogramType = params.getInt(KEY_ARG_MESSAGE_MEDIA_MINIPROGRAMTYPE);// 可选打开 开发版，体验版和正式版
            api.sendReq(req);
        } catch (Exception e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            Log.e(TAG, e.getMessage());
        }
        return true;
    }

    protected boolean openBusinessView(CordovaArgs args, CallbackContext callbackContext) {
        currentCallbackContext = callbackContext;
        // 使用已注册的全局wxAPI实例，而不是创建新实例
        IWXAPI api = getWxAPI(cordova.getActivity());

        final JSONObject params;
        try {
            params = args.getJSONObject(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        // 检查businessType必须传入
        if (!params.has(KEY_ARG_MESSAGE_MEDIA_BUSINESSTYPE)) {
            callbackContext.error("缺少必要参数：businessType");
            return true;
        }

        Log.d(TAG, "openBusinessView params: " + params.toString());

        WXOpenBusinessView.Req req = new WXOpenBusinessView.Req();
        try {
            req.businessType = params.getString(KEY_ARG_MESSAGE_MEDIA_BUSINESSTYPE); // 申请到bussinessType
            
            if (params.has(KEY_ARG_MESSAGE_MEDIA_EXTINFO)) {
                req.extInfo = params.getString(KEY_ARG_MESSAGE_MEDIA_EXTINFO); // 拉起的可选参数
            }
            
            if (params.has(KEY_ARG_MESSAGE_MEDIA_QUERY)) {
                req.query = params.getString(KEY_ARG_MESSAGE_MEDIA_QUERY); // 拉起query参数
            }
            
            Log.d(TAG, "openBusinessView request - businessType: " + req.businessType + 
                      ", extInfo: " + req.extInfo + ", query: " + req.query);
            
            if (api.sendReq(req)) {
                Log.d(TAG, "openBusinessView request sent successfully");
                // 调用sendNoResultPluginResult保持回调等待微信响应
                sendNoResultPluginResult(callbackContext);
            } else {
                Log.e(TAG, "openBusinessView request failed");
                callbackContext.error(ERROR_SEND_REQUEST_FAILED);
            }
        } catch (Exception e) {
            Log.e(TAG, "openBusinessView error: " + e.getMessage(), e);
            callbackContext.error(ERROR_INVALID_PARAMETERS);
        }
        return true;
    }

}
