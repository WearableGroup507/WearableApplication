#include "ButterworthFilter.h"

cButterworthFilter::cButterworthFilter(int FilterOrder,float SampleRate,float cutoff)
{
	m_FilterOrder = FilterOrder;
	m_SampleRate  = SampleRate;
	m_CutoffFreq  = cutoff;

	a  = tan((M_PI) * cutoff / SampleRate);
	a2 = a * a;
	A  = new float[FilterOrder];
	d1 = new float[FilterOrder];
	d2 = new float[FilterOrder];
	w0 = new float[FilterOrder];
	w1 = new float[FilterOrder];
	w2 = new float[FilterOrder];

	for (int i = 0; i < FilterOrder; i++)
	{
		float r = sin((M_PI) * (2.0f * i + 1.0f) / (4.0f * (float)FilterOrder));
		float s = a2 + 2.0f * a * r + 1.0f;
		A[i] = 1.0f / s;
		d1[i] = 2.0f * (1 - a2) / s;
		d2[i] = -(a2 - 2.0f * a * r + 1.0f) / s;
	}
}


float cButterworthFilter::GetResult (float input)
{
	float result = 0.0f;

	for (int i = 0; i < m_FilterOrder; i++)
	{
		w0[i] = d1[i] * w1[i] + d2[i] * w2[i] + input;
		result = A[i] * (w0[i] - 2.0f * w1[i] + w2[i]);

		w2[i] = w1[i];
		w1[i] = w0[i];
	}

	return result;
}
