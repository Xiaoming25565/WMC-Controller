package com.wmc.remote;

import java.util.ArrayList;
import java.util.List;

/**
 * RC-6 MCE 红外信号编码器（Windows Media Center 遥控器协议）。
 *
 * 协议规格（依据 Linux 内核 rc-core 的权威实现 ir-rc6-decoder.c 与 rc-ir-raw.c）：
 *   - 载波频率   : 36000 Hz（36 kHz）
 *   - 基准单元   : 444 µs
 *   - 引导码     : 6 单元载波 + 2 单元空闲（2664 µs 亮 + 888 µs 灭）
 *   - 头部 4 位  : 起始位(1) + 模式位(110)，值 0b1110，MSB 先发，反相 Manchester
 *   - 头部切换位 : 1 位，2 单元半周期，固定发 0
 *   - 数据 32 位 : 高 16 位厂商码 0x800F + 低 16 位按键码（bit15 为 MCE 切换位），MSB 先发
 *   - Manchester : 位 '1' = 前半周期载波（亮 444µs + 灭 444µs）；位 '0' = 前半周期空闲（灭 444µs + 亮 444µs）
 *   - 帧尾       : 6 单元空闲（2664 µs）
 *   相邻同极性半周期会自动合并为更长的一段（与真实遥控器硬件及内核编码器行为一致）。
 *
 * 输出为 ConsumerIrManager.transmit(carrier, pattern) 所需的 int[]（微秒，交替亮/灭，以亮开头）。
 */
public final class RC6Encoder {

    /** 载波频率 36 kHz */
    public static final int CARRIER_HZ = 36000;

    /** 基准单元（µs） */
    public static final int UNIT = 444;

    /** 引导码：6 单元亮 */
    private static final int LEADER_MARK = 6 * UNIT;
    /** 引导码：2 单元灭 */
    private static final int LEADER_SPACE = 2 * UNIT;

    /** 头部切换位半周期：2 单元 */
    private static final int TOGGLE_HALF = 2 * UNIT;

    /** 帧尾空闲：6 单元 */
    private static final int TRAILER_SPACE = 6 * UNIT;

    /** MCE 厂商码（32 位数据高 16 位） */
    public static final int MCE_VENDOR = 0x800F0000;

    /** MCE 切换位（32 位数据 bit15） */
    public static final int MCE_TOGGLE_BIT = 0x8000;

    private RC6Encoder() {
    }

    /**
     * 编码一个 WMC/MCE 按键为红外发射时序。
     *
     * @param keyCode 按键码（低 16 位，例如 KEY_PLAY = 0x0416），见 {@link MceKey}
     * @param toggle  MCE 切换位状态；新按下一次按键应交替翻转，长按连发时保持相同
     * @return 交替 亮/灭 的微秒数组（以亮开始，以灭结束），可直接交给 ConsumerIrManager
     */
    public static int[] encode(int keyCode, boolean toggle) {
        int body = MCE_VENDOR | (keyCode & 0xFFFF) | (toggle ? MCE_TOGGLE_BIT : 0);
        return encodeBody(body);
    }

    /**
     * 将完整 32 位数据体（厂商码+按键码+切换位）编码为时序。
     * 公开用于调试/校验。
     */
    public static int[] encodeBody(int body) {
        PulseBuilder b = new PulseBuilder();

        // 引导码：6T 亮 + 2T 灭
        b.add(true, LEADER_MARK);
        b.add(false, LEADER_SPACE);

        // 头部 4 位：起始位(1) + 模式位 110，值 0b1110，MSB 先发
        int header = 0b1110;
        for (int i = 3; i >= 0; i--) {
            manchesterBit(b, (header >>> i) & 1, UNIT);
        }

        // 头部切换位：固定发 0，半周期为 2 单元（先灭后亮）
        b.add(false, TOGGLE_HALF);
        b.add(true, TOGGLE_HALF);

        // 32 位数据体，MSB 先发
        for (int i = 31; i >= 0; i--) {
            manchesterBit(b, (body >>> i) & 1, UNIT);
        }

        // 帧尾：6T 空闲
        if (b.lastPulse) {
            b.add(false, TRAILER_SPACE);
        } else {
            int lastIdx = b.durations.size() - 1;
            b.durations.set(lastIdx, b.durations.get(lastIdx) + TRAILER_SPACE);
        }

        return b.toArray();
    }

    /**
     * 反相 Manchester 编码一个位（内核 invert=1 语义）：
     *   位 '1' → 前半周期亮(UNIT) + 后半周期灭(UNIT)
     *   位 '0' → 前半周期灭(UNIT) + 后半周期亮(UNIT)
     */
    private static void manchesterBit(PulseBuilder b, int bit, int half) {
        if (bit == 1) {
            b.add(true, half);
            b.add(false, half);
        } else {
            b.add(false, half);
            b.add(true, half);
        }
    }

    /**
     * 按极性合并相邻同极性段的时序构建器。
     * 真实红外波形中连续的亮/灭会自然合并，内核编码器亦如此处理。
     */
    private static final class PulseBuilder {
        final List<Integer> durations = new ArrayList<Integer>();
        boolean lastPulse = false;
        boolean empty = true;

        void add(boolean pulse, int us) {
            if (empty) {
                durations.add(us);
                lastPulse = pulse;
                empty = false;
            } else if (pulse == lastPulse) {
                int idx = durations.size() - 1;
                durations.set(idx, durations.get(idx) + us);
            } else {
                durations.add(us);
                lastPulse = pulse;
            }
        }

        int[] toArray() {
            int[] out = new int[durations.size()];
            for (int i = 0; i < out.length; i++) {
                out[i] = durations.get(i);
            }
            return out;
        }
    }
}
