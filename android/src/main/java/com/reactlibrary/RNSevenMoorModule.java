
package com.reactlibrary;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.m7.imkfsdk.KfStartHelper;
import com.m7.imkfsdk.MainActivity;
import com.m7.imkfsdk.utils.PermissionUtils;

public class RNSevenMoorModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public RNSevenMoorModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNSevenMoor";
    }

    @ReactMethod
    public void registerSDK(String key, String userName, String userId) {
//        Toast.makeText(reactContext.getCurrentActivity(),"key:"+key+"--userName:"+userName+"--userId:"+userId, Toast.LENGTH_SHORT).show();
        final KfStartHelper helper = new KfStartHelper(reactContext.getCurrentActivity());
        helper.setSaveMsgType(1);
        helper.initSdkChat(key, userName, userId);
        /**
         * 文件写入权限 （初始化需要写入文件，点击在线客服按钮之前需打开文件写入权限）
         */
        if (PermissionUtils.hasAlwaysDeniedPermission(reactContext.getCurrentActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PermissionUtils.requestPermissions(reactContext.getCurrentActivity(), 0x11, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionUtils.OnPermissionListener() {
                @Override
                public void onPermissionGranted() {
                }

                @Override
                public void onPermissionDenied(String[] deniedPermissions) {
                    Toast.makeText(reactContext.getCurrentActivity(), "权限不够", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            reactContext.getCurrentActivity().finish();
                        }
                    }, 2000);
                }
            });
        }
    }
}