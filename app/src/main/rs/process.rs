#pragma version(1)
//#pragma rs java_package_name(ntust.lps.smartglasses_rs)
#pragma rs java_package_name(tw.edu.ntust.jojllman.wearableapplication.BLE)
#pragma rs_fp_relaxed

rs_allocation ProcessFrame;
rs_allocation SegFrame;
rs_allocation PointFrame;
rs_allocation PrePointFrame;
rs_allocation CrossPointFrame;
volatile uint32_t* crossPointNumber;
volatile uint32_t* crossPointLeft;
volatile uint32_t* crossPointRight;
volatile uint32_t* crossPointTop;
volatile uint32_t* crossPointBottom;
int width;
int height;
int blockLeft;
int blockRight;
int blockTop;
int blockBottom;
int crossBlockRangeOut = 6;
float sv_thres = 0.25f;
float v_thres = 0.4f;
bool qrDetected;
bool crossPointDetected;
long2 point1;
long2 point2;
long2 point3;

uchar4 __attribute__((kernel)) drawPoint(uchar4 in, uint32_t x, uint32_t y) {

    uchar4 draw = (uchar4)(0, 0, 0, 0);
    if(!qrDetected){
        return in;
    }else{
        draw = in;
        if(x > (point1.x-5) && x < (point1.x+5)){
            if(y > (point1.y-5) && y < (point1.y+5)){
                draw.r = 255;
                draw.g = 0;
                draw.b = 0;
                draw.a = 0;
                return draw;
            }
        }
        if(x > (point2.x-5) && x < (point2.x+5)){
            if(y > (point2.y-5) && y < (point2.y+5)){
                draw.r = 0;
                draw.g = 255;
                draw.b = 0;
                draw.a = 0;
                return draw;
            }
        }
        if(x > (point3.x-5) && x < (point3.x+5)){
            if(y > (point3.y-5) && y < (point3.y+5)){
                draw.r = 0;
                draw.g = 0;
                draw.b = 255;
                draw.a = 0;
                return draw;
            }
        }
    }

    return draw;
}

uchar4 __attribute__((kernel)) drawBlock(uchar4 in, uint32_t x, uint32_t y) {
    uchar4 draw;
    if(!crossPointDetected){
        return in;
    }
    else{
        int x_int = (int)x;
        int y_int = (int)y;
        if(((abs(x_int - blockLeft) < 3 || abs(x_int - blockRight) < 3) && y_int > blockTop && y_int < blockBottom)
            || ((abs(y_int - blockTop) < 3 || abs(y_int - blockBottom) < 3) && x_int > blockLeft && x_int < blockRight)){
            draw.r = 0;
            draw.g = 0;
            draw.b = 255;
            draw.a = 0;
            return draw;
        }
        return in;
    }
}

uchar __attribute__((kernel)) zeroUcharAllocation(uint32_t x, uint32_t y) {
    return 0;
}

uchar4 __attribute__((kernel)) zeroUchar4Allocation(uint32_t x, uint32_t y) {
    uchar4 result;
    result.r = 0;
    result.g = 0;
    result.b = 0;
    result.a = 0;
    return result;
}

uchar4 __attribute__((kernel)) segmentation(uchar4 in, uint32_t x, uint32_t y) {
        uchar4 result;
        uchar4 segType;
        float4 rgbPixel = rsUnpackColor8888(in);


        float minium ,delta;
        float h,s,v;
        float r = rgbPixel.r;
        float g = rgbPixel.g;
        float b = rgbPixel.b;

        if(r >= g && r >= b){
            v = r;
            //R>G>B
            if(g > b){
                minium = b;
                delta = v-minium;
                if(v != 0){
                    s = delta/v;
                    h = (60*(g-b))/delta;
                }
                else{
                    s = 0;
                    h = 0;
                }
            //R>B>G
            }else{
                minium = g;
                delta = v-minium;
                if(v != 0){
                    s = delta/v;
                    h = 360 - (60*(b-g))/delta;
                }
                else{
                    s = 0;
                    h = 0;
                }
            }
        }
        else if(g >= r && g >= b){
            v = g;
            //G>R>B
            if(b > r){
                minium = r;
                delta = v - minium;
                if(v != 0){
                    s = delta/v;
                    h = 120 + (60*(b-r))/delta;
                }
                else{
                    s = 0;
                    h = 0;
                }
            //G>B>R
            }else{
                minium = b;
                delta = v-minium;
                if(v != 0){
                    s = delta/v;
                    h = 120 - (60*(r-b))/delta;
                }
                else{
                    s = 0;
                    h = 0;
                }
            }
        }
        else{
            v = b;
            //B>R>G
            if(r > g){
                minium = g;
                delta = v-minium;
                if(v != 0){
                    s = delta/v;
                    h = 240 + (60*(r-g))/delta;
                }
                else{
                    s = 0;
                    h = 0;
                }
            //B>G>R
            }else{
                minium = r;
                delta = v-minium;
                if(v != 0){
                    s = delta/v;
                    h = 240 - (60*(g-r))/delta;
                }
                else{
                    s = 0;
                    h = 0;
                }
            }
        }
        if(s*v >= sv_thres){
            if(h>=170&&h<=270){ //Blue
                result.r = 0;     result.g = 0;     result.b = 255;   result.a = 0;
                segType.r = 0;    segType.g = 0;    segType.b = 1;    segType.a = 0;
            }else if(h>=30&&h<=90){ //Yellow
                result.r = 255;   result.g = 255;   result.b = 0;     result.a = 0;
                segType.r = 0;    segType.g = 1;    segType.b = 0;    segType.a = 0;
            }else if(h<=20||h>=320){ //Red
                result.r = 255;   result.g = 0;     result.b = 0;     result.a = 0;
                segType.r = 1;    segType.g = 0;    segType.b = 0;    segType.a = 0;
            }else{
                result.r = 0;     result.g = 0;     result.b = 0;     result.a = 0;
                segType.r = 0;    segType.g = 0;    segType.b = 0;    segType.a = 0;
            }
        }else{
            if(v < v_thres){  //Black
                result.r = 0;     result.g = 0;     result.b = 0;     result.a = 0;
                segType.r = 0;    segType.g = 0;    segType.b = 0;    segType.a = 1;
            }else{  //White
                result.r = 255;   result.g = 255;   result.b = 255;   result.a = 0;
                segType.r = 0;    segType.g = 0;    segType.b = 0;    segType.a = 0;
            }
        }
        rsSetElementAt_uchar4(SegFrame, segType, x, y);

        return result;
}

uchar4 __attribute__((kernel)) intersection(uint32_t x, uint32_t y)
{
    uchar4 result = (uchar4){0, 0, 0, 0};

    int sum_0 = 0;//R
    int sum_1 = 0;//G
    int sum_2 = 0;//B
    int sum_3 = 0;//A
    int blockSize = 2;
    uchar4 element;
    uchar prePoint;
    //prePoint = rsGetElementAt_uchar(PrePointFrame, x, y);
    uchar pointType; // No:0 BY:1 RY:2
    uchar pointCombin;

    int left = x < blockSize ? blockSize : x > width-blockSize-1 ? width-blockSize-1 : x;
    int top = y < blockSize ? blockSize : y > height-blockSize-1 ? height-blockSize-1 : y;
    for(int j=-blockSize; j<=blockSize; j++){
        for(int i=-blockSize; i<=blockSize; i++){
            element = rsGetElementAt_uchar4(SegFrame, left+i, top+j);
            sum_0 += element.r;
            sum_1 += element.g;
            sum_2 += element.b;
            sum_3 += element.a;
        }
    }

    if(sum_0==0&&sum_1>0&&sum_2>0&&sum_3>0){
        pointType = 1;
    }else if(sum_0>0&&sum_1>0&&sum_2==0&&sum_3>0){
        pointType = 2;
    }else{
        pointType = 0;
    }
    //rsSetElementAt_uchar(PrePointFrame, pointType, x, y);

    if(pointType == 1 || prePoint == 1){
        result.r = 0;     result.g = 0;   result.b = 255;   result.a = 0;
        pointCombin = 1;
    }else if(pointType == 2 || prePoint == 2){
        result.r = 255;   result.g = 0;   result.b = 0;     result.a = 0;
        pointCombin = 2;
    }else{
        result.r = 0;     result.g = 0;   result.b = 0;     result.a = 0;
        pointCombin = 0;
    }
    rsSetElementAt_uchar(PointFrame, pointCombin, x, y);

    return result;
}

void __attribute__((kernel)) crossPoint(uchar in, uint32_t x, uint32_t y)
{
    uchar result = 0;

        // No:0 BY:1 RY:2
        int blockSize = 2;
        int crossBlockRangeIn = crossBlockRangeOut-4;

        int left = x < crossBlockRangeOut ? crossBlockRangeOut : x > width-crossBlockRangeOut-1 ? width-crossBlockRangeOut-1 : x;
        int top = y < crossBlockRangeOut ? crossBlockRangeOut : y > height-crossBlockRangeOut-1 ? height-crossBlockRangeOut-1 : y;
        int cross_x;
        int cross_y;

        if(in == 1){
            for(int j=(-crossBlockRangeOut); j<=crossBlockRangeOut; j++){
                if(abs(top+j-(int)y) < crossBlockRangeIn)
                    continue;
                for(int i=(-crossBlockRangeOut); i<=crossBlockRangeOut; i++){
                    if(abs(left+i-(int)x) < crossBlockRangeIn)
                        continue;
                    if(rsGetElementAt_uchar(PointFrame, left+i, top+j) == 2){
                        rsAtomicInc(crossPointNumber);
                        cross_x = (x+left+i)/2;
                        cross_y = (y+top+j)/2;
                        rsSetElementAt_uchar(CrossPointFrame, 1, cross_x, cross_y);
                        j = crossBlockRangeOut+1;
                            break;
                    }
                }
            }
        }
}

uchar4 __attribute__((kernel)) displayCrossPoint(uchar in, uint32_t x, uint32_t y)
{
    uchar4 result;

    if(in == 1){
        result.r = 255;     result.g = 255;   result.b = 255;     result.a = 0;
    }else {
        result.r = 0;     result.g = 0;   result.b = 0;     result.a = 0;
    }

    return result;
}

void __attribute__((kernel)) findCrossPoint(uchar in, uint32_t x, uint32_t y){
    if(in == 1){
        rsAtomicMin(crossPointLeft, x);
        rsAtomicMax(crossPointRight, x);
        rsAtomicMin(crossPointTop, y);
        rsAtomicMax(crossPointBottom, y);
    }
}
