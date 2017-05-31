package tw.edu.ntust.jojllman.wearableapplication.BLE;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Long2;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.detector.Detector;

/**
 * Created by LPS-Max005 on 2015/11/16.
 */
public class VideoProcessing {
    private static final String TAG = "VideoProcessing";
    private static final String TAG2 = "Tag Talk";
    private static final String TAG3 = "Tag Test -1 ";
    private final int NUM_BITMAPS = 3;
    private int mCurrentBitmap = 0;
    private int width;
    private int height;

    private RenderScript mRS;
    private Allocation mInputRgbAllocation;
    private Allocation mProcessingAllocation;
    private Allocation mSegAllocation;
    private Allocation mPointAllocation;
    private Allocation mPrePointAllocation;
    private Allocation mCrossPointAllocation;
    private Allocation mOutputAllocation;
    private Allocation mCrossPointNumber;
    private Allocation mCrossPointLeft;
    private Allocation mCrossPointRight;
    private Allocation mCrossPointTop;
    private Allocation mCrossPointBottom;
    //private Allocation[] mOutAllocations;

    private ScriptC_process mScriptProcessing;

    private int[] intArray;
    private boolean onProcess;

    int[] crossNumber = new  int[1];
    int[] crossLeft = new int[1];
    int[] crossRight = new int[1];
    int[] crossTop = new int[1];
    int[] crossBottom = new int[1];

    public void renderscript_init(Bitmap src, Context context){
        mRS = RenderScript.create(context);
        width = src.getWidth();
        height = src.getHeight();

        Type.Builder rgbTypeBuilder = new Type.Builder(mRS, Element.RGBA_8888(mRS));
        rgbTypeBuilder.setX(width);
        rgbTypeBuilder.setY(height);
        Type.Builder luminanceBuilder = new Type.Builder(mRS, Element.U8(mRS));
        luminanceBuilder.setX(width);
        luminanceBuilder.setY(height);
        Type.Builder uint32Builder = new Type.Builder(mRS, Element.U32(mRS));

        intArray = new int[width*height];

        mInputRgbAllocation = Allocation.createFromBitmap(mRS, src);
        mOutputAllocation = Allocation.createFromBitmap(mRS, src);

        mSegAllocation = Allocation.createTyped(mRS, rgbTypeBuilder.create(),
                Allocation.USAGE_SCRIPT);
        mProcessingAllocation = Allocation.createTyped(mRS, rgbTypeBuilder.create(),
                Allocation.USAGE_SCRIPT);

        mPointAllocation = Allocation.createTyped(mRS, luminanceBuilder.create(),
                Allocation.USAGE_SCRIPT);
        mPrePointAllocation = Allocation.createTyped(mRS, luminanceBuilder.create(),
                Allocation.USAGE_SCRIPT);
        mCrossPointAllocation = Allocation.createTyped(mRS, luminanceBuilder.create(),
                Allocation.USAGE_SCRIPT);

        mCrossPointNumber = Allocation.createTyped(mRS, uint32Builder.create());
        mCrossPointLeft = Allocation.createTyped(mRS, uint32Builder.create());
        mCrossPointRight = Allocation.createTyped(mRS, uint32Builder.create());
        mCrossPointTop = Allocation.createTyped(mRS, uint32Builder.create());
        mCrossPointBottom = Allocation.createTyped(mRS, uint32Builder.create());

        mCurrentBitmap += (mCurrentBitmap + 1) % NUM_BITMAPS;
        /*
        mOutAllocations = new Allocation[NUM_BITMAPS];
        for (int i = 0; i < NUM_BITMAPS; ++i) {
            mOutAllocations[i] = Allocation.createFromBitmap(mRS, src);
        }*/
        mScriptProcessing = new ScriptC_process(mRS);
        mScriptProcessing.set_width(width);
        mScriptProcessing.set_height(height);

        mScriptProcessing.bind_crossPointNumber(mCrossPointNumber);
        mScriptProcessing.bind_crossPointLeft(mCrossPointLeft);
        mScriptProcessing.bind_crossPointRight(mCrossPointRight);
        mScriptProcessing.bind_crossPointTop(mCrossPointTop);
        mScriptProcessing.bind_crossPointBottom(mCrossPointBottom);

        mScriptProcessing.set_ProcessFrame(mProcessingAllocation);
        mScriptProcessing.set_SegFrame(mSegAllocation);
        mScriptProcessing.set_PointFrame(mPointAllocation);
        mScriptProcessing.set_PrePointFrame(mPrePointAllocation);
        mScriptProcessing.set_CrossPointFrame(mCrossPointAllocation);
    }

    public void render_normal(Bitmap src, Bitmap dst){
        dst = src;
    }

    public void render_blank(Bitmap src, Bitmap dst){
        mInputRgbAllocation = Allocation.createFromBitmap(mRS, src);
        mScriptProcessing.forEach_zeroUchar4Allocation(mOutputAllocation);
        mOutputAllocation.copyTo(dst);
        //mScriptProcessing.forEach_zeroUchar4Allocation(mOutAllocations[mCurrentBitmap]);
        //mOutAllocations[mCurrentBitmap].copyTo(dst);
        //mCurrentBitmap = (mCurrentBitmap + 1) % NUM_BITMAPS;
    }

    public void render_segmentation(Bitmap src, Bitmap dst){
        mInputRgbAllocation = Allocation.createFromBitmap(mRS, src);
        mScriptProcessing.forEach_segmentation(mInputRgbAllocation, mOutputAllocation);
        mOutputAllocation.copyTo(dst);
    }

    public void render_intersection(Bitmap src, Bitmap dst){
        mInputRgbAllocation = Allocation.createFromBitmap(mRS, src);
        mScriptProcessing.forEach_segmentation(mInputRgbAllocation, mOutputAllocation);
        mScriptProcessing.forEach_intersection(mOutputAllocation);
        mOutputAllocation.copyTo(dst);
    }

    public void render_crosspoint(Bitmap src, Bitmap dst){
        crossPoint(src);
        mScriptProcessing.forEach_displayCrossPoint(mCrossPointAllocation, mOutputAllocation);
        mOutputAllocation.copyTo(dst);
    }

    public void render_colortagdetect(Bitmap src, Bitmap dst){
        if(crossPoint(src)){
            mScriptProcessing.set_crossPointDetected(true);
            mScriptProcessing.forEach_findCrossPoint(mCrossPointAllocation);
            mCrossPointLeft.copyTo(crossLeft);
            mCrossPointRight.copyTo(crossRight);
            mCrossPointTop.copyTo(crossTop);
            mCrossPointBottom.copyTo(crossBottom);

            int centerX = findCenterX();
            int centerY = findCenterY();
            int tagSide = findTagWidth()/2;
            Log.i(TAG, "Center X is " + centerX + " Center Y is " + centerY);
            mScriptProcessing.set_blockLeft(centerX-tagSide);
            mScriptProcessing.set_blockRight(centerX+tagSide);
            mScriptProcessing.set_blockTop(centerY-tagSide);
            mScriptProcessing.set_blockBottom(centerY+tagSide);
            if(!onProcess && !MainActivity.mTTSService.isSpeaking()){
                colorTagProcess process = new colorTagProcess();
                process.getInput(src);
                process.getValue(centerX, centerY, width, height);
                process.start();
            }
        }
        else{
            mScriptProcessing.set_crossPointDetected(false);
        }
        mScriptProcessing.forEach_drawBlock(mInputRgbAllocation, mOutputAllocation);
        mOutputAllocation.copyTo(dst);
    }

    //READ_TEST
    public void render_qrtagdetect(Bitmap src, Bitmap dst){
        if(crossPoint(src)){
            mScriptProcessing.set_crossPointDetected(true);
            mScriptProcessing.forEach_findCrossPoint(mCrossPointAllocation);
            mCrossPointLeft.copyTo(crossLeft);
            mCrossPointRight.copyTo(crossRight);
            mCrossPointTop.copyTo(crossTop);
            mCrossPointBottom.copyTo(crossBottom);

            int centerX = findCenterX();
            int centerY = findCenterY();
            int tagSide = findTagWidth()/2;
            Log.i(TAG, "Center X is " + centerX + " Center Y is " + centerY);
            mScriptProcessing.set_blockLeft(centerX-tagSide);
            mScriptProcessing.set_blockRight(centerX+tagSide);
            mScriptProcessing.set_blockTop(centerY-tagSide);
            mScriptProcessing.set_blockBottom(centerY+tagSide);
            if(!onProcess && !MainActivity.mTTSService.isSpeaking()){
                qrTagProcess process = new qrTagProcess();
                process.getInput(src);
                process.initailPoint(findTagWidth(), centerX, centerY, width, height);
                process.start();
            }
        }
        else{
            mScriptProcessing.set_crossPointDetected(false);
        }
        mScriptProcessing.forEach_drawBlock(mInputRgbAllocation, mOutputAllocation);
        mOutputAllocation.copyTo(dst);
    }

    public void render_qrdetect(Bitmap src, Bitmap dst){
        mInputRgbAllocation = Allocation.createFromBitmap(mRS, src);
        if(!onProcess && !MainActivity.mTTSService.isSpeaking()){
            qrDetect detect = new qrDetect();
            detect.getInput(src);
            detect.start();
        }
        mScriptProcessing.forEach_drawPoint(mInputRgbAllocation, mOutputAllocation);
        mOutputAllocation.copyTo(dst);
    }


    void initialPoint(){
        crossLeft[0] = width;
        crossTop[0] = width;
        mCrossPointLeft.copyFrom(crossLeft);
        mCrossPointTop.copyFrom(crossTop);

        crossNumber[0] = 0;
        crossRight[0] = 0;
        crossBottom[0] = 0;
        mCrossPointNumber.copyFrom(crossNumber);
        mCrossPointRight.copyFrom(crossRight);
        mCrossPointBottom.copyFrom(crossBottom);
    }

    boolean crossPoint(Bitmap src){
        initialPoint();
        mScriptProcessing.forEach_zeroUcharAllocation(mCrossPointAllocation);
        mInputRgbAllocation = Allocation.createFromBitmap(mRS, src);
        mScriptProcessing.forEach_segmentation(mInputRgbAllocation, mOutputAllocation);
        mScriptProcessing.forEach_intersection(mOutputAllocation);
        mScriptProcessing.forEach_crossPoint(mPointAllocation);
        mCrossPointNumber.copyTo(crossNumber);
        Log.i(TAG, "Cross Point Number is " + crossNumber[0]);

        if(crossNumber[0] < 50){

            int outRange = mScriptProcessing.get_crossBlockRangeOut();
            Log.i(TAG, "Not found cross point in range " + outRange);

            if(outRange >= 30)
                outRange = 6;
            else
                outRange += 4;

            mScriptProcessing.set_crossBlockRangeOut(outRange);
            return false;
        }else {
            int outRange = mScriptProcessing.get_crossBlockRangeOut();
            Log.i(TAG, "Found cross point in range " + outRange);
            return true;
        }
    }

    int findTagWidth(){
        int width;

        if((crossRight[0]-crossLeft[0]) > (crossBottom[0]-crossTop[0]))
            width = crossRight[0]-crossLeft[0];
        else
            width = crossBottom[0]-crossTop[0];

        return width;
    }

    int findCenterX(){
        return (crossRight[0]-crossLeft[0])/2+crossLeft[0];
    }

    int findCenterY(){
        return (crossBottom[0]-crossTop[0])/2+crossTop[0];
    }

    public class colorTagProcess extends Thread {
        int center_x;
        int center_y;
        int bitmapWidth;
        int bitmapHeight;
        Bitmap input;

        public void getInput(Bitmap src){
            input = Bitmap.createBitmap(src);
        }

        public void getValue(int cx, int cy, int inputWidth, int inputHeight) {
            center_x = cx;
            center_y = cy;
            bitmapWidth = inputWidth;
            bitmapHeight = inputHeight;
        }

        public String tagPos() {
            String notify = null;
            Log.i(TAG, "Center x is " + center_x + " Center y is " + center_y);
            int heightUnit = bitmapHeight / 5;
            if (2 * heightUnit < center_x && center_x < 3 * heightUnit)
                notify = "前方是";
            else if (center_x < 2 * heightUnit)
                notify = "左方是";
            else if (center_x > 3 * heightUnit)
                notify = "右方是";
            return notify;
        }

        @Override
        public void run() {
            onProcess = true;

            int pixel = input.getPixel(center_x, center_y);
            int r = (pixel >> 16) & 0xff;
            int g = (pixel >> 8) & 0xff;
            int b = pixel & 0xff;
            Log.i(TAG, "R is " + r + " G is " + g + " B is " + b);
            float minium, delta;
            float h, v;
            String tagContents = "";
            if (r >= g && r >= b) {
                v = r;
                if (g > b) {
                    minium = b;
                    delta = v - minium;
                    if (v != 0)
                        h = (60 * (g - b)) / delta;
                    else
                        h = 0;
                } else {
                    minium = g;
                    delta = v - minium;
                    if (v != 0)
                        h = 360 - (60 * (b - g)) / delta;
                    else
                        h = 0;
                }
            } else if (g >= r && g >= b) {
                v = g;
                if (b > r) {
                    minium = r;
                    delta = v - minium;
                    if (v != 0)
                        h = 120 + (60 * (b - r)) / delta;

                    else
                        h = 0;
                } else {
                    minium = b;
                    delta = v - minium;
                    if (v != 0)
                        h = 120 - (60 * (r - b)) / delta;

                    else
                        h = 0;
                }
            } else {
                v = b;
                if (r > g) {
                    minium = g;
                    delta = v - minium;
                    if (v != 0)
                        h = 240 + (60 * (r - g)) / delta;
                    else
                        h = 0;
                } else {
                    minium = r;
                    delta = v - minium;
                    if (v != 0)
                        h = 240 - (60 * (g - r)) / delta;
                    else
                        h = 0;
                }
            }

            if (h >= 170 && h <= 270) { //Blue
                Log.i(TAG, "Blue");
                tagContents = "男廁";
            } else if (h >= 30 && h <= 90) { //Yellow
                Log.i(TAG, "Yellow");
                tagContents = "服務台";
            } else if (h <= 20 || h >= 320) { //Red
                Log.i(TAG, "Red");
                tagContents = "女廁";
            } else {
                Log.i(TAG, "Black or White");
            }
            MainActivity.mTTSService.speak(tagPos() + tagContents);

            onProcess = false;
        }
    }

    public class qrTagProcess extends Thread {
        int start_x;
        int start_y;
        int center_x;
        int center_y;
        int end_x;
        int end_y;
        int tag_width;
        int bitmapWidth;
        int bitmapHeight;
        Bitmap input;

        public void getInput(Bitmap src){
            input = Bitmap.createBitmap(src);
        }

        public void initailPoint(int w, int cx, int cy, int inputWidth, int inputHeight) {
            if (w > 100)
                tag_width = w;
            else
                tag_width = 100;
            center_x = cx;
            center_y = cy;
            int tag_side = tag_width / 2;
            if (cx < tag_side)
                start_x = 0;
            else if (cx >= inputWidth - tag_side)
                start_x = inputWidth - tag_side - 1;
            else
                start_x = cx - tag_side;
            if (cy < tag_side)
                start_y = 0;
            else if (cy >= inputHeight - tag_side)
                start_y = inputHeight - tag_side - 1;
            else
                start_y = cy - tag_side;
            end_x = start_x + tag_width;
            end_y = start_y + tag_width;
            bitmapWidth = inputWidth;
            bitmapHeight = inputHeight;
            Log.i(TAG2,"[ " + bitmapWidth +","+bitmapHeight+" ]");
        }

        public String tagPos() {
            String notify = null;
            Log.i(TAG, "Center x is " + center_x + " Center y is " + center_y);

            int heightUnit = bitmapHeight / 5;
            if (2 * heightUnit < center_x && center_x < 3 * heightUnit)
                notify = "請往正前靠近";
            else if (center_x < 2 * heightUnit)
                notify = "請往左並靠近";
            else if (center_x > 3 * heightUnit)
                notify = "請往右並靠近";
            return notify;
        }

        @Override
        public void run() {
            onProcess = true;

            String contents;
            int[] qrArray = new int[tag_width * tag_width];
            if (( start_x + tag_width <= 640 )&&( start_y + tag_width <= 480 ) ){
                input.getPixels(qrArray, 0, tag_width, start_x, start_y, tag_width, tag_width);
            }
            else {
                onProcess = false;
                return;
            }

            Log.i(TAG2,"qrArray = " + qrArray);
            LuminanceSource source = new RGBLuminanceSource(tag_width, tag_width, qrArray);
            BinaryBitmap binary = new BinaryBitmap(new HybridBinarizer(source));

            Reader reader = new QRCodeReader();
            try {
                Result result = reader.decode(binary);
                contents = result.getText();
                Log.i(TAG, "QR code contents is " + contents);
                Log.i(TAG2,"0. " + contents );
                MainActivity.mTTSService.speak(contents);
            } catch (NotFoundException e) {
                Log.i(TAG, "Not found");
                Log.i(TAG2,"1. " + tagPos() );
                MainActivity.mTTSService.speak(tagPos());
            } catch (ChecksumException e) {
                Log.e(TAG, "hecksum error");
                Log.i(TAG2,"2. hecksum error");
                //MainActivity.mTTSService.speak(tagNotify());
            } catch (FormatException e) {
                Log.e(TAG, "Format error");
                Log.i(TAG2,"3. " + tagPos() );
                MainActivity.mTTSService.speak(tagPos());
            }

            onProcess = false;
        }
    }

    public class qrDetect extends Thread {
        Bitmap input;

        public void getInput(Bitmap src){
            input = src;
        }

        @Override
        public void run(){
            onProcess = true;

            input.getPixels(intArray, 0, width, 0, 0, width, height);
            LuminanceSource source = new RGBLuminanceSource(width, height, intArray);
            BinaryBitmap binary = new BinaryBitmap(new HybridBinarizer(source));
            try {
                DetectorResult detectorResult = new Detector(binary.getBlackMatrix()).detect(null);
                int length = detectorResult.getPoints().length;
                Long2[] scriptPoint = new Long2[length];
                for(int i = 0; i < length; ++i){
                    ResultPoint point = detectorResult.getPoints()[i];
                    scriptPoint[i] = new Long2((long)point.getX(), (long)point.getY());
                    Log.i(TAG, "Point " + i + " at X = " + scriptPoint[i].x + " , Y = " + scriptPoint[i].y);
                }
                mScriptProcessing.set_qrDetected(true);
                mScriptProcessing.set_point1(scriptPoint[0]);
                mScriptProcessing.set_point2(scriptPoint[1]);
                mScriptProcessing.set_point3(scriptPoint[2]);

                String contents = null;
                Reader reader = new QRCodeReader();
                Result result = reader.decode(binary);
                contents = result.getText();
                Log.i(TAG, "QR code contents is " + contents);
                MainActivity.mTTSService.speak(contents);
                Thread.sleep(1500);
            }catch (NotFoundException e){
                Log.i(TAG, "Not found");
                mScriptProcessing.set_qrDetected(false);
            }catch (ChecksumException e){
                Log.e(TAG, "hecksum error");
            }catch (FormatException e){
                Log.e(TAG, "Format error");
                mScriptProcessing.set_qrDetected(false);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

            onProcess = false;
        }
    }

}
