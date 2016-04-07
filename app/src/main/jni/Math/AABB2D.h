#pragma once
#include "Vector2.h"
#include "Polygon2D.h"

class AABB2D
{
public:
	AABB2D( const Polygon2D& poly );
	void AddPoint( const Vector2& p );
	AABB2D() {}
	AABB2D( const Vector2& orgin )
		: m_Min( orgin ), m_Max( orgin )
	{}
	void ReBuild( const Polygon2D& poly );
	void ReBuild( const Polygon2Ds& polys );
	void AddPolygon2D( const Polygon2D& poly );
	void AddPoint( const point2& p );
	void Larger( float val );
	void Larger( float x, float y );
	void SetBounding( float left, float right, float top, float down );
	void SetBounding( float value );
	bool IsContain( const AABB2D& rhs );
	bool IsCollision( const AABB2D& rhs );
	void Move(float x, float y);
	void Scale(float v);
	void ChangeFace();
	Vector2 m_Min, m_Max, m_Len;
};
bool Collision( const AABB2D& lhs, const AABB2D& rhs );