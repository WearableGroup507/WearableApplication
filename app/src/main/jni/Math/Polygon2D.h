#pragma warning(disable:4819)
#pragma once

#include "Vector2.h"
#include "Vector3.h"
#include "BasicMath.h"
#include <vector>

#include <boost/geometry.hpp>
#include <boost/geometry/geometries/polygon.hpp>
#include <boost/geometry/geometries/point_xy.hpp>
#include <boost/geometry/geometry.hpp>

typedef boost::geometry::model::d2::point_xy<float> point2;
typedef boost::geometry::model::polygon<point2> polygon;

Vec2 Point2toVec2(const point2& p);
point2 Point2toVec2(const Vec2& p);
class Polygon2D
{
public:
	Polygon2D(): m_Angle( 0 ), m_zPoint( 0 ), m_zRange( 0.01f ) {}
	~Polygon2D();
	polygon::ring_type& Points(){ return m_Polygon.outer();}
	const polygon::ring_type& CPoints() const { return m_Polygon.outer();}
	void AddPoint( float x, float y );
	void AddPoint( const Vec2& p );
	void Offset( float x, float y );
	void Offset( const Vec2& v );
	void Offset( float x, float y, float z );
	void Offset( const Vec3& v );
	void SetAngle( float angle );
	void SetZRange( float scale ) {m_zRange = scale;}
	void SetZPoint( float rz ) {m_zPoint = rz;}
	float GetAngle() {return m_Angle;}
	float GetZRange() {return m_zRange;}
	float GetZPoint() {return m_zPoint;}
	void Rotation( float angle, const Vec2& middle = Vec2::ZERO );
	bool IsCollision( const Polygon2D& rhs ) const;
	bool CollisionZ( const Polygon2D& rhs ) const;
	void Clear();
private:
	// Calculate the distance between [minA, maxA] and [minB, maxB]
	// The distance will be negative if the intervals overlap
	inline float IntervalDistance( float minA, float maxA, float minB, float maxB )
	{
		if ( minA < minB )
		{
			return minB - maxA;
		}
		else
		{
			return minA - maxB;
		}
	}
	// Calculate the projection of a polygon on an axis and returns it as a [min, max] interval
	void ProjectPolygon( const Vec2& axis, Polygon2D& polygon, float* min, float* max );
private:
	float	m_Angle;
	polygon m_Polygon;
	float   m_zRange; // ª½®|
	float   m_zPoint;
};
typedef std::vector<Polygon2D> Polygon2Ds;

