package tw.edu.ntust.jojllman.wearableapplication.BLE;

/**
 * Created by LPS-Max005 on 2015/11/16.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

public class MjpegView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "MjpegView";
    private static final String TAG2 = "TestMJ";
    private Context ActivityContext;

    public final static int SIZE_STANDARD   = 1;
    public final static int SIZE_BEST_FIT   = 4;
    public final static int SIZE_FULLSCREEN = 8;

    private MjpegViewThread thread;
    private MjpegInputStream mIn = null;
    private boolean mRun = false;
    private boolean surfaceDone = false;
    private int dispWidth;
    private int dispHeight;
    private int displayMode;
    private int videoProcessingState = 0;

    public final static int STATE_NORMAL = 0;
    public final static int STATE_SEGMENTATION = 1;
    public final static int STATE_INTERSECTION = 2;
    public final static int STATE_CROSSPOINT = 5;
    public final static int STATE_COLORTAGDETECT = 3;
    public final static int STATE_QRTAGDETECT = 4;
    public final static int STATE_QRDETECT = 7;
    public final static int STATE_BLANK = 6;
    private VideoProcessing processing = new VideoProcessing();

    public class MjpegViewThread extends Thread {
        private SurfaceHolder mSurfaceHolder;

        public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) {
            Log.i(TAG2,"MjpegViewThread(SurfaceHolder surfaceHolder, Context context) ");
            mSurfaceHolder = surfaceHolder;
        }

        private Rect destRect(int bmw, int bmh) {
            Log.i(TAG2,"Rect destRect(int bmw, int bmh)");
            int tempx;
            int tempy;
            if (displayMode == MjpegView.SIZE_STANDARD) {
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_BEST_FIT) {
                float bmasp = (float) bmw / (float) bmh;
                bmw = dispWidth;
                bmh = (int) (dispWidth / bmasp);
                if (bmh > dispHeight) {
                    bmh = dispHeight;
                    bmw = (int) (dispHeight * bmasp);
                }
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
                //return new Rect(0,0,100,100);
            }
            if (displayMode == MjpegView.SIZE_FULLSCREEN){
                return new Rect(0, 0, dispWidth, dispHeight);
                //return new Rect(0,0,100,100);
            }
            return null;
        }

        public void setSurfaceSize(int width, int height) {
            Log.i(TAG2,"setSurfaceSize(int width, int height)");
            synchronized(mSurfaceHolder) {
                dispWidth = width;
                dispHeight = height;
            }
        }

        public void run() {
            Log.i(TAG2,"run()");
            Bitmap input;
            Bitmap output;
            Rect destRect;
            Canvas c = null;
            Paint p = new Paint();
            boolean rs_init = false;
            while (!mRun) {
                if(surfaceDone) {
                    try {
                        c = mSurfaceHolder.lockCanvas();
                        synchronized (mSurfaceHolder) {
                            try {
                                input = output = mIn.readMjpegFrame();
                                if(!rs_init) {
                                    Log.i(TAG, "RS init");
                                    processing.renderscript_init(input, ActivityContext);
                                    rs_init = true;
                                }
                                destRect = destRect(input.getWidth(), input.getHeight());
                                switchstate(input, output);
                                if(c!=null) {
                                    c.drawColor(Color.BLACK);
                                    c.drawBitmap(output, null, destRect, p);
                                }
                            } catch (IOException e) {
                                e.getStackTrace();
                                Log.d(TAG, "catch IOException hit in run", e);
                            }
                        }
                    } finally {
                        if (c != null) {
                            mSurfaceHolder.unlockCanvasAndPost(c);
                        }
                    }
                }
            }
            if(mRun){
                System.out.println("thread interrupt");
                this.interrupt();
            }
        }

        private void switchstate(Bitmap input, Bitmap output){
            Log.i(TAG2,"switchstate(Bitmap input, Bitmap output)");
            if(videoProcessingState == STATE_NORMAL){
                Log.i(TAG, "Mode Normal");
                processing.render_normal(input, output);
            }
            else if(videoProcessingState == STATE_SEGMENTATION){
                Log.i(TAG, "Mode Segmentation");
                processing.render_segmentation(input, output);
            }
            else if(videoProcessingState == STATE_INTERSECTION){
                Log.i(TAG, "Mode Intersection");
                processing.render_intersection(input, output);
            }
            else if(videoProcessingState == STATE_CROSSPOINT){
                Log.i(TAG, "Mode Cross Point");
                processing.render_crosspoint(input, output);
            }
            else if(videoProcessingState == STATE_COLORTAGDETECT){
                Log.i(TAG, "Mode Color TAG Detect");
                processing.render_colortagdetect(input, output);
            }
            else if(videoProcessingState == STATE_QRTAGDETECT){
                Log.i(TAG, "Mode QR TAG Detect");
                processing.render_qrtagdetect(input, output);
            }
            else if(videoProcessingState == STATE_QRDETECT){
                Log.i(TAG, "Mode QR Detect");
                processing.render_qrdetect(input, output);
            }
            else if(videoProcessingState == STATE_BLANK){
                Log.i(TAG, "Mode Blank");
                processing.render_blank(input, output);
            }
        }
    }
    public void setState(int state){
        Log.i(TAG2,"setState(int state)");
        videoProcessingState = state;
    }

    private void init(Context context) {
        Log.i(TAG2,"init(Context context)");
        ActivityContext = context;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        thread = new MjpegViewThread(holder, context);
        setFocusable(true);
        displayMode = MjpegView.SIZE_STANDARD;
        dispWidth = getWidth();
        dispHeight = getHeight();
    }

    public void startPlayback() {
        Log.i(TAG2,"startPlayback()");
        if(mIn != null) {
            mRun = false;
            if(thread.getState() == MjpegViewThread.State.NEW){
                thread.start();
            }
        }
    }

    public void stopPlayback() {
        Log.i(TAG2,"stopPlayback()");
        mRun = true;
//        boolean retry = true;
//        while(retry) {
//            try {
//                thread.join();
//                retry = false;
//            } catch (InterruptedException e) {
//                e.getStackTrace();
//                Log.d(TAG, "catch IOException hit in stopPlayback", e);
//            }
//        }
    }

    public MjpegView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG2,"MjpegView(Context context, AttributeSet attrs)");
        init(context);
    }

    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
        thread.setSurfaceSize(w, h);
        Log.i(TAG2,"surfaceChanged(SurfaceHolder holder, int f, int w, int h)");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceDone = false;
        Log.i(TAG2,"surfaceDestroyed(SurfaceHolder holder)");
        stopPlayback();
    }

    public MjpegView(Context context) {
        super(context);
        Log.i(TAG2,"MjpegView(Context context)");
        init(context);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        surfaceDone = true;
        Log.i(TAG2,"surfaceCreated(SurfaceHolder holder)");
    }

    public void setSource(MjpegInputStream source) {
        mIn = source;
        Log.i(TAG2,"setSource(MjpegInputStream source)");
        startPlayback();
    }

    public void setDisplayMode(int s) {
        displayMode = s;
        Log.i(TAG2,"setDisplayMode(int s)");
    }
}