package tw.edu.ntust.jojllman.wearableapplication.BLE;

/**
 * Created by Lian on 2016/5/23.
 */

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class TextToSpeechService extends Service implements TextToSpeech.OnInitListener{
    private static final String TAG = "TextToSpeechService";
    private TextToSpeech mTextToSpeech;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class LocalBinder extends Binder {
        TextToSpeechService getService() {
            return  TextToSpeechService.this;
        }
    }
    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        mTextToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.CHINESE);
                    SharedPreferences settings = getSharedPreferences("Preference", 0);
                    mTextToSpeech.setSpeechRate(settings.getFloat("TTS_Rate", 1.0f));
                }
            }
        });
        Log.d("Service State","Service Created");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service State", "Service Start");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Log.i(TAG, "Initilization Succeeded!");
            mTextToSpeech.setLanguage(Locale.getDefault());
        } else {
            Log.i(TAG, "Initialization failed");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service Destroy");
        if(mTextToSpeech !=null){
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
    }

    public void speak(String toSpeak){
        mTextToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    public boolean isSpeaking(){
        return mTextToSpeech.isSpeaking();
    }
}



