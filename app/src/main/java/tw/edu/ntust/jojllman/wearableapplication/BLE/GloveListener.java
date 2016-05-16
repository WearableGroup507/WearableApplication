package tw.edu.ntust.jojllman.wearableapplication.BLE;

import java.util.List;

/**
 * Created by Lian on 2016/5/16.
 */
public interface GloveListener {
    void RecognizeUpdate(final String result, final boolean isRecognizing);
    void RSSIUpdate(final boolean isLeftHand, final int rssi);
    void RecordFileUpdate(final List<String> files);
}
