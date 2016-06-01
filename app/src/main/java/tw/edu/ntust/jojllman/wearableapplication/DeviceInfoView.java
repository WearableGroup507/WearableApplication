package tw.edu.ntust.jojllman.wearableapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jojllman on 2015/11/26.
 */
public class DeviceInfoView extends View {

    public static final short GLASS = 1;
    public static final short BRACELET = 2;
    public static final short GLOVE = 3;
    public static final String[] DEVICE_NAME = {"眼鏡", "手環", "手套"};
    public static boolean useTextSignal = false;

    private Bitmap deviceBitmap = null;
    private short deviceType = GLASS;
    private short battery = 100;
    private short signal = 100;

    public DeviceInfoView(Context context) {
        super(context);
        deviceBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.eye);
    }

    public DeviceInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        deviceBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.eye);
    }

    public DeviceInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        deviceBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.eye);
    }

    public DeviceInfoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        deviceBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.eye);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        Rect rect1 = new Rect();
        this.getDrawingRect(rect1);
        rect1.set(rect1.left + rect1.width() / 4, rect1.top, rect1.right - rect1.width() / 4, rect1.bottom / 2);
        canvas.drawBitmap(deviceBitmap, null, rect1, paint);

        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(getResources().getDisplayMetrics().density * 32);


        int width = canvas.getWidth() / 5;
        int height = (int) ((canvas.getHeight() / 5 * 3) - ((paint.descent() + paint.ascent()) / 2)) ;
        canvas.drawText("電量", width, height, paint);
        width = canvas.getWidth() / 5 * 4;
        canvas.drawText("訊號", width, height, paint);
        height = (int) ((canvas.getHeight() / 5 * 4) - ((paint.descent() + paint.ascent()) / 2)) ;
        width = canvas.getWidth() / 5;
        paint.setColor(Color.GREEN);
        canvas.drawText(battery + "%", width, height, paint);
        width = canvas.getWidth() / 5 * 4;
        paint.setColor(Color.RED);
        canvas.drawText(getTxtSignal(), width, height, paint);
    }

    public void setBitmapToDraw(Bitmap bitmap){
        if(bitmap != null)
            this.deviceBitmap = bitmap;
    }

    public void setDeviceType(short type){
        if(type >= 1 && type <= 3)
            this.deviceType = type;

        updateContentDescription();
    }

    public void setBattery(short battery) {
        if(battery < 0 || battery > 100)
            return;

        this.battery = battery;

        updateContentDescription();
    }

    public void setSignal(short signal) {
        this.signal = signal;
        this.invalidate();
        updateContentDescription();
    }

    private void updateContentDescription(){
        String str = DEVICE_NAME[deviceType-1]
                + "裝置已配對，電量"
                + battery + "%，訊號"
                + getTxtSignal();
        if(getTxtSignal().equals("未知"))str = DEVICE_NAME[deviceType-1] + "裝置未配對";
        setContentDescription(str);
    }

    private String getTxtSignal(){
        if(!useTextSignal){
            return ""+signal;
        }
        if(signal < -100){
            return "弱";
        }else if(signal < -50){
            return "中";
        }else if(signal < 0){
            return "強";
        }else{
            return "未知";
        }
    }
}
