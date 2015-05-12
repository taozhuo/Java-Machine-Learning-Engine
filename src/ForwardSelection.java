import java.util.*;

public class ForwardSelection {

	public static int select(Dataset training, int k)
	{		
		//twenty-eighty splitting
		List<Integer> negList=new ArrayList<Integer>();
		List<Integer> posList=new ArrayList<Integer>();
		int numNeg=0;
		int numPos=0;
		for (Sample s : training.data )
		{
			if (s.category==0) 
			{
				negList.add(new Integer(training.data.indexOf(s)));
				numNeg++;
			}
			if (s.category==1) 
			{
				
				posList.add(new Integer(training.data.indexOf(s)));
				numPos++;
			}
		}
		int numTune=(int)(training.data.size()*0.1);
		Dataset tuning=Dataset.makeCopy(training);
		Dataset newtraining=Dataset.makeCopy(training);
		tuning.data=new ArrayList<Sample>();		
		for(int i=0;i<numTune;i++)
		{
			tuning.data.add(training.data.get(negList.get(i).intValue()));
			tuning.data.add(training.data.get(posList.get(i).intValue()));
			newtraining.data.remove(negList.get(i).intValue());
			newtraining.data.remove(posList.get(i).intValue());
		}		
		//now select bestN features
		int bestN=0;
		KNN knnForward=new KNN(newtraining, tuning);
		knnForward.setK(k);		
		double correctRatio=0.0;
		while(bestN<=training.types.size())
		{
			bestN++;
			knnForward.setN(bestN);
			double ratio=knnForward.classify();
			if (ratio>=correctRatio) 
			{
				correctRatio=ratio;
			}			
			else break; 			
		}
		System.out.println("correct ratio on subset of features:" + correctRatio);
		return bestN-1;
		
	}
	public static void main(String[] args) {
		
	}

}
