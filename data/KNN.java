import java.util.*;
import java.lang.Math;
import java.util.*;

public class KNN {

	public Dataset training;
	public Dataset test;
	public int numNeighbor;
	
	//set parameter k
	public void setK(int k)
	{
		numNeighbor=k;
	}
	
	// Euclidean distance
	private double distance(Sample s1, Sample s2)
	{
		double d=0;
		int dim=s1.features.size();
		for (int i=0;i<dim;i++)
		{	
			int type=training.types.get(i);
			if (type==1)
				d+=Math.pow((s1.features.get(i)-
						s2.features.get(i)),2.0);
			else if (type==2)
			{
				if (s1.features.get(i).doubleValue()==
					s2.features.get(i).doubleValue())
				d+=0.0;
				else d+=1.0;		
			}
		}
		d=Math.sqrt(d);
		return d;
	}
	
	KNN()
	{}
	
	//constructor
	KNN(Dataset t1, Dataset t2)
	{
		training=t1;
		test=t2;
		numNeighbor=0;
	}
	
	//make classification on test set
	public double classify()
	{
		//initialization
		double ratio=0.0;
		double[] distances=
			new double[training.data.size()];
		
		int[] index=
			new int[training.data.size()];
		Arrays.fill(index, 0);
		for (int p=0;p<test.data.size();p++)
		{
			//System.out.print("Now do test sample: "+p+",");
			Arrays.fill(distances, 0.0);
			Arrays.fill(index, 0);
			Sample instance=test.data.get(p);
			int cat;
			// calculate the distances
			for (int i=0;i<training.data.size();i++)
			{
				Sample s=training.data.get(i);
				double d=this.distance(s, instance);
				distances[i]=d;
				index[i]=i;
			}
			//sort the distances array
			for(int i=0;i<training.data.size()-1;i++)
			{
				int min=i;
				for (int j = i+1; j<training.data.size(); j++)
					if (distances[j] < distances[min])       
						min = j;
				double tmp1=distances[i];
				int tmp2=index[i];
				distances[i]=distances[min];
				index[i]=index[min];
				distances[min]=tmp1;
				index[min]=tmp2;	
			}
		
			// majority vote
			int numPositive=0;
			int numNegative=0;
			for (int i=0;i<numNeighbor;i++) 
			{
				int pos=index[i];
				if (training.data.get(pos).category==0) numNegative++;
				else numPositive++;
			}
			if (numNegative>numPositive) cat=0;
			else cat=1;
			int trueCat=test.data.get(p).category;
			if (cat==trueCat) 
			{
				ratio++;
				//System.out.print(" is correct, ");
			}
			//else System.out.print(" is wrong, ");
			//System.out.println("total correct, "+ratio);
		}//for(p)
		
		System.out.print("#Correct: "+ratio+", size: "+test.data.size()+", ");
		ratio=ratio/(double)test.data.size();
		return ratio;
	}
	
	
	//find the bset K
	public int findK()
	{
		//initialization
		double best=0.0;
		int bestK=0;
		double[] ratio=new double[5];
		Arrays.fill(ratio, 0.0);
		double[] distances=
			new double[training.data.size()];
		
		int[] index=
			new int[training.data.size()];
		Arrays.fill(index, 0);
		for (int p=0;p<training.data.size();p++)
		{
			
			Arrays.fill(distances, 0.0);
			Arrays.fill(index, 0);
			Sample instance=training.data.get(p);
			int cat;
			// calculate the distances
			int t=0;
			for (int i=0;i<training.data.size();i++)
			{
				if (p==i) continue;
				Sample s=training.data.get(i);
				double d=this.distance(s, instance);
				distances[t]=d;
				index[t]=i;
				t++;
			}
			//sort the distances array
			for(int i=0;i<t-1;i++)
			{
				int min=i;
				for (int j = i+1; j<t; j++)
					if (distances[j] < distances[min])       
						min = j;
				double tmp1=distances[i];
				int tmp2=index[i];
				distances[i]=distances[min];
				index[i]=index[min];
				distances[min]=tmp1;
				index[min]=tmp2;	
			}
		
			// majority vote
			int numPositive;
			int numNegative;
			for (int k=0;k<5;k++)
			{
				numPositive=0;
				numNegative=0;
				for (int i=0;i<2*k+1;i++) 
				{
					int pos=index[i];
					if (training.data.get(pos).category==0) numNegative++;
					else numPositive++;
				}
				if (numNegative>numPositive) cat=0;
				else cat=1;
				int trueCat=training.data.get(p).category;
				if (cat==trueCat) ratio[k]++;
			}
			
		}//for(p)
			//find the best K
		for (int k=0;k<5;k++)
		{
			ratio[k]=(double)ratio[k]/(double)training.data.size();
		//	System.out.println("Correct ratio when K="
	//				+(2*k+1)+" is: "+ratio[k]);
			
			if (ratio[k]>best)
			{
				best=ratio[k];
				bestK=k;
			}
		}

	return bestK;
	}
	
	//main function for testing
	public static void main(String[] args) {
		Dataset training=new Dataset();
		training.readData(args[0], args[1]);
		Dataset test=new Dataset();
		test.readData(args[0], args[2]);
		
		KNN knearest=new KNN(training, test);
		double[] meanList=Statistics.mean(training);
		double[] varianceList=Statistics.variation(training);
		Statistics.nomalize(training, meanList, varianceList);
		int bestK=knearest.findK();
		System.out.println("The best K is: "+(2*bestK+1));
		knearest.setK(2*bestK+1);

		
	}
}
