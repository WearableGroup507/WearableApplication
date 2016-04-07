package tw.edu.ntust.jojllman.wearableapplication.BLE;

public class GloveSignData
{
	public final static int DOF_GEST = 10;	// DOF of gesture/finger features
	public final static int DOF_QUAT = 4;		// DOF of quaternion
	public final static int DOF_ACCE = 3;		// DOF of acceleration
	public final static int DOF_RSSI = 1;		// DOF of RSSI
	public final static int LEFT = 0;			// left hand
	public final static int RIGHT = 1;		// right hand

	public float[] 	_gestL;					// left hand gesture/finger features
	public float[] 	_quatL;					// left hand quaternion
	public float[] 	_acceL;					// left hand acceleration
	public float   	_rssiL;					// left hand RSSI
	public float[] 	_gestR;					// right hand gesture/finger features
	public float[] 	_quatR;					// right hand quaternion
	public float[] 	_acceR;					// right hand acceleration
	public float   	_rssiR;					// right hand RSSI

	public GloveSignData()
	{
		_gestL = new float[DOF_GEST];
		_quatL = new float[DOF_QUAT];
		_acceL = new float[DOF_ACCE];
		_rssiL = 0.0f;
		_gestR = new float[DOF_GEST];
		_quatR = new float[DOF_QUAT];
		_acceR = new float[DOF_ACCE];
		_rssiR = 0.0f;
	}

	public GloveSignData(float[] gestL, float[] quatL, float[] acceL, float rssiL, float[] gestR, float[] quatR, float[] acceR, float rssiR)
	{
		System.arraycopy(gestL, 0, _gestL, 0, DOF_GEST);
		System.arraycopy(quatL, 0, _quatL, 0, DOF_QUAT);
		System.arraycopy(acceL, 0, _acceL, 0, DOF_ACCE);
		_rssiL = rssiL;
		System.arraycopy(gestR, 0, _gestR, 0, DOF_GEST);
		System.arraycopy(quatR, 0, _quatR, 0, DOF_QUAT);
		System.arraycopy(acceR, 0, _acceR, 0, DOF_ACCE);
		_rssiR = rssiR;
	}
	
	public float[] getL()
	{
		int len = DOF_GEST+DOF_QUAT+DOF_ACCE+DOF_RSSI;
		float[] allL = new float[len];
		
		System.arraycopy(_gestL, 0, allL, 0, DOF_GEST);
		System.arraycopy(_quatL, 0, allL, DOF_GEST, DOF_QUAT);
		System.arraycopy(_acceL, 0, allL, DOF_GEST+DOF_QUAT, DOF_ACCE);
		allL[len - 1] = _rssiL;
		
		return allL;
	}
	
	public float[] getR()
	{
		int len = DOF_GEST+DOF_QUAT+DOF_ACCE+DOF_RSSI;
		float[] allR = new float[len];
		
		System.arraycopy(_gestR, 0, allR, 0, DOF_GEST);
		System.arraycopy(_quatR, 0, allR, DOF_GEST, DOF_QUAT);
		System.arraycopy(_acceR, 0, allR, DOF_GEST+DOF_QUAT, DOF_ACCE);
		allR[len - 1] = _rssiR;
		
		return allR;
	}
	
	public String getCsv()
	{
		String str = _gestL[0] + "," + _gestL[1] + "," + _gestL[2] + "," + _gestL[3] + "," + _gestL[4] + "," + _gestL[5] + "," + _gestL[6] + "," + _gestL[7] + "," + _gestL[8] + "," + _gestL[9] + ","
				   + _quatL[0] + "," + _quatL[1] + "," + _quatL[2] + "," + _quatL[3] + ","
				   + _acceL[0] + "," + _acceL[1] + "," + _acceL[2] + "," + _rssiL + ","
				   + _gestR[0] + "," + _gestR[1] + "," + _gestR[2] + "," + _gestR[3] + "," + _gestR[4] + "," + _gestR[5] + "," + _gestR[6] + "," + _gestR[7] + "," + _gestR[8] + "," + _gestR[9] + ","
			       + _quatR[0] + "," + _quatR[1] + "," + _quatR[2] + "," + _quatR[3] + ","
				   + _acceR[0] + "," + _acceR[1] + "," + _acceR[2] + "," + _rssiR;
		
		return str;
	}
}



