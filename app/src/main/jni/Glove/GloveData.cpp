//------------------------------------------------------------------------------
// Include
#include "Glove/GloveData.h"

//------------------------------------------------------------------------------
// Class Methods
void CGloveData::Init()
{
	Zero();

	m_sampleNum = SAMPLENUM;
	
	m_FingerStartValue[0][0] = 4420,m_FingerProductValue[0][0] = 6310;
	m_FingerStartValue[0][1] = 3830,m_FingerProductValue[0][1] = 5000;
	m_FingerStartValue[0][2] = 4700,m_FingerProductValue[0][2] = 5775;
	m_FingerStartValue[0][3] = 4430,m_FingerProductValue[0][3] = 6305;
	m_FingerStartValue[0][4] = 4130,m_FingerProductValue[0][4] = 6460;
	m_FingerStartValue[0][5] = 4800,m_FingerProductValue[0][5] = 6750;
	m_FingerStartValue[0][6] = 4885,m_FingerProductValue[0][6] = 7420;
	m_FingerStartValue[0][7] = 5400,m_FingerProductValue[0][7] = 7200;
	m_FingerStartValue[0][8] = 4870,m_FingerProductValue[0][8] = 7850;
	m_FingerStartValue[0][9] = 5180,m_FingerProductValue[0][9] = 7380;


	m_FingerStartValue[1][0] = 4340,m_FingerProductValue[1][0] = 6730;
	m_FingerStartValue[1][1] = 4600,m_FingerProductValue[1][1] = 6890;		
	m_FingerStartValue[1][2] = 4600,m_FingerProductValue[1][2] = 7150;
	m_FingerStartValue[1][3] = 5120,m_FingerProductValue[1][3] = 7430;		
	m_FingerStartValue[1][4] = 4350,m_FingerProductValue[1][4] = 6650;
	m_FingerStartValue[1][5] = 4860,m_FingerProductValue[1][5] = 7415;		
	m_FingerStartValue[1][6] = 4352,m_FingerProductValue[1][6] = 7168;
	m_FingerStartValue[1][7] = 4352,m_FingerProductValue[1][7] = 5632;		
	m_FingerStartValue[1][8] = 3328,m_FingerProductValue[1][8] = 4102;
	m_FingerStartValue[1][9] = 5120,m_FingerProductValue[1][9] = 5888;
}

void CGloveData::SetRawData(float flex[NUM_FLEX],float gyro[NUM_GYRO],float acc[NUM_ACCE])
{
	for(int i = 0 ; i < NUM_FLEX ; i++)
		m_fFlexSensor[i] = flex[i];
	for(int i = 0 ; i < NUM_GYRO ; i++)
		m_fGyroSensor[i] = gyro[i];
	for(int i = 0 ; i < NUM_ACCE ; i++)
		m_fAccSensor[i]  = acc[i];
}

void CGloveData::Zero()
{
	ZeroGyro();
	ZeroFlex();
	ZeroAcc();
	ZeroRotationAngle();
	ZeroFingerAngle();
	ZeroPosition();
    ZeroRssi();
    /// other features add below

	ResetCalibration();
	m_accRate[0] = m_accRate[0] = 0;  m_accRate[2] = -1;
}

void CGloveData::ZeroFlex(){
	for (int i = 0; i < NUM_FLEX; i++)
		this->m_fFlexSensor[i] = 0.0f;
}

void CGloveData::ZeroFingerAngle()
{
	for (int i = 0; i < NUM_FLEX; i++)
		this->m_fFingerAngle[i] = 0.0f;
}

void CGloveData::ZeroGyro()
{
	for (int i = 0; i < NUM_GYRO; i++)
		this->m_fGyroSensor[i] = 0.0f;
}

void CGloveData::ZeroRotationAngle()
{
	for (int i = 0; i < NUM_GYRO; i++)
		this->m_fGyroRate[i] = 0.0f;
	m_qRotation = Quaternion(1,0,0,0);
}

void CGloveData::ZeroAcc()
{
	for (int i = 0; i < NUM_ACCE; i++)
		this->m_fAccSensor[i] = 0.0f;
}

void CGloveData::ZeroPosition()
{
	for (int i = 0; i < NUM_ACCE; i++)
		this->m_fPosition[i] = 0.0f;
}

void CGloveData::ZeroRssi()
{
    m_fRssi = 0.0f;
}

void CGloveData::Raw2Rate(){
	//sample the raw data for m_sampleNum times
	if(m_connectTime < m_sampleNum){
		m_connectTime++;

		m_gyroDcOffset[0] += m_fGyroSensor[0];
		m_gyroDcOffset[1] += m_fGyroSensor[1];
		m_gyroDcOffset[2] += m_fGyroSensor[2];

		m_accDcOffset[0] += m_fAccSensor[0];
		m_accDcOffset[1] += m_fAccSensor[1];
		m_accDcOffset[2] += m_fAccSensor[2];
	}
	else{

		if(!m_IsCalibrate)    //calculate the zero offset of the raw data
		{
			m_IsCalibrate = true;

			m_gyroDcOffset[0] /= m_sampleNum;
			m_gyroDcOffset[1] /= m_sampleNum;
			m_gyroDcOffset[2] /= m_sampleNum;

			m_accDcOffset[0] /= m_sampleNum;
			m_accDcOffset[1] /= m_sampleNum;
			m_accDcOffset[2] /= m_sampleNum;
		}
		else  // convert raw data
		{
			//Gyroscope the rate of angle (deg/s)
			for(int i = 0 ; i < NUM_GYRO ; i++)				
			{
				//raw data subtract the zero offset
				int DeltaRate = ((int)m_fGyroSensor[i] - m_gyroDcOffset[i]);

				//filter out the value between +/- 1000
				if(DeltaRate<1000 && DeltaRate > -1000)
					DeltaRate = 0;

				//raw value * sensitivity /1000
				//sensitivity value is from data sheet
				m_AngleRate[i] =  DeltaRate* m_gyroSentivity;

				///the range of angle rate is +/-250
				if(m_AngleRate[i] > 0 && m_AngleRate[i] > 250)
					m_AngleRate[i] = 250;
				else if(m_AngleRate[i] < 0 && m_AngleRate[i] < -250)
					m_AngleRate[i] = -250;
			}	


			for(int i = 0 ; i < NUM_ACCE ; i++)				
			{
				//convert the z value
				if(i==2)
				{
					//raw data subtract the zero offset and invert the  negative value to position value
					int DeltaZg = (((int)m_fAccSensor[i] - m_accDcOffset[i]-16384)) ;
					//convert to gravity value
					m_accRate[i] = (double)DeltaZg/16384;
				}
				//convert the x,y value
				else
				{	
					//raw data subtract the zero offset
					int Deltag = (((int)m_fAccSensor[i] - m_accDcOffset[i])) ;
					//convert to gravity value
					m_accRate[i] = (double)Deltag/16384;
				}

				//the range of gravity value is +/-2
				if(m_accRate[i] > 0 && m_accRate[i] > 2)
					m_accRate[i] = 2;
				else if(m_accRate[i] < 0 && m_accRate[i] < -2)
					m_accRate[i] = -2;
			}
		}
	}
}

void CGloveData::ResetData()
{
	ZeroGyro();
	ZeroFlex();
	ZeroAcc();
	ZeroRotationAngle();
	ZeroFingerAngle();
	ZeroPosition();
    ZeroRssi();
    /// other features add below
}

void CGloveData::ResetCalibration(){
	m_IsCalibrate = false;
	m_gyroDcOffset[0] =	m_gyroDcOffset[1] = m_gyroDcOffset[2] = 0;
	m_accDcOffset[0] = m_accDcOffset[1] = m_accDcOffset[2] = 0;
}
//------------------------------------------------------------------------------
// End of file
