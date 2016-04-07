#include <iostream>
#include <math.h>
#ifndef CBUTTERWORTHFILTER_H_
#define CBUTTERWORTHFILTER_H_

#define M_PI       3.14159265358979323846

class cButterworthFilter
{
public:
	int 		m_FilterOrder;		// The order of filter
	float 		m_SampleRate;		// The sample rate (Hz)
	float 		m_CutoffFreq;		// The cut-off frequency (Hz)


	cButterworthFilter(int FilterOrder,float SampleRate,float CutoffFreq);
	float GetResult(float input);

private:


	float a, a2;
	float *A, *d1, *d2, *w0, *w1, *w2;
};

#endif



