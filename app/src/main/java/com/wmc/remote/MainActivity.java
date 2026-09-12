package com.wmc.remote;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 模拟 Windows Media Center（绿色）遥控器的安卓应用主界面。
 * 通过手机红外发射器输出标准 RC-6 MCE 信号（36 kHz，厂商码 0x800F）。
 */
public class MainActivity extends Activity {

    private IREmitter emitter;
    private TextView statusBar;

    /** MCE 协议切换位：每次新按下翻转，长按连发保持 */
    private boolean toggle = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable repeatTask;
    private int repeatCode = -1;
    private boolean repeatActive = false;

    /** 长按连发间隔（毫秒） */
    private static final long REPEAT_DELAY_MS = 110L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emitter = new IREmitter(this);
        statusBar = (TextView) findViewById(R.id.status_bar);

        if (!emitter.hasEmitter()) {
            statusBar.setText("未检测到红外发射器：本机可能没有红外功能（需带红外头的手机）");
            statusBar.setBackgroundResource(R.drawable.bg_banner_warn);
        } else if (!emitter.carrierSupported(RC6Encoder.CARRIER_HZ)) {
            statusBar.setText("红外已就绪，但设备可能不支持 36kHz 载波，发射可能无效");
            statusBar.setBackgroundResource(R.drawable.bg_banner_warn);
        } else {
            statusBar.setText("红外发射器已就绪 · RC-6 MCE 36kHz · 手机顶部对准接收器");
            statusBar.setBackgroundResource(R.drawable.bg_banner_ok);
        }

        wireKey(R.id.btn_power, MceKey.POWER, false);
        wireKey(R.id.btn_start, MceKey.MCE_START, false);
        wireKey(R.id.btn_dvdmenu, MceKey.DVD_MENU, false);
        wireKey(R.id.btn_guide, MceKey.GUIDE, false);
        wireKey(R.id.btn_info, MceKey.INFO, false);
        wireKey(R.id.btn_back, MceKey.BACK, false);
        wireKey(R.id.btn_livetv, MceKey.LIVE_TV, false);
        wireKey(R.id.btn_tv, MceKey.MY_TV, false);

        wireKey(R.id.btn_up, MceKey.UP, true);
        wireKey(R.id.btn_down, MceKey.DOWN, true);
        wireKey(R.id.btn_left, MceKey.LEFT, true);
        wireKey(R.id.btn_right, MceKey.RIGHT, true);
        wireKey(R.id.btn_ok, MceKey.OK, false);

        wireKey(R.id.btn_vol_up, MceKey.VOLUME_UP, true);
        wireKey(R.id.btn_vol_down, MceKey.VOLUME_DOWN, true);
        wireKey(R.id.btn_mute, MceKey.MUTE, false);
        wireKey(R.id.btn_ch_up, MceKey.CHANNEL_UP, true);
        wireKey(R.id.btn_ch_down, MceKey.CHANNEL_DOWN, true);

        wireKey(R.id.btn_prev, MceKey.PREVIOUS, true);
        wireKey(R.id.btn_rew, MceKey.REWIND, true);
        wireKey(R.id.btn_play, MceKey.PLAY, false);
        wireKey(R.id.btn_ff, MceKey.FAST_FORWARD, true);
        wireKey(R.id.btn_pause, MceKey.PAUSE, false);
        wireKey(R.id.btn_next, MceKey.NEXT, true);
        wireKey(R.id.btn_record, MceKey.RECORD, false);
        wireKey(R.id.btn_stop, MceKey.STOP, false);
        wireKey(R.id.btn_eject, MceKey.EJECT, false);

        wireKey(R.id.btn_red, MceKey.RED, false);
        wireKey(R.id.btn_green, MceKey.GREEN, false);
        wireKey(R.id.btn_yellow, MceKey.YELLOW, false);
        wireKey(R.id.btn_blue, MceKey.BLUE, false);

        wireKey(R.id.btn_1, MceKey.NUM_1, false);
        wireKey(R.id.btn_2, MceKey.NUM_2, false);
        wireKey(R.id.btn_3, MceKey.NUM_3, false);
        wireKey(R.id.btn_4, MceKey.NUM_4, false);
        wireKey(R.id.btn_5, MceKey.NUM_5, false);
        wireKey(R.id.btn_6, MceKey.NUM_6, false);
        wireKey(R.id.btn_7, MceKey.NUM_7, false);
        wireKey(R.id.btn_8, MceKey.NUM_8, false);
        wireKey(R.id.btn_9, MceKey.NUM_9, false);
        wireKey(R.id.btn_star, MceKey.STAR, false);
        wireKey(R.id.btn_0, MceKey.NUM_0, false);
        wireKey(R.id.btn_pound, MceKey.POUND, false);
        wireKey(R.id.btn_clear, MceKey.CLEAR, false);
        wireKey(R.id.btn_enter, MceKey.ENTER, false);

        wireKey(R.id.btn_mytv, MceKey.MY_TV, false);
        wireKey(R.id.btn_pictures, MceKey.MY_PICTURES, false);
        wireKey(R.id.btn_music, MceKey.MY_MUSIC, false);
        wireKey(R.id.btn_videos, MceKey.MY_VIDEOS, false);
        wireKey(R.id.btn_recordedtv, MceKey.RECORDED_TV, false);
        wireKey(R.id.btn_radio, MceKey.RADIO, false);
        wireKey(R.id.btn_zoom, MceKey.ZOOM, false);
        wireKey(R.id.btn_caption, MceKey.CAPTION, false);
        wireKey(R.id.btn_more, MceKey.MORE_PROGRAMS, false);
    }

    @Override
    protected void onDestroy() {
        stopRepeat();
        super.onDestroy();
    }

    private void wireKey(int id, final int keyCode, final boolean repeatable) {
        View v = findViewById(id);
        if (v == null) {
            return;
        }
        if (repeatable) {
            v.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    int action = event.getActionMasked();
                    if (action == MotionEvent.ACTION_DOWN) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        startRepeat(keyCode);
                        return true;
                    } else if (action == MotionEvent.ACTION_UP
                            || action == MotionEvent.ACTION_CANCEL) {
                        stopRepeat();
                        view.performClick();
                        return true;
                    }
                    return false;
                }
            });
        } else {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    sendKey(keyCode, false);
                }
            });
        }
    }

    /** 按下：翻转切换位并立即发送，随后按间隔连发（模拟遥控器长按） */
    private void startRepeat(int keyCode) {
        stopRepeat();
        repeatCode = keyCode;
        repeatActive = true;
        sendKey(keyCode, false);
        repeatTask = new Runnable() {
            @Override
            public void run() {
                if (!repeatActive) {
                    return;
                }
                sendKey(repeatCode, true);
                handler.postDelayed(this, REPEAT_DELAY_MS);
            }
        };
        handler.postDelayed(repeatTask, REPEAT_DELAY_MS);
    }

    private void stopRepeat() {
        repeatActive = false;
        if (repeatTask != null) {
            handler.removeCallbacks(repeatTask);
            repeatTask = null;
        }
    }

    private void sendKey(int keyCode, boolean repeat) {
        if (!emitter.hasEmitter()) {
            toast("本机没有红外发射器，无法发送");
            return;
        }
        if (!repeat) {
            toggle = !toggle;
        }
        int[] pattern = RC6Encoder.encode(keyCode, toggle);
        boolean sent = emitter.transmit(RC6Encoder.CARRIER_HZ, pattern);
        if (sent) {
            statusBar.setText(String.format("已发送: %s · 0x%04X · 载波 36kHz",
                    MceKey.name(keyCode), keyCode));
        } else {
            statusBar.setText("发送失败：系统拒绝了红外发射");
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
