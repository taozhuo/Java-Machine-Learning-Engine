import java.util.*;
import java.lang.Math;

public class DecisionTree {
	
	public List<DecisionTree> children; 
	public int rule;
	public int id;
	public boolean isLeaf;
	public int cat;
	public int numInter;
	public Dataset training;
	public boolean isDeleted;
	public static Map<Integer, Double> thresholdMap;
	public static ArrayList<Integer> selectedFeatures;
	public static int countInter;
	public static int countTotal;
	public static double scoreOfBestTreeFound;  
	public static double globalAccuracy;
	public static int[] globalDelete;
	
	public static Set<String> featureSubset;
	
	public DecisionTree(Dataset input) {
		countTotal++;
		isLeaf=false;
		isDeleted=false;
		cat=0;
		children=new ArrayList<DecisionTree>();
		training=new Dataset();
		training=Dataset.makeCopy(input);
	}

	//calculate the entropy of the given set
	public static double entropy(Dataset training)
	{
		double numPositive=0.0;
		double numNegative=0.0;
		for (Sample s : training.data)
			if (s.category==0) numNegative++;
			else numPositive++;
		numNegative=numNegative/(double)(training.data.size());
		numPositive=numPositive/(double)(training.data.size());
		if (numNegative==0.0 || numPositive==0.0) return 0.0;
		return (-numPositive*(Math.log10(numPositive)/Math.log10(2))
				-numNegative*(Math.log10(numNegative)/Math.log10(2)));
	}
	
	//information gain measurement Gain(S,A)
	public static double infoGain(Dataset training, int attribute)
	{
		double e=DecisionTree.entropy(training);
		List<Dataset> sv=subSets(training, attribute);
		double expected=0.0;
		for (Dataset d : sv)
		{
			if (d.data.size()==0) return 0.0;
		}
		for (Dataset d : sv)
		{
			int size1=d.data.size();
			int size2=training.data.size();
			expected+=((double)size1) / ((double)size2) * entropy(d); 
		}
		return e-expected;
	}
	
	//transfer continuous to discrete
	public void conToDis()
	{
		Dataset training2=Dataset.makeCopy(training);
		for (int i=0;i<training.types.size();i++)
		{
			int type=training.types.get(i).intValue();
			if (type==2 || type==3) continue;
			
			//change this feature into discrete type
			training2.types.set(i, 2);
			training2.specs.get(i).set(0, "0");
			training2.specs.get(i).set(1, "1");
			double[] range=new double[training.data.size()];
			int[] classes=new int[training.data.size()];
			double[] points=new double[training.data.size()];
			int numPoints;
			for (int j=0;j<training.data.size();j++)
			{
				range[j]=training.data.get(j).features.get(i).doubleValue();
				classes[j]=training.data.get(j).category;
			}
			
			//sort the range[] and classes[]
			for(int t=0;t<range.length-1;t++)
			{
				int min=t;
				for (int j = t+1; j<range.length; j++)
					if (range[j] < range[min])       
				          min = j;
				double tmp1=range[t];
				int tmp2=classes[t];
				range[t]=range[min];
				classes[t]=classes[min];
				range[min]=tmp1;
				classes[min]=tmp2;
			}
			
			//find changing points
			int pvalue=classes[0];
			numPoints=0;
			int tag=0;
			for (int k=0;k<range.length;k++)
			{
				tag=Math.abs(pvalue-classes[k]);
				if (tag==0) continue;
				pvalue=classes[k];
				points[numPoints]=(range[k]+range[k-1])/2.0;
				numPoints++;
			}
			
			//find the threshold with highest info-gain
			double max=0.0;
			int maxp=0;
			
			for (int k=0;k<numPoints;k++)
			{
				Dataset training3=Dataset.makeCopy(training2);
				double threshold=points[k];
				for (Sample s : training3.data)
				{
					if (s.features.get(i).doubleValue()>threshold)
						s.features.set(i, new Double(1.0));
					else s.features.set(i, new Double(0.0));
				}
				//calculate info-gain
				double gain=infoGain(training3,i);
				if (gain>max)
				{
					max=gain;
					maxp=k;
				}
			}
			
			//transform the dataset actually
			Integer key=new Integer(i);
			Double value=new Double(points[maxp]);
			thresholdMap.put(key, value);
			for (Sample s : training2.data)
			{
				if (s.features.get(i).doubleValue()>points[maxp])
					s.features.set(i, new Double(1.0));
				else s.features.set(i, new Double(0.0));
			}
		} // end for (i)
		training=training2;
	}
	
	//iteratively build the classifying tree
	public int buildTree(String measure)
	{
		double e=entropy(training);
		if(e==0.0)
		{
			isLeaf=true;
			children=null;
			cat=training.data.get(0).category;
			return 1;
		}
		
		int numValues=training.specs.size();
		double max=0.0;
		int p=0;
		//System.out.println(selectedFeatures);
		if (measure=="")
		for (int i=0;i<numValues;i++)
		{
			if (selectedFeatures!=null && !selectedFeatures.contains(i)) continue;
			if (training.types.get(i).intValue()==3) continue;
			double gain=infoGain(training, i);
			if (gain>max) 
			{
				max=gain;
				p=i;				
			}
		} 
		//dealing with noise
		if (max==0.0)
		{
			int numPos=0;
			int numNeg=0;
			for (Sample s : training.data)
			{
				if (s.category==0) numNeg++;
				if (s.category==1) numPos++;
			}
			if (numNeg>numPos) cat=0;
			else cat=1;
			isLeaf=true;
			children=null;
			return 1;
		}
		countInter++;
		id=countInter;
		SortedMap<Integer, Integer> sm=new TreeMap<Integer, Integer>();
		//record the splitting feature at this level
		rule=p;
		
		
		List<Dataset> newData=new ArrayList<Dataset>();
		newData=subSets(training, p);
		for (Dataset ds : newData)
		{
			ds.featureNames=training.featureNames;
			ds.specs=training.specs;
			ds.types=training.types;
		}
		children=new ArrayList<DecisionTree>();
		for (int i=0;i<newData.size();i++)
		{
			Dataset ds=newData.get(i);
			DecisionTree t=new DecisionTree(ds);
			children.add(t);			
			t.buildTree();
		}
		return 0;
	}
	
	//split the dataset to sub-trees
	public static List<Dataset> subSets(Dataset training, int attribute)
	{
		int numValues=training.specs.get(attribute).size();
		List<Dataset> sv=new ArrayList<Dataset>(numValues);
		for (int i=0;i<numValues;i++)
			sv.add(new Dataset());
		for (Sample s : training.data)
		{
			int p=(int)(s.features.get(attribute).doubleValue());
			sv.get(p).data.add(s);
		}
		return sv;
	}
	
	//traverse the whole tree
	public void traverse(int level)
	{
		//String catString=null;
		if (isLeaf==false)
		{
			countTotal++;
			countInter++;
			String name=training.featureNames.get(rule);
			if (countTotal<=10) 
				{
					System.out.println(name+"(id: "+id+")");

				}
			featureSubset.add(name);
			for (DecisionTree t : children)
			{
				for (int j=0;j<=level;j++)
					System.out.print("\t");
				t.traverse(level+1);
			}
		}
		else if (isLeaf==true)
		{
			countTotal++;
			
			if (countTotal<=10) System.out.println("Leaf"+"(class: "+cat+")");
		}
	}
	
	//copy a tree
	public static DecisionTree copyTree(DecisionTree oldTree)
	{
		//copy primitive types
		Dataset ds=oldTree.training;
		DecisionTree newTree=new DecisionTree(ds);
		newTree.id=oldTree.id;
		newTree.isLeaf=oldTree.isLeaf;
		newTree.rule=oldTree.rule;
		newTree.cat=oldTree.cat;
		if (newTree.isLeaf==true) 
		{
			newTree.children=null;
			return newTree;	
		}
		//copy children trees recursively
		int numChildren=oldTree.children.size();
		newTree.children=new ArrayList<DecisionTree>(numChildren);
		for(int i=0;i<numChildren;i++)
		{
			DecisionTree d1=oldTree.children.get(i);
			DecisionTree d2=copyTree(d1);
			newTree.children.add(i, d2);
		}
		return newTree;
	}
	
	public int classifySample(Sample instance)
	{
		if (isLeaf==true)return cat;
		int branch=(int)(instance.features.get(rule).doubleValue());
		return children.get(branch).classifySample(instance);
	}
	
	public double classifySet(Dataset testSet)
	{
		//change the continuous features
		for (int i=0;i<testSet.types.size();i++)
		{
			int type=testSet.types.get(i).intValue();
			if (type==1)
			{
				testSet.types.set(i, 2);
				testSet.specs.get(i).set(0, "0");
				testSet.specs.get(i).set(1, "1");
				double threshold=thresholdMap.get(new Integer(i)).doubleValue();
				// make changes to the test set
				for (Sample s : testSet.data)
				{
					if (s.features.get(i).doubleValue()>threshold)
						s.features.set(i, new Double(1.0));
					else s.features.set(i, new Double(0.0));
				}
			}
		}
		// make classification 
		int numCorrect=0;
		for (Sample s : testSet.data)
		{
			int cat=classifySample(s);
			//System.out.println("classified as: "+cat
				//	+" / true class: "+s.category);
			if (cat==s.category) numCorrect++;
		}
		//System.out.println("Number of correct: "+numCorrect+" / "
		//		+ testSet.data.size());
		double accuracy=(double)numCorrect/(double)(testSet.data.size());
		//System.out.println("Accuracy is: "+accuracy);
		return accuracy;
	}
	
	//divide the training set into 80% training and 20% tuning
	public Dataset eightyTwenty()
	{
		int len=training.data.size();
		int len1=(int)(training.data.size()*0.8);
		int len2=len-len1;
		Dataset newTraining=Dataset.makeCopy(training);
		Dataset tuning=Dataset.makeCopy(training);
		tuning.data.clear();
		List<Integer> index=new ArrayList<Integer>(len);
		for (int i=0;i<len;i++)
			index.add(new Integer(i));
		Collections.shuffle(index);
		Collections.shuffle(newTraining.data);
		for (int i=0;i<len2;i++)
		{
			Sample s=newTraining.data.get(0);
			tuning.data.add(s);
			newTraining.data.remove(0);
		//	int l1=newTraining.data.size();
		//	int l2=tuning.data.size();
			//System.out.println(l1+" "+l2);
		}
		training=newTraining;
		return tuning;
		
	}
	
	//random pruning, based on score on tuning set
	public DecisionTree pruning(int K, Dataset tuning)
	{
		double bestAccuracy=0;
		double originalAccuracy=classifySet(tuning);
		System.out.println("Original score is: "+originalAccuracy);
		DecisionTree copiedTree=DecisionTree.copyTree(this);
		DecisionTree bestTree=copiedTree;
		int L=100;
		//int[] record;
		// generate R nodes for deletion
		for (int l=0;l<L;l++)
		{
			int R=(int)(Math.random()*(K-1)+1);
			//System.out.println("****** L="+l+"******");
			//System.out.print(R+" nodes deleted: ");
			int[] toDelete=new int[R];
			
			for (int i=0;i<R;i++)
			{
				int candidate=(int)(Math.random()*(countInter-1)+1);
				int j;
				for (j=0;j<toDelete.length;j++)
					if(toDelete[j]==candidate)
						break;
				if (j<toDelete.length) 
				{
					i--;
					continue;
				}	
				toDelete[i]=candidate;
			}
			Arrays.sort(toDelete);
		//	for (int i : toDelete)
		//		System.out.print(i+" ");
		//	System.out.println();
			DecisionTree CopiedAndPruned=DecisionTree.copyTree(copiedTree);
			//CopiedAndPruned.traverse();
			for(int i : toDelete)
			{
				CopiedAndPruned.findAndCut(i);
			}
			double accuracy=CopiedAndPruned.classifySet(tuning);
		//	System.out.println("Accuracy is: "+accuracy);
			if (accuracy>bestAccuracy) 
			{
				bestTree=DecisionTree.copyTree(CopiedAndPruned);
				bestAccuracy=accuracy;
				globalAccuracy=accuracy;
				globalDelete=new int[R];
				for (int ii=0;ii<R;ii++)
					globalDelete[ii]=toDelete[ii];
			}
		}//for(l)
		if (globalAccuracy>originalAccuracy)
		{
			System.out.println("*******Found a better Tree!********");
			System.out.println("Best score is: "+globalAccuracy);
			System.out.print("By deleting nodes(id):  ");
			for (int i : globalDelete)
				System.out.print(i+" ");
			System.out.println("");
			//bestTree.traverse();
		}
		else 
		{
			System.out.println("*******No better tree found!********");
		}
		return bestTree;
	}
	
	// find and prune the specified node
	public void findAndCut(int toDelete)
	{
		if (isLeaf==true) return;
		if (toDelete==id)
		{
			isDeleted=true;
			Dataset ds=training;
			int numNeg=0;
			int numPos=0;
			for (Sample s : ds.data)
			{
				if (s.category==0) numNeg++;
				else numPos++;
			}
			if (numNeg>numPos) cat=0;
			else cat=1;
			id=0;
			isLeaf=true;
			children=null;
			return;
		}
		for (DecisionTree subTree : children)
		{
			subTree.findAndCut(toDelete);
		}
	}
	
	public void DUMP_TREE()
	{
		traverse(0);
	}
	
	public void REPORT_TREE_SIZE()
	{
		System.out.println("No. internal nodes:"+ countInter);
		System.out.println("No. total nodes:"+ countTotal);
	}
	
	//main function
	public static void main(String[] args) {
		//cross validation
		/*Dataset nasa=new Dataset();
		nasa.readData("housing.names", "housing.data");
		CrossValidation.divide(nasa, "housing.data");
		*/
		
		//read in training and test sets
		Dataset training=new Dataset();
		training.readData(args[0], args[1]);
		Dataset test=new Dataset();
		test.readData(args[0], args[2]);
		
		//build training tree
		DecisionTree.countTotal=0;
		DecisionTree.countInter=0;
		DecisionTree.scoreOfBestTreeFound=0.0;
		DecisionTree.selectedFeatures=null;
		DecisionTree root=new DecisionTree(training);
		DecisionTree.thresholdMap=new TreeMap<Integer, Double>();
		root.conToDis();
		
		//divide the training set into 80% and 20%
		Dataset tuning=root.eightyTwenty();
		root.buildTree();
		
		featureSubset=new TreeSet<String>();
		System.out.println("Before pruning, the learned tree(pre-order traverse) is: ");
		countInter=0;
		countTotal=0;
		root.traverse(0);
		System.out.println("\nomitted nodes below......\n");
		System.out.println("Feature subset in this tree: ");
		System.out.println(featureSubset);
		System.out.println();
		
		System.out.println("Number of internal nodes is: "+DecisionTree.countInter);
		System.out.println("Total nodes: "+DecisionTree.countTotal);
		System.out.println();
		
		//make stochastic pruning with tuning set
		System.out.println("*************Now doing pruning***********");
		DecisionTree tree=root.pruning(5,tuning);
		root=tree;
		System.out.println();
		countInter=0;
		countTotal=0;
		
		System.out.println("After pruning, the learned tree(pre-order traverse) is: ");
		root.traverse(0);
		System.out.println("\nomitted nodes below......\n");

		System.out.println();
		System.out.println("Number of internal nodes is: "+DecisionTree.countInter);
		System.out.println("Total nodes: "+DecisionTree.countTotal);
		System.out.println();
		
		//make classification on test set
		System.out.println("*************Now doing testing***********");
		double accuracy=root.classifySet(test);
		System.out.println("Accuracy: "+accuracy);		
	}
}
