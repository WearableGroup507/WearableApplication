/**
*    CHand
*    load 3D hand model and draw on openGL panel all hand data record in m_leftHandTrans and m_rightHandTrans
*
*	 Load_Model
*    load all 3D hand joint model into the system and use parameter to control and bend angle
*    @parameter(MCP_rotate) 1th finger joint angle
*	 @parameter(PIP_rotate) 2nd finger joint angle
*	 @parameter(DIP_rotate) 3rd finger joint angle
*
*
*	 Draw_Hand
*	 call Function Draw_RightHand and Function Draw_LeftHand to draw left and right hand
*	 ª`·N:About orientation, we get the euler angle from CGloveData which transform by AHRS and then still need to rotate 90 degree to get true hand orientation.   
**/

#include <Windows.h>
#include "GLRrawPanel/glm.h"

#include <gl/gl.h>
#include <gl/glu.h>

#include "GloveData.h"


class CHand{
public:
	CHand();
	void Load_Model();
	void Draw_Hand();

	//Hand Transform Data
	CGloveData m_leftHandTrans;
	CGloveData m_rightHandTrans;

private:

	//Hand Bone Model parameter
	GLMmodel *rBone_PalmModel;
	GLMmodel *rBone_Pink_MCP  ,*rBone_Pink_PIP  ,*rBone_Pink_DIP   ;
	GLMmodel *rBone_Ring_MCP  ,*rBone_Ring_PIP  ,*rBone_Ring_DIP   ;
	GLMmodel *rBone_Middle_MCP,*rBone_Middle_PIP,*rBone_Middle_DIP ;
	GLMmodel *rBone_Index_MCP ,*rBone_Index_PIP ,*rBone_Index_DIP  ;
	GLMmodel *rBone_Thumb_MCP ,*rBone_Thumb_PIP ,*rBone_Thumb_DIP  ;

	GLMmodel *lBone_PalmModel;
	GLMmodel *lBone_Pink_MCP  ,*lBone_Pink_PIP  ,*lBone_Pink_DIP   ;
	GLMmodel *lBone_Ring_MCP  ,*lBone_Ring_PIP  ,*lBone_Ring_DIP   ;
	GLMmodel *lBone_Middle_MCP,*lBone_Middle_PIP,*lBone_Middle_DIP ;
	GLMmodel *lBone_Index_MCP ,*lBone_Index_PIP ,*lBone_Index_DIP  ;
	GLMmodel *lBone_Thumb_MCP ,*lBone_Thumb_PIP ,*lBone_Thumb_DIP  ;

	GLMmodel *humanBody;

	//Loading Model	
	void Load_RightHand_Model();
	void Load_LeftHand_Model();
	//right hand
	void Draw_RightHand();
	void Draw_Pink  (GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate);
	void Draw_Ring  (GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate);
	void Draw_Middle(GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate);
	void Draw_Index (GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate);
	void Draw_Thumb (GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate);
	//left hand
	void Draw_LeftHand();
	void Draw_Left_Pink  (GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate);
	void Draw_Left_Ring  (GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate);
	void Draw_Left_Middle(GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate);
	void Draw_Left_Index (GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate);
	void Draw_Left_Thumb (GLfloat MCP_rotate,GLfloat PIP_rotate,GLfloat DIP_rotate);
};