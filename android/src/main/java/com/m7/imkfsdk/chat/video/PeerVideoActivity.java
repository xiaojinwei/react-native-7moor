package com.m7.imkfsdk.chat.video;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.reactlibrary.R;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.requesturl.RequestUrl;
import com.moor.videosdk.api.M7RoomAPI;
import com.moor.videosdk.api.M7RoomError;
import com.moor.videosdk.api.M7RoomListener;
import com.moor.videosdk.api.M7RoomNotification;
import com.moor.videosdk.api.M7RoomResponse;
import com.moor.videosdk.utils.LooperExecutor;
import com.moor.videosdk.webrtcpeer.M7MediaConfiguration;
import com.moor.videosdk.webrtcpeer.M7PeerConnection;
import com.moor.videosdk.webrtcpeer.M7WebRTCPeer;

import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for receiving the video stream of a peer
 *
 */
public class PeerVideoActivity extends Activity implements M7WebRTCPeer.Observer, M7RoomListener {
    private static final String TAG = "PeerVideoActivity";

    private int ConstantsId = 0;
    private LooperExecutor executor;
    private static M7RoomAPI m7RoomAPI;
    private int roomId = 0;
    public static String username, roomname;

    private M7MediaConfiguration peerConnectionParameters;
    private M7WebRTCPeer m7WebRTCPeer;

    private SessionDescription localSdp;
    private SessionDescription remoteSdp;

    private SurfaceViewRenderer masterView;
    private SurfaceViewRenderer localView;

    private int publishVideoRequestId;
    private int sendIceCandidateRequestId;

    private String calluser;
    private boolean backPressed = false;
    private Thread backPressedThread = null;

    private RelativeLayout chat_video_rl;
    private TextView chat_video_tv_title;
    private Chronometer chat_video_chr;
    private boolean flag = true;
    private CallState callState;

    private enum CallState{
        IDLE, PUBLISHING, PUBLISHED, WAITING_REMOTE_USER, RECEIVING_REMOTE_USER
    }

    private VideoRefuseReceiver videoRefuseReceiver;
    private MediaPlayer mMediaPlayer;
    private Map<Integer, String> videoRequestUserMapping;
    private Handler mHandler = null;
    public Map<String, Boolean> userPublishList = new HashMap<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callState = CallState.IDLE;
        setContentView(R.layout.kf_activity_chat_video);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey(IMChatManager.CONSTANT_VIDEO_USERNAME)) {
            Toast.makeText(this, "参数错误",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        this.username = extras.getString(IMChatManager.CONSTANT_VIDEO_USERNAME, "");
        this.roomname = extras.getString(IMChatManager.CONSTANT_VIDEO_ROOMNAME, "");
        System.out.println("username is:"+username);
        System.out.println("room is:"+roomname);
        mHandler = new Handler();
        videoRequestUserMapping = new HashMap<>();
        masterView = (SurfaceViewRenderer) findViewById(R.id.gl_surface);
        localView = (SurfaceViewRenderer) findViewById(R.id.gl_surface_local);
        chat_video_rl = (RelativeLayout) findViewById(R.id.chat_video_rl);
        chat_video_chr = (Chronometer) findViewById(R.id.chat_video_chr);
        chat_video_tv_title = (TextView) findViewById(R.id.chat_video_tv_title);

        videoRefuseReceiver = new VideoRefuseReceiver();
        IntentFilter intentFilter = new IntentFilter(IMChatManager.VIDEO_REFUSE_ACTION);
        registerReceiver(videoRefuseReceiver, intentFilter);

        EglBase rootEglBase = EglBase.create();
        masterView.init(rootEglBase.getEglBaseContext(), null);
        masterView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        localView.init(rootEglBase.getEglBaseContext(), null);
        localView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

        M7MediaConfiguration.M7VideoFormat receiverVideoFormat = new M7MediaConfiguration.M7VideoFormat(352, 288, PixelFormat.RGB_888, 14);
        peerConnectionParameters = new M7MediaConfiguration(   M7MediaConfiguration.M7RendererType.OPENGLES,
                M7MediaConfiguration.M7AudioCodec.OPUS, 0,
                M7MediaConfiguration.M7VideoCodec.VP8, 0,
                receiverVideoFormat,
                M7MediaConfiguration.M7CameraPosition.FRONT, false);
        m7WebRTCPeer = new M7WebRTCPeer(peerConnectionParameters, this, localView, this);
        m7WebRTCPeer.registerMasterRenderer(masterView);
        m7WebRTCPeer.initialize();
        m7WebRTCPeer.startLocalMedia();

        //连接websocket
        connectWebSocket();

        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag) {
                    chat_video_rl.setVisibility(View.GONE);
                    flag = false;
                }else {
                    chat_video_rl.setVisibility(View.VISIBLE);
                    flag = true;
                }
            }
        });

    }

    public void switchCamera(View view) {
        m7WebRTCPeer.switchCameraPosition();
    }

    private void connectWebSocket() {
        //TODO change url
        String wsUri = RequestUrl.videoWSSUrl;
        if(executor==null) {
            executor = new LooperExecutor();
            executor.requestStart();
        }

        if(m7RoomAPI ==null) {
            m7RoomAPI = new M7RoomAPI(executor, wsUri, this);
            CertificateFactory cf;
            Certificate ca = null;
            try {
                cf = CertificateFactory.getInstance("X.509");
                InputStream caInput = new BufferedInputStream(PeerVideoActivity.this.getAssets().open("kurento_room_base64.cer"));
                ca = cf.generateCertificate(caInput);
                m7RoomAPI.addTrustedCertificate("ca", ca);
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            m7RoomAPI.useSelfSignedCertificate(true);
        }

        if (!m7RoomAPI.isWebSocketConnected()) {
            m7RoomAPI.connectWebSocket();
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        if(m7WebRTCPeer != null) {
//            m7WebRTCPeer.stopLocalMedia();
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if(m7WebRTCPeer != null) {
//            m7WebRTCPeer.startLocalMedia();
//        }
//    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(videoRefuseReceiver);
        chat_video_chr.stop();
        if(m7RoomAPI != null) {
            if (m7RoomAPI.isWebSocketConnected()) {
                m7RoomAPI.sendLeaveRoom(roomId);
            }
            m7RoomAPI.disconnectWebSocket();
            m7RoomAPI.removeObserver(this);
            executor.requestStop();
            m7RoomAPI = null;
            executor = null;
        }
        endCall();
//        Process.killProcess(Process.myPid());
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // If back button has not been pressed in a while then trigger thread and toast notification
        if (!this.backPressed){
            this.backPressed = true;
            Toast.makeText(this,"再按一次退出界面", Toast.LENGTH_SHORT).show();
            this.backPressedThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                        backPressed = false;
                    } catch (InterruptedException e){ Log.d("VCA-oBP","Successfully interrupted"); }
                }
            });
            this.backPressedThread.start();
        }
        // If button pressed the second time then call super back pressed
        // (eventually calls onDestroy)
        else {
            if (this.backPressedThread != null)
                this.backPressedThread.interrupt();
            super.onBackPressed();
        }
    }

    public void hangup(View view) {

        if(calluser != null && !"".equals(calluser)) {
            finish();
        }else {

            Intent cancelIntent = new Intent(IMChatManager.CANCEL_VIDEO_ACTION);
            sendBroadcast(cancelIntent);
            finish();
        }

    }

    /**
     * Terminates the current call and ends activity
     */
    private void endCall() {
        callState = CallState.IDLE;
        try
        {
            if (m7WebRTCPeer != null) {
                m7WebRTCPeer.close();
                m7WebRTCPeer = null;
            }
        }
        catch (Exception e){e.printStackTrace();}
    }

    private Runnable offerWhenReady = new Runnable() {
        @Override
        public void run() {
            // Generate offers to receive video from all peers in the room
            for (Map.Entry<String, Boolean> entry : userPublishList.entrySet()) {
                if (entry.getValue()) {
                    GenerateOfferForRemote(entry.getKey());
                    entry.setValue(false);
                }
            }
        }
    };

    private void GenerateOfferForRemote(String remote_name){
        m7WebRTCPeer.generateOffer(remote_name, false);
        callState = CallState.WAITING_REMOTE_USER;
    }

    @Override
    public void onInitialize() {

        Log.e("VideoActivity", "回调了onInitialize");
//        m7WebRTCPeer.generateOffer("derp", true);
//        callState = CallState.PUBLISHING;
    }

    @Override
    public void onLocalSdpOfferGenerated(final SessionDescription sessionDescription, M7PeerConnection m7PeerConnection) {
        Log.e("VideoActivity", "回调了onLocalSdpOfferGenerated，callState is:"+callState.name());

        if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED) {
            localSdp = sessionDescription;
            Log.e("VideoActivity", "LocalSdp is:"+localSdp.description);

            //（2）将本地SDP信息发送给服务端
            publishVideoRequestId = ++ConstantsId;
            m7RoomAPI.sendPublishVideo(localSdp.description, false, publishVideoRequestId);
            Log.e("VideoActivity", "执行了sendPublishVideo");

        } else {
            // 获取到服务器返回的sdp信息  (4)
            remoteSdp = sessionDescription;
            publishVideoRequestId = ++ConstantsId;
            String username = m7PeerConnection.getConnectionId();
            videoRequestUserMapping.put(publishVideoRequestId, username);
            String sender = username + "_webcam";
            //请求服务器把远端的流发给手机(5)
            m7RoomAPI.sendReceiveVideoFrom(sender, remoteSdp.description, publishVideoRequestId);
        }
    }

    @Override
    public void onLocalSdpAnswerGenerated(SessionDescription sessionDescription, M7PeerConnection m7PeerConnection) {
        Log.e("VideoActivity", "回调了onLocalSdpAnswerGenerated");
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate, M7PeerConnection m7PeerConnection) {

        sendIceCandidateRequestId = ++ConstantsId;
        if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED){
            Log.e("VideoActivity", "回调了onIceCandidate username");
            m7RoomAPI.sendOnIceCandidate(this.username, iceCandidate.sdp,
                    iceCandidate.sdpMid, Integer.toString(iceCandidate.sdpMLineIndex), sendIceCandidateRequestId);
        } else {

            m7RoomAPI.sendOnIceCandidate(m7PeerConnection.getConnectionId(), iceCandidate.sdp,
                    iceCandidate.sdpMid, Integer.toString(iceCandidate.sdpMLineIndex), sendIceCandidateRequestId);
        }
    }

    @Override
    public void onIceStatusChanged(PeerConnection.IceConnectionState iceConnectionState, M7PeerConnection m7PeerConnection) {
        Log.e("VideoActivity", "回调了onIceStatusChanged");

    }

    @Override
    public void onRemoteStreamAdded(MediaStream mediaStream, M7PeerConnection m7PeerConnection) {
        Log.e("VideoActivity", "回调了onRemoteStreamAdded");
        //远端的流显示在界面上(6)
        m7WebRTCPeer.setActiveMasterStream(mediaStream);
    }

    @Override
    public void onRemoteStreamRemoved(MediaStream mediaStream, M7PeerConnection m7PeerConnection) {
        Log.e("VideoActivity", "回调了onRemoteStreamRemoved");
    }

    @Override
    public void onPeerConnectionError(String s) {
        Log.e("VideoActivity", "回调了onPeerConnectionError"+s);
    }

    @Override
    public void onDataChannel(DataChannel dataChannel, M7PeerConnection connection) {
    }

    @Override
    public void onBufferedAmountChange(long l, M7PeerConnection connection, DataChannel channel) {

    }

    @Override
    public void onStateChange(M7PeerConnection connection, DataChannel channel) {

    }

    @Override
    public void onMessage(DataChannel.Buffer buffer, M7PeerConnection connection, DataChannel channel) {

    }

    @Override
    public void onRoomResponse(M7RoomResponse response) {
        Log.e(TAG, "OnRoomResponse:" + response);

        if (response.getMethod()== M7RoomAPI.Method.JOIN_ROOM) {
            userPublishList = new HashMap<>(response.getUsers());
        }
        //加入房间结果
        List<HashMap<String, String>> mapList = response.getValues();
        if(mapList!=null) {
            for (HashMap<String, String> map : mapList) {
                for (String key : map.keySet()) {

                    if (key.equals("id")) {
                        //对方name
                        calluser = map.get("id");
                        //加入房间成功了
                    }
                }
            }
        }

        if (Integer.valueOf(response.getId()) == publishVideoRequestId){
            SessionDescription sd = new SessionDescription(SessionDescription.Type.ANSWER,
                    response.getValue("sdpAnswer").get(0));
            if (callState == CallState.PUBLISHING){
                callState = CallState.PUBLISHED;
                m7WebRTCPeer.processAnswer(sd, "derp");
                Log.e(TAG, "状态PUBLISHING变为PUBLISHED");
                //拉取远端流
                mHandler.postDelayed(offerWhenReady, 2000);

            } else if (callState == CallState.WAITING_REMOTE_USER){
                callState = CallState.RECEIVING_REMOTE_USER;
                String connectionId = videoRequestUserMapping.get(publishVideoRequestId);
                m7WebRTCPeer.processAnswer(sd, connectionId);
                Log.e(TAG, "状态WAITING_REMOTE_USER变为RECEIVING_REMOTE_USER");
                Handler handler = new Handler();
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                try{
//                                    if(mMediaPlayer != null) {
//                                        mMediaPlayer.stop();
//                                        mMediaPlayer.release();
//                                    }
//                                }catch (Exception e){}
                                chat_video_chr.setBase(SystemClock.elapsedRealtime());
                                chat_video_chr.start();
                                chat_video_tv_title.setText("与客服进行通话中");
                            }
                        });
                    }
                }, 1000);

            }
        }
    }

    @Override
    public void onRoomError(M7RoomError error) {
        Log.e(TAG, "OnRoomError:" + error);
        logAndToast("进入房间失败"+error.getCode());
        finish();
    }

    @Override
    public void onRoomNotification(M7RoomNotification notification) {
        Map<String, Object> map = notification.getParams();
        if(notification.getMethod().equals("iceCandidate")) {
            String sdpMid = map.get("sdpMid").toString();
            int sdpMLineIndex = Integer.valueOf(map.get("sdpMLineIndex").toString());
            String sdp = map.get("candidate").toString();

            IceCandidate ic = new IceCandidate(sdpMid, sdpMLineIndex, sdp);

            if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED) {
                m7WebRTCPeer.addRemoteIceCandidate(ic, "derp");
            } else {
                m7WebRTCPeer.addRemoteIceCandidate(ic, notification.getParam("endpointName").toString());
            }
        }else if(notification.getMethod().equals("participantLeft")) {
            PeerVideoActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }else if(notification.getMethod().equals("participantPublished")) {
            final String user = map.get("id").toString();
            userPublishList.put(user, true);
            Log.e("--------------", "participantPublished接收到远端流加入");
            mHandler.postDelayed(offerWhenReady, 2000);
        }else if(notification.getMethod().equals("participantJoined")) {
            final String user = map.get("id").toString();
            userPublishList.put(user, true);
            calluser = user;
        }
    }

    @Override
    public void onRoomConnected() {
        System.out.println("roomConnected");
        if (m7RoomAPI.isWebSocketConnected()) {
            //连接成功，加入房间
            System.out.println("roomConnected joinRoom");
            joinRoom();
            m7WebRTCPeer.generateOffer("derp", true);
            callState = CallState.PUBLISHING;

        }
    }

    @Override
    public void onRoomDisconnected() {
        PeerVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast("连接服务器失败");
                finish();
            }
        });

    }

    private void logAndToast(String message) {
        Log.i(TAG, message);
        showToast(message);
    }

    public void showToast(String string) {
        try {
            CharSequence text = string;
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 加入房间
     */
    private void joinRoom () {
        if (m7RoomAPI != null) {
            ConstantsId++;
            roomId = ConstantsId;
            if (m7RoomAPI.isWebSocketConnected()) {
                m7RoomAPI.sendJoinRoom(this.username, this.roomname, roomId);
            }
        }
    }

    class VideoRefuseReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            showToast("对方拒绝了邀请");
            finish();
        }
    }
}