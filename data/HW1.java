
public class HW1 {	
	public static void main(String[] args) {
		//Cross validation
		/*Dataset nasa=new Dataset();
		nasa.readData(args[0], args[1]);
		System.out.println("*****Now doing KNN test*****");
		CrossValidation.divide(nasa, args[1]);
		*/
		
		
		Dataset training=new Dataset();
		training.readData(args[0], args[1]);
		Dataset test=new Dataset();
		test.readData(args[0], args[2]);
		
		
		
		//normalization
		double[] meanList=Statistics.mean(training);
		double[] varianceList=Statistics.variation(training);
		Statistics.nomalize(training, meanList, varianceList);
		meanList=Statistics.mean(test);
		varianceList=Statistics.variation(test);
		Statistics.nomalize(test, meanList, varianceList);
		
		//KNN test
		System.out.println("***********Now doing KNN test**********");
		KNN knearest=new KNN(training, test);
		int bestK=knearest.findK();
		knearest.setK(2*bestK+1);
		double ratio=knearest.classify();
		System.out.println("Best K is: "+knearest.numNeighbor+
			", Accuracy: "+ratio);
		System.out.println();
		
		//Decision Tree test
		System.out.println("***********Now doing Tree Learner test**********");
		DecisionTree.main(args);
		
		/*for (int i=1;i<=10;i++)
		{
			String specFile=args[0];
			String trainingFile="training"+i+".data";
			String testFile="test"+i+".data";	
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
		}*/
		
	}//main
}
