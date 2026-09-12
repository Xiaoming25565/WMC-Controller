import com.wmc.remote.RC6Encoder;

import java.util.ArrayList;
import java.util.List;

/**
 * RC6Encoder 独立往返校验：
 * 依据 Linux 内核 ir-rc6-decoder.c 的状态机（含 decrease_duration 事件拆分语义），
 * 将编码器产出的时序反向解码为 scancode，与期望的 0x800Fxxxx 按键码比对。
 */
public class RC6SelfCheck {

    private static final int UNIT = 444;
    private static int failures = 0;

    public static void main(String[] args) {
        check(0x0400, "数字0");
        check(0x0401, "数字1");
        check(0x0409, "数字9");
        check(0x040a, "Clear");
        check(0x040b, "Enter");
        check(0x040c, "Power");
        check(0x040d, "MCE绿色键");
        check(0x040e, "Mute");
        check(0x040f, "Info");
        check(0x0410, "Vol+");
        check(0x0412, "Ch+");
        check(0x0414, "快进");
        check(0x0416, "播放");
        check(0x0417, "录制");
        check(0x0418, "暂停");
        check(0x0419, "停止");
        check(0x041a, "下一个");
        check(0x041b, "上一个");
        check(0x041e, "上");
        check(0x0422, "OK");
        check(0x0423, "返回");
        check(0x0424, "DVD菜单");
        check(0x0425, "LiveTV");
        check(0x0426, "指南");
        check(0x0427, "缩放");
        check(0x0446, "我的电视");
        check(0x045a, "字幕");
        check(0x045b, "红键");
        check(0x045c, "绿键");
        check(0x045d, "黄键");
        check(0x045e, "蓝键");
        check(0x046f, "更多");

        checkToggle(0x0416);
        checkInvariants(0x0422, false);

        if (failures == 0) {
            System.out.println("ALL CHECKS PASSED");
        } else {
            System.out.println(failures + " CHECKS FAILED");
            System.exit(1);
        }
    }

    private static void check(int keyCode, String name) {
        for (boolean toggle : new boolean[]{false, true}) {
            int[] pattern = RC6Encoder.encode(keyCode, toggle);
            DecodeResult r = decode(pattern);
            int expectScancode = 0x800F0000 | (keyCode & 0xFFFF);
            boolean ok = r.ok && r.scancode == expectScancode && r.toggle == toggle;
            if (!ok) {
                failures++;
                System.out.printf("FAIL %s(0x%04X) toggle=%b: decode ok=%b scancode=0x%08X (expect 0x%08X) toggleBit=%b (expect %b)%n",
                        name, keyCode, toggle, r.ok, r.scancode, expectScancode, r.toggle, toggle);
            } else {
                System.out.printf("PASS %-10s 0x%04X toggle=%b -> scancode 0x%08X toggleBit=%b%n",
                        name, keyCode, toggle, r.scancode, r.toggle);
            }
        }
    }

    private static void checkToggle(int keyCode) {
        int[] a = RC6Encoder.encode(keyCode, false);
        int[] b = RC6Encoder.encode(keyCode, true);
        if (java.util.Arrays.equals(a, b)) {
            failures++;
            System.out.println("FAIL toggle: false 与 true 波形完全相同");
        } else {
            System.out.println("PASS toggle: false/true 波形存在差异");
        }
    }

    private static void checkInvariants(int keyCode, boolean toggle) {
        int[] p = RC6Encoder.encode(keyCode, toggle);
        if (p.length < 2 || p[0] <= 0 || p[1] <= 0) {
            failures++;
            System.out.println("FAIL invariants: 数组为空或前两位非法");
            return;
        }
        if (p[0] != 2664) {
            failures++;
            System.out.println("FAIL invariants: 引导码亮段=" + p[0] + " (期望 2664)");
        }
        long total = 0;
        for (int d : p) {
            total += d;
        }
        if (total < 35000 || total > 45000) {
            failures++;
            System.out.println("FAIL invariants: 帧总时长=" + total + " µs (期望约 40ms)");
        } else {
            System.out.println("PASS invariants: 段数=" + p.length + " 总时长=" + total + " µs");
        }
    }

    // ------------------------------------------------------------------
    // 内核 ir-rc6-decoder.c 状态机转录（含 decrease_duration 语义）
    // ------------------------------------------------------------------
    private static final class DecodeResult {
        boolean ok;
        int scancode;
        boolean toggle;
    }

    private static final class Cursor {
        final List<Ev> events;
        int idx = 0;
        int dur;
        boolean pulse;

        Cursor(List<Ev> events) {
            this.events = events;
            Ev first = events.get(0);
            this.pulse = first.pulse;
            this.dur = first.duration;
        }

        boolean advance() {
            idx++;
            if (idx >= events.size()) return false;
            Ev e = events.get(idx);
            pulse = e.pulse;
            dur = e.duration;
            return true;
        }

        boolean consume(int use) {
            if (dur < use) return false;
            dur -= use;
            if (dur == 0) return advance();
            return true;
        }
    }

    private static DecodeResult decode(int[] pattern) {
        List<Ev> events = new ArrayList<Ev>();
        boolean pulse = true;
        for (int d : pattern) {
            events.add(new Ev(pulse, d));
            pulse = !pulse;
        }
        if (events.isEmpty()) {
            DecodeResult r = new DecodeResult();
            r.ok = false;
            return r;
        }

        DecodeResult res = new DecodeResult();
        Cursor cur = new Cursor(events);
        int state = 0; // 0=INACTIVE 1=PREFIX_SPACE 2=HEADER_START 3=HEADER_END 4=TOGGLE_START 5=TOGGLE_END 6=BODY_START 7=BODY_END 8=FINISHED
        int header = 0, count = 0, body = 0, wanted = 32;
        boolean toggleBit = false;

        for (int guard = 0; guard < 5000; guard++) {
            switch (state) {
                case 0:
                    if (!cur.pulse) {
                        if (!cur.advance()) return fail(res);
                        break;
                    }
                    if (!eq(cur.dur, 6 * UNIT, UNIT)) return fail(res);
                    cur.dur = 0;
                    if (!cur.advance()) return fail(res);
                    state = 1;
                    count = 0;
                    break;
                case 1:
                    if (cur.pulse) return fail(res);
                    if (!eq(cur.dur, 2 * UNIT, UNIT / 2)) return fail(res);
                    cur.dur = 0;
                    if (!cur.advance()) return fail(res);
                    state = 2;
                    header = 0;
                    break;
                case 2:
                    if (!eq(cur.dur, UNIT, UNIT / 2)) return fail(res);
                    header <<= 1;
                    if (cur.pulse) header |= 1;
                    count++;
                    if (!cur.consume(UNIT)) return fail(res);
                    state = 3;
                    break;
                case 3:
                    state = (count == 4) ? 4 : 2;
                    if (!cur.consume(UNIT)) return fail(res);
                    break;
                case 4:
                    if (!eq(cur.dur, 2 * UNIT, UNIT / 2)) return fail(res);
                    toggleBit = cur.pulse;
                    if (!cur.consume(2 * UNIT)) return fail(res);
                    state = 5;
                    break;
                case 5:
                    if ((header & 0x08) == 0) return fail(res);
                    state = 6;
                    count = 0;
                    body = 0;
                    if (!cur.consume(2 * UNIT)) return fail(res);
                    break;
                case 6:
                    if (eq(cur.dur, UNIT, UNIT / 2)) {
                        if (count < 32) {
                            body <<= 1;
                            if (cur.pulse) body |= 1;
                        }
                        count++;
                        if (!cur.consume(UNIT)) return fail(res);
                        state = 7;
                    } else if (!cur.pulse && cur.dur >= 6 * UNIT - UNIT / 2) {
                        state = 8;
                    } else {
                        return fail(res);
                    }
                    break;
                case 7:
                    state = (count == wanted) ? 8 : 6;
                    if (!cur.consume(UNIT)) return fail(res);
                    break;
                case 8:
                    if (cur.pulse) return fail(res);
                    res.ok = true;
                    res.toggle = (body & 0x8000) != 0;
                    res.scancode = body & ~0x8000;
                    return res;
                default:
                    return fail(res);
            }
        }
        return fail(res);
    }

    private static DecodeResult fail(DecodeResult r) {
        r.ok = false;
        return r;
    }

    private static boolean eq(long v, long expect, long margin) {
        return Math.abs(v - expect) <= margin;
    }

    private static final class Ev {
        final boolean pulse;
        final int duration;

        Ev(boolean pulse, int duration) {
            this.pulse = pulse;
            this.duration = duration;
        }
    }
}
