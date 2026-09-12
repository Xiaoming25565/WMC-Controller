package com.wmc.remote;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.util.Log;

/**
 * 红外发射封装：基于 Android ConsumerIrManager（API 19+）。
 * 发射前通过 hasIrEmitter() 检测手机是否带红外发射头。
 */
public class IREmitter {

    private static final String TAG = "IREmitter";

    private final ConsumerIrManager ir;
    private final boolean emitterPresent;

    public IREmitter(Context context) {
        ConsumerIrManager tmp = null;
        try {
            tmp = (ConsumerIrManager) context.getSystemService(Context.CONSUMER_IR_SERVICE);
        } catch (Throwable t) {
            Log.w(TAG, "getSystemService(CONSUMER_IR_SERVICE) failed", t);
        }
        ir = tmp;
        emitterPresent = ir != null && ir.hasIrEmitter();
    }

    /** 手机是否具备红外发射能力 */
    public boolean hasEmitter() {
        return emitterPresent;
    }

    /**
     * 指定载波频率是否在设备支持范围内。
     * 部分设备不报告频率范围，此时视为支持。
     */
    public boolean carrierSupported(int hz) {
        if (ir == null) {
            return false;
        }
        try {
            ConsumerIrManager.CarrierFrequencyRange[] ranges = ir.getCarrierFrequencies();
            if (ranges == null || ranges.length == 0) {
                return true;
            }
            for (ConsumerIrManager.CarrierFrequencyRange r : ranges) {
                if (hz >= r.getMinFrequency() && hz <= r.getMaxFrequency()) {
                    return true;
                }
            }
            return false;
        } catch (Throwable t) {
            return true;
        }
    }

    /**
     * 发射一组红外时序。
     *
     * @param carrierHz 载波频率（Hz）
     * @param pattern   交替亮/灭的微秒数组，以亮开始
     * @return 是否成功提交发射
     */
    public boolean transmit(int carrierHz, int[] pattern) {
        if (ir == null || !emitterPresent) {
            return false;
        }
        try {
            ir.transmit(carrierHz, pattern);
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "transmit failed", t);
            return false;
        }
    }
}
