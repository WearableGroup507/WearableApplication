#pragma once
#include "Vector2.h"
#include "Vector3.h"
#include "Polygon2D.h"

template <class ParentPtr>
class AABB3D
{
public:
	ParentPtr m_ParentPtr;
	AABB3D( const Polygon2D& poly ,ParentPtr parentPtr) : m_ParentPtr(parentPtr)
	{
		ReBuild( poly );
	}
	void AddPoint( const Vector2& p )
	{
		if ( p.x > m_Max.x )
		{
			m_Max.x = p.x;
		}
		else if ( p.x < m_Min.x )
		{
			m_Min.x = p.x;
		}

		if ( p.y > m_Max.y )
		{
			m_Max.y = p.y;
		}
		else if ( p.y < m_Min.y )
		{
			m_Min.y = p.y;
		}

		m_Len.x = std::abs(m_Max.x - m_Min.x);
		m_Len.y = std::abs(m_Max.y - m_Min.y);
	}
	AABB3D(){}
	AABB3D( ParentPtr parentPtr) : m_ParentPtr(parentPtr){}
	AABB3D( const Vector2& orgin ,ParentPtr parentPtr) : m_ParentPtr(parentPtr)
	{
		Vector3 ori;
		ori.x = orgin.x;
		ori.y = orgin.y;
		ori.z = 0.0f;
		m_Min = ori;
		m_Max = ori;

		m_Len.x = std::abs(m_Max.x - m_Min.x);
		m_Len.y = std::abs(m_Max.y - m_Min.y);
		m_Len.z = std::abs(m_Max.z - m_Min.z);
	}
	AABB3D( const Vector3& orgin ,ParentPtr parentPtr) : m_ParentPtr(parentPtr), m_Min( orgin ), m_Max( orgin )
	{
		m_Len.x = std::abs(m_Max.x - m_Min.x);
		m_Len.y = std::abs(m_Max.y - m_Min.y);
		m_Len.z = std::abs(m_Max.z - m_Min.z);
	}
	void ReBuild( const Polygon2D& poly )
	{
		Vector2 ori = Point2toVec2( poly.CPoints()[0] );
		m_Max.x = ori.x;
		m_Max.y = ori.y;
		m_Min.x = ori.x;
		m_Min.y = ori.y;

		for ( auto it = poly.CPoints().begin(); it != poly.CPoints().end(); ++it )
		{
			AddPoint( *it );
		}
	}
	void ReBuild( const Polygon2Ds& polys )
	{
		AddPolygon2D( *polys.begin() );

		for ( auto it = ++polys.begin(); it != polys.end(); ++it )
		{
			AddPolygon2D( *it );
		}
	}
	void AddPolygon2D( const Polygon2D& poly )
	{
		for ( auto it = poly.CPoints().begin(); it != poly.CPoints().end(); ++it )
		{
			AddPoint( *it );
		}
	}
	void AddPoint( const point2& p )
	{
		if ( p.x() > m_Max.x )
		{
			m_Max.x = p.x();
		}
		else if ( p.x() < m_Min.x )
		{
			m_Min.x = p.x();
		}

		if ( p.y() > m_Max.y )
		{
			m_Max.y = p.y();
		}
		else if ( p.y() < m_Min.y )
		{
			m_Min.y = p.y();
		}

		m_Len.x = std::abs(m_Max.x - m_Min.x);
		m_Len.y = std::abs(m_Max.y - m_Min.y);
	}
	void Larger( float val )
	{
		m_Min.x -= val;
		m_Min.y -= val;
		m_Min.z -= val;
		m_Max.x += val;
		m_Max.y += val;
		m_Max.z += val;

		m_Len.x = std::abs(m_Max.x - m_Min.x);
		m_Len.y = std::abs(m_Max.y - m_Min.y);
		m_Len.z = std::abs(m_Max.z - m_Min.z);
	}
	void Larger( float x, float y, float z )
	{
		m_Min.x -= x;
		m_Min.y -= y;
		m_Min.z -= z;
		m_Max.x += x;
		m_Max.y += y;
		m_Max.z += z;

		m_Len.x = std::abs(m_Max.x - m_Min.x);
		m_Len.y = std::abs(m_Max.y - m_Min.y);
		m_Len.z = std::abs(m_Max.z - m_Min.z);
	}
	void SetBounding( float left, float right, float top, float down, float front, float back )
	{
		m_Min.x = left;
		m_Min.y = top;
		m_Min.z = front;
		m_Max.x = right;
		m_Max.y = down;
		m_Max.z = back;

		m_Len.x = std::abs(m_Max.x - m_Min.x);
		m_Len.y = std::abs(m_Max.y - m_Min.y);
		m_Len.z = std::abs(m_Max.z - m_Min.z);
	}
	void SetBounding( float value )
	{
		m_Min.x = value;
		m_Min.y = value;
		m_Min.z = value;
		m_Max.x = value;
		m_Max.y = value;
		m_Max.z = value;

		m_Len.x = std::abs(m_Max.x - m_Min.x);
		m_Len.y = std::abs(m_Max.y - m_Min.y);
		m_Len.z = std::abs(m_Max.z - m_Min.z);
	}
	template<class ParentPtr2>
	bool IsContain( const AABB3D<ParentPtr2>& rhs )
	{
		if ( m_Min.x <= rhs.m_Min.x &&
			m_Min.y <= rhs.m_Min.y &&
			m_Min.z <= rhs.m_Min.z &&
			m_Max.x >= rhs.m_Max.x &&
			m_Max.y >= rhs.m_Max.y &&
			m_Max.z >= rhs.m_Max.z )
		{
			return true;
		}

		return false;
	}
	template<class ParentPtr2>
	bool IsCollision( const AABB3D<ParentPtr2>& rhs )
	{
		if( this->m_Max.x < rhs.m_Min.x || this->m_Min.x > rhs.m_Max.x )
		{
			return false;
		}

		if( this->m_Max.y < rhs.m_Min.y || this->m_Min.y > rhs.m_Max.y )
		{
			return false;
		}

		if( this->m_Max.z < rhs.m_Min.z || this->m_Min.z > rhs.m_Max.z )
		{
			return false;
		}

		return true;
	}
	void Move(float x, float y, float z)
	{
		m_Min.x += x;
		m_Max.x += x;
		m_Min.y += y;
		m_Max.y += y;
		m_Min.z += z;
		m_Max.z += z;
	}
	void Scale(float v)
	{
		m_Min *= v;
		m_Max *= v;

		m_Len.x = std::abs(m_Max.x - m_Min.x);
		m_Len.y = std::abs(m_Max.y - m_Min.y);
		m_Len.z = std::abs(m_Max.z - m_Min.z);
	}
	void Scale( float x, float y, float z )
	{
		m_Min.x *= x;
		m_Max.x *= x;
		m_Min.y *= y;
		m_Max.y *= y;
		m_Min.z *= z;
		m_Max.z *= z;

		m_Len.x = std::abs(m_Max.x - m_Min.x);
		m_Len.y = std::abs(m_Max.y - m_Min.y);
		m_Len.z = std::abs(m_Max.z - m_Min.z);
	}
	void ChangeFace()
	{
		Vec2 vmin(m_Min.x, m_Min.y), vmax(m_Max.x, m_Max.y);
		m_Min.x = -vmax.x;
		m_Max.x = -vmin.x;
	}
	void SetZWidth(float z)
	{
		m_Max.z = z / 2.0f;
		m_Min.z = -m_Max.z;
		m_Len.z = z;
	}
	Vector3 m_Min, m_Max, m_Len;
};

template<class ParentPtr, class ParentPtr2>
bool Collision( const AABB3D<ParentPtr>& lhs, const AABB3D<ParentPtr2>& rhs )
{
	if( lhs.m_Max.x < rhs.m_Min.x || lhs.m_Min.x > rhs.m_Max.x )
	{
		return false;
	}

	if( lhs.m_Max.y < rhs.m_Min.y || lhs.m_Min.y > rhs.m_Max.y )
	{
		return false;
	}

	if( lhs.m_Max.z < rhs.m_Min.z || lhs.m_Min.z > rhs.m_Max.z )
	{
		return false;
	}

	return true;
}

template<class _Ty1, class _Ty2>
struct AABB_is_collision
		: public std::binary_function<_Ty1, _Ty2, bool>
{
	// functor for collision
	bool operator()( const _Ty1& _Left, const _Ty2& _Right ) const
	{
		if( _Left->m_Max.x < _Right.m_Min.x || _Left->m_Min.x > _Right.m_Max.x )
		{
			return false;
		}

		if( _Left->m_Max.y < _Right.m_Min.y || _Left->m_Min.y > _Right.m_Max.y )
		{
			return false;
		}

		if( _Left->m_Max.z < _Right.m_Min.z || _Left->m_Min.z > _Right.m_Max.z )
		{
			return false;
		}
		return true;
	}
};