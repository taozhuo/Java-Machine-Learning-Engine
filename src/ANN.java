import java.util.*;

public class ANN {

	public Dataset training;
	public Dataset test;
	public Dataset tuning;
	public ArrayList<Double> w=new ArrayList<Double>();
	public double w0;
	ArrayList<Integer> epochlist=new ArrayList<Integer> ();
	
	ANN(Dataset t1, Dataset t2)
	{
		training=t1;
		test=t2;
		tuning=eightyTwenty();
	}
	
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
			int l1=newTraining.data.size();
			int l2=tuning.data.size();
			//System.out.println(l1+" "+l2);
		}
		training=newTraining;
		return tuning;
		
	}
	public double classify(Dataset dataset)
	{
		double ratio=0;
		int len=training.specs.size();
		for (Sample s : dataset.data)
		{
			double output=0;
			output+=w0;
			for (int i=0;i<len;i++)
			{
				int type=training.types.get(i);
				if (type==1 || type==2)
					output+=w.get(i)*s.features.get(i);
			}
			if (output>0) output=1;
				else output=0;
			if (output==s.category)
			{
				ratio++;
			}
		}
		return ratio/(double)dataset.data.size();
	}
	
	public int trainweight()
	{
		int len=training.specs.size();
		ArrayList<Double> maxw=new ArrayList<Double>();
		w.clear();
		for (int i=0;i<training.specs.size();i++)
		{
			w.add(0.0);
			maxw.add(0.0);
		}
		int epoch=0;
		w0=0; //bias
		double maxw0=0;
		double delta=0.25;
		
		double oldratio=classify(tuning);
		int count=0;
		
		double maxratio=0;
		int k=0;
		for (Sample s : training.data)
		{
			k++;
			double target=s.category;
			if (target==0) target=-1;

			count++;
			if (count==10) 
			{
				count=0;
				//System.out.println(w);
				System.out.println("learning rate="+delta);
			}
			double output=0;
			
			if (oldratio>maxratio)
			{
				maxratio=oldratio;
				for (int j=0;j<len;j++)
				{
					maxw.set(j,w.get(j));
				}
				maxw0=w0;
				epoch=k;
			}

			
			//generate output using current weights
			for (int i=0;i<len;i++)
			{
				int type=training.types.get(i);
				if (type==1 || type==2)
					output+=w.get(i)*s.features.get(i);
			}
			output+=w0;
			if (output>0) output=1;
			else output=-1;
			
			//adjust weights
			for (int i=0;i<len;i++)
			{
				int type=training.types.get(i);
				if (type==3) continue;
				double oldw=w.get(i);
				double neww=oldw+
					delta*(target-output)*s.features.get(i);
				w.set(i, neww);
			}
			
			//adjust bias
			w0=w0+delta*(target-output)*1;
			
			//adjust learning rate
			double newratio=classify(tuning);
			if (newratio>oldratio)	
			{
				delta*=0.99;
				if (newratio>maxratio)
				{
					maxratio=newratio;
				
					for (int j=0;j<len;j++)
					{
						maxw.set(j,w.get(j));
					}
					maxw0=w0;
					epoch=k;
				}

			}				
		if (newratio<oldratio)
			{
				delta*=1.01;			
			}
		}
		
		//restore the best weight found
		for (int j=0;j<len;j++)
		{
			w.set(j,maxw.get(j));
		}
		w0=maxw0;

		epochlist.add(epoch);
		return 0;
	}
	
	public static void main(String[] args) {
			ArrayList<Double> bestw=new ArrayList<Double>();
			ANN tempann=null;

			String specFile=args[0];
			String trainingFile=args[1];
			String testFile=args[2];	

			Dataset training=new Dataset();
			training.readData(specFile, trainingFile);
			Dataset test=new Dataset();
			test.readData(specFile, testFile);
			
			ANN ann=new ANN(training,test);
			System.out.println("********NOW TRAINING******");
			ann.trainweight();
			System.out.println();
		System.out.println("The calculated weights are:");
		//calculate average weights based on cross validatio
		for (int j=0;j<ann.w.size();j++)
		{

			System.out.print("w["+(j+1)+"]="+ann.w.get(j)+" \t ");
			System.out.println(" feature name: "+ann.training.featureNames.get(j));
			
		}
		System.out.println();
		System.out.println("Bias w0="+ann.w0);
		
		System.out.println("\n"+"************TESTING***********");

		double ratio=ann.classify(ann.test);
		System.out.println("Correct ratio="+ratio+"\n");
	}

}
