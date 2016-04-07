#include "AABB2D.h"
#include "Polygon2D.h"

void AABB2D::AddPoint( const Vector2& p )
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
	m_Len.y = std::abs(m_Max.y - m_Min.y);;
}

void AABB2D::Larger( float val )
{
	m_Min.x -= val;
	m_Min.y -= val;
	m_Max.x += val;
	m_Max.y += val;

	m_Len.x = std::abs(m_Max.x - m_Min.x);
	m_Len.y = std::abs(m_Max.y - m_Min.y);;
}

void AABB2D::Larger( float x, float y )
{
	m_Min.x -= x;
	m_Min.y -= y;
	m_Max.x += x;
	m_Max.y += y;

	m_Len.x = std::abs(m_Max.x - m_Min.x);
	m_Len.y = std::abs(m_Max.y - m_Min.y);;
}

void AABB2D::SetBounding( float left, float right, float top, float down )
{
	m_Min.x = left;
	m_Min.y = top;
	m_Max.x = right;
	m_Max.y = down;

	m_Len.x = std::abs(m_Max.x - m_Min.x);
	m_Len.y = std::abs(m_Max.y - m_Min.y);;
}

void AABB2D::SetBounding( float value )
{
	m_Min.x = value;
	m_Min.y = value;
	m_Max.x = value;
	m_Max.y = value;

	m_Len.x = std::abs(m_Max.x - m_Min.x);
	m_Len.y = std::abs(m_Max.y - m_Min.y);;
}

bool AABB2D::IsContain( const AABB2D& rhs )
{
	if ( m_Min.x <= rhs.m_Min.x &&
	                m_Min.y <= rhs.m_Min.y &&
	                m_Max.x >= rhs.m_Max.x &&
	                m_Max.y >= rhs.m_Max.y )
	{
		return true;
	}

	return false;
}

bool Collision( const AABB2D& lhs, const AABB2D& rhs )
{
	if ( lhs.m_Min.x <= rhs.m_Min.x && lhs.m_Max.x >= rhs.m_Min.x )
	{
		if ( lhs.m_Min.y <= rhs.m_Min.y && lhs.m_Max.y >= rhs.m_Min.y )
		{
			return true;
		}

		if ( lhs.m_Min.y <= rhs.m_Max.y && lhs.m_Max.y >= rhs.m_Max.y )
		{
			return true;
		}
	}

	if ( lhs.m_Min.x <= rhs.m_Max.x && lhs.m_Max.x >= rhs.m_Max.x )
	{
		if ( lhs.m_Min.y <= rhs.m_Min.y && lhs.m_Max.y >= rhs.m_Min.y )
		{
			return true;
		}

		if ( lhs.m_Min.y <= rhs.m_Max.y && lhs.m_Max.y >= rhs.m_Max.y )
		{
			return true;
		}
	}

	return false;
}

bool AABB2D::IsCollision( const AABB2D& rhs )
{
	if ( Collision( *this, rhs ) || Collision( rhs, *this ) )
	{
		return true;
	}
}

void AABB2D::ReBuild( const Polygon2D& poly )
{
	m_Min = m_Max = Point2toVec2( poly.CPoints()[0] );

	for ( auto it = poly.CPoints().begin(); it != poly.CPoints().end(); ++it )
	{
		AddPoint( *it );
	}
}

void AABB2D::ReBuild( const Polygon2Ds& polys )
{
	AddPolygon2D( *polys.begin() );

	for ( auto it = ++polys.begin(); it != polys.end(); ++it )
	{
		AddPolygon2D( *it );
	}
}

AABB2D::AABB2D( const Polygon2D& poly )
{
	ReBuild( poly );
}

void AABB2D::AddPolygon2D( const Polygon2D& poly )
{
	for ( auto it = poly.CPoints().begin(); it != poly.CPoints().end(); ++it )
	{
		AddPoint( *it );
	}
}

void AABB2D::AddPoint( const point2& p )
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
	m_Len.y = std::abs(m_Max.y - m_Min.y);;
}

void AABB2D::Move( float x, float y )
{
	m_Min.x += x;
	m_Max.x += x;
	m_Min.y += y;
	m_Max.y += y;
}

void AABB2D::ChangeFace()
{
	Vec2 vmin = m_Min, vmax = m_Max;
	m_Min.x = -vmax.x;
	m_Max.x = -vmin.x;
}

void AABB2D::Scale( float v )
{
	m_Min *= v;
	m_Max *= v;

	m_Len.x = std::abs(m_Max.x - m_Min.x);
	m_Len.y = std::abs(m_Max.y - m_Min.y);;
}