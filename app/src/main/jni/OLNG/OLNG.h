/****************************************************
*    OLNG.h
*    @Author Chin-Feng Chang
*	 @Brief  The data structure reference from the 
*            SIGGRAPH 2011 for motion retrieval
*****************************************************/

//------------------------------------------------------------------------------
// Includes
#include <iostream>
#include <sstream>
#include <vector>
#include <fstream>

#include "EasyLog.h"
#include "Defs/Defs.h"

#ifndef OLNG_H_
#define OLNG_H_
#define PATHSIZE 1

//------------------------------------------------------------------------------
// Definition
typedef struct{
	int ti;             //time of retrieval
	int ki;             //Index of similar motion at time t
	double edgeCost;    // the cost of edge
}OLNG_Edge;


//------------------------------------------------------------------------------
// Class declaration
class OLNG_Node{
public:
	OLNG_Node():ti(0),ki(0),SignClipInd(0),nodeCost(0){}

	//Index of OLNG Node
	int                    ti;                      // time step
	int                    ki;                      // index of node at ti column

	int                    SignClipInd;             // index in the database
	double                 nodeCost;                // the cost of signal from KNN search
	std::vector<OLNG_Edge> inEdge;                  // the edge info which point to this edge
	std::vector<OLNG_Edge> outEdge;                 // the edge info which this node point to.

	OLNG_Node& operator=(const OLNG_Node& node)
	{
		this->ti = node.ti;
		this->ki = node.ki;

		this->SignClipInd = node.SignClipInd;
		this->nodeCost    = node.nodeCost;
		this->inEdge      = node.inEdge;
		this->outEdge     = node.outEdge;
		return *this;
	}
};


class OLNG{
public:
	//(25 , 200) ; (25,40)
	OLNG():M(25),K(KNN*0.8f),currTime(0){  Initial();  }

	/******************************************/
	/*                Class Member            */
	/******************************************/
	//constant number of OLNG
	int                                           M;                   //the window size of Graph (Upper bound)
	int                                           K;                   //the size of a column of node
	int                                           currTime;			   

	//push the KNN search result data								   
	OLNG_Node**                                   m_Graph;             //graph consist of OLNG Node
	float*                                        m_finalCost;         //path cost of last column
	float                                         m_minimumFinalCost;  //the minimum path cost
	bool                                          m_isFull;            //if the graph node is full or not
	bool                                          m_getPath;           //if the motion path is found or not
	float*                                        m_penaltyCost;       //the penalty cost of each column
	float                                         m_penaltySum;        //the sum of all penalty
	

	/******************************************/
	/*              Class Function            */
	/******************************************/
																						   
	void                                          Initial();							   //Initial the Graph memory and parameter
	void                                          ClearGraph();							   //Clear Graph and Initial Graph to Empty
	void                                          BuildGraph(std::vector<OLNG_Node> SGN);  //Build OLNG graph by the input OLNG_Node which is consist of feature
	void                                          InsertEdge(int);						   //insert edge according to valid condition below
	void                                          GetFinalCostPath2();					   //Compute Path Cost
	void                                          UpdateNode(std::vector<OLNG_Node>);	   //update the new graph node as the new input come.
																						   
//	void                                          PrintOLNG(int type);					   //output the OLNG at the common line by type
//	void                                          PrintGraph();							   //output OLNG by the frame number of node
//	void                                          PrintEdge();							   //output OLNG by the frame number of node with edge between node
//	void                                          PrintGraphWithCost();					   //output OLNG by the frame number of node with edge between node and cost
//	void                                          SetColor(int f=7,int b=0);			   //set text color at the common line
};
#endif
//------------------------------------------------------------------------------
// End of file

