
package com.reactlibrary;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.m7.imkfsdk.KfStartHelper;
import com.m7.imkfsdk.MainActivity;
import com.m7.imkfsdk.utils.PermissionUtils;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.utils.MoorUtils;

import java.lang.reflect.Method;

public class RNSevenMoorModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    KfStartHelper helper;

    public RNSevenMoorModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    private Activity getReactContextCurrentActivity(ReactApplicationContext reactContext){
        try {
            Class<? extends ReactApplicationContext> aClass = reactContext.getClass();
            Method getCurrentActivity = aClass.getMethod("getCurrentActivity");
            Object invoke = getCurrentActivity.invoke(reactContext);
            return (Activity)invoke;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getName() {
        return "RNSevenMoor";
    }

    @ReactMethod
    public void registerSDK(String key, String userName, String userId) {
//        Toast.makeText(reactContext.getCurrentActivity(),"key:"+key+"--userName:"+userName+"--userId:"+userId, Toast.LENGTH_SHORT).show();
        IMChatManager.getInstance().quitSDk();
        if (helper == null) {
            helper = KfStartHelper.getInstance(getReactContextCurrentActivity(reactContext));
        }

        /**
         * 文件写入权限 （初始化需要写入文件，点击在线客服按钮之前需打开文件写入权限）
         * 读取设备 ID 权限 （初始化需要获取用户的设备 ID）
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionUtils.hasAlwaysDeniedPermission(getReactContextCurrentActivity(reactContext), Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                PermissionUtils.requestPermissions(getReactContextCurrentActivity(reactContext), 0x11, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE}, new PermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                    }

                    @Override
                    public void onPermissionDenied(String[] deniedPermissions) {
                        Toast.makeText(getReactContextCurrentActivity(reactContext), "权限不够", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getReactContextCurrentActivity(reactContext).finish();
                            }
                        }, 2000);
                    }
                });
            }
        }

//        helper.setSaveMsgType(1);
        helper.initSdkChat(key, userName, userId, true);
        Log.d("KfStartHelper", "key:" + key + "--userName:" + userName + "---userId:" + userId);

//        reactContext.getCurrentActivity().startActivity(new Intent(reactContext.getCurrentActivity(),MainActivity.class));
    }

    @ReactMethod
    public void sdkGetUnReadMessage(String key, String userName, String userId, final Promise promise) {
        try {
            if (MoorUtils.isInitForUnread(getReactContextCurrentActivity(reactContext))) {
                IMChatManager.getInstance().getMsgUnReadCountFromService(new IMChatManager.HttpUnReadListen() {
                    @Override
                    public void getUnRead(int acount) {
//                        Toast.makeText(reactContext.getCurrentActivity(), "未读消息数为：" + acount, Toast.LENGTH_SHORT).show();

                        promise.resolve(acount);
                    }
                });
            }
//            Log.d("KfStartHelper Message", "key:" + key + "--userName:" + userName + "---userId:" + userId + "--unReadCount：" + unReadCount);

//            promise.resolve(unReadCount);

        } catch (Exception e) {
            promise.reject("1", e.getMessage());
        }
    }
}