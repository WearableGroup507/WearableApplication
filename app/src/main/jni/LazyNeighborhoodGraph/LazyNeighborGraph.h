#include <iostream>
#include <sstream>
#include <vector>

#ifndef LazyNeighborhoodGraph_H_
#define LazyNeighborhoodGraph_H_
#define HandDOF 34

typedef struct{
	int            k;
	int            t;
}NodeIndex;

typedef struct{
	int            k;
	int            t;
	float          Edgecost;
}SLNG_Edge;

typedef struct{
	int                SignClipInd;
	float              cost;
	std::vector<SLNG_Edge>  DirectEdge;
}SLNG_Node;

class LazyNeighborGraph{
public:
	LazyNeighborGraph():M(8),K(8),topIndex(0){}

	int                              M; //the size of Graph
	int                              K; //the size of a column of node
	int                                           currSize;
	//push the KNN search result data
	std::vector<std::vector<SLNG_Node>>        NeighborGraph;
	std::vector<float>                         penaltyCost;
	std::vector<float>                         finalCost;
	std::vector<int>                           finalparent;
	std::vector<int>                           minCostPath;
	std::vector<int>                           SecondCostPath;
	std::vector<int>                           ThirdCostPath;
	float                                      PathCost;
	int                                        PathLastNode_NGId;
	int                                        topIndex;
	bool                                       IsFull;

	void         PrintGraph();
	void         BackTravel();

	void         initialGraph();
	void         BuildingGraph(std::vector<SLNG_Node> SGN);
	void         UpdateNode(std::vector<SLNG_Node>);
	void         Insert_tEdge(int);
	void         Insert_t1Edge(int,int);
	void         Insert_t2Edge(int,int);
	void         ClearGraph();
	void         GetMinCostPath();
	void         GetMinCostPath_update();

//	void         SetColor(int f=7,int b=0);
};



#endif

