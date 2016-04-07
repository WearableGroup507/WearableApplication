/************************************************************************/
/*      source link                                                     */
/*http://gcyrobot.blogspot.tw/2012/12/kalman-filter.html                */
/************************************************************************/

#ifndef __KALMAN_FILTER__
#define __KALMAN_FILTER__

class KalmanFilter
{
public:

	KalmanFilter(double r_measure,double q_angle,double q_bias,double arg_angle)
	{
		Q_angle = q_angle;
		Q_bias = q_bias;
		R_measure = r_measure;
		angle = arg_angle;

		bias = 0;
		P[0][0] = 0;
		P[0][1] = 0;
		P[1][0] = 0;
		P[1][1] = 0;
	}

	/* Accelerometer Angle , Gyro Rate , dt */
	double Update(double acc,double gyro,double dt)
	{
		rate = gyro - bias;
		angle += rate * dt;

		P[0][0] += dt * (dt * P[1][1] - P[0][1] - P[1][0] + Q_angle);
		P[0][1] -= dt * P[1][1];
		P[1][0] -= dt * P[1][1];
		P[1][1] += Q_bias * dt;

		angle_diff = acc - angle;

		S = P[0][0] + R_measure;

		K[0] = P[0][0] / S;
		K[1] = P[1][0] / S;

		angle += K[0] * angle_diff;
		bias += K[1] * angle_diff;

		P[0][0] -= K[0] * P[0][0];
		P[0][1] -= K[0] * P[0][1];
		P[1][0] -= K[1] * P[0][0];
		P[1][1] -= K[1] * P[0][1];

		return angle;
	}

private:

	double R_measure;
	double Q_angle;
	double Q_bias;

	double angle;
	double bias;
	double rate;

	double angle_diff;
	double S;
	double P[2][2];
	double K[2];
};

#endif