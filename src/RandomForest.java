import java.util.*;

public class RandomForest {

	int numFeatures;
	int numTrees;
	ArrayList<DecisionTree> Forest;
	Dataset training;
	Dataset test;
	
	public RandomForest(int t, String spec, String tr, String te)
	{
		
		numTrees=t;
		training=new Dataset();
		training.readData(spec, tr);
		test=new Dataset();
		test.readData(spec, te);
		Forest=new ArrayList<DecisionTree>();
	}
	
	public void run()
	{
	
		ArrayList<Integer> allFeatures=new ArrayList<Integer>();
		ArrayList<Integer> selectedFeatures=new ArrayList<Integer>();
		for(int i=0;i<training.types.size();i++)
			allFeatures.add(new Integer(i));
		
		//build training tree
		for(int K=0;K<numTrees;K++)
		{
			Collections.shuffle(allFeatures);
			selectedFeatures.clear();
			//select 5 features
			for(int i=0;i<5;i++)
				selectedFeatures.add(allFeatures.get(i));
			//System.out.println(selectedFeatures);
			DecisionTree.selectedFeatures=selectedFeatures;
			DecisionTree.featureSubset=new TreeSet<String>();
			DecisionTree.countTotal=0;
			DecisionTree.countInter=0;
			DecisionTree.scoreOfBestTreeFound=0.0;
			Collections.shuffle(training.data);
			DecisionTree root=new DecisionTree(training);
			
			DecisionTree.thresholdMap=new TreeMap<Integer, Double>();
			root.conToDis();
			
			System.out.println("*************TREE NO. "+K+"**********");
			System.out.println("selected features:"+selectedFeatures);
		
			 
			//divide the training set into 80% and 20%
			ArrayList<Integer> indexFeatures=new ArrayList<Integer>();
			for (int i=0;i<training.types.size();i++)
			{
				
				indexFeatures.add(1);
			}
			root.buildTree("info_gain", indexFeatures);
			
			
			
			double accuracy=root.classifySet(test);
			System.out.println("Accuracy: "+accuracy);	
			root.traverse(0);
			System.out.println();
			System.out.println("Number of internal nodes is: "+DecisionTree.countInter);
			System.out.println("Total nodes: "+DecisionTree.countTotal);
			System.out.println();
		}
	}

	public static void main(String[] args) 
	{

		RandomForest rf=new RandomForest(100,"census_income_filtered_scaled.names","training1.data","test1.data");
		rf.run();

	}

}
