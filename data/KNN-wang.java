
import java.util.*;
import java.io.*;

/**
* For CS760's homework 1, KNN.java Implement the K-nearest neighbor
*
* @author Shengnan Wang
* @version 1.0
*/
public class KNN{
	
	public int k = 5;
    public int ftSize ;//number of features 
    public double priorRatio = 1.0; //the ratio of priority probabilities of pos over neg 
    public int[] testOutput ;//category of each test example
    public int[] trainOutput ;//category of each training example
    public double[] weights ; //weights of features 
    public String[] ftType ;

    public String[] categories;//possible categories
    public double [][] testData ; 
    public double [][] trainData ; 
    public Vector<Vector<String>> ftPosVals;
        
    /**
    * Constructs a KNN object 
    */    
    public KNN(){
    }
       
    /**
     * initialize the KNN object 
     * @param trainSet
     * @param testSet
     */
    public void initialize(DataReader trainSet, DataReader testSet) throws Exception{
    	this.ftSize = trainSet.ftSize;
    	weights = new double[ftSize];
    	Arrays.fill(weights, 1.0);
      	ftType = new String[ftSize];
      	ftPosVals = new Vector<Vector<String>>();
    	categories = new String[trainSet.posOutputs.size()];
    	trainOutput = new int[trainSet.instances.size()];
    	testOutput = new int[testSet.instances.size()];
    	testData = new double[testSet.instances.size()][ftSize];
    	trainData = new double[trainSet.instances.size()][ftSize];
    	//initialize weights and ftType and ftPosVals
      	Vector<String> vnew = new Vector<String>();
    	for(int i=0; i<ftSize; i++){
      		ftType[i] = trainSet.ftTypes.elementAt(i);
      		for(int j=0; j<trainSet.ftPosVals.elementAt(i).size(); j++)
      			vnew.add(trainSet.ftPosVals.elementAt(i).elementAt(j));
      		ftPosVals.add(vnew);
      	}
      	//read in possible categories	
    	for (int i=0; i<trainSet.posOutputs.size(); i++){
    		categories[i] = trainSet.posOutputs.elementAt(i);
    	}
    	//read in feature vector and category of each training and test example
    	//for discrete feature, store the index of the i-th possible values
    	for (int r = 0; r < trainSet.instances.size(); r++){
    		trainOutput[r] = trainSet.instances.elementAt(r).category;
    		for (int c = 0; c < ftSize; c++){
    			if(ftType[c].equals("continuous"))
    				trainData[r][c] = Double.parseDouble(trainSet.instances.elementAt(r).ftVals.elementAt(c));
    			else // discrete feature
    				trainData[r][c] = ftPosVals.elementAt(c).indexOf(trainSet.instances.elementAt(r).ftVals.elementAt(c));
    		}
    	}
    	for (int r = 0; r < testSet.instances.size(); r++){
    		testOutput[r] = testSet.instances.elementAt(r).category;
    		for (int c = 0; c < ftSize; c++)
    			if(ftType[c].equals("continuous"))
    				testData[r][c] = Double.parseDouble(trainSet.instances.elementAt(r).ftVals.elementAt(c));
    			else // discrete feature
    				testData[r][c] = ftPosVals.elementAt(c).indexOf(trainSet.instances.elementAt(r).ftVals.elementAt(c));
    	}
    }
    /** set the value of K which is the number of nearest neighbors*/
    public void setK(int k){
        this.k = k ;
    }
    
    /**
     * set the priorRatio
     * @param prior, Prob(pos)/Prob(neg)
     */
    public void setPrior(double prior){
    	priorRatio = prior;
    }
    
    /** 
     * set the feature weights
     * @param ftWeights
     */
    public void setWeights(double[] ftWeights){
    	weights = ftWeights.clone();
    }
    
    /**
     * compute distance of two samples
     * @param src, source sample to compare
     * @param des, destined sample to be compared
     * @return the distance between the two example
     */
    public double distance(double[] src, double[] des){
    	double d = 0.0;
    	for(int i=0; i<ftSize; i++){
    		if(weights[i] != 0.0){
    			if(ftType[i].equals("continuous"))
    				d += weights[i] * Math.pow(des[i]-src[i],2);
    			else if(des[i] != src[i])
    				d += weights[i] * 1.0;
    		}
    	}
    	return Math.sqrt(d);
    }
    
    /** Using the first distance function definition, (Euclidena Distance for continuous features)
     * distance of each feature between the test sample and the training samples to get the total distance 
     * @param testEx, the example to be tested
     * @param the predicated category
     * */
    public int getCategory(double[] testEx) throws Exception{
        double [] dis = new double[trainData.length] ; // distance to each training example
        int[] wholeList = new int[trainData.length] ; //index to train label
        Arrays.fill(dis, 0);
    	// compute the distances of the test example to all the training examples
        for (int i = 0 ; i < trainData.length; i ++ ){
        	dis[i] = distance(trainData[i],testEx);
        	wholeList[i] = i ;
        }       
        
        //bubble ordering based on low distance
        for (int i = 0 ; i < trainData.length ; i ++ ) {
            for (int j = 0 ; j < trainData.length-1 ; j ++ ) {
                if(dis[j] > dis[j+1]){
                    double p = dis[j+1] ;
                    dis[j+1] = dis[j] ;
                    dis[j] = p ;
                    
                    //exchange indices
                    int ind = wholeList[j+1] ;
                    wholeList[j+1] = wholeList[j] ;
                    wholeList[j] = ind ;
                } 
            }
        }
//        System.out.print("after sorting;");
//        for(int i = 0; i < dis.length; i++)
//        	System.out.println(wholeList[i] + " dis: " + dis[i]); 
        // count the # of categories of the K nearest neighbors
        int neg = 0 ;
        int pos = 0 ;
        for (int i = 0 ; i < k ; i ++ ) { 
            if(trainOutput[wholeList[i]] == 0) neg++ ;
            if(trainOutput[wholeList[i]] == 1) pos++ ;
            System.out.print(" index:"+ wholeList[i] + "out:"+ trainOutput[wholeList[i]]);
        }
        System.out.println("");
        
      //  System.out.println("neg:" + neg + ";" + "pos:" + pos);
        if(neg > pos) return(0) ;//k is odd, neg and pos can't be equal
        return(1) ;
    } 
    
    /**
     * test on the whole test set and calculate the accuracy
     * @return accuracy
     * @throws Exception
     */
    public double getAccuracy() throws Exception{
    	double correctCnt = 0;
    	try{
    		for(int i = 0; i<testData.length; i++){
    			if(testOutput[i] == getCategory(testData[i])) 
    				correctCnt += 1.0;
    	
    			System.out.println("predic:"+ getCategory(testData[i]) + "true:"+ testOutput[i]);
    			}
    		return correctCnt/testData.length;
    	}catch(Exception e){
    		throw new Exception("\ngetAccuracy Exception: "+e.getMessage());
    	}
    }
    
   /**
    * find the best K using leave one out testing within the training set
    * @return
    */
   public int findK(){
	    double bestRatio=0.0;
	    int category; // initialized to be negative
		int bestK = 0;
		int N = trainData.length;
		int[] kvalue = {1,3,5,7,9,11,13,15,25,37,51};
		double[] accuracy=new double[kvalue.length];
		double[] dis = new double[N];
		Arrays.fill(accuracy, 0.0);
        Arrays.fill(dis, 0);
		int[] wholeList = new int[N] ; //index to train label
        for(int i = 0; i<wholeList.length; i++)
        	wholeList[i] = i;
        for(int i=0; i < N; i++){
        	for(int j=0; j< N; j++){
        		if(j == i) // distance to self is 0
        			continue;
        		dis[j] = distance(trainData[i],trainData[j]);
        	}
        	//bubble ordering based on low distance
            for (int ii = 0 ; ii < dis.length; ii ++ ) {
                for (int jj = 0 ; jj < dis.length -1 ; jj ++ ) {
                    if(dis[jj] > dis[jj+1]){
                        double p = dis[jj+1] ;
                        dis[jj+1] = dis[jj] ;
                        dis[jj] = p ;
                        
                        //exchange indices
                        int ind = wholeList[jj+1] ;
                        wholeList[jj+1] = wholeList[jj] ;
                        wholeList[jj] = ind ;
                    } 
                }
            }
            
            int neg = 0 ;
            int pos = 0 ;
            
            for (int k = 0 ; k < kvalue.length ; k ++ ) { 
            	for(int p=0; p < kvalue[k]; p++){
            		
            			if(trainOutput[wholeList[p]] == 0) neg++ ;
            			else pos ++;  
            		
            	}
            	if(pos > neg) category = 1;
            	else category = 0;
        	    if(category == trainOutput[i]) accuracy[k]++ ;
            }
        }
        
            for (int k = 1; k < kvalue.length; k++){
            	accuracy[k] = 1.0*accuracy[k]/(N-1);
            	if(accuracy[k] > bestRatio){
            		bestRatio = accuracy[k];
            		bestK = kvalue[k]; //exclude self
            	}
             }
    return bestK;
}

   public void run() throws Exception {
    	/*PrintStream output = new PrintStream(new FileOutputStream("KNN_results_K=" + k + ".txt")) ; 
    	output.println("Using the first similarity function for KNN: ") ;
    	int correctNum = 0 ;
        for (int i = 0 ; i < testData.length; i ++ ) {
            if(testOutput[i] == getCategory(testData[i]))
                            correctNum ++ ;	
            else{
                output.printf("%d-th test example is categorized incorrectly", i) ;
            }
        }
        output.println("The overall fraction correct on the test phrases is " + correctNum+ "/" + testData.length + " = " + 1.0*correctNum/testData.length) ;*/
      	System.out.println("K:= " + k);
    	System.out.println("Using the first similarity function for KNN: ") ;
    	int correctNum = 0 ;
        for (int i = 0 ; i < testData.length; i ++ ) {
            if(testOutput[i] == getCategory(testData[i]))
                            correctNum ++ ;	
           // else{
              //  System.out.printf("%d-th test example is categorized incorrectly\n", i) ;
          //  }
        }
        System.out.println("The overall fraction correct on the test phrases is " + correctNum+ "/" + testData.length + " = " + 1.0*correctNum/testData.length) ;
     }
    
    
    public static void main(String args[]) throws Exception { 
        if (args.length != 3) {
            System.err.println("Usage: java KNN *.names trainingSetFile testSetFile"); 
            System.exit(-1); 
        }
        DataReader train = new DataReader(args[0],args[1]);
        DataReader test = new DataReader(args[0],args[2]);
        KNN my = new KNN();
        my.initialize(train, test);
       // System.out.println(my.findK());
	    
//        int[] kvalue = {1,3,5,7,11,15,25,51};
//
//        for(int i = 0; i < kvalue.length; i++)
//        {
//        	my.setK(kvalue[i]);
//        	my.run();
//        }
        my.setK(11);
        System.out.println(my.getAccuracy());
    }

}

