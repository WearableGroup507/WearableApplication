package tw.edu.ntust.jojllman.wearableapplication.BLE;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by HuangYuChang on 15/5/25.
 */
public class DeleteService extends Service {
    private Intent transferIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.RECEIVER_DELETE");

    @Override
    public IBinder onBind(Intent arg0){
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.i("DeleteService", "Create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("DeleteService", "Start");
        transferIntent.putExtra("turnOff", true);
        sendBroadcast(transferIntent);
        return super.onStartCommand(intent, flags, startId);
    }
}
