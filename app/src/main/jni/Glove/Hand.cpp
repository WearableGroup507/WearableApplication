#include "Glove/Hand.h"

CHand::CHand(){
	Load_Model();
}

void CHand::Load_Model()
{
	Load_RightHand_Model();
	Load_LeftHand_Model();
}

void CHand::Load_RightHand_Model()
{
	//Right hand
	/*****Palm*****/
	{
		rBone_PalmModel = glmReadOBJ("Hand Bone/RHand_Palm.obj");
		glmScale(rBone_PalmModel,0.3);
		glmFacetNormals(rBone_PalmModel);
		glmVertexNormals(rBone_PalmModel, 90);
	}


	/*****Pinky*****/
	{
		rBone_Pink_MCP = glmReadOBJ("Hand Bone/Pink_MCP.obj");
		glmScale(rBone_Pink_MCP,0.3);
		glmFacetNormals(rBone_Pink_MCP);
		glmVertexNormals(rBone_Pink_MCP, 90);

		rBone_Pink_PIP = glmReadOBJ("Hand Bone/Pink_PIP.obj");
		glmScale(rBone_Pink_PIP,0.3);
		glmFacetNormals(rBone_Pink_PIP);
		glmVertexNormals(rBone_Pink_PIP, 90);

		rBone_Pink_DIP = glmReadOBJ("Hand Bone/Pink_DIP.obj");
		glmScale(rBone_Pink_DIP,0.3);
		glmFacetNormals(rBone_Pink_DIP);
		glmVertexNormals(rBone_Pink_DIP, 90);
	}

	/*****Ring*****/
	{
		rBone_Ring_MCP = glmReadOBJ("Hand Bone/Ring_MCP.obj");
		glmScale(rBone_Ring_MCP,0.3);
		glmFacetNormals(rBone_Ring_MCP);
		glmVertexNormals(rBone_Ring_MCP, 90);

		rBone_Ring_PIP = glmReadOBJ("Hand Bone/Ring_PIP.obj");
		glmScale(rBone_Ring_PIP,0.3);
		glmFacetNormals(rBone_Ring_PIP);
		glmVertexNormals(rBone_Ring_PIP, 90);

		rBone_Ring_DIP = glmReadOBJ("Hand Bone/Ring_DIP.obj");
		glmScale(rBone_Ring_DIP,0.3);
		glmFacetNormals(rBone_Ring_DIP);
		glmVertexNormals(rBone_Ring_DIP, 90);
	}


	/*****Middle*****/
	{
		rBone_Middle_MCP = glmReadOBJ("Hand Bone/Middle_MCP.obj");
		glmScale(rBone_Middle_MCP,0.3);
		glmFacetNormals(rBone_Middle_MCP);
		glmVertexNormals(rBone_Middle_MCP, 90);

		rBone_Middle_PIP = glmReadOBJ("Hand Bone/Middle_PIP.obj");
		glmScale(rBone_Middle_PIP,0.3);
		glmFacetNormals(rBone_Middle_PIP);
		glmVertexNormals(rBone_Middle_PIP, 90);

		rBone_Middle_DIP = glmReadOBJ("Hand Bone/Middle_DIP.obj");
		glmScale(rBone_Middle_DIP,0.3);
		glmFacetNormals(rBone_Middle_DIP);
		glmVertexNormals(rBone_Middle_DIP, 90);
	}

	/*****Index*****/
	{
		rBone_Index_MCP = glmReadOBJ("Hand Bone/Index_MCP.obj");
		glmScale(rBone_Index_MCP,0.3);
		glmFacetNormals(rBone_Index_MCP);
		glmVertexNormals(rBone_Index_MCP, 90);

		rBone_Index_PIP = glmReadOBJ("Hand Bone/Index_PIP.obj");
		glmScale(rBone_Index_PIP,0.3);
		glmFacetNormals(rBone_Index_PIP);
		glmVertexNormals(rBone_Index_PIP, 90);

		rBone_Index_DIP = glmReadOBJ("Hand Bone/Index_DIP.obj");
		glmScale(rBone_Index_DIP,0.3);
		glmFacetNormals(rBone_Index_DIP);
		glmVertexNormals(rBone_Index_DIP, 90);
	}

	/*****Thumb*****/
	{
		rBone_Thumb_MCP = glmReadOBJ("Hand Bone/Thumb_MCP.obj");
		glmScale(rBone_Thumb_MCP,0.3);
		glmFacetNormals(rBone_Thumb_MCP);
		glmVertexNormals(rBone_Thumb_MCP, 90);

		rBone_Thumb_PIP = glmReadOBJ("Hand Bone/Thumb_PIP.obj");
		glmScale(rBone_Thumb_PIP,0.3);
		glmFacetNormals(rBone_Thumb_PIP);
		glmVertexNormals(rBone_Thumb_PIP, 90);

		rBone_Thumb_DIP = glmReadOBJ("Hand Bone/Thumb_DIP.obj");
		glmScale(rBone_Thumb_DIP,0.3);
		glmFacetNormals(rBone_Thumb_DIP);
		glmVertexNormals(rBone_Thumb_DIP, 90);
	}
}

void CHand::Load_LeftHand_Model()
{
	/*****Palm*****/
	lBone_PalmModel = glmReadOBJ("Hand Bone/LHand_Palm.obj");
	glmScale(lBone_PalmModel,0.3);
	glmFacetNormals(lBone_PalmModel);
	glmVertexNormals(lBone_PalmModel, 90);

	/*****Pinky*****/
	{
		lBone_Pink_MCP = glmReadOBJ("Hand Bone/Pink_MCP_L.obj");
		glmScale(lBone_Pink_MCP,0.3);
		glmFacetNormals(lBone_Pink_MCP);
		glmVertexNormals(lBone_Pink_MCP, 90);

		lBone_Pink_PIP = glmReadOBJ("Hand Bone/Pink_PIP_L.obj");
		glmScale(lBone_Pink_PIP,0.3);
		glmFacetNormals(lBone_Pink_PIP);
		glmVertexNormals(lBone_Pink_PIP, 90);

		lBone_Pink_DIP = glmReadOBJ("Hand Bone/Pink_DIP_L.obj");
		glmScale(lBone_Pink_DIP,0.3);
		glmFacetNormals(lBone_Pink_DIP);
		glmVertexNormals(lBone_Pink_DIP, 90);
	}

	/*****Ring*****/
	{
		lBone_Ring_MCP = glmReadOBJ("Hand Bone/Ring_MCP_L.obj");
		glmScale(lBone_Ring_MCP,0.3);
		glmFacetNormals(lBone_Ring_MCP);
		glmVertexNormals(lBone_Ring_MCP, 90);

		lBone_Ring_PIP = glmReadOBJ("Hand Bone/Ring_PIP_L.obj");
		glmScale(lBone_Ring_PIP,0.3);
		glmFacetNormals(lBone_Ring_PIP);
		glmVertexNormals(lBone_Ring_PIP, 90);

		lBone_Ring_DIP = glmReadOBJ("Hand Bone/Ring_DIP_L.obj");
		glmScale(lBone_Ring_DIP,0.3);
		glmFacetNormals(lBone_Ring_DIP);
		glmVertexNormals(lBone_Ring_DIP, 90);
	}


	/*****Middle*****/
	{
		lBone_Middle_MCP = glmReadOBJ("Hand Bone/Middle_MCP_L.obj");
		glmScale(lBone_Middle_MCP,0.3);
		glmFacetNormals(lBone_Middle_MCP);
		glmVertexNormals(lBone_Middle_MCP, 90);

		lBone_Middle_PIP = glmReadOBJ("Hand Bone/Middle_PIP_L.obj");
		glmScale(lBone_Middle_PIP,0.3);
		glmFacetNormals(lBone_Middle_PIP);
		glmVertexNormals(lBone_Middle_PIP, 90);

		lBone_Middle_DIP = glmReadOBJ("Hand Bone/Middle_DIP_L.obj");
		glmScale(lBone_Middle_DIP,0.3);
		glmFacetNormals(lBone_Middle_DIP);
		glmVertexNormals(lBone_Middle_DIP, 90);
	}

	/*****Index*****/
	{
		lBone_Index_MCP = glmReadOBJ("Hand Bone/Index_MCP_L.obj");
		glmScale(lBone_Index_MCP,0.3);
		glmFacetNormals(lBone_Index_MCP);
		glmVertexNormals(lBone_Index_MCP, 90);

		lBone_Index_PIP = glmReadOBJ("Hand Bone/Index_PIP_L.obj");
		glmScale(lBone_Index_PIP,0.3);
		glmFacetNormals(lBone_Index_PIP);
		glmVertexNormals(lBone_Index_PIP, 90);

		lBone_Index_DIP = glmReadOBJ("Hand Bone/Index_DIP_L.obj");
		glmScale(lBone_Index_DIP,0.3);
		glmFacetNormals(lBone_Index_DIP);
		glmVertexNormals(lBone_Index_DIP, 90);
	}

	/*****Thumb*****/
	{
		lBone_Thumb_MCP = glmReadOBJ("Hand Bone/Thumb_MCP_L.obj");
		glmScale(lBone_Thumb_MCP,0.3);
		glmFacetNormals(lBone_Thumb_MCP);
		glmVertexNormals(lBone_Thumb_MCP, 90);

		lBone_Thumb_PIP = glmReadOBJ("Hand Bone/Thumb_PIP_L.obj");
		glmScale(lBone_Thumb_PIP,0.3);
		glmFacetNormals(lBone_Thumb_PIP);
		glmVertexNormals(lBone_Thumb_PIP, 90);

		lBone_Thumb_DIP = glmReadOBJ("Hand Bone/Thumb_DIP_L.obj");
		glmScale(lBone_Thumb_DIP,0.3);
		glmFacetNormals(lBone_Thumb_DIP);
		glmVertexNormals(lBone_Thumb_DIP, 90);
	}
}

void CHand::Draw_Hand()
{ 
	Draw_RightHand();
	Draw_LeftHand();
}

/*Right Hand*/
void CHand::Draw_RightHand()
{
	//////////////////////////////////////////////////////////////////////////
	////    Right Hand 
	////
	glPushMatrix();		

	//glRotatef(2.0f * acos(m_hand_orientation->w)*180.0f/3.1415 , -m_hand_orientation->y ,-m_hand_orientation->z,-m_hand_orientation->x);
	
	glTranslatef(-0.6,0.1,-0.2);
		Radian theta;
		Vector3 rotationAxix;
		m_rightHandTrans.m_qRotation.ToAngleAxis(theta,rotationAxix);
		glRotatef(-90,1,0,0);
		glRotatef(theta.valueDegrees(),rotationAxix.x,rotationAxix.y,rotationAxix.z);
	glTranslatef(0.6,-0.1,0.2);

	glTranslatef(m_rightHandTrans.m_fPosition.x,m_rightHandTrans.m_fPosition.y,-m_rightHandTrans.m_fPosition.z);
	glmDraw(rBone_PalmModel, GLM_MATERIAL | GLM_SMOOTH);
	Draw_Thumb (0                                   , -m_rightHandTrans.m_fFingerAngle[9]  ,-m_rightHandTrans.m_fFingerAngle[8]);
	Draw_Index (m_rightHandTrans.m_fFingerAngle[7]  , m_rightHandTrans.m_fFingerAngle[6]  ,m_rightHandTrans.m_fFingerAngle[6]);
	Draw_Middle(m_rightHandTrans.m_fFingerAngle[5]  , m_rightHandTrans.m_fFingerAngle[4]  ,m_rightHandTrans.m_fFingerAngle[4]);
	Draw_Ring  (m_rightHandTrans.m_fFingerAngle[3]  , m_rightHandTrans.m_fFingerAngle[2]  ,m_rightHandTrans.m_fFingerAngle[2]);
	Draw_Pink  (m_rightHandTrans.m_fFingerAngle[1]  , m_rightHandTrans.m_fFingerAngle[0]  ,m_rightHandTrans.m_fFingerAngle[0]);	
 	glPopMatrix();
}

void CHand::Draw_Pink  (GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate)
{
	glPushMatrix();

	glTranslatef(0,0,-0.05);
	glRotatef(MCP_rotate,-1,0, 0);
	glTranslatef(0,0,0.05);

	glPushMatrix();  
	glmDraw(rBone_Pink_MCP  , GLM_MATERIAL | GLM_SMOOTH);
	glPopMatrix();	

	//-----------------------------
	glTranslatef(0,-0.07,0.25);
	glRotatef(PIP_rotate,-1,0, 0);
	glTranslatef(0,0.07,-0.25);

	glmDraw(rBone_Pink_PIP  , GLM_MATERIAL | GLM_SMOOTH);

	//-----------------------------
	glTranslatef(0,-0.11,0.41);
	glRotatef(DIP_rotate,-1,0, 0);
	glTranslatef(0,0.11,-0.41);

	glmDraw(rBone_Pink_DIP  , GLM_MATERIAL | GLM_SMOOTH);

	glPopMatrix();
}

void CHand::Draw_Ring  (GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate)
{
	glPushMatrix();

	glTranslatef(0,-0.04,0.08);
	glRotatef(MCP_rotate,-1,0, 0);
	glTranslatef(0,0.04,-0.08);

	glPushMatrix();  
	glmDraw(rBone_Ring_MCP  , GLM_MATERIAL | GLM_SMOOTH);
	glPopMatrix();	

	//-----------------------------
	glTranslatef(0,-0.1,0.42);
	glRotatef(PIP_rotate,-1,0, 0);
	glTranslatef(0,0.1,-0.42);

	glmDraw(rBone_Ring_PIP  , GLM_MATERIAL | GLM_SMOOTH);

	//-----------------------------
	glTranslatef(0,-0.15 ,0.64);
	glRotatef(DIP_rotate,-1,0, 0);
	glTranslatef(0,0.15,-0.64);

	glmDraw(rBone_Ring_DIP  , GLM_MATERIAL | GLM_SMOOTH);

	glPopMatrix();
}

void CHand::Draw_Middle(GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate)
{
	glPushMatrix();

	glTranslatef(0,-0.01,0.1);
	glRotatef(MCP_rotate,-1,0, 0);
	glTranslatef(0,0.01,-0.1);

	glPushMatrix();  
	glmDraw(rBone_Middle_MCP  , GLM_MATERIAL | GLM_SMOOTH);
	glPopMatrix();	

	//-----------------------------
	glTranslatef(0,-0.08,0.53);
	glRotatef(PIP_rotate,-1,0, 0);
	glTranslatef(0,0.08,-0.53);

	glmDraw(rBone_Middle_PIP  , GLM_MATERIAL | GLM_SMOOTH);

	//-----------------------------
	glTranslatef(0,-0.14 ,0.76);
	glRotatef(DIP_rotate,-1,0, 0);
	glTranslatef(0,0.14,-0.76);

	glmDraw(rBone_Middle_DIP  , GLM_MATERIAL | GLM_SMOOTH);

	glPopMatrix();
}

void CHand::Draw_Index (GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate)
{
	glPushMatrix();

	glTranslatef(0,-0.01,0.1);
	glRotatef(MCP_rotate,-1,0, 0);
	glTranslatef(0,0.01,-0.1);

	glPushMatrix();  
	glmDraw(rBone_Index_MCP  , GLM_MATERIAL | GLM_SMOOTH);
	glPopMatrix();	

	//-----------------------------
	glTranslatef(0,-0.07,0.47);
	glRotatef(PIP_rotate,-1,0, 0);
	glTranslatef(0,0.07,-0.47);

	glmDraw(rBone_Index_PIP  , GLM_MATERIAL | GLM_SMOOTH);

	//-----------------------------
	glTranslatef(0,-0.12 ,0.68);
	glRotatef(DIP_rotate,-1,0, 0);
	glTranslatef(0,0.12,-0.68);

	glmDraw(rBone_Index_DIP  , GLM_MATERIAL | GLM_SMOOTH);

	glPopMatrix();
}

void CHand::Draw_Thumb (GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate)
{
	glPushMatrix();

	glTranslatef(-0.4,0.05,-0.6);
	glRotatef(MCP_rotate,0.5,-1,-0.5);
	glTranslatef(0.4,-0.05,0.6);

	glPushMatrix();  
	glmDraw(rBone_Thumb_MCP  , GLM_MATERIAL | GLM_SMOOTH);
	glPopMatrix();	

	//-----------------------------
	glTranslatef(-0.3,-0.08,-0.2);
	glRotatef(PIP_rotate,0.5,-1,-0.5);
	glTranslatef(0.3,0.08,0.2);

	glmDraw(rBone_Thumb_PIP  , GLM_MATERIAL | GLM_SMOOTH);

	//-----------------------------
	glTranslatef(-0.23,-0.14,0.07);
	glRotatef(DIP_rotate,0.5,-1,-0.5);
	glTranslatef(0.23,0.14,-0.07);

	glmDraw(rBone_Thumb_DIP  , GLM_MATERIAL | GLM_SMOOTH);

	glPopMatrix();
}


/*Left Hand*/
void CHand::Draw_LeftHand()
{
	//////////////////////////////////////////////////////////////////////////
	////    Left Hand 
	////
		
// 	glTranslatef(0.6,-0.1,0.2);
// 
// 	//第三版四元數旋轉
// 	Quaternion tmpLX(Radian(-(Math::PI/180)*m_right_gloveData._gyro[1]),m_left_hand_orientation->xAxis());
// 	Quaternion tmpLY(Radian(-(Math::PI/180)*m_right_gloveData._gyro[2]),m_left_hand_orientation->yAxis());
// 	Quaternion tmpLZ(Radian(-(Math::PI/180)*m_right_gloveData._gyro[0]),m_left_hand_orientation->zAxis());
// 	//將四元數右乘上要旋轉的角度和軸構成的四元數就完成旋轉
// 	*m_left_hand_orientation = *m_left_hand_orientation * tmpLX * tmpLY * tmpLZ;
// 
// 	//四元數轉旋轉矩陣
// 	Matrix3 m_LMatrix;
// 	(*m_left_hand_orientation).ToRotationMatrix(m_LMatrix);
// 	GLfloat mL[  ] = {0.0f,0.0f,0.0f,0.0f,
// 		0.0f,0.0f,0.0f,0.0f,
// 		0.0f,0.0f,0.0f,0.0f,
// 		0.0f,0.0f,0.0f,1.0f,
// 	};
// 	for(int i = 0;i < 3 ; i++)
// 	{
// 		for(int j = 0 ; j< 3 ; j++)
// 		{
// 			mL[i*4 + j] = m_LMatrix[i][j];
// 		}
// 	}
// 	glMultMatrixf(mL);
// 
// 	glTranslatef(-0.6,0.1,-0.2);
	//glRotatef(2.0f * acos(m_leftHandTrans.m_qRotation.w)*180.0f/Math::PI , m_leftHandTrans.m_qRotation.x ,m_leftHandTrans.m_qRotation.y,m_leftHandTrans.m_qRotation.z);
	

	glPushMatrix();	

	glTranslatef(0.6,-0.1,0.2);
		Radian theta;
		Vector3 rotationAxix;
		m_leftHandTrans.m_qRotation.ToAngleAxis(theta,rotationAxix);
		glRotatef(-90,1,0,0);
		glRotatef(theta.valueDegrees(),rotationAxix.x,rotationAxix.y,rotationAxix.z);
	glTranslatef(-0.6,0.1,-0.2);
	

	glTranslatef(m_leftHandTrans.m_fPosition.x,m_leftHandTrans.m_fPosition.y,-m_leftHandTrans.m_fPosition.z);
	glmDraw(lBone_PalmModel, GLM_MATERIAL | GLM_SMOOTH);
	Draw_Left_Pink  (m_leftHandTrans.m_fFingerAngle[9]  , m_leftHandTrans.m_fFingerAngle[8]  ,m_leftHandTrans.m_fFingerAngle[8]);		
	Draw_Left_Ring  (m_leftHandTrans.m_fFingerAngle[7]  , m_leftHandTrans.m_fFingerAngle[6]  ,m_leftHandTrans.m_fFingerAngle[6]);
	Draw_Left_Middle(m_leftHandTrans.m_fFingerAngle[5]  , m_leftHandTrans.m_fFingerAngle[4]  ,m_leftHandTrans.m_fFingerAngle[4]);
	Draw_Left_Index (m_leftHandTrans.m_fFingerAngle[3]  , m_leftHandTrans.m_fFingerAngle[2]  ,m_leftHandTrans.m_fFingerAngle[2]);
	Draw_Left_Thumb (0                                   ,  -m_leftHandTrans.m_fFingerAngle[1]  , -m_leftHandTrans.m_fFingerAngle[0]);	

	glPopMatrix();		
}

void CHand::Draw_Left_Pink (GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate)
{	
	glPushMatrix();

	glTranslatef(0,0,-0.05);
	glRotatef(MCP_rotate,-1,0,0);
	glTranslatef(0,0,0.05);

	glmDraw(lBone_Pink_MCP  , GLM_MATERIAL | GLM_SMOOTH);

	//-----------------------------
	glTranslatef(0,-0.04,0.23);
	glRotatef(PIP_rotate,-1,0,0);
	glTranslatef(0,0.04,-0.23);

	glmDraw(lBone_Pink_PIP  , GLM_MATERIAL | GLM_SMOOTH);

	//-----------------------------
	glTranslatef(0,-0.07,0.36);
	glRotatef(DIP_rotate,-1,0, 0);
	glTranslatef(0,0.07,-0.36);

	glmDraw(lBone_Pink_DIP  , GLM_MATERIAL | GLM_SMOOTH);

	glPopMatrix();
}

void CHand::Draw_Left_Ring  (GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate)
{
	glPushMatrix();

	glTranslatef(0,0.04,0.01);
	glRotatef(MCP_rotate,-1,0, 0);
	glTranslatef(0,-0.04,-0.01);

	glPushMatrix();  
	glmDraw(lBone_Ring_MCP  , GLM_MATERIAL | GLM_SMOOTH);
	glPopMatrix();	

	//-----------------------------
	glTranslatef(0,-0.06,0.38);
	glRotatef(PIP_rotate,-1,0, 0);
	glTranslatef(0,0.06,-0.38);

	glmDraw(lBone_Ring_PIP  , GLM_MATERIAL | GLM_SMOOTH);

	//-----------------------------
	glTranslatef(0,-0.12 ,0.61);
	glRotatef(DIP_rotate,-1,0, 0);
	glTranslatef(0,0.12,-0.61);

	glmDraw(lBone_Ring_DIP  , GLM_MATERIAL | GLM_SMOOTH);

	glPopMatrix();
}

void CHand::Draw_Left_Middle(GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate)
{
	glPushMatrix();

	glTranslatef(0,-0.01,0.1);
	glRotatef(MCP_rotate,-1,0, 0);
	glTranslatef(0,0.01,-0.1);

	glPushMatrix();  
	glmDraw(lBone_Middle_MCP  , GLM_MATERIAL | GLM_SMOOTH);
	glPopMatrix();	

	//-----------------------------
	glTranslatef(0,-0.09,0.52);
	glRotatef(PIP_rotate,-1,0, 0);
	glTranslatef(0,0.09,-0.52);

	glmDraw(lBone_Middle_PIP  , GLM_MATERIAL | GLM_SMOOTH);

	//-----------------------------
	glTranslatef(0,-0.14 ,0.76);
	glRotatef(DIP_rotate,-1,0, 0);
	glTranslatef(0,0.14,-0.76);

	glmDraw(lBone_Middle_DIP  , GLM_MATERIAL | GLM_SMOOTH);

	glPopMatrix();
}

void CHand::Draw_Left_Index (GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate)
{
	glPushMatrix();

	glTranslatef(0,-0.01,0.1);
	glRotatef(MCP_rotate,-1,0, 0);
	glTranslatef(0,0.01,-0.1);

	glPushMatrix();  
	glmDraw(lBone_Index_MCP  , GLM_MATERIAL | GLM_SMOOTH);
	glPopMatrix();	

	//-----------------------------
	glTranslatef(0,-0.1,0.49);
	glRotatef(PIP_rotate,-1,0, 0);
	glTranslatef(0,0.1,-0.49);

	glmDraw(lBone_Index_PIP  , GLM_MATERIAL | GLM_SMOOTH);

	//-----------------------------
	glTranslatef(0,-0.15 ,0.69);
	glRotatef(DIP_rotate,-1,0, 0);
	glTranslatef(0,0.15,-0.69);

	glmDraw(lBone_Index_DIP  , GLM_MATERIAL | GLM_SMOOTH);

	glPopMatrix();
}

void CHand::Draw_Left_Thumb (GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate)
{
	glPushMatrix();

	glTranslatef(-0.4,0.05,-0.6);
	glRotatef(MCP_rotate,0.5,-1,-0.5);
	glTranslatef(0.4,-0.05,0.6);

	glPushMatrix();  
	glmDraw(lBone_Thumb_MCP  , GLM_MATERIAL | GLM_SMOOTH);
	glPopMatrix();	

	//-----------------------------
	glTranslatef(0.16,-0.15,-0.11);
	glRotatef(PIP_rotate,0.87,1.97,0);
	glTranslatef(-0.16,0.15,0.11);

	glmDraw(lBone_Thumb_PIP  , GLM_MATERIAL | GLM_SMOOTH);

	//-----------------------------
	glTranslatef(0.08,-0.14,0.13);
	glRotatef(DIP_rotate,0.47,2.22,1.23);
	glTranslatef(-0.08,0.14,-0.13);

	glmDraw(lBone_Thumb_DIP  , GLM_MATERIAL | GLM_SMOOTH);

	glPopMatrix();
}
