import java.util.*;
import java.io.*;
import java.lang.Math;

/**
* For CS540's homework 3, naiveBayes.java Implement the Bayesian Reasoning
*
* @author Shengnan Wang
* @version 1.0
*/
public class naiveBayes{

	/** The number of positions based on the distance to the central word*/
    public int NoOfPositions = 7 ;

    /** The number of features*/
    public int NoOfFeatures = 14 ;
    
    /** The number of Speech Tag*/
    public int NoOfST = 37 ;
    
    /** The number of training samples*/
    public int NoOfTrainingSamples  ;
    
    /** The number of testing samples*/
    public int NoOfTestingSamples ;
    
    /** wordX */
    public String wordX ;
    
    /** wordY */
    public String wordY ;
    
    /** a floating point number, the ratio of occurrences of wordX to that of wordY*/
    public float fraction ;
    
    /** The name of training dataset file*/
    public String trainingFileName ;
    
    /** The name of testing dataset file*/
    public String testingFileName ;
    
    /** Training samples*/
    public String [][] trainData ; 
    
    /** Testing samples*/
    public String [][] testData ; 
    
    /** The central word of the training samples*/
    public String [] trainLabel ;
    
    /** The central word of the testing samples*/
    public String [] testLabel ;
    
    /** The whole phrase of the test samples*/
    public String [] testPhrases ;
    
    /** The conditional probability of the central wordX given its neighbors*/
    public double [][] conditionalProb_X ;
    
    /** The conditional probability of the central wordY given its neighbors*/
    public double [][] conditionalProb_Y ;
    
    /** The counts of the "part of the speech" of words before and after the central word*/
    public int [][] tagCountProb_X;
    
    /** The counts of the "part of the speech" of words before and after the central word*/
    public int [][] tagCountProb_Y;
    
    /** The prior probability of wordX*/
    public double prior_X ;
    
    /** The prior probability of wordX*/
    public double prior_Y ;  
    
    /** to handle unmatched features*/
    public double smooth = 0.000000000001 ;
    
    /** the representation of the part of the speech*/
    public String [] ST ;
    
    /**
    * Creates a naiveBayes object 
    *
    */    
    public naiveBayes(String x, String y, float f, String trainName, String TestName){
        wordX = x ;
        wordY = y ;
        fraction = f ;
        prior_X = fraction/(fraction+1.0) ;
        prior_Y = 1.0/(fraction+1.0) ;
        trainingFileName = trainName ;
        testingFileName = TestName ;
        conditionalProb_X = new double [NoOfFeatures][NoOfST] ;
        conditionalProb_Y = new double [NoOfFeatures][NoOfST] ;
        tagCountProb_X = new int [2][NoOfST];
        tagCountProb_Y = new int [2][NoOfST];
        for(int i = 0; i < 2; i ++)
        	for(int j = 0; j < NoOfST; j ++){
        		tagCountProb_X [i][j] = 1 ;
        		tagCountProb_Y [i][j] = 1 ;
        	}
        		
        ST = new String[NoOfST] ;
        
    }
    
    /** set 'the part of the speech' table to be compared to*/
    public void setST(){
        ST[0]="CC";
        ST[1]="CD";
        ST[2]="DT";
        ST[3]="EX";
        ST[4]="FW";
        ST[5]="IN";
        ST[6]="JJ";
        ST[7]="JJR";
        ST[8]="JJS";
        ST[9]="LS";
        ST[10]="MD";
        ST[11]="NN";
        ST[12]="NNS";
        ST[13]="NNP";
        ST[14]="NNP";
        ST[15]="PDT";
        ST[16]="POS";
        ST[17]="PRP";
        ST[18]="PRP";
        ST[19]="RB";
        ST[20]="RBR";
        ST[21]="RBS";
        ST[22]="RP";
        ST[23]="SYM";
        ST[24]="TO";
        ST[25]="UH";
        ST[26]="VB";
        ST[27]="VBD";
        ST[28]="VBG";
        ST[29]="VBN";
        ST[30]="VBP";
        ST[31]="VBZ";
        ST[32]="WDT";
        ST[33]="WP";
        ST[34]="WP$";
        ST[35]="WRB";
        ST[36]="*";
    }
    
    /** count and return the number of lines read in*/
    public int setNoOfSamples(String dataFile) throws java.io.IOException {
        RandomAccessFile datafile = new RandomAccessFile(dataFile, "r") ;
        int sampleCount = 0 ;
        while (datafile.getFilePointer() < datafile.length()) {
            String currentLine = datafile.readLine();
            if (currentLine.length()>=0)sampleCount ++ ;
        }
        return sampleCount;
    }
    
    /** compute and set the number of training and testing samples*/
    public void setSampleNo() throws java.io.IOException {
    	NoOfTrainingSamples = setNoOfSamples(trainingFileName) ; 
    	NoOfTestingSamples = (setNoOfSamples(testingFileName)/3 + 1) ; //each test sample has 3(right,wrong,blank) lines 
    	//System.out.println(NoOfTrainingSamples + "   " + NoOfTestingSamples) ;
    }	
    
    /** initialize the data*/
    public void initialize(){
        trainData = new String[NoOfTrainingSamples][NoOfFeatures] ;  
    	testData = new String[NoOfTestingSamples][NoOfFeatures] ; 
    	trainLabel = new String[NoOfTrainingSamples] ;
    	testLabel = new String[NoOfTestingSamples] ;
    	testPhrases = new String[NoOfTestingSamples] ;
    }
    
    /** set the feature values of each training and test sample*/
    public void getFeatures() throws java.io.IOException {
    	/** read in training samples*/
    	RandomAccessFile datafile = new RandomAccessFile(trainingFileName, "r") ;
        StringTokenizer st;
        String currentLine ;
        for (int i = 0 ; i < NoOfTrainingSamples ; i ++ ) {
            //System.out.println(i) ;	
            currentLine = datafile.readLine() ;
            st = new StringTokenizer(currentLine, " \n") ;
            for (int j = 0 ; j < NoOfPositions ; j ++ ) {// for 7 words before central word
                st.nextToken() ;
                st.nextToken() ;
                trainData[i][j] = st.nextToken() ; 
                st.nextToken() ;
                st.nextToken() ; 
            }
            
            trainLabel[i] = st.nextToken() ;
            st.nextToken() ; 
            st.nextToken() ; 
            st.nextToken() ;
            st.nextToken() ;
            
            for (int j = NoOfPositions ; j < NoOfFeatures ; j ++ ) {// for 7 words after central word
                st.nextToken() ; 
                st.nextToken() ;
                trainData[i][j] = st.nextToken() ; 
                st.nextToken() ; 
                st.nextToken() ;
            }
        }
        
        datafile = new RandomAccessFile(testingFileName, "r") ;
        for (int i = 0 ; i < NoOfTestingSamples ; i ++ ) {
            currentLine = datafile.readLine() ;
            testPhrases[i] = currentLine ;
            st = new StringTokenizer(currentLine, " \n") ;
            for (int j = 0 ; j < NoOfPositions ; j ++ ) {// for 7 words before central word
                st.nextToken() ;//skip the word
                st.nextToken() ;//skip "["
                testData[i][j] = st.nextToken() ;
                st.nextToken() ;//skip "stem"
                st.nextToken() ;//skip "]"
            }
            
            testLabel[i] = st.nextToken() ;
            st.nextToken() ; //skip "["
            st.nextToken() ; //skip "part of the speech"
            st.nextToken() ; //skip "stem"
            st.nextToken() ; //skip "]"
            
            for (int j = NoOfPositions ; j < NoOfFeatures ; j ++ ) {// for 7 words after central word
                st.nextToken() ;
                st.nextToken() ;
                testData[i][j] = st.nextToken() ; 
                st.nextToken() ;
                st.nextToken() ;
            }
            datafile.readLine() ;
            datafile.readLine() ;
        }
        
    }
    /** estimate the conditional prob's for each feature given the central word*/
    public void estimateProb(){
        for (int i = 0 ; i < NoOfFeatures ; i ++ ) {
            int [] stat = new int[NoOfST] ;
            for (int j = 0 ; j < NoOfST ; j ++ ) {
            	for (int k = 0 ; k < NoOfTrainingSamples ; k++ ){
            	    if((trainLabel[k].equals(wordX))&&(trainData[k][i].equals(ST[j])))stat[j] ++ ;
            	}
            }
            double sum = 0.0 ;
            for (int j = 0 ; j < NoOfST - 1 ; j ++ ) {
                conditionalProb_X[i][j] = ( stat[j] + smooth )/ ( NoOfTrainingSamples + smooth*NoOfST ) ;
                sum = sum + conditionalProb_X[i][j] ; 
                if (i == (NoOfPositions - 1))
                	tagCountProb_X[0][j] = stat[j] ;
                if (i == NoOfPositions)
                	tagCountProb_X[1][j] = stat[j] ;
            }
            // count the # of punctuation, important for the too-two pair
            if (i == (NoOfPositions - 1))
            	tagCountProb_X[0][NoOfST - 1] = stat[NoOfST - 1] ;
            if (i == (NoOfPositions))
            	tagCountProb_X[1][NoOfST - 1] = stat[NoOfST - 1] ;
            conditionalProb_X[i][NoOfST - 1] = 1.0 - sum ;
        }
        
        for (int i = 0 ; i < NoOfFeatures ; i ++ ) {
            int [] stat = new int[NoOfST] ;
            for (int j = 0 ; j < NoOfST ; j ++ ) {
            	for (int k = 0 ; k < NoOfTrainingSamples ; k++ ){
            	    if((trainLabel[k].equals(wordY))&&(trainData[k][i].equals(ST[j])))stat[j] ++ ;
            	}
            }
            double sum = 0.0 ;
            for (int j = 0 ; j < NoOfST - 1 ; j ++ ) {
                conditionalProb_Y[i][j] = ( stat[j] + smooth )/ ( NoOfTrainingSamples + smooth*NoOfST ) ;
                sum = sum + conditionalProb_Y[i][j] ; 
                if (i == (NoOfPositions - 1))
                	tagCountProb_Y[0][j] = stat[j] ;
                if (i == NoOfPositions)
                	tagCountProb_Y[1][j] = stat[j] ;
            }
            // count the # of punctuation
            if (i == (NoOfPositions - 1))
            	tagCountProb_Y[0][NoOfST - 1] = stat[NoOfST - 1] ;
            if (i == (NoOfPositions))
            	tagCountProb_Y[1][NoOfST - 1] = stat[NoOfST - 1] ;
            conditionalProb_Y[i][NoOfST - 1] = 1.0 - sum ;
        }
        
    }
    
    /** predict the central word by Naive Bayes using log*/
    public String getPrediction_1(int index){
        double prob_X = 0.0 ; 
        double prob_Y = 0.0 ; 
        for (int i = 0 ; i < NoOfFeatures ; i ++ ) {
            for (int j = 0 ; j < NoOfST ; j ++ ) {
            	if(testData[index][i].equals(ST[j])){
            	    prob_X = prob_X + Math.log(conditionalProb_X[i][j]) ;
            	    prob_Y = prob_Y + Math.log(conditionalProb_Y[i][j]) ;
            	}    
            }
        }
        prob_X = prob_X + Math.log(prior_X) ;
        prob_Y = prob_Y + Math.log(prior_Y) ;
        //System.out.println(prob_X+ " " + prob_Y + "    " + testLabel[index] ) ;
        if(prob_X>prob_Y)return(wordX);
        return(wordY);
    }
    
    /** predict the central word by Naive Bayes using only two reference tags*/
    public String getPrediction_2(int index){
        double prob_X = 1.0 ; 
        double prob_Y = 1.0 ; 
            for (int j = 0 ; j < NoOfST ; j ++ ) {
            	if(testData[index][NoOfPositions - 1].equals(ST[j])){
            		if(testLabel[index].equals(wordX))
            			prob_X = prob_X * tagCountProb_X[0][j] ;
            		else prob_Y = prob_Y * tagCountProb_Y[0][j] ;
            	}
            	if(testData[index][NoOfPositions].equals(ST[j])){
            		if(testLabel[index].equals(wordX))
            			prob_X = prob_X * tagCountProb_X[0][j] ;
            		else prob_Y = prob_Y * tagCountProb_Y[0][j] ;
            	}
            }
        prob_X = prob_X * prior_X ;
        prob_Y = prob_Y * prior_Y ;
        // System.out.println(prob_X+ " " + prob_Y + "    " + testLabel[index] ) ;
        if(prob_X>prob_Y)return(wordX);
        return(wordY);
    }

    /** run and print out the results of using two diff designs*/
    public void run() throws java.io.IOException {
    	PrintStream output = new PrintStream(new FileOutputStream("NB_results_" + wordX + "_" + wordY + ".txt")) ; 
    	output.println("Using the first design for Naive Bayes: ") ;
    	int correctNum = 0 ;
        for (int i = 0 ; i < NoOfTestingSamples ; i ++ ) {
            String result = getPrediction_1(i) ;
            if(result.equals(testLabel[i])){
                correctNum ++ ;	
            }else{
                output.println(testPhrases[i]) ;
            }
        }
        output.println("The overall fraction correct on the test phrases is " + correctNum+ "/" + NoOfTestingSamples + " = " + 1.0*correctNum/NoOfTestingSamples) ;
        output.println("The number of training phrases read: " + NoOfTrainingSamples ) ;
        output.println("The number of testing phrases read: " + NoOfTestingSamples ) ;
        
        output.println("\nUsing the second design for Naive Bayes: ") ;
    	correctNum = 0 ;
        for (int i = 0 ; i < NoOfTestingSamples ; i ++ ) {
            String result = getPrediction_2(i) ;
            if(result.equals(testLabel[i])){
                correctNum ++ ;	
            }else{
                output.println(testPhrases[i]) ;
            }
        }
        output.println("The overall fraction correct on the test phrases is " + correctNum+ "/" + NoOfTestingSamples + " = " + 1.0*correctNum/NoOfTestingSamples) ;
        output.println("The number of training phrases read: " + NoOfTrainingSamples ) ;
        output.println("The number of testing phrases read: " + NoOfTestingSamples ) ;
    }    
        
    public static void main(String args[]) throws java.io.IOException {
        if (args.length != 5) {
            System.err.println("Usage:  java naiveBayes wordX wordY fractionXoverY fileOfTrainingCases fileOfTestPhrases"); 
            System.exit(-1); 
        }
        String string1 = args[0] ;
        String string2 = args[1] ;
        String string3 = args[2] ;
        String string4 = args[3] ;
        String string5 = args[4] ;
        Float fractionF = new Float(string3) ;
        naiveBayes my = new naiveBayes (string1, string2, fractionF.floatValue(), string4, string5 ) ;
        my.setST() ;
        my.setSampleNo() ;
        my.initialize() ;
        my.getFeatures() ;
        my.estimateProb() ;
        my.run() ;
    }

}