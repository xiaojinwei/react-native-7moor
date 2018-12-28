package com.m7.imkfsdk.chat.video;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.reactlibrary.R;
import com.moor.imkf.AcceptVideoListener;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.RefuseVideoListener;

/**
 * Created by longwei on 2017/6/12.
 */

public class InComingVideoActivity extends Activity{

    private String username;
    private String roomname;

    private ImageView iv_accept_call, iv_end_call;
    private VideoPCCancelReceiver videoPCCancelReceiver;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kf_activity_chat_incoming_video);

        username = getIntent().getStringExtra(IMChatManager.CONSTANT_VIDEO_USERNAME);
        roomname = getIntent().getStringExtra(IMChatManager.CONSTANT_VIDEO_ROOMNAME);

        videoPCCancelReceiver = new VideoPCCancelReceiver();
        IntentFilter intentFilter = new IntentFilter(IMChatManager.VIDEO_PC_CANCEL_ACTION);
        registerReceiver(videoPCCancelReceiver, intentFilter);

        iv_accept_call = (ImageView) findViewById(R.id.iv_accept_call);
        iv_end_call = (ImageView) findViewById(R.id.iv_end_call);

        if(username != null && !"".equals(username) &&
                roomname != null && !"".equals(roomname)) {

            iv_accept_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    accept();
                }
            });

            iv_end_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    refuse();
                }
            });
            //播放提示音
            startAlarm();
        }

    }

    private void startAlarm() {
        try {
            mMediaPlayer = MediaPlayer.create(this, getSystemDefultRingtoneUri());
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Uri getSystemDefultRingtoneUri() {
        return RingtoneManager.getActualDefaultRingtoneUri(this,
                RingtoneManager.TYPE_RINGTONE);
    }

    private void accept() {

        IMChatManager.getInstance().acceptVideo(new AcceptVideoListener() {
            @Override
            public void onSuccess() {
                Intent intent = new Intent(InComingVideoActivity.this, PeerVideoActivity.class);
                intent.putExtra(IMChatManager.CONSTANT_VIDEO_USERNAME, username);
                intent.putExtra(IMChatManager.CONSTANT_VIDEO_ROOMNAME, roomname);
                startActivity(intent);

                finish();
            }

            @Override
            public void onFailed() {

            }
        });

    }

    private void refuse() {
        IMChatManager.getInstance().refuseVideo(new RefuseVideoListener() {
            @Override
            public void onSuccess() {
                finish();
            }

            @Override
            public void onFailed() {
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            if(mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }
        }catch (Exception e){}
        unregisterReceiver(videoPCCancelReceiver);
    }

    class VideoPCCancelReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(InComingVideoActivity.this, "对方取消了邀请", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
