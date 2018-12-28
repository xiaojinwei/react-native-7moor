package com.m7.imkfsdk;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.m7.imkfsdk.utils.PermissionUtils;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.model.entity.CardInfo;
import com.moor.imkf.utils.MoorUtils;

import java.net.URLEncoder;
import com.reactlibrary.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.kf_activity_main);

        /**
         * 第一步：初始化help 文件
         */
        final KfStartHelper helper = new KfStartHelper(MainActivity.this);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                /**
                 * 第二步
                 * 设置参数
                 * 初始化sdk方法，必须先调用该方法进行初始化后才能使用IM相关功能
                 * @param receiverAction 接收新消息广播的action,必须和AndroidManifest中注册的广播中的action一致
                 * @param accessId       接入id（需后台配置获取）
                 * @param userName       用户名
                 * @param userId         用户id
                 */
                String s = "https://wap.boosoo.com.cn/bobishop/goodsdetail?id=10160&mid=36819";
//                CardInfo ci = new CardInfo("http://seopic.699pic.com/photo/40023/0579.jpgz_wh1200.jpg", "我是一个标题当初读书", "我是name当初读书。", "价格 1000-9999", "https://www.baidu.com");
                CardInfo ci = null;
                try {
                    ci = new CardInfo("http://seopic.699pic.com/photo/40023/0579.jpg_wh1200.jpg", "我是一个标题当初读书", "我是name当初读书。", "价格 1000-9999", URLEncoder.encode(s, "utf-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                helper.setCard(ci);
                helper.setSaveMsgType(1);
                helper.initSdkChat("5c10e000-042d-11e9-a401-6f2189532759", "mama", "1");
            }
        });

        /**
         * 文件写入权限 （初始化需要写入文件，点击在线客服按钮之前需打开文件写入权限）
         */
//        if (PermissionUtils.hasAlwaysDeniedPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//            PermissionUtils.requestPermissions(this, 0x11, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionUtils.OnPermissionListener() {
//                @Override
//                public void onPermissionGranted() {
//                }
//
//                @Override
//                public void onPermissionDenied(String[] deniedPermissions) {
//                    Toast.makeText(MainActivity.this, "权限不够", Toast.LENGTH_SHORT).show();
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            finish();
//                        }
//                    }, 2000);
//                }
//            });
//        }


    }
    public void showMsgUnread(){
        if (MoorUtils.isInitForUnread(MainActivity.this)) {
            IMChatManager.getInstance().getMsgUnReadCountFromService(new IMChatManager.HttpUnReadListen() {
                @Override
                public void getUnRead(int acount) {
                    Toast.makeText(MainActivity.this, "未读消息数为：" + acount, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            //未初始化，消息当然为 ：0
            Toast.makeText(MainActivity.this, "还没初始化", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
