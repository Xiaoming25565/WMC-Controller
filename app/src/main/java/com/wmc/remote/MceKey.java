package com.wmc.remote;

import java.util.HashMap;
import java.util.Map;

/**
 * Windows Media Center 遥控器按键码表（scancode 低 16 位）。
 * 来源：Linux 内核 rc-core 官方按键映射 drivers/media/rc/keymaps/rc-rc6-mce.c
 * （厂商码 0x800F，完整 scancode = 0x800F0000 | 此处按键码）
 */
public final class MceKey {

    // 数字键
    public static final int NUM_0 = 0x0400;
    public static final int NUM_1 = 0x0401;
    public static final int NUM_2 = 0x0402;
    public static final int NUM_3 = 0x0403;
    public static final int NUM_4 = 0x0404;
    public static final int NUM_5 = 0x0405;
    public static final int NUM_6 = 0x0406;
    public static final int NUM_7 = 0x0407;
    public static final int NUM_8 = 0x0408;
    public static final int NUM_9 = 0x0409;
    public static final int CLEAR = 0x040A;   // 清除 (KEY_DELETE)
    public static final int ENTER = 0x040B;   // 确认
    public static final int STAR = 0x041D;    // *
    public static final int POUND = 0x041C;   // #

    // 顶部功能键
    public static final int POWER = 0x040C;   // 电源
    public static final int MCE_START = 0x040D; // 绿色 MCE 键 (KEY_MEDIA)
    public static final int MUTE = 0x040E;    // 静音
    public static final int INFO = 0x040F;    // 信息
    public static final int VOLUME_UP = 0x0410;
    public static final int VOLUME_DOWN = 0x0411;
    public static final int CHANNEL_UP = 0x0412;
    public static final int CHANNEL_DOWN = 0x0413;

    // 播放控制
    public static final int FAST_FORWARD = 0x0414;
    public static final int REWIND = 0x0415;
    public static final int PLAY = 0x0416;
    public static final int RECORD = 0x0417;
    public static final int PAUSE = 0x0418;
    public static final int STOP = 0x0419;
    public static final int NEXT = 0x041A;      // 下一曲/跳进
    public static final int PREVIOUS = 0x041B;  // 上一曲/跳回
    public static final int PLAY_PAUSE = 0x046E;

    // 方向键
    public static final int UP = 0x041E;
    public static final int DOWN = 0x041F;
    public static final int LEFT = 0x0420;
    public static final int RIGHT = 0x0421;
    public static final int OK = 0x0422;

    // 菜单导航
    public static final int BACK = 0x0423;      // 返回/退出
    public static final int DVD_MENU = 0x0424;
    public static final int LIVE_TV = 0x0425;   // LiveTV
    public static final int GUIDE = 0x0426;     // 指南/EPG
    public static final int ZOOM = 0x0427;      // 画面比例
    public static final int VISUALIZATION = 0x0432;
    public static final int SLIDE_SHOW = 0x0433;
    public static final int EJECT = 0x0434;     // 弹出

    // 媒体库快捷方式
    public static final int MY_TV = 0x0446;
    public static final int MY_MUSIC = 0x0447;
    public static final int RECORDED_TV = 0x0448;
    public static final int MY_PICTURES = 0x0449;
    public static final int MY_VIDEOS = 0x044A;
    public static final int RADIO = 0x0450;
    public static final int CAPTION = 0x045A;   // 字幕/Teletext

    // 彩色键
    public static final int RED = 0x045B;
    public static final int GREEN = 0x045C;
    public static final int YELLOW = 0x045D;
    public static final int BLUE = 0x045E;

    public static final int TV_POWER = 0x0465;
    public static final int MORE_PROGRAMS = 0x046F; // 开始媒体应用

    private static final Map<Integer, String> NAMES = new HashMap<Integer, String>();

    static {
        NAMES.put(NUM_0, "数字0"); NAMES.put(NUM_1, "数字1"); NAMES.put(NUM_2, "数字2");
        NAMES.put(NUM_3, "数字3"); NAMES.put(NUM_4, "数字4"); NAMES.put(NUM_5, "数字5");
        NAMES.put(NUM_6, "数字6"); NAMES.put(NUM_7, "数字7"); NAMES.put(NUM_8, "数字8");
        NAMES.put(NUM_9, "数字9"); NAMES.put(CLEAR, "清除"); NAMES.put(ENTER, "确认");
        NAMES.put(STAR, "*"); NAMES.put(POUND, "#");
        NAMES.put(POWER, "电源"); NAMES.put(MCE_START, "MCE键");
        NAMES.put(MUTE, "静音"); NAMES.put(INFO, "信息");
        NAMES.put(VOLUME_UP, "音量+"); NAMES.put(VOLUME_DOWN, "音量-");
        NAMES.put(CHANNEL_UP, "频道+"); NAMES.put(CHANNEL_DOWN, "频道-");
        NAMES.put(FAST_FORWARD, "快进"); NAMES.put(REWIND, "快退");
        NAMES.put(PLAY, "播放"); NAMES.put(RECORD, "录制"); NAMES.put(PAUSE, "暂停");
        NAMES.put(STOP, "停止"); NAMES.put(NEXT, "下一曲"); NAMES.put(PREVIOUS, "上一曲");
        NAMES.put(PLAY_PAUSE, "播放/暂停");
        NAMES.put(UP, "上"); NAMES.put(DOWN, "下"); NAMES.put(LEFT, "左"); NAMES.put(RIGHT, "右");
        NAMES.put(OK, "OK");
        NAMES.put(BACK, "返回"); NAMES.put(DVD_MENU, "DVD菜单"); NAMES.put(LIVE_TV, "LiveTV");
        NAMES.put(GUIDE, "指南"); NAMES.put(ZOOM, "画面比例"); NAMES.put(VISUALIZATION, "可视化");
        NAMES.put(SLIDE_SHOW, "幻灯片"); NAMES.put(EJECT, "弹出");
        NAMES.put(MY_TV, "我的电视"); NAMES.put(MY_MUSIC, "我的音乐");
        NAMES.put(RECORDED_TV, "录制的电视"); NAMES.put(MY_PICTURES, "图片");
        NAMES.put(MY_VIDEOS, "视频"); NAMES.put(RADIO, "广播"); NAMES.put(CAPTION, "字幕");
        NAMES.put(RED, "红键"); NAMES.put(GREEN, "绿键"); NAMES.put(YELLOW, "黄键"); NAMES.put(BLUE, "蓝键");
        NAMES.put(TV_POWER, "电视电源"); NAMES.put(MORE_PROGRAMS, "更多程序");
    }

    private MceKey() {
    }

    public static String name(int keyCode) {
        String n = NAMES.get(keyCode);
        return n != null ? n : String.format("0x%04X", keyCode);
    }
}
