#include "LazyNeighborhoodGraph/LazyNeighborGraph.h"

/*initialize graph*/
void LazyNeighborGraph::initialGraph(){
	
	//parameter setting	
	currSize = 0;
	IsFull = false;

	finalCost.resize(K);
	for(int i = 0 ; i < K ; i++)
		finalCost[i] = 0;

	penaltyCost.resize(M);
	for(int i = 0 ; i < M ; i++)
		penaltyCost[i] = 0;
}

/*Build Lazy Neighborhood Graph*/
void LazyNeighborGraph::BuildingGraph(std::vector<SLNG_Node> SGN){
	/*Add Node to SLNG*/

// 	std::cout << "SGN result\n";
// 	for(int i = 0 ; i < SGN.size() ; i++)
// 		std::cout << "ID = " << SGN[i].SignClipInd << " cost = " << SGN[i].cost << "\n";
// 	std::cout << "----------------------------------------------------------\n";

	if(topIndex == 0)
		initialGraph();

	if(topIndex < M)
	{		
		//add column node to graph
		NeighborGraph.push_back(SGN);

		//find max cost value for the penalty cost as inserting the edge
		float maxCost = -1;
		for(int i =0 ; i < SGN.size() ; i++)
		{
			if(maxCost < SGN[i].cost)
				maxCost = SGN[i].cost;
			// 			std::cout << "SGN["<< i  << "] cost = " << SGN[i].cost << std::endl;
			// 			system("pause");
		}
		//std::cout << "max = " << maxCost << std::endl;
		penaltyCost[topIndex] = 2*maxCost;
		//std::cout << "------------------------------------\n";

		Insert_tEdge(topIndex);
		if(topIndex>=1)
			Insert_t1Edge(topIndex-1,topIndex);
		if(topIndex>=2)
			Insert_t2Edge(topIndex-2,topIndex);

		topIndex++;
	}
	else
	{
		
		GetMinCostPath();
		
		if(!IsFull)
			IsFull = true;
		
		UpdateNode(SGN);
	}
}

/*Insert Graph Edge for time t1 column*/
void LazyNeighborGraph::Insert_tEdge(int t1){
	//the case of t1 = t2
	int t2 = t1;

// 	std::cout << "Insert_tEdge\n";
// 	std::cout << "(t1,t2) = (" << t1 << "," << t2 << ")\n";
// 	std::cout << "(NeighborGraph[t1],NeighborGraph[t2]) = (" << NeighborGraph[t1].size() << "," << NeighborGraph[t2].size() << ")\n";
		
	for(int k1 = 0 ; k1 < NeighborGraph[t1].size()-1 ; k1++)
	{
		for(int k2 = k1+1 ; k2 < NeighborGraph[t2].size() ; k2++)
		{
			if(NeighborGraph[t1][k1].SignClipInd + 1 == NeighborGraph[t2][k2].SignClipInd)
			{
				SLNG_Edge tmpEdge;
				tmpEdge.t = t2;
				tmpEdge.k = k2;
				tmpEdge.Edgecost = NeighborGraph[t2][k2].cost;
				NeighborGraph[t1][k1].DirectEdge.push_back(tmpEdge);
			}
		}
	}	
}

/*Insert Graph Edge for time t1 and t2 column,which t2 = t1 -1*/
void LazyNeighborGraph::Insert_t1Edge(int t1,int t2){
	//the case of t1 = t2 -1
// 	std::cout << "Insert_t1Edge\n";
// 	std::cout << "(t1,t2) = (" << t1 << "," << t2 << ")\n";
// 	std::cout << "(NeighborGraph[t1],NeighborGraph[t2]) = (" << NeighborGraph[t1].size() << "," << NeighborGraph[t2].size() << ")\n";
	{				
		for(int k1 = 0 ; k1 < NeighborGraph[t1].size()-1 ; k1++)
		{
			for(int k2 = 0 ; k2 < NeighborGraph[t2].size() ; k2++)
			{
				int IndDiff = NeighborGraph[t2][k2].SignClipInd - NeighborGraph[t1][k1].SignClipInd;
				if(IndDiff >=0 && IndDiff <=4)
				{
					SLNG_Edge tmpEdge;
					tmpEdge.t = t2;
					tmpEdge.k = k2;
					tmpEdge.Edgecost = NeighborGraph[t1][k1].cost;
					NeighborGraph[t1][k1].DirectEdge.push_back(tmpEdge);
				}
			}
		}
	}
	
}

/*Insert Graph Edge for time t1 and t2 column,which t2 = t1 -2*/
void LazyNeighborGraph::Insert_t2Edge(int t1,int t2){
	//the case of t1 = t2 -2
// 	std::cout << "Insert_t2Edge\n";
// 	std::cout << "(t1,t2) = (" << t1 << "," << t2 << ")\n";
// 	std::cout << "(NeighborGraph[t1],NeighborGraph[t2]) = (" << NeighborGraph[t1].size() << "," << NeighborGraph[t2].size() << ")\n";

	for(int k1 = 0 ; k1 < NeighborGraph[t1].size()-1 ; k1++)
	{
		for(int k2 = 0 ; k2 < NeighborGraph[t2].size() ; k2++)
		{
			int IndDiff = NeighborGraph[t2][k2].SignClipInd - NeighborGraph[t1][k1].SignClipInd;
			if(IndDiff >=1 && IndDiff <=4)
			{
				SLNG_Edge tmpEdge;
				tmpEdge.t = t2;
				tmpEdge.k = k2;
				tmpEdge.Edgecost = (NeighborGraph[t1][k1].cost) * 2;
				NeighborGraph[t1][k1].DirectEdge.push_back(tmpEdge);
			}
		}
	}
}

/*Update Lazy Neighborhood Graph*/
void LazyNeighborGraph::UpdateNode(std::vector<SLNG_Node> SGN){
	/*step 1  move node and update the node to SLGN*/
	float maxCost = -1;
	for(int i = 1 ; i < M ; i++)
	{
		for(int j = 0; j < K ; j++)
		{
			//move forward
			NeighborGraph[i-1][j] =  NeighborGraph[i][j];
			
			for(int edge_stl = 0 ; edge_stl < NeighborGraph[i-1][j].DirectEdge.size() ; edge_stl++)
			{
				NeighborGraph[i-1][j].DirectEdge[edge_stl].t--;
			}
			

			//add new node
			if(i==M-1)
			{
				NeighborGraph[i][j] =  SGN[j];
				if(maxCost < SGN[j].cost)
					maxCost = SGN[j].cost;
			}
		}
		penaltyCost[i-1] = penaltyCost[i];
	}


	penaltyCost[topIndex-1] = 2*maxCost;

	/*step 2  insert the edge*/
	Insert_tEdge(M-1);
	Insert_t1Edge(M-2,M-1);
	Insert_t2Edge(M-3,M-1);
}

/*Find Out Minimum Cost Path by Dijkstra's Algorithm*/
void LazyNeighborGraph::GetMinCostPath(){
	// 	//ori graph
	//std::cout << "---------------------------------\n ori graph\n";
// 	for(int i =0;i<K;i++)
// 	{
// 		for(int j=0;j<M;j++)
// 			std::cout << NeighborGraph[j][i].cost << " ";
// 		std::cout << std::endl;
// 	}
	//std::cout << "Process Dijkstra\n";
	//0�����s�W���I�s���Ҧ��̥��䪺SLNG�I
	int TotalNodeNum = NeighborGraph.size()*K + 1;
	/*��l��
      1.�]�w���X��false
      2.��U�I���Z�����L���j
    */
	std::vector<float> d(TotalNodeNum,99999);                       //����I���Z��
	std::vector<int>   parent(TotalNodeNum,-1);                       //�������I�����`�I
	std::vector<bool>  visit(TotalNodeNum,false);                    //�O�_���X�L

	/*  �N�_�I���Z���]��0�A���`�I���ۤv  */
	d[0] = 0;
	parent[0] = 0;

	//�C����X�@�I
	for (int i=0 ; i<TotalNodeNum-1 ; i++)
	{
		//�_�I = a, ���I = b
		//��X�Z��d�̤p�����_�I
		int a = -1 ,b = -1 ,min = 99999;
		for (int j=0; j<TotalNodeNum; j++)
		{
			if (!visit[j] && d[j] < min)  //��X�|�����X�L�B�Z���̤p���I
			{
				a = j;
				min = d[j];
			}
		}
		//std::cout << "current process a = " << NeighborGraph[(a-1)/K][(a-1)%K].SignClipInd << "\n";
		if (a == -1) break;  //�䤣��ŦX���I�h����
		visit[a] = true;     //���ŦX���I�A�N����X�]�� true
		
 		//�]�L�Ia���Ҧ���
		int tmpT,tmpK;
		if(a==0)//source node
		{
			for (int j=1; j<=K; j++)
			{
				d[j] = d[a] + NeighborGraph[0][j-1].cost;
				parent[j] = a;
				//std::cout <<"node " << j << " ,node cost : " << NeighborGraph[0][j-1].cost <<"\n";
			}
		}
		else//not source node
		{
			for (int j=0; j<NeighborGraph[(a-1)/K][(a-1)%K].DirectEdge.size(); j++)
			{				
				//std::cout << "( " << NeighborGraph[(a-1)/K][(a-1)%K].DirectEdge[j].t << "," << NeighborGraph[(a-1)/K][(a-1)%K].DirectEdge[j].k << ")\n";
				//��s��a�I��s���I���Z��
				int b = NeighborGraph[(a-1)/K][(a-1)%K].DirectEdge[j].t*K +NeighborGraph[(a-1)/K][(a-1)%K].DirectEdge[j].k+1;
				float w = NeighborGraph[(a-1)/K][(a-1)%K].DirectEdge[j].Edgecost;
				//std::cout << "b = " << b <<"\n";
				if (!visit[b] && d[a] + w < d[b])
				{
					//std::cout << "( " << NeighborGraph[(a-1)/K][(a-1)%K].DirectEdge[j].t << "," << NeighborGraph[(a-1)/K][(a-1)%K].DirectEdge[j].k << ") ";
					d[b] = d[a] + w;
					parent[b] = a;					
				}				
			}
		} 
		//std::cout <<"\n";
		//for(int i = 1 ; i < TotalNodeNum ; i++)
		//	std::cout << parent[i] << " ";
		//std::cout <<"\n--------------------------\n";
	}

 	//Back Traversal for calculate final cost
 	{
 		for(int i = K*(M-1)+1 ; i<TotalNodeNum ; i++)
 		{
			//std::cout << "back traversal = " << i << std::endl;
			float tmpCost = 0;
			int currParent = parent[i];
			/************************************************************************/
			/*��parent�I��-1��ܨS��Path,�]���N�e����penality cost + node cost      */
			/*�_�h���e�lpath����l�I�A�N��l�I�e��penality cost �[�^ path cost      */
			/************************************************************************/
			//std::cout << "current node i = " << i << std::endl;
			if(currParent==-1)
			{
				//std::cout << "Type 1\n";
				tmpCost = NeighborGraph[(i-1)/K][(i-1)%K].cost;
				for(int j = 0 ; j < M-1 ; j++)
					tmpCost += penaltyCost[j];
			}
			else
			{			
				//std::cout << "Type 2\n";
				while(1)
				{
					if(currParent==-1)
					{
						for(int j = 0 ; j < (currParent-1)/K ; j++)
							tmpCost += penaltyCost[j];
						break;
					}
					else if(currParent==0)
						break;
					else
					{
						tmpCost += NeighborGraph[(i-1)/K][(i-1)%K].cost;
						currParent = parent[currParent];
						//std::cout << "next Parent node = " << currParent << std::endl;
					}
				}
			}
			
			finalCost[i-(K*(M-1)+1)] = tmpCost;
 		}
		//std::cout << "final cost size = " << finalCost.size()<< std::endl; 
 	}

	//final cost
// 	std::cout << "-----------------------------------\n";
// 	std::cout << "final cost\n";
// 	for(int i = 0 ; i < finalCost.size() ; i++)
// 		std::cout << "final cost[" << i << "]=" << finalCost[i] << "\n";

	//DeleteGraph();
	finalparent = parent;

	//BackTravel();
	/*Update minimum cost path*/	
	float miniCost = 99999,SecondCost = 99999,ThirdCost = 99999;
	int miniIndex = -1  ,SecondIndex = -1  ,ThirdIndex = -1;
	{
		/*find minimum path index*/	
		for(int i = 0 ; i < K ; i++)
		{
			if(finalCost[i] < miniCost)
			{
				miniCost = finalCost[i];
				miniIndex = i;
			}
		}
		PathCost = miniCost;
		PathLastNode_NGId = miniIndex;
		//std::cout << "miniIndex = " << miniIndex << std::endl;

		/*record path node clip index*/
		std::vector<int> tmpClipIndex;
		miniIndex +=57;
		int currParent = finalparent[miniIndex];
		//std::cout << "currParent = " << currParent << std::endl;

		tmpClipIndex.push_back(NeighborGraph[(miniIndex-1)/K][(miniIndex-1)%K].SignClipInd);
		if(currParent!=-1)
		{			
			while(1)
			{
				if(currParent==-1 || currParent==0)	
					break;
				else
				{
					tmpClipIndex.push_back(NeighborGraph[(currParent-1)/K][(currParent-1)%K].SignClipInd);
					currParent = finalparent[currParent];
				}
			}
		}

		if(IsFull)
			minCostPath.push_back(tmpClipIndex[tmpClipIndex.size()-1]);
		else
		{
			/*back pop to minCostPath*/
			for(int i=tmpClipIndex.size()-1 ; i>=0 ; i--)
			{
				minCostPath.push_back(tmpClipIndex[i]);
				//std::cout << tmpClipIndex[i] << " -> ";
			}
		}	

// 		/*print mini path*/
// 		SetColor(4,0);
// 		std::cout << "Cost = " << miniCost << "\n";
// 		for(int i=tmpClipIndex.size()-1 ; i>=0 ; i--)
// 		{
// 			std::cout << tmpClipIndex[i] << " -> ";
// 		}
// 		std::cout << "\n";
	}	
	/*find second mini path*/
	{		
		for(int i = 0 ; i < K ; i++)
		{
			if(finalCost[i] < SecondCost && finalCost[i] > miniCost)
			{
				SecondCost = finalCost[i];
				SecondIndex = i;
			}
		}

		/*record path node clip index*/
		std::vector<int> tmpClipIndex2;
		SecondIndex +=57;
		int currParent = finalparent[SecondIndex];
		//std::cout << "currParent = " << currParent << std::endl;

		tmpClipIndex2.push_back(NeighborGraph[(SecondIndex-1)/K][(SecondIndex-1)%K].SignClipInd);
		if(currParent!=-1)
		{			
			while(1)
			{
				if(currParent==-1 || currParent==0)	
					break;
				else
				{
					tmpClipIndex2.push_back(NeighborGraph[(currParent-1)/K][(currParent-1)%K].SignClipInd);
					currParent = finalparent[currParent];
				}
			}
		}

		if(IsFull)
			SecondCostPath.push_back(tmpClipIndex2[tmpClipIndex2.size()-1]);
		else
		{
			/*back pop to minCostPath*/
			for(int i=tmpClipIndex2.size()-1 ; i>=0 ; i--)
			{
				SecondCostPath.push_back(tmpClipIndex2[i]);
				//std::cout << tmpClipIndex[i] << " -> ";
			}
		}	

// 		/*print second path*/
// 		SetColor(3,0);
// 		std::cout << "Cost = " << SecondCost << "\n";
// 		for(int i=tmpClipIndex2.size()-1 ; i>=0 ; i--)
// 		{
// 			std::cout << tmpClipIndex2[i] << " -> ";
// 		}
// 		std::cout << "\n";
	}

	/*find third mini path*/
	{
		for(int i = 0 ; i < K ; i++)
		{
			if(finalCost[i] < ThirdCost && finalCost[i] > miniCost && finalCost[i] > SecondCost) 
			{
				ThirdCost = finalCost[i];
				ThirdIndex = i;
			}
		}

		/*record path node clip index*/
		std::vector<int> tmpClipIndex3;
		ThirdIndex +=57;
		int currParent = finalparent[ThirdIndex];
		//std::cout << "currParent = " << currParent << std::endl;

		tmpClipIndex3.push_back(NeighborGraph[(ThirdIndex-1)/K][(ThirdIndex-1)%K].SignClipInd);
		if(currParent!=-1)
		{			
			while(1)
			{
				if(currParent==-1 || currParent==0)	
					break;
				else
				{
					tmpClipIndex3.push_back(NeighborGraph[(currParent-1)/K][(currParent-1)%K].SignClipInd);
					currParent = finalparent[currParent];
				}
			}
		}

		if(IsFull)
			ThirdCostPath.push_back(tmpClipIndex3[tmpClipIndex3.size()-1]);
		else
		{
			/*back pop to minCostPath*/
			for(int i=tmpClipIndex3.size()-1 ; i>=0 ; i--)
			{
				ThirdCostPath.push_back(tmpClipIndex3[i]);
				//std::cout << tmpClipIndex[i] << " -> ";
			}
		}	

// 		/*print third path*/
// 		SetColor(2,0);
// 		std::cout << "Cost = " << ThirdCost << "\n";
// 		for(int i=tmpClipIndex3.size()-1 ; i>=0 ; i--)
// 		{
// 			std::cout << tmpClipIndex3[i] << " -> ";
// 		}
// 		std::cout << "\n";
	}
// 		PrintGraph();
//  		SetColor(2,0);		
// 		SetColor();
// 		system("pause");
//  		std::cout << "\n";
//  		SetColor();
 		//system("pause");
		
}

/*Update miniCostPath*/
void LazyNeighborGraph::GetMinCostPath_update(){
	{
		/*find minimum path index*/
		int miniCost = 99999;
		int miniIndex = -1;
		for(int i = 0 ; i < K ; i++)
		{
			if(finalCost[i] < miniCost)
			{
				std::cout << "i = " << i << " finalcost = " << finalCost[i] << "\n";
				miniCost = finalCost[i];
				miniIndex = i;
			}
		}
		miniIndex +=57;
		std::cout << "miniIndex = " << miniIndex << std::endl;
		minCostPath.push_back(NeighborGraph[(miniIndex-1)/K][(miniIndex-1)%K].SignClipInd);
		std::cout << "New Output = "<< NeighborGraph[(miniIndex-1)/K][(miniIndex-1)%K].SignClipInd << " -> \n";
	}
}

/*Clear Graph*/
void LazyNeighborGraph::ClearGraph(){
	//swap the vector to an empty vector can clear it memory
	std::vector<std::vector<SLNG_Node>>        tmpNeighborGraph;
	std::vector<float>                         tmppenaltyCost;
	std::vector<float>                         tmpfinalCost;
	std::vector<int>                           tmpminCostPath;
	std::vector<int>                           tmpminCostPath2;
	std::vector<int>                           tmpminCostPath3;

	NeighborGraph.swap(tmpNeighborGraph);
	penaltyCost.swap(tmppenaltyCost);
	finalCost.swap(tmpfinalCost);
	minCostPath.swap(tmpminCostPath);
	SecondCostPath.swap(tmpminCostPath2);
	ThirdCostPath.swap(tmpminCostPath3);
	
	topIndex = 0;
}

/*Back travel path of Lazy Neighborhood Graph*/
void LazyNeighborGraph::BackTravel()
{	
	std::cout <<"back travel,Graph size = " << NeighborGraph.size() << std::endl;

	//Back Traversal
	{
		for(int i = K*(NeighborGraph.size()-1)+1 ; i<K*NeighborGraph.size() +1 ; i++)
		{
			int currParent = finalparent[i];
			std::cout << "i= " << i << "path =>  " << NeighborGraph[(i-1)/K][(i-1)%K].SignClipInd << " ";
			if(currParent==-1)
			{
				std::cout  << "		cost = "<< finalCost[i-57] << std::endl;
			}
			else
			{			
				while(1)
				{
					if(currParent==-1 || currParent==0)	
					{
						std::cout  << "		cost = "<< finalCost[i-57] << std::endl;
						break;
					}
					else
					{
						std::cout  << "<--" << NeighborGraph[(currParent-1)/K][(currParent-1)%K].SignClipInd << " ";
						currParent = finalparent[currParent];
					}
				}
			}
		}
	}
}

/*Print Out Lazy Neighborhood Graph*/
//void LazyNeighborGraph::PrintGraph()
//{
//	SetColor(6,0);
//	std::cout << "________________________________________________________\n\n";
//	std::cout << "Neighbor Graph:\n";
//	for(int i=0;i<NeighborGraph.size();i++)
//	{
//		for(int j=0;j<K;j++)
//		{
//			std::cout << NeighborGraph[i][j].SignClipInd << " ";
//		}
//		std::cout << std::endl;
//	}
//	std::cout << "________________________________________________________\n\n";
//
//	SetColor(5,0);
//	std::cout << "edge list: Index (t,k,cost)\n";
//	for(int i=0;i< NeighborGraph.size() ; i++)
//	{
//		for(int j =0 ; j< NeighborGraph[i].size() ; j++)
//		{
//			std::cout << NeighborGraph[i][j].SignClipInd;
//			if(NeighborGraph[i][j].DirectEdge.size() > 0)
//			{
//				std::cout  << " size = " << NeighborGraph[i][j].DirectEdge.size() << "\n";
//				for(int m =0 ; m< NeighborGraph[i][j].DirectEdge.size() ; m++)
//				{
//					int tmp_t = NeighborGraph[i][j].DirectEdge[m].t,tmp_k = NeighborGraph[i][j].DirectEdge[m].k;
//					std::cout  << "G( " << tmp_t << "," << tmp_k << ") = " << NeighborGraph[tmp_t][tmp_k].SignClipInd << " "<< NeighborGraph[i][j].DirectEdge[m].Edgecost <<")\n ";
//				}
//			}
//			std::cout << "\n-----------------------\n";
//		}
//
//	}
//	std::cout << "________________________________________________________\n\n";
//
//	SetColor(4,0);
//	std::cout << "mini path\n";
//	for(int i = 0 ; i < minCostPath.size() ; i++)
//	{
//		std::cout << minCostPath[i] << " -> ";
//	}
//	std::cout << "________________________________________________________\n\n";
//	SetColor();
//}

/*Set Common Line Text Color*/
//void LazyNeighborGraph::SetColor(int f,int b)
//{
//	unsigned short ForeColor=f+16*b;
//	HANDLE hCon = GetStdHandle(STD_OUTPUT_HANDLE);
//	SetConsoleTextAttribute(hCon,ForeColor);
//}
