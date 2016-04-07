#include "AHRS.h"

AHRS::AHRS(float samplePeriod)
{	
	m_samplePeriod = samplePeriod;
	Reset();	
}

void AHRS::Reset()
{
	//inertial navigation
	m_kp = 5.0f;
	m_ki = 0.002f;

	//m_Quaternion     = Quaternion(0.7,0.7,0,0);
	m_Quaternion     = Quaternion(0.7,0.7,0,0);
	m_integralFB     = Vector3::ZERO;

	//position system
	alpha            = 0.9;
	m_RotationMatrix = Matrix3::IDENTITY;
	m_Position       = Vector3::ZERO;
	m_linPosition    = Vector3::ZERO;
	m_linAcc         = Vector3::ZERO;

	G = Vector3(0,1,0);
	m_eulerAngle = Vector3(90,0,0);

	/*butterworth filter initialize*/
	m_pVelocityXHP = new cButterworthFilter(1, m_samplePeriod, 0.1f);
	m_pVelocityYHP = new cButterworthFilter(1, m_samplePeriod, 0.1f);
	m_pVelocityZHP = new cButterworthFilter(1, m_samplePeriod, 0.1f);

	m_pPositionXHP = new cButterworthFilter(1, m_samplePeriod, 0.1f);
	m_pPositionYHP = new cButterworthFilter(1, m_samplePeriod, 0.1f);
	m_pPositionZHP = new cButterworthFilter(1, m_samplePeriod, 0.1f);

	m_gravityDirectionTrues = Vector3::ZERO;
	m_gravityDirection      = Vector3::ZERO;

}

void AHRS::MahonyAHRSupdateIMU(Vector3 gyro,Vector3 acc)
{

	Quaternion q(m_Quaternion.w,m_Quaternion.x,m_Quaternion.y,m_Quaternion.z);

	// Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
	{	
		
		// Normalize measurement
		acc.normalise();
		m_gravityDirectionTrues = acc;

		// Estimated direction of gravity and vector perpendicular to magnetic flux
		Vector3 v(2*(q.x*q.z - q.w*q.y),
				  2*(q.w*q.x + q.y*q.z),
 				  q.w*q.w - q.x*q.x - q.y*q.y + q.z*q.z);
		m_gravityDirection = v;

		// Error is sum of cross product between estimated and measured direction of gravity
		Vector3 error(acc.y * v.z - acc.z * v.y,
			          acc.z * v.x - acc.x * v.z,
			          acc.x * v.y - acc.y * v.x);
				
		// Compute and apply integral feedback
		m_integralFB = m_integralFB + error*m_samplePeriod;

		// Apply feedback terms
		Vector3 Ref = gyro + m_kp * error + m_ki * m_integralFB;

		// Compute rate of change of quaternion
		Vector4 pDot = 0.5f * QuaternProd(Vector4(q.x,q.y,q.z,q.w), Vector4(Ref.x, Ref.y, Ref.z,0.0f));// Compute rate of change of quaternion

		// Itergrate rate of change of quaternion
		q = Quaternion(q.w + pDot.w*m_samplePeriod,
					   q.x + pDot.x*m_samplePeriod,
			           q.y + pDot.y*m_samplePeriod,
			           q.z + pDot.z*m_samplePeriod);

		//normalize quaternion
		float Qnorm = sqrt(q.Norm());
		m_Quaternion = 	Quaternion(q.w/(Qnorm),q.x/(Qnorm),q.y/(Qnorm),q.z/(Qnorm));

// 		std::cout << "m_Quaternion : " << m_Quaternion << "\n";
// 		std::cout << "Euler : "      << m_Quaternion.getPitch(false).valueDegrees() << " " << m_Quaternion.getYaw(false).valueDegrees() << " " << m_Quaternion.getRoll(false).valueDegrees() << "\n";
// 		std::cout << "-------------------------------\n";
	}
}

void AHRS::AHRSupdatePostion(Vector3 gyro,Vector3 acc)
{	
	//Sensor Coordinate to Render Coordinate
	/*Render coordinate 0329*/
	Vector3 Oriacc =  Vector3((-acc.y),(-acc.z),(acc.x));
	gyro = Vector3(DownAccuracy(-gyro.y),DownAccuracy(-gyro.z),DownAccuracy(gyro.x));
	acc =  Vector3(DownAccuracy(-acc.y),DownAccuracy(-acc.z),DownAccuracy(acc.x));
	//std::cout << "AHRS acc rate: ("  << acc.x << "," << acc.y << "," << acc.z << ")\n" ;
	//std::cout << "gryo rate: ("  << gyro.x << "," << gyro.y << "," << gyro.z << ")\n" ;

	if(acc==Vector3::ZERO)  return;		
	MahonyAHRSupdateIMU(gyro,acc);   //Calculate Quaternion by gyroscope value and acc value

	Matrix3 m_QR;
	m_Quaternion.ToRotationMatrix(m_QR);
	m_QR.Transpose();
	Vector3 tcAcc = m_QR * acc;
	Vector3 linAcc = tcAcc - Vector3(0,0,1);
	if(linAcc.x < 0.05 && linAcc.x > -0.05) linAcc.x = 0;
	if(linAcc.y < 0.05 && linAcc.y > -0.05) linAcc.y = 0;
	if(linAcc.z < 0.05 && linAcc.z > -0.05) linAcc.z = 0;
 	if(linAcc.x*linAcc.y*linAcc.z<0.001 && linAcc.x*linAcc.y*linAcc.z > 0.001) linAcc=Vector3::ZERO;
// 
 	if(linAcc.x!=0  || linAcc.y!=0 || linAcc.z!=0)
	{
		//android version
		G = alpha * G + (1-alpha) * Oriacc;

		// -------------------------------------------------------------------
		// Calculate linear acceleration in Earth frame (subtracting gravity)
		m_linAcc = Oriacc-G;
		m_linAcc = Vector3(m_linAcc.x*(-1),m_linAcc.y*(-1),m_linAcc.z);
		if(m_linAcc.x < 0.03 && m_linAcc.x > -0.03) m_linAcc.x = 0;
		if(m_linAcc.y < 0.03 && m_linAcc.y > -0.03) m_linAcc.y = 0;
		if(m_linAcc.z < 0.03 && m_linAcc.z > -0.03) m_linAcc.z = 0;

		// -------------------------------------------------------------------
		// Calculate linear position (integrate velocity)
		//std::cout <<"Is Moving\n";
		m_linPosition = m_linPosition + ((m_linAcc*9.81)* m_samplePeriod*m_samplePeriod*0.5)*100;
		m_linPositionHP = Vector3(m_pPositionXHP->GetResult(m_linPosition.x),
			m_pPositionYHP->GetResult(m_linPosition.y),
			m_pPositionZHP->GetResult(m_linPosition.z));
		m_Position = m_linPosition;
	}	
}

Vector4 AHRS::QuaternProd (Vector4 quat, Vector4 refer)
{
	Vector4 result(quat.w * refer.x + quat.x * refer.w + quat.y * refer.z - quat.z * refer.y,
		quat.w * refer.y - quat.x * refer.z + quat.y * refer.w + quat.z * refer.x,
		quat.w * refer.z + quat.x * refer.y - quat.y * refer.x + quat.z * refer.w,
 		quat.w * refer.w - quat.x * refer.x - quat.y * refer.y - quat.z * refer.z);
	
	return result;
}

void AHRS::Quatern2Matrix()
{
	Matrix3 Q2M(2* m_Quaternion.w*m_Quaternion.w - 1 + 2*m_Quaternion.x*m_Quaternion.x,
				2*(m_Quaternion.x*m_Quaternion.y + m_Quaternion.w*m_Quaternion.z)	  ,
				2*(m_Quaternion.x*m_Quaternion.z - m_Quaternion.w*m_Quaternion.y)	  ,
				2*(m_Quaternion.x*m_Quaternion.y - m_Quaternion.w*m_Quaternion.z)	  ,
				2* m_Quaternion.w*m_Quaternion.w - 1 + 2*m_Quaternion.y*m_Quaternion.y,
				2*(m_Quaternion.y*m_Quaternion.z + m_Quaternion.w*m_Quaternion.x)	  ,
				2*(m_Quaternion.x*m_Quaternion.z + m_Quaternion.w*m_Quaternion.y)	  ,
				2*(m_Quaternion.y*m_Quaternion.z - m_Quaternion.w*m_Quaternion.x)	  ,
				2* m_Quaternion.w*m_Quaternion.w - 1 + 2*m_Quaternion.z*m_Quaternion.z);
	//std::cout << "Rotation Matrix " << Q2M << "\n";

	m_RotationMatrix = Q2M.Transpose();
//	std::cout << "Rotation Matrix' " << m_RotationMatrix << "\n";
}

double AHRS::DownAccuracy (double d)
{
	return (double)(int)(d*1000)/1000;
}

Quaternion AHRS::GetOritation()
{
	return m_Quaternion;
}

Vector3 AHRS::GetlinPostion()
{
	return m_Position;
}

