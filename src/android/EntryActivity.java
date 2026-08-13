package __PACKAGE_NAME__.wxapi;

import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessView;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.modelbiz.ChooseCardFromWXCardPackage;
import com.tencent.mm.opensdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.paysdk.adapter.opensdk.WechatPayCompat;
import __PACKAGE_NAME__.MainActivity;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import xu.li.cordova.wechat.Wechat;

/**
 * Created by xu.li<AthenaLightenedMyPath@gmail.com> on 9/1/15.
 */
public class EntryActivity extends Activity implements IWXAPIEventHandler {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        route(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        route(intent);
    }

    /**
     * 回包先给 PaySDK 认一次，不是它的再交给 OpenSDK。
     *
     * <p>支付走 PaySDK 之后，微信按类名把结果回跳到本类（商户侧的
     * {@code .wxapi.WXPayEntryActivity}），与接 OpenSDK 时同一个落点；分享、授权、小程序、
     * 业务视图的回包只有 OpenSDK 认得，所以两条链都得留着。
     *
     * <p>不能两条都走：微信只回跳一次，都处理会让同一笔结果兑现两遍。先问 PaySDK 是安全的，
     * 它认不出来时返回 false 且不回调。
     *
     * <p>回调仍交给本类的 {@link #onResp}，支付结果因此与其它命令共用同一套投递：拿不到挂起的
     * JS 回调就拉起主界面（冷启动回包），成功转 JSON，失败按错误码给文案，最后 finish。
     */
    private void route(Intent intent) {
        if (WechatPayCompat.handleIntent(this, intent, this)) {
            return;
        }

        IWXAPI api = Wechat.getWxAPI(this);

        if (api == null) {
            startMainActivity();
        } else {
            api.handleIntent(intent, this);
        }
    }

    @Override
    public void onResp(BaseResp resp) {
        Log.d(Wechat.TAG, "onResp - errCode:" + resp.errCode + ", errStr:" + resp.errStr + ", type:" + resp.getType());

        CallbackContext ctx = Wechat.getCurrentCallbackContext();

        if (ctx == null) {
            startMainActivity();
            return;
        }

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                switch (resp.getType()) {
                    case ConstantsAPI.COMMAND_SENDAUTH:
                        Log.d(Wechat.TAG, "COMMAND_SENDAUTH;");
                        auth(resp);
                        break;
                    case ConstantsAPI.COMMAND_CHOOSE_CARD_FROM_EX_CARD_PACKAGE:
                        Log.d(Wechat.TAG, "COMMAND_CHOOSE_CARD_FROM_EX_CARD_PACKAGE;");
                        plunckInvoiceData(resp);
                        break;
                    case ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM:
                        Log.d(Wechat.TAG, "miniprogram back;");
                        WXLaunchMiniProgram.Resp miniProResp = (WXLaunchMiniProgram.Resp) resp;
                        launchMiniProResp(miniProResp);
                        break;
                    case ConstantsAPI.COMMAND_OPEN_BUSINESS_VIEW:
                        Log.d(Wechat.TAG, "business view back;");
                        WXOpenBusinessView.Resp businessResp = (WXOpenBusinessView.Resp) resp;
                        launchBusinessViewResp(businessResp);
                        break;
                    case ConstantsAPI.COMMAND_PAY_BY_WX:
                    default:
                        Bundle bundle = new Bundle();
                        resp.toBundle(bundle);

                        JSONObject json = new JSONObject();
                        Set<String> keys = bundle.keySet();
                        for (String key : keys) {
                            try {
                                json.put(key, JSONObject.wrap(bundle.get(key)));
                            } catch(JSONException e) {
                                Log.d(Wechat.TAG, "put json error: " + e.getMessage());
                            }
                        }
                        Log.d(Wechat.TAG, "resp: " + json.toString());
                        ctx.success(json);
                        break;
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                // ctx.error(Wechat.ERROR_WECHAT_RESPONSE_USER_CANCEL);
                ctx.error(String.format("用户点击取消并返回, errCode: %d, errStr: %s", resp.errCode, resp.errStr));
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                // ctx.error(Wechat.ERROR_WECHAT_RESPONSE_AUTH_DENIED);
                ctx.error(String.format("授权失败, errCode: %d, errStr: %s", resp.errCode, resp.errStr));
                break;
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                // ctx.error(Wechat.ERROR_WECHAT_RESPONSE_SENT_FAILED);
                ctx.error(String.format("发送失败, errCode: %d, errStr: %s", resp.errCode, resp.errStr));
                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                // ctx.error(Wechat.ERROR_WECHAT_RESPONSE_UNSUPPORT);
                ctx.error(String.format("微信不支持, errCode: %d, errStr: %s", resp.errCode, resp.errStr));
                break;
            case BaseResp.ErrCode.ERR_COMM:
                // ctx.error(Wechat.ERROR_WECHAT_RESPONSE_COMMON);
                ctx.error(String.format("普通错误, errCode: %d, errStr: %s", resp.errCode, resp.errStr));
                break;
            default:
                ctx.error(String.format("errCode: %d, errStr: %s", resp.errCode, resp.errStr));
                break;
        }

        finish();
    }

    @Override
    public void onReq(BaseReq req) {
        // 获取开放标签传递的extinfo数据逻辑
        if (req.getType() == ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX && req instanceof ShowMessageFromWX.Req) {
            ShowMessageFromWX.Req showReq = (ShowMessageFromWX.Req) req;
            WXMediaMessage mediaMsg = showReq.message;
            String extInfo = mediaMsg.messageExt;

            Log.i(Wechat.TAG, "Launch from Wechat extInfo: " + extInfo);
            startMainActivity();

            Wechat.transmitLaunchFromWX(extInfo);
        }

        finish();
    }

    protected void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setPackage(getApplicationContext().getPackageName());
        getApplicationContext().startActivity(intent);
    }

    protected void launchMiniProResp(WXLaunchMiniProgram.Resp launchMiniProResp) {
        CallbackContext ctx = Wechat.getCurrentCallbackContext();
        String extraData = launchMiniProResp.extMsg; //对应小程序组件 <button open-type="launchApp"> 中的 app-parameter 属性
        JSONObject response = new JSONObject();
        try {
            response.put("extMsg", extraData);
        } catch (Exception e) {
            Log.e(Wechat.TAG, e.getMessage());
        }
        ctx.success(response);
    }

    protected void launchBusinessViewResp(WXOpenBusinessView.Resp businessResp) {
        CallbackContext ctx = Wechat.getCurrentCallbackContext();
        Log.d(Wechat.TAG, businessResp.toString());
        JSONObject response = new JSONObject();
        try {
            response.put("businessType", businessResp.businessType != null ? businessResp.businessType : "");
            response.put("extMsg", businessResp.extMsg != null ? businessResp.extMsg : "");
        } catch (Exception e) {
            Log.e(Wechat.TAG, e.getMessage());
        }
        ctx.success(response);
    }

    protected void auth(BaseResp resp) {
        SendAuth.Resp res = ((SendAuth.Resp) resp);

        Log.d(Wechat.TAG, res.toString());

        // get current callback context
        CallbackContext ctx = Wechat.getCurrentCallbackContext();

        if (ctx == null) {
            return;
        }

        JSONObject response = new JSONObject();
        try {
            response.put("code", res.code);
            response.put("state", res.state);
            response.put("country", res.country);
            response.put("lang", res.lang);
        } catch (JSONException e) {
            Log.e(Wechat.TAG, e.getMessage());
        }

        ctx.success(response);
    }

    protected void plunckInvoiceData(BaseResp resp) {

        CallbackContext ctx = Wechat.getCurrentCallbackContext();
        ChooseCardFromWXCardPackage.Resp resp1 = (ChooseCardFromWXCardPackage.Resp) resp;
        JSONObject response = new JSONObject();

        try {
            JSONArray resp2 = new JSONArray(resp1.cardItemList);
            response.put("data", resp2);
        } catch (JSONException e) {
            Log.e(Wechat.TAG, e.getMessage());
        }

        ctx.success(response);
    }
}