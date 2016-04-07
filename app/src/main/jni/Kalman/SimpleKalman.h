#include <iostream>
#include <math.h>

#ifndef SIMPLEKALMAN_H
#define SIMPLEKALMAN_H



class Kalman
{
public:
	
	Kalman(double iq=0,double ir=0)
	{
		x = 0;
		q = iq;
		r = ir;
		
		p = sqrt(q * q + r * r); //p直接取q與r的平方和開根號
	}

	double Update(double value)
	{
		p += q;
		k = p / (p + r); 
		//隨著時間更新時，現在的結果會是上一個結果加上上一個結果和現在量測結果的差再乘以增益的值
		x += k * (value - x);
		//下一個結果預測的偏差值就可以將p乘上1 - k後的值在加上q預測誤差值得到下一個要修正的結果
		p *= (1 - k);
		return x;
	}

	double GetK(){return k;}
	double Getq(){return q;}
	double Getr(){return r;}

private:
	double k; //kalman gain 卡爾曼增益
	double p; //estimation error cvariance  預測誤差
	double q; //process noise cvariance
	double r; //measurement noise covariance 感測器誤差
	double x; //value 紀錄狀態值
};


#endif