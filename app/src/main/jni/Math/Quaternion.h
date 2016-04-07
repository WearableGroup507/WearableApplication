#pragma once
/*
-----------------------------------------------------------------------------
This source file is part of OGRE
(Object-oriented Graphics Rendering Engine)
For the latest info, see http://www.ogre3d.org/

Copyright (c) 2000-2009 Torus Knot Software Ltd

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
-----------------------------------------------------------------------------
*/
// This file is based on material originally from:
// Geometric Tools, LLC
// Copyright (c) 1998-2010
// Distributed under the Boost Software License, Version 1.0.
// http://www.boost.org/LICENSE_1_0.txt
// http://www.geometrictools.com/License/Boost/LICENSE_1_0.txt

#include "Matrix3.h"
#include "Vector2.h"


/** Implementation of a Quaternion, i.e. a rotation around an axis.
*/
class Quaternion
{
public:
	inline Quaternion (
	        float fW = 1.0,
	        float fX = 0.0, float fY = 0.0, float fZ = 0.0 )
	{
		w = fW;
		x = fX;
		y = fY;
		z = fZ;
	}
	/// Construct a quaternion from a rotation matrix
	inline Quaternion( const Matrix3& rot )
	{
		this->FromRotationMatrix( rot );
	}
	/// Construct a quaternion from an angle/axis
	inline Quaternion( const Radian& rfAngle, const Vector3& rkAxis )
	{
		this->FromAngleAxis( rfAngle, rkAxis );
	}
	/// Construct a quaternion from 3 orthonormal local axes
	inline Quaternion( const Vector3& xaxis, const Vector3& yaxis, const Vector3& zaxis )
	{
		this->FromAxes( xaxis, yaxis, zaxis );
	}
	/// Construct a quaternion from 3 orthonormal local axes
	inline Quaternion( const Vector3* akAxis )
	{
		this->FromAxes( akAxis );
	}
	/// Construct a quaternion from 4 manual w/x/y/z values
	inline Quaternion( float* valptr )
	{
		memcpy( &w, valptr, sizeof( float ) * 4 );
	}

	/** Exchange the contents of this quaternion with another.
	*/
	inline void swap( Quaternion& other )
	{
		std::swap( w, other.w );
		std::swap( x, other.x );
		std::swap( y, other.y );
		std::swap( z, other.z );
	}

	/// Array accessor operator
	inline float operator [] ( const size_t i ) const
	{
		assert( i < 4 );
		return *( &w + i );
	}

	/// Array accessor operator
	inline float& operator [] ( const size_t i )
	{
		assert( i < 4 );
		return *( &w + i );
	}

	/// Pointer accessor for direct copying
	inline float* ptr()
	{
		return &w;
	}

	/// Pointer accessor for direct copying
	inline const float* ptr() const
	{
		return &w;
	}

	void FromRotationMatrix ( const Matrix3& kRot );
	void ToRotationMatrix ( Matrix3& kRot ) const;
	void FromAngleAxis ( const Radian& rfAngle, const Vector3& rkAxis );
	void ToAngleAxis ( Radian& rfAngle, Vector3& rkAxis ) const;
	inline void ToAngleAxis ( Degree& dAngle, Vector3& rkAxis ) const
	{
		Radian rAngle;
		ToAngleAxis ( rAngle, rkAxis );
		dAngle = rAngle;
	}
	void FromAxes ( const Vector3* akAxis );
	void FromAxes ( const Vector3& xAxis, const Vector3& yAxis, const Vector3& zAxis );
	void ToAxes ( Vector3* akAxis ) const;
	void ToAxes ( Vector3& xAxis, Vector3& yAxis, Vector3& zAxis ) const;
	/// Get the local x-axis
	Vector3 xAxis( void ) const;
	/// Get the local y-axis
	Vector3 yAxis( void ) const;
	/// Get the local z-axis
	Vector3 zAxis( void ) const;

	inline Quaternion& operator= ( const Quaternion& rkQ )
	{
		w = rkQ.w;
		x = rkQ.x;
		y = rkQ.y;
		z = rkQ.z;
		return *this;
	}
	Quaternion operator+ ( const Quaternion& rkQ ) const;
	Quaternion operator- ( const Quaternion& rkQ ) const;
	Quaternion operator* ( const Quaternion& rkQ ) const;
	Quaternion operator* ( float fScalar ) const;
	friend Quaternion operator* ( float fScalar,
	                              const Quaternion& rkQ );
	Quaternion operator- () const;
	inline bool operator== ( const Quaternion& rhs ) const
	{
		return ( rhs.x == x ) && ( rhs.y == y ) &&
		       ( rhs.z == z ) && ( rhs.w == w );
	}
	inline bool operator!= ( const Quaternion& rhs ) const
	{
		return !operator==( rhs );
	}
	// functions of a quaternion
	float Dot ( const Quaternion& rkQ ) const; // dot product
	float Norm () const;  // squared-length
	/// Normalises this quaternion, and returns the previous length
	float normalise( void );
	Quaternion Inverse () const;  // apply to non-zero quaternion
	Quaternion UnitInverse () const;  // apply to unit-length quaternion
	Quaternion Exp () const;
	Quaternion Log () const;

	// rotation of a vector by a quaternion
	Vector3 operator* ( const Vector3& rkVector ) const;

	/** Calculate the local roll element of this quaternion.
	@param reprojectAxis By default the method returns the 'intuitive' result
	that is, if you projected the local Y of the quaternion onto the X and
	Y axes, the angle between them is returned. If set to false though, the
	result is the actual yaw that will be used to implement the quaternion,
	which is the shortest possible path to get to the same orientation and
	may involve less axial rotation.
	*/
	Radian getRoll( bool reprojectAxis = true ) const;
	/** Calculate the local pitch element of this quaternion
	@param reprojectAxis By default the method returns the 'intuitive' result
	that is, if you projected the local Z of the quaternion onto the X and
	Y axes, the angle between them is returned. If set to true though, the
	result is the actual yaw that will be used to implement the quaternion,
	which is the shortest possible path to get to the same orientation and
	may involve less axial rotation.
	*/
	Radian getPitch( bool reprojectAxis = true ) const;
	/** Calculate the local yaw element of this quaternion
	@param reprojectAxis By default the method returns the 'intuitive' result
	that is, if you projected the local Z of the quaternion onto the X and
	Z axes, the angle between them is returned. If set to true though, the
	result is the actual yaw that will be used to implement the quaternion,
	which is the shortest possible path to get to the same orientation and
	may involve less axial rotation.
	*/
	Radian getYaw( bool reprojectAxis = true ) const;
	/// Equality with tolerance (tolerance is max angle difference)
	bool equals( const Quaternion& rhs, const Radian& tolerance ) const;
	/** Gets the shortest arc quaternion to rotate this vector to the destination
	vector.
	@remarks
	If you call this with a dest vector that is close to the inverse
	of this vector, we will rotate 180 degrees around the 'fallbackAxis'
	(if specified, or a generated axis if not) since in this case
	ANY axis of rotation is valid.
	*/

	Quaternion getRotationTo( const Vector3& orig, const Vector3& dest,
	                          const Vector3& fallbackAxis = Vector3::ZERO ) const
	{
		// Based on Stan Melax's article in Game Programming Gems
		Quaternion q;
		// Copy, since cannot modify local
		Vector3 v0 = orig;
		Vector3 v1 = dest;
		v0.normalise();
		v1.normalise();
		float d = v0.dotProduct( v1 );

		// If dot == 1, vectors are the same
		if ( d >= 1.0 )
		{
			return Quaternion::IDENTITY;
		}

		if ( d < ( 1e-6f - 1.0 ) )
		{
			if ( fallbackAxis != Vector3::ZERO )
			{
				// rotate 180 degrees about the fallback axis
				q.FromAngleAxis( Radian( Math::PI ), fallbackAxis );
			}
			else
			{
				// Generate an axis
				Vector3 axis = Vector3::UNIT_X.crossProduct( orig );

				if ( axis.isZeroLength() ) // pick another if colinear
				{
					axis = Vector3::UNIT_Y.crossProduct( orig );
				}

				axis.normalise();
				q.FromAngleAxis( Radian( Math::PI ), axis );
			}
		}
		else
		{
			float s = Math::Sqrt( ( 1 + d ) * 2 );
			float invs = 1 / s;
			Vector3 c = v0.crossProduct( v1 );
			q.x = c.x * invs;
			q.y = c.y * invs;
			q.z = c.z * invs;
			q.w = s * 0.5f;
			q.normalise();
		}

		return q;
	}
	static Vector3 GetRotation( const Vector3& src, float angle, const Vector3& middle_up = Vector3::NEGATIVE_UNIT_Z )
	{
		Quaternion q;
		q.FromAngleAxis( Degree( angle ), middle_up );
		return q * src;
	}
	static Vector2 GetRotation( const Vector2& src, float angle, const Vector2& middle = Vector2::ZERO )
	{
		Quaternion q;
		Vec3 tmpsrc( src.x, src.y, 0 );
		Vec3 up( middle.x, middle.y, -1 );
		q.FromAngleAxis( Degree( angle ), up );
		tmpsrc = q * tmpsrc;
		return Vec2( tmpsrc.x, tmpsrc.y );
	}
	// spherical linear interpolation
	static Quaternion Slerp ( float fT, const Quaternion& rkP,
	                          const Quaternion& rkQ, bool shortestPath = false );

	static Quaternion SlerpExtraSpins ( float fT,
	                                    const Quaternion& rkP, const Quaternion& rkQ,
	                                    int iExtraSpins );

	// setup for spherical quadratic interpolation
	static void Intermediate ( const Quaternion& rkQ0,
	                           const Quaternion& rkQ1, const Quaternion& rkQ2,
	                           Quaternion& rka, Quaternion& rkB );

	// spherical quadratic interpolation
	static Quaternion Squad ( float fT, const Quaternion& rkP,
	                          const Quaternion& rkA, const Quaternion& rkB,
	                          const Quaternion& rkQ, bool shortestPath = false );

	// normalised linear interpolation - faster but less accurate (non-constant rotation velocity)
	static Quaternion nlerp( float fT, const Quaternion& rkP,
	                         const Quaternion& rkQ, bool shortestPath = false );

	// cutoff for sine near zero
	static const float ms_fEpsilon;

	// special values
	static const Quaternion ZERO;
	static const Quaternion IDENTITY;

	float w, x, y, z;

	/// Check whether this quaternion contains valid values
	inline bool isNaN() const
	{
		return _isnan( x ) || _isnan( y ) || _isnan( z ) || _isnan( w );
	}

	/** Function for writing to a stream. Outputs "Quaternion(w, x, y, z)" with w,x,y,z
	being the member values of the quaternion.
	*/
	inline friend std::ostream& operator <<
	( std::ostream& o, const Quaternion& q )
	{
		o << "Quaternion(" << q.w << ", " << q.x << ", " << q.y << ", " << q.z << ")";
		return o;
	}

};

