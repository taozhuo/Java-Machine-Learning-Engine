import java.util.*;

public class HW1 {	
	public static void main(String[] args) {

		//KNN TEST
		System.out.println("**********TASK 1: KNN**********");
		{
			for (int i=1;i<=10;i++)
			{
				String specFile=args[0];
				String trainingFile="training"+i+".data";
				String testFile="test"+i+".data";	
				//String specFile=args[0];
				//String trainingFile=args[1];
			//	String testFile=args[2];	
				Dataset training=new Dataset();
				training.readData(specFile, trainingFile);
				Dataset test=new Dataset();
				test.readData(specFile, testFile);
				
				KNN knearest=new KNN(training, test);
				
				//normalization
				double[] meanList=Statistics.mean(training);
				double[] varianceList=Statistics.variation(training);
				Statistics.nomalize(training, meanList, varianceList);
				meanList=Statistics.mean(test);
				varianceList=Statistics.variation(test);
				Statistics.nomalize(test, meanList, varianceList);
				
				
				int bestK=knearest.findK();
				knearest.setK(2*bestK+1);
				double ratio=knearest.classify();
				System.out.println("Test file "+i+" : Best K is: "+knearest.numNeighbor+
					", Accuracy: "+ratio);
			
					System.out.println("Now forward selection:");
					int bestN=ForwardSelection.select(training,bestK);
					System.out.println("The number of features:"+bestN);
					
				
				System.out.println("");
			}
		}
		
		
		System.out.println("**********TASK 2: Naive Bayes**********");

		//BAYESIAN TEST
		{
			for (int i=1;i<=1;i++)
			{
				
				String specFile=args[0];
				String trainingFile="training"+i+".data";
				String testFile="test"+i+".data";	
			//	String specFile=args[0];
			//	String trainingFile=args[1];
			//	String testFile=args[2];		
				Dataset training=new Dataset();
				training.readData(specFile, trainingFile);
				Dataset test=new Dataset();
				test.readData(specFile, testFile);
			//	System.out.println("now start naivebayes");
				
				//learning curve
				int trainsize=training.data.size();
				int testsize=test.data.size();
				int batch=trainsize/9;
				Collections.shuffle(training.data);
				for(int j=0;j<9;j++)
				{
					int fromIndex=batch*(j+1);
					Dataset newtraining=Dataset.makeCopy(training);
					//System.out.println("fromIndex: "+fromIndex+" size:"+
				//			newtraining.data.size());
					for (int rm=fromIndex;rm<trainsize;rm++)
					{
					//	System.out.println("now remove "+rm);
						newtraining.data.remove(fromIndex);
					}
					System.out.println("newsize: "+newtraining.data.size());
					NaiveBayes nb=new NaiveBayes(newtraining, test);
					nb.learning();
					double ratio=nb.classify();
					//System.out.println("Test file:"+testFile+"  Accuracy: "+ratio);
					System.out.println("batch: "+j+"  Accuracy: "+ratio);
				}
				
				//ForwardSelection.select(training);
			}
		}
	}//main
}
