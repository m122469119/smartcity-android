package sjtu.dclab.smartcity.ui.chat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import sjtu.dclab.smartcity.R;
import sjtu.dclab.smartcity.SQLite.DBManager;
import sjtu.dclab.smartcity.chat.*;
import sjtu.dclab.smartcity.community.config.Me;
import sjtu.dclab.smartcity.community.entity.Friend;
import sjtu.dclab.smartcity.community.entity.Message;
import sjtu.dclab.smartcity.ui.chat.audio.MediaRecordFunc;
import sjtu.dclab.smartcity.ui.chat.utils.CommonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends Activity implements OnClickListener {

    public static final int REQUEST_CODE_CAMERA = 18;
    public static final int REQUEST_CODE_LOCAL = 19;
    public static final int REQUEST_CODE_SELECT_FILE = 24;

    private ListView listView;
    private Button btnSendMessage, btnMore;
    private EditText etMessage;
    private View more;
    private View buttonSetModeKeyboard;
    private View buttonPressToSpeak;
    private LinearLayout btnContainer;
    private View buttonSetModeVoice;
    private TextView chatTitle;
    private ImageView micImage;
    private View recordingContainer;
    private TextView recordingHint;

    private File cameraFile;
    private int mRecordingResult;

    private InputMethodManager manager;
    private AnimationDrawable animationDrawable;
    MediaRecordFunc mRecorder;
    private PowerManager.WakeLock wakeLock;

    private Friend friend;
    private DeprecatedMessageAdapter deprecatedAdapter;

    private DBManager dbm;
    private BroadcastReceiverHelper broadcastReceiverHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_chat);

        initView();
        setUpView();

        //发消息按钮监听
        btnSendMessage.setOnClickListener(new SendMsgListener());

        //发送多媒体文件按钮监听
        findViewById(R.id.view_camera).setOnClickListener(this);
        findViewById(R.id.view_file).setOnClickListener(this);
        findViewById(R.id.view_photo).setOnClickListener(this);

        //语音消息相关
        mRecorder = MediaRecordFunc.getInstance();
        buttonPressToSpeak.setOnTouchListener(new PressToSpeakListen());

        Intent intent = getIntent();
        friend = (Friend) intent.getSerializableExtra(String.valueOf(R.string.friend));
        deprecatedAdapter = DeprecatedMessages.loadMessages(friend.getName());

        // 设置聊天名称
        chatTitle.setText(friend.getName());

        updateChatList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        broadcastReceiverHelper = new BroadcastReceiverHelper(getApplicationContext());
        broadcastReceiverHelper.register(PushCallback.ACTION);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiverHelper);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wakeLock.isHeld()) wakeLock.release();
        // 停止语音播放 TODO

        // 停止录音
        mRecorder.stopRecordAndFile();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbm.closeDB();
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.chat_list);
        btnSendMessage = (Button) findViewById(R.id.MessageButton);
        etMessage = (EditText) findViewById(R.id.MessageText);
        more = findViewById(R.id.more);
        buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
        btnMore = (Button) findViewById(R.id.btn_more);
        buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
        btnContainer = (LinearLayout) findViewById(R.id.ll_btn_container);
        buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
        chatTitle = (TextView) findViewById(R.id.tv_chat_title);

        micImage = (ImageView) findViewById(R.id.mic_image);
        animationDrawable = (AnimationDrawable) micImage.getBackground();
        animationDrawable.setOneShot(false);

        recordingContainer = findViewById(R.id.view_talk);
        recordingHint = (TextView) findViewById(R.id.recording_hint);

    }

    private void setUpView() {
        dbm = new DBManager(this);

        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "demo");
    }


    public void updateChatList() {
        List<MessageEntity> msgs = reloadMsg();
        List<HashMap<String, Object>> history = new ArrayList<HashMap<String, Object>>();
        for (MessageEntity msg : msgs) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("name", msg.getName());
            map.put("content", msg.getContent());
            history.add(map);
        }
        deprecatedAdapter.setMsgEntities(msgs);
        SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), history, R.layout.chat_item,
                new String[]{"name", "content"}, new int[]{R.id.messagedetail_row_name, R.id.messagedetail_row_text});
        listView.setAdapter(adapter);
        listView.setSelection(history.size() - 1);
    }

    protected List<MessageEntity> reloadMsg() {
        List<MessageEntity> msgList = dbm.getMsg(Me.id, friend.getId());
        return msgList;
    }

    private class SendMsgListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            String content = etMessage.getText().toString();
            if (content.length() == 0) {
                Toast.makeText(getApplicationContext(), "请输入内容", Toast.LENGTH_SHORT).show();
            } else {
                Message msg = new Message();
                msg.setContent(content);
                msg.setFrom(Me.id);
                msg.setTo(friend.getId());
                msg.setName("我");
                msg.setType(1); //TODO
                msg.setSerialId("");
                Publisher.publishMessage(msg);
                etMessage.setText("");
                //db
                dbm.saveMsg(msg);
                updateChatList();
            }
        }
    }

    /**
     * onActivityResult
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        btnContainer.setVisibility(View.GONE);
        //listView.setSelection(listView.getCount());
        if (requestCode == REQUEST_CODE_LOCAL) { // 发送本地图片
            if (data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    //sendPicByUri(selectedImage); TODO
                }
            } else if (requestCode == REQUEST_CODE_CAMERA) { // 发送照片
                if (cameraFile != null && cameraFile.exists()) {
                    //sendPicture(cameraFile.getAbsolutePath()); TODO
                }
            } else if (requestCode == REQUEST_CODE_SELECT_FILE) { // 发送选择的文件
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        //sendFile(uri); TODO
                    }
                }
            }
        }
    }

    /**
     * 消息图标点击事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        hideKeyboard();
        switch (view.getId()) {
            case R.id.view_photo:
                selectPicFromLocal();   // 点击图片图标
                break;
            case R.id.view_camera:
                selectPicFromCamera();  // 点击照相图标
                break;
            case R.id.view_file:
                selectFileFromLocal();  // 发送文件
                break;
            default:
                break;
        }
    }

    /**
     * 点击文字输入框
     *
     * @param v
     */
    public void editClick(View v) {
        listView.setSelection(listView.getCount() - 1);
        if (more.getVisibility() == View.VISIBLE) {
            more.setVisibility(View.GONE);
//            iv_emoticons_normal.setVisibility(View.VISIBLE);
//            iv_emoticons_checked.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 显示或隐藏图标按钮页
     *
     * @param view
     */
    public void more(View view) {
        if (more.getVisibility() == View.GONE) {
            System.out.println("more gone");
            hideKeyboard();
            more.setVisibility(View.VISIBLE);
            btnContainer.setVisibility(View.VISIBLE);
//            emojiIconContainer.setVisibility(View.GONE);
        } else if (more.getVisibility() == View.VISIBLE) {
            more.setVisibility(View.GONE);
            btnContainer.setVisibility(View.GONE);
        }
//        else {
//            if (emojiIconContainer.getVisibility() == View.VISIBLE) {
//                emojiIconContainer.setVisibility(View.GONE);
//                btnContainer.setVisibility(View.VISIBLE);
//                iv_emoticons_normal.setVisibility(View.VISIBLE);
//                iv_emoticons_checked.setVisibility(View.INVISIBLE);
//            } else {
//                more.setVisibility(View.GONE);
//            }
//        }
    }

    /**
     * 显示语音图标按钮
     *
     * @param view
     */
    public void setModeVoice(View view) {
        hideKeyboard();
        etMessage.setVisibility(View.GONE);
        more.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        buttonSetModeKeyboard.setVisibility(View.VISIBLE);
        btnSendMessage.setVisibility(View.GONE);
        btnMore.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.VISIBLE);
        btnContainer.setVisibility(View.VISIBLE);
//        iv_emoticons_normal.setVisibility(View.VISIBLE);
//        iv_emoticons_checked.setVisibility(View.INVISIBLE);
//        emojiIconContainer.setVisibility(View.GONE);
    }

    /**
     * 显示键盘图标
     *
     * @param view
     */
    public void setModeKeyboard(View view) {
        etMessage.setVisibility(View.VISIBLE);
        more.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        buttonSetModeVoice.setVisibility(View.VISIBLE);
        etMessage.requestFocus();
        buttonPressToSpeak.setVisibility(View.GONE);
        btnSendMessage.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏软键盘
     */
    private void hideKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 发送语音
     *
     * @param filePath
     * @param fileName
     * @param length
     * @param isResend
     */
    private void sendVoice(String filePath, String fileName, String length,
                           boolean isResend) {
        if (!(new File(filePath).exists())) {
            return;
        }
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从图库获取图片
     */
    private void selectPicFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_LOCAL);
    }

    /**
     * 照相获取图片
     */
    public void selectPicFromCamera() {
        if (!CommonUtils.isExitsSdcard()) {
            String st = getResources().getString(R.string.sd_card_does_not_exist);
            Toast.makeText(getApplicationContext(), st, Toast.LENGTH_SHORT).show();
            return;
        }
        //TODO 设置拍照文件
        cameraFile = new File(getFilesDir() + "/testTakingPhoto.jpg");
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                        MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
                REQUEST_CODE_CAMERA);
    }

    /**
     * 选择文件
     */
    private void selectFileFromLocal() {
        Intent intent = null;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
    }

    /**
     * 按住说话listener
     */
    class PressToSpeakListen implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    animationDrawable.start();
                    /*if (!CommonUtils.isExitsSdcard()) {
                        String st4 = getResources().getString(R.string.Send_voice_need_sdcard_support);
                        Toast.makeText(ChatActivity.this, st4, Toast.LENGTH_SHORT).show();
                        return false;
                    }*/
                    try {
                        v.setPressed(true);
                        wakeLock.acquire();
                        //if (VoicePlayClickListener.isPlaying) VoicePlayClickListener.currentPlayListener.stopPlayVoice();
                        recordingContainer.setVisibility(View.VISIBLE);
                        recordingHint.setText(getString(R.string.move_up_to_cancel));
                        recordingHint.setBackgroundColor(Color.TRANSPARENT);
                        //录制开始
                        //voiceRecorder.startRecording(null, String.valueOf(friend.getId()), getApplicationContext());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mRecordingResult = mRecorder.startRecordAndFile(getFilesDir() +
                                        "/" + Me.id + "_" + friend.getId() + "_" + "test.amr");//TODO:修改存储路径
                            }
                        }).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        v.setPressed(false);
                        if (wakeLock.isHeld()) wakeLock.release();
                        //if (voiceRecorder != null) voiceRecorder.discardRecording();
                        recordingContainer.setVisibility(View.INVISIBLE);
                        Toast.makeText(ChatActivity.this, R.string.recoding_fail, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if (event.getY() < 0) {
                        recordingHint.setText(getString(R.string.release_to_cancel));
                        recordingHint.setBackgroundResource(R.drawable.recording_text_hint_bg);
                    } else {
                        recordingHint.setText(getString(R.string.move_up_to_cancel));
                        recordingHint.setBackgroundColor(Color.TRANSPARENT);
                        animationDrawable.start();
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                    if (animationDrawable.isRunning()) animationDrawable.stop();
                    v.setPressed(false);
                    recordingContainer.setVisibility(View.INVISIBLE);
                    if (wakeLock.isHeld()) wakeLock.release();
                    if (event.getY() < 0) {
                        // 丢弃录音文件
                        //voiceRecorder.discardRecording();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mRecorder.stopRecordAndFile();  // 停止录音
                                // 丢弃录音文件 TODO
                            }
                        }).start();
                    } else {
                        String st1 = getResources().getString(R.string.Recording_without_permission);
                        String st2 = getResources().getString(R.string.The_recording_time_is_too_short);
                        String st3 = getResources().getString(R.string.send_failure_please);
                        try {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    mRecorder.stopRecordAndFile();  // 停止录音
                                    // 发送录音文件 TODO
                                }
                            }).start();
//                            int length = voiceRecorder.stopRecoding();
//                            if (length > 0)
//                                sendVoice(voiceRecorder.getVoiceFilePath(),
//                                        voiceRecorder.getVoiceFileName(String.valueOf(friend.getId())),
//                                        Integer.toString(length), false);
//                            else if (length == EMError.INVALID_FILE) {
//                                Toast.makeText(getApplicationContext(), st1, Toast.LENGTH_SHORT).show();
//                            }
//                            else {
//                                Toast.makeText(getApplicationContext(), st2, Toast.LENGTH_SHORT).show();
//                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ChatActivity.this, st3, Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                default:
                    recordingContainer.setVisibility(View.INVISIBLE);
                    //if (voiceRecorder != null) voiceRecorder.discardRecording();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mRecorder.stopRecordAndFile();  // 停止录音
                        }
                    }).start();
                    return false;
            }
        }
    }

    private class BroadcastReceiverHelper extends BroadcastReceiver {
        Context context;
        BroadcastReceiverHelper receiver;

        public BroadcastReceiverHelper(Context context) {
            this.context = context;
            this.receiver = this;
        }

        public void register(String action) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(action);
            context.registerReceiver(receiver, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BROADCAST", "msg received");
            updateChatList();
        }
    }
}

