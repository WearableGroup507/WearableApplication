/*
    OLNG.cpp
    Author: Chin-Feng Chang
*/
//------------------------------------------------------------------------------
// Includes
#include "OLNG/OLNG.h"

//------------------------------------------------------------------------------
//  Methods

/**
*    @Function	Initial
*    @Brief		Initial the Graph memory and parameter
*/
void OLNG::Initial()
{
	//Initial Graph Memory depend on the M and K value
	m_Graph = new OLNG_Node*[M];
	for(int i = 0; i < M; i++)
		m_Graph[i] = new OLNG_Node[K];

	//Initial Other Member Memory 
	m_penaltyCost = new float[M];
	m_finalCost   = new float[K];

	//Initial OLNG State
	m_isFull      = false;
	m_getPath     = false;
}


/**
*    @Function	ClearGraph
*    @Brief		Clear Graph and Initial Graph to Empty
*/
void OLNG::ClearGraph()
{
	//Delete All Memory
	for(int ind = 0; ind < M; ind++)
		delete [] m_Graph[ind];
	delete [] m_Graph;

	delete [] m_penaltyCost;
	delete [] m_finalCost;

	currTime = 0;
	Initial();
}

/**
*    @Function	BuildGraph
*    @Brief		Build OLNG graph by the input OLNG_Node which is consist of feature
*				step 1. Get KNN search result
*				step 2. Add the detail info about the node and assign to OLNG
*				step 3. Insert Edge according to valid condition between OLNG node
*				step 4. Compute path cost and get the path of minimum cost,as the OLGN is full
*				step 5. Update the Graph as the new input signal coming
*
*	 @Param		SGN the OLNG_Node which is consist of feature
*/
void OLNG::BuildGraph(std::vector<OLNG_Node> SGN)
{
//	__android_log_print(ANDROID_LOG_DEBUG, "OLNG::BuildGraph", "RUN");
	if(!m_isFull)
	{	
		//add OLNG Node into m_Graph and search maximum cost for m_penaltyCost
		float maxCost = 0;
		for(int i = 0; i< SGN.size() ; i++)
		{
			SGN[i].ti = currTime;
			SGN[i].ki = i;
			m_Graph[currTime][i] = SGN[i];

			if(maxCost<SGN[i].nodeCost)
				maxCost = SGN[i].nodeCost;
		}
		m_penaltyCost[currTime] = maxCost*2;

		//inset edge for new column
		InsertEdge(currTime);		

		//insert edge for between last two columns
		if(currTime==M-1)
		{	
			InsertEdge(M);
			m_isFull = true;
		}
	}
	else
	{
		//Reset final cost value
		for(int i = 0 ; i < K ; i++)
			m_finalCost[i] = 0;		

		GetFinalCostPath2();

		UpdateNode(SGN);
	}
	currTime++;
}

/**
*    @Function	InsertEdge
*    @Brief		insert edge according to valid condition below
*				(1) i1 + 1 = i2 & t1 + 1 = t2
*				(2) i1 + 2 = i2 & t1 + 1 = t2
*				(3) i1 + 1 = i2 & t1 + 2 = t2
*
*				(4) i1 + 2 = i2 & t1 + 2 = t2
*				(5) i1 + 3 = i2 & t1 + 2 = t2
*				(6) i1 + 4 = i2 & t1 + 2 = t2
*	 @Param		currT is the value of time step.
*/
void OLNG::InsertEdge(int currT)
{
	//std::cout << "Time = " << currT << "\n";
	//PrintGraph();
	int t1 = currT - 2,t2;

	//start insert edge process as OLNG have three columns
	if(currT >=2)
	{		
		//Case 1: t1 + 1 = t2
		t2 = t1 + 1;

		for(int k1 = 0 ; k1 < K ; k1++)   // k1 node at t1 column
		{
			for(int k2 = 0 ; k2 < K ; k2++) // k2 node at t2 column
			{
				//compute i2 - i1
				int IndDiff = m_Graph[t2][k2].SignClipInd - m_Graph[t1][k1].SignClipInd;

				// i2 - i1 is 1 or 2 ==> condition (1) & (2)
				if(IndDiff == 1 || IndDiff ==2)
				{
					//set inEdge and outEdge
					OLNG_Edge inE,outE;

					if(currT!=M)
					{
						inE.ti = currTime-2;
						inE.ki = k1;
						inE.edgeCost = 0;

						outE.ti = currTime-1;
						outE.ki = k2;
						outE.edgeCost = 0;
					}
					else
					{
						inE.ti = currTime-1;
						inE.ki = k1;
						inE.edgeCost = 0;

						outE.ti = currTime;
						outE.ki = k2;
						outE.edgeCost = 0;
					}

					//push into inEdge of m_graph[t2][k2]
					m_Graph[t2][k2].inEdge.push_back(inE);
					//push into outEdge of m_graph[t1][k1]
					m_Graph[t1][k1].outEdge.push_back(outE);
					
				}
			}
		}
		/////////////////////////////////////////////////////////////////////////////////////////

		//Case 2 : t1 + 2 = t2
		t2 = t1 + 2;
		if(t2==M) return; //out of range


		//std::cout << "t1 = " << t1 << "  t2 = " << t2 << "\n";
		for(int k1 = 0 ; k1 < K ; k1++)   // k1 node at t1 column
		{
			for(int k2 = 0 ; k2 < K ; k2++) // k2 node at t2 column
			{
				//compute i2 - i1
				int IndDiff = m_Graph[t2][k2].SignClipInd - m_Graph[t1][k1].SignClipInd;

				// i2 - i1 is 1 ~ 4 ==> condition (1) & (2)
				if(IndDiff >= 1 && IndDiff <=4)
				{
					OLNG_Edge inE,outE;

					inE.ti = currTime-2;
					inE.ki = k1;
					inE.edgeCost = m_penaltyCost[t1+1];  //cross edge has penalty cost
					//push into inEdge of m_graph[t2][k2]
					m_Graph[t2][k2].inEdge.push_back(inE);
					
					outE.ti = currTime;
					outE.ki = k2;
					outE.edgeCost = m_penaltyCost[t1+1];
					//push into outEdge of m_graph[t1][k1]
					m_Graph[t1][k1].outEdge.push_back(outE);					
				}
			}
		}		
	}
}

/**
*    @Function	GetFinalCostPath2
*    @Brief		Compute Path Cost,back travel the graph and find the
*               minimum node cost each time step and sum the penalty
*               for each OLNG node of last column
*/
void OLNG::GetFinalCostPath2()
{
//	__android_log_print(ANDROID_LOG_DEBUG, "OLNG::GetFinalCostPath2", "RUN");
	m_minimumFinalCost = -1;
	/*Calculate the time offset depend on current time and OLNG windows size*/
	int tOffset = (currTime - M) <= 0 ? 0 : (currTime - M) ;

	/*Trace all graph node in the last column*/
	for(int ki = 0 ; ki < this->K ; ki++)
	{		
		//start travel from the last column
		int km = ki,tm = M-1;
		m_finalCost[ki] = m_Graph[tm][km].nodeCost;

		//trace graph node follow the in-edge
		while(1)
		{
			if(tm < 0) break;

			/*accumulate node cost as the path cost*/
			m_finalCost[ki]+=m_Graph[tm][km].nodeCost;

			/*stop trace if in-edge is null*/
			if(m_Graph[tm][km].inEdge.size()==0)
				break;
			else
			{		
				int minimumCost = 9999999,lastNode = -1;

				for(int edgeIdx  = 0 ; edgeIdx < m_Graph[tm][km].inEdge.size() ; edgeIdx++)
				{
					if(minimumCost > (m_Graph[tm][km].inEdge[edgeIdx].edgeCost + m_Graph[tm][km].nodeCost))
					{
						minimumCost = m_Graph[tm][km].inEdge[edgeIdx].edgeCost + m_Graph[tm][km].nodeCost;
						lastNode = edgeIdx;
					}
				}

				m_finalCost[ki]+=minimumCost;
				int tm_1 = m_Graph[tm][km].inEdge[lastNode].ti;
				int km_1 = m_Graph[tm][km].inEdge[lastNode].ki;
				
				/*transfer the time value to OLNG column index*/
				tm = tm_1 - tOffset;
				km = km_1;
			}
		}

		/*If the length of path is shorter than M, then */
		if(tm!=0) 
			for(int j = 0 ; j < tm-1 ; j++)
				m_finalCost[ki] += m_penaltyCost[j];	

		if(m_minimumFinalCost==-1)
			m_minimumFinalCost = m_finalCost[ki];
		else if(m_finalCost[ki] < m_minimumFinalCost)
			m_minimumFinalCost = m_finalCost[ki];
	}
	m_penaltySum = 0;
	for(int m = 0 ; m < M ; m++)
		m_penaltySum += m_penaltyCost[m];

	m_getPath = true;
}

/**
*    @Function	UpdateNode
*    @Brief		update the new graph node as the new input come.
*				step 1. Delete the edge from t0 to t1 and t2
*               step 2. Moving the Graph node to last id
*               step 3. Insert edge for new graph node
*	 @Param		SGN the OLNG_Node which is consist of feature
*/
void OLNG::UpdateNode(std::vector<OLNG_Node> SGN)
{
	int t0 = 0,t1 = 1;

	//step 1. Delete the edge from t0 to t1 and t2
	for(int ki = 0 ; ki < K ;ki++)
	{
		//Delete all inEdge of t1 (cause all of it come from t0)
		std::vector<OLNG_Edge> nullVec;
		//nullVec.swap(m_Graph[t1][ki].inEdge);
		m_Graph[t1][ki].inEdge.swap(nullVec);


		//trace the edge of graph node at time t2 ,then delete the edge start from t0
		for(int ei = 0 ; ei < m_Graph[t1][ki].inEdge.size() ; ei++)
			if(m_Graph[t1][ki].inEdge[ei].ti == t0)
				m_Graph[t1][ki].inEdge.erase(m_Graph[t1][ki].inEdge.begin() + ei);
	}

	//step 2. Moving the Graph node to last id
	for(int ti = t1 ; ti < M ;ti++)
	{
		
		for(int moveki = 0 ; moveki < K ; moveki++)
		{
			//std::cout << "(ti-t1,ki) = ("  << ti-t1 << "," << ki << ")\n";
			m_Graph[ti-t1][moveki] = m_Graph[ti][moveki];
		}
		m_penaltyCost[ti-t1] = m_penaltyCost[ti];
	}


	//step 3. Insert node into M-1 cloumn and edge for new graph node
	float maxCost = 0;
	for(int i = 0; i< SGN.size() ; i++)
	{
		SGN[i].ti = currTime;
		SGN[i].ki = i;
		m_Graph[M-1][i] = SGN[i];

		if(maxCost<SGN[i].nodeCost)
			maxCost = SGN[i].nodeCost;
	}
	m_penaltyCost[M-1] = maxCost*2;
	InsertEdge(M-1);
	InsertEdge(M);
}

/**
*    @Function	PrintOLNG
*    @Brief		output the OLNG at the common line by type
*	 @Param		type decide the output format. 0 : print OLNG, 1: print OLNG with Cost, 2: print OLNG edge 
*    @Return	
*/
//void OLNG::PrintOLNG(int type)
//{
//	switch(type)
//	{
//	case 0:      //print OLNG
//		PrintGraph();
//		break;
//	case 1:		 //print OLNG with Cost
//		PrintGraphWithCost();
//		break;
//	case 2:      //print OLNG edge
//		PrintEdge();
//		break;
//	}
//	system("pause");
//}

/**
*    @Function	PrintGraph
*    @Brief		output OLNG by the frame number of node
*/
//void OLNG::PrintGraph()
//{
//	//print out the graph which is index in the database
//	for(int ki = 0 ; ki < K ; ki++)
//	{
//		for(int ti = 0 ; ti < M ; ti++)
//			std::cout << m_Graph[ti][ki].SignClipInd << "_";
//		std::cout << "\n";
//	}
//	std::cout << "==============================================\n";
//}

/**
*    @Function	PrintEdge
*    @Brief		output OLNG by the frame number of node with edge between node
*/
//void OLNG::PrintEdge()
//{
//	for(int ti = 0 ; ti < this->M ; ti++)
//	{
//		std::cout << "Column " << ti << "_th\n";
//		for(int ki = 0 ; ki < this->K ; ki++)
//		{
//			std::cout << "Node " << ki << "_th\n";
//
//			for(int ei = 0 ; ei < m_Graph[ti][ki].outEdge.size() ; ei++)
//				std::cout << "(" << m_Graph[ti][ki].outEdge[ei].ti << "," << m_Graph[ti][ki].outEdge[ei].ki << ")\n";
//			std::cout << "-----------------------------------------------\n";
//		}
//		std::cout << "==============================================\n";
//	}
//}

/**
*    @Function	PrintGraphWithCost
*    @Brief		output OLNG by the frame number of node with edge between node and cost
*/
//void OLNG::PrintGraphWithCost()
//{
//	std::cout << "PrintGraphWithCost\n";
//	//print out the graph which is index in the database
//	for(int ki = 0 ; ki < K ; ki++)
//	{
//		SetColor(3,0);
//		for(int ti = 0 ; ti < M ; ti++)
//			std::cout << m_Graph[ti][ki].SignClipInd << "_";
//		SetColor();
//		SetColor(5,0);
//		std::cout << "(" << m_finalCost[ki] << ")\n";
//		SetColor();
//	}
//	std::cout << "==============================================\n";
//
//}

/**
*    @Function	SetColor
*    @Brief		set text color at the common line
*/
//void OLNG::SetColor(int f,int b)
//{
//	unsigned short ForeColor=f+16*b;
//	HANDLE hCon = GetStdHandle(STD_OUTPUT_HANDLE);
//	SetConsoleTextAttribute(hCon,ForeColor);
//}
//------------------------------------------------------------------------------
// End of file
