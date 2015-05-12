import java.util.*;

public class NaiveBayes {
	TreeMap<Double, Double> probNeg=new TreeMap<Double, Double>();
	TreeMap<Double, Double> probPos=new TreeMap<Double, Double>();
	List<TreeMap<Double, Double>> negProbList=
			new ArrayList<TreeMap<Double, Double>>();
	List<TreeMap<Double, Double>> posProbList=
			new ArrayList<TreeMap<Double, Double>>();
	Dataset training;
	Dataset test;
	double[] meanListNeg;
	double[] meanListPos;
	double[] varianceListNeg;
	double[] varianceListPos;
	double numNeg;
	double numPos;
	
	public NaiveBayes(Dataset t1, Dataset t2)
	{
		training=t1;
		test=t2;
		for (int i=0;i<training.types.size();i++)
		{
			TreeMap<Double, Double> tm1=new TreeMap<Double, Double>();
			TreeMap<Double, Double> tm2=new TreeMap<Double, Double>();
			negProbList.add(tm1);
			posProbList.add(tm2);			
		}
	}
	
	public void learning()
	{
		//calculate likelihood for discrete features, only store number of counts
		List<Sample> d=new ArrayList<Sample>(training.data);
		numNeg=0.0;
		numPos=0.0;
		meanListNeg=new double[training.types.size()];
		meanListPos=new double[training.types.size()];
		varianceListNeg=new double[training.types.size()];
		varianceListPos=new double[training.types.size()];
		Arrays.fill(meanListNeg, 0.0);
		Arrays.fill(meanListPos, 0.0);
		Arrays.fill(varianceListNeg, 0.0);
		Arrays.fill(varianceListPos, 0.0);
		for(Sample s : d)
		{
			if (s.category==0) numNeg++;
			if (s.category==1) numPos++;
			for(int index=0;index<s.features.size();index++)
			{
				Double f=s.features.get(index);
				int type=training.types.get(index);
				TreeMap<Double, Double> tm1=negProbList.get(index);
				TreeMap<Double, Double> tm2=posProbList.get(index);
				// discrete features
				if (type==2)
				{
					//negative samples
					if (s.category==0)
					{
						if (tm1.containsKey(f)) 
						{
							double count=tm1.get(f).doubleValue();
							tm1.put(f, new Double(count+1));
						}
						else tm1.put(f, new Double(1));
					}					
					//positive samples
					if (s.category==1)
					{
						if (tm2.containsKey(f)) 
						{
							double count=tm2.get(f).doubleValue();
							tm2.put(f, new Double(count+1));	
						}
						else tm2.put(f, new Double(1));
					}					
				}				
				//continuous features
				if (type==1)
				{
					//negative samples
					if (s.category==0)
						meanListNeg[index]+=f.doubleValue();				
					//positive samples
					if (s.category==1)
						meanListPos[index]+=f.doubleValue();					
				}
				
			} 
		} 		
		for(int i=0;i<training.types.size();i++)
		{
			meanListNeg[i]/=numNeg;
			meanListPos[i]/=numPos;
		}		
		//calculate variance list
		for(Sample s : d)
		{
			for (int i=0;i<training.types.size();i++)
			{
				double f=s.features.get(i).doubleValue();
				if (s.category==0)
				{
					varianceListNeg[i]+=Math.pow((f-meanListNeg[i]), 2);
				}
				if (s.category==1)
				{
					varianceListPos[i]+=Math.pow((f-meanListPos[i]), 2);
				}
			}
		}		
		for (int i=0;i<training.types.size();i++)
		{
			varianceListNeg[i]/=numNeg;
			varianceListPos[i]/=numPos;
		}
	} 

	public double classify()
	{
		double likelihoodNeg=0.0;
		double likelihoodPos=0.0;
		int result;
		double correctratio=0.0;
		// calculate the sum of log of likelihood of all features
		for (Sample s : test.data)
		{
			for (int i=0;i<training.types.size();i++)
			{
				Double f=s.features.get(i);
				//discrete features
				if (training.types.get(i)==2)  
				{
		     		double pNeg=0;
		     		if (negProbList.get(i).containsKey(f))
		     		{
		     			pNeg=negProbList.get(i).get(f).doubleValue();
		     		}
		     		pNeg=(pNeg+0.00001)/(numNeg);
					
					double pPos=0;
					if (posProbList.get(i).containsKey(f))
					{
						pPos=posProbList.get(i).get(f).doubleValue();
						
					}
					pPos=(pPos+0.00001)/(numPos);
					
					likelihoodNeg+=Math.log(pNeg);
					likelihoodPos+=Math.log(pPos);
				}
				else if (training.types.get(i)==1) 
				//continuous features
				{
					double meanNeg=meanListNeg[i];
					double meanPos=meanListPos[i];
					double stdDevNeg=Math.sqrt(varianceListNeg[i]);
					double stdDevPos=Math.sqrt(varianceListPos[i]);
					double gaussianNeg=Math.exp(-(f.doubleValue()-meanNeg)/stdDevNeg);
					double gaussianPos=Math.exp(-(f.doubleValue()-meanPos)/stdDevPos);
					likelihoodNeg+=Math.log(gaussianNeg);
					likelihoodPos+=Math.log(gaussianPos);
				}			
			}
			if (likelihoodNeg>likelihoodPos) result=0;
			else result=1;
			if (result==s.category) correctratio++;
		}		
		return correctratio/(double)(test.data.size());
	}
}
