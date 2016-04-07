#include "Polygon2D.h"
#include "Quaternion.h"

using namespace boost::geometry;


bool Polygon2D::IsCollision( const Polygon2D& rhs ) const
{
	if ( m_Polygon.outer().size() < 3 )
	{
		for ( auto it = m_Polygon.outer().begin(); it != m_Polygon.outer().end(); ++it )
		{
			if ( within( *it, rhs.m_Polygon ) ) { return true; }
		}
	}
	else
	{
		return intersects<polygon, polygon>( m_Polygon, rhs.m_Polygon );
	}
	return false;
}


bool Polygon2D::CollisionZ( const Polygon2D& rhs ) const
{
	if ( abs( m_zPoint - rhs.m_zPoint ) <= ( m_zRange + rhs.m_zRange ) * 0.5 )
	{
		return true;
	}
	else
	{
		return false;
	}
}


void Polygon2D::ProjectPolygon( const Vec2& axis, Polygon2D& polygon, float* min, float* max )
{
	// To project a point on an axis use the dot product
	float d = axis.dotProduct( Point2toVec2( polygon.Points()[0] ) );
	*min = d;
	*max = d;

	for ( size_t i = 0; i < polygon.Points().size(); i++ )
	{
		d = Point2toVec2( polygon.Points()[i] ).dotProduct( axis );

		if ( d < *min )
		{
			*min = d;
		}
		else
		{
			if ( d > *max )
			{
				*max = d;
			}
		}
	}
}

void Polygon2D::AddPoint( float x, float y )
{
	m_Polygon.outer().push_back( point2( x, y ) );
}

void Polygon2D::AddPoint( const Vec2& p )
{
	m_Polygon.outer().push_back( point2( p.x, p.y ) );
}

void Polygon2D::Offset( float x, float y )
{
	for ( auto it = m_Polygon.outer().begin();
	                it != m_Polygon.outer().end(); ++it )
	{
		it->x( it->x() + x );
		it->y( it->y() + y );
	}
}

void Polygon2D::Offset( const Vec2& v )
{
}


void Polygon2D::Offset( float x, float y, float z )
{
	for ( auto it = m_Polygon.outer().begin();
	                it != m_Polygon.outer().end(); ++it )
	{
		it->x( it->x() + x );
		it->y( it->y() + y );
	}

	m_zPoint += z;
}


void Polygon2D::Offset( const Vec3& v )
{
	m_zPoint += v.z;
}

void Polygon2D::SetAngle( float angle )
{
	for ( auto it = Points().begin(); it != Points().end(); ++it )
	{
		*it = Point2toVec2(Quaternion::GetRotation( Point2toVec2(*it), angle - m_Angle, Vec2::ZERO ));
	}
	m_Angle = angle;
}

void Polygon2D::Rotation( float angle, const Vec2& middle /*= Vec2::ZERO*/ )
{
	m_Angle = angle;
	for ( auto it = Points().begin(); it != Points().end(); ++it )
	{
		*it = Point2toVec2(Quaternion::GetRotation( Point2toVec2(*it), angle, middle ));
	}
}

void Polygon2D::Clear()
{
	m_Polygon.clear();
}

Polygon2D::~Polygon2D()
{
}

Vec2 Point2toVec2( const point2& p )
{
	return Vec2( p.x(), p.y() );
}

point2 Point2toVec2( const Vec2& p )
{
	return point2(p.x, p.y);
}
