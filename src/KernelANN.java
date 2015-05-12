import java.util.*;

public class KernelANN {

	public Dataset training;
	public Dataset test;
	public Dataset tuning;
	public Dataset exemplar;
	public double[] w;
	double w0;
	public double[][] trainmatrix;
	public double[][] testmatrix;
	public double[][] tuningmatrix;
	public double sigma;
	int labelindex=0;
	
	KernelANN(Dataset t1, Dataset t2, double s)
	{
		training=t1;
		test=t2;
		sigma=s;
		//split the training set into tuning set and exemplar set
		eightyTwenty();
		
		for (int i=0;i<training.specs.size();i++)
		{
			int type=training.types.get(i);
			if (type==1 || type==2) continue;
			labelindex=i;
			break;
		}
		
		//generate the kernel matrix
		kernelmatrix();
	}
	
	
	public int eightyTwenty()
	{
		int len=training.data.size();
		int len1=(int)(training.data.size()*0.8);
		int len2=(int)(training.data.size()*0.1);
		Dataset newTraining=Dataset.makeCopy(training);
		tuning=Dataset.makeCopy(training);
		exemplar=Dataset.makeCopy(training);
		tuning.data.clear();
		exemplar.data.clear();
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
			
			s=newTraining.data.get(0);
			exemplar.data.add(s);
			newTraining.data.remove(0);

		}
		//System.out.println(tuning.data.size());
	//	System.out.println(exemplar.data.size());
		training=newTraining;
		return 0;
		
	}
	
	public double gaussian(Sample s1, Sample s2)
	{
		double result=0;
		double sum=0;
		for(int i=0;i<s1.features.size();i++)
		{
			int type=training.types.get(i);
			
			double diff=0;
			if (type==3) continue;
			if (type==1)
				sum+=Math.pow((s1.features.get(i)-
						s2.features.get(i)),2.0);
			else if (type==2)
			{
				if (s1.features.get(i).doubleValue()==
					s2.features.get(i).doubleValue())
				sum+=0.0;
				else sum+=1.0;		
			}
			
		}
		double r1=Math.pow(sigma,2);
		double r2=-1*sum/r1;
		
		result=Math.exp(r2);
		//System.out.println(result);
		return result;
	}
	
	public int kernelmatrix()
	{
		trainmatrix=
			new double[training.data.size()][exemplar.data.size()];
		testmatrix=
			new double[test.data.size()][exemplar.data.size()];
		tuningmatrix=
			new double[tuning.data.size()][exemplar.data.size()];
		
		for (int i=0;i<training.data.size();i++)
			for(int j=0;j<exemplar.data.size();j++)
			{
				
				trainmatrix[i][j]=gaussian(training.data.get(i), exemplar.data.get(j));
			}
		
		
		for (int i=0;i<test.data.size();i++)
			for(int j=0;j<exemplar.data.size();j++)
			{
				testmatrix[i][j]=
					gaussian(test.data.get(i), exemplar.data.get(j));
			}
		for (int i=0;i<tuning.data.size();i++)
			for(int j=0;j<exemplar.data.size();j++)
			{
				tuningmatrix[i][j]=
					gaussian(tuning.data.get(i), exemplar.data.get(j));
			}
		return 0;
	}
	
	public double classify(double[][] matrix, Dataset dataset)
	{
		double ratio=0;
		int len=exemplar.data.size();
		int num=dataset.data.size();
		for (int i=0;i<num;i++)
		{
			double output=0;
			output+=w0;
			for (int j=0;j<len;j++)
			{
				output+=w[j]*matrix[i][j];
			}
			if (output>0) output=1;
				else output=0;
			
			if (output==dataset.data.get(i).category)
			{
				ratio++;
			}
		}
		return ratio/(double)num;
	}
	
	public int trainweight(double[][] matrix)
	{
		double maxratio=0;
		
		int len=exemplar.data.size();
		w=new double[len];
		double[] maxw=new double[len];
		for (int i=0;i<len;i++)
		{
			
			w[i]=0.0;
			maxw[i]=0.0;
		}
		w0=0; //bias
		double maxw0=0;
		
		//learning rate adjustment
		double eta=0.25;
		
		
		int count=0;
		double olddeltaw;
		for (int i=0;i<training.data.size();i++)
		{
			double target=training.data.get(i).category;
			if (target==0) target=-1;
			double oldratio=classify(tuningmatrix, tuning);
			if (oldratio>maxratio)
			{
				maxratio=oldratio;
				for (int j=0;j<len;j++)
				{
					maxw[j]=w[j];
				}
				maxw0=w0;
			}

			count++;
			if (count==10) 
			{
				count=0;
		//		System.out.print("w=");
		//		for (int i2=0;i<w.length;i++)
		//			System.out.println(w[i2]+" ");
		//		System.out.println();
				System.out.println("learning rate="+eta);
			}
			double output=0;
			
			//generate output using current weights
			for (int j=0;j<len;j++)
			{
				output+=w[j]*matrix[i][j];
			}
			output+=w0;
			if (output>0) output=1;
			else output=-1;
			
			//adjust weights
			for (int j=0;j<len;j++)
			{

				double oldw=w[j];
		//		System.out.println(training.data.get(i).category);
				olddeltaw=eta*(target-output)
				*matrix[i][j];
				double neww=oldw+
					eta*(target-output)
					*matrix[i][j];
				
				w[j]=neww;
				//System.out.println(w[j]);
			}
			
			//adjust bias
			w0=w0+eta*(target-output)*1;
			
			//adjust learning rate
			double newratio= classify(tuningmatrix, tuning);
			
			
			//store the weights with best classifying result
			if (newratio>oldratio)	
				{
					eta*=0.99;
					if (newratio>maxratio)
					{
						maxratio=newratio;
					
						for (int j=0;j<len;j++)
						{
							maxw[j]=w[j];
						}
						maxw0=w0;
					}

				}				
			if (newratio<oldratio)
				{
					eta*=1.01;			
				}
		}

		for (int j=0;j<len;j++)
		{
			w[j]=maxw[j];
		}
		w0=maxw0;
		return 0;
	}
	
	public static void main(String[] args) {
		double[] sigma={0.03, 0.1, 0.3, 1, 3, 10};
		double maxratio=0;
		double bestsigma=0;
		double[] best_w=null;
	
			//for (int i=1;i<=10;i++)
		//	{
				String specFile=args[0];
				String trainingFile=args[1];
				String testFile=args[2];	
	
				Dataset training=new Dataset();
				training.readData(specFile, trainingFile);
				Dataset test=new Dataset();
				test.readData(specFile, testFile);
				KernelANN kann=new KernelANN(training,test,0.3);
				best_w=new double[kann.exemplar.data.size()];
			for (int g=0;g<sigma.length;g++)
			{
				System.out.println("********SIGMA="+sigma[g]+"**********");

				double[] meanList=Statistics.mean(training);
				double[] varianceList=Statistics.variation(training);
				Statistics.nomalize(training, meanList, varianceList);
				
				kann=new KernelANN(training,test,sigma[g]);
				kann.trainweight(kann.trainmatrix);

				
				double ratio=kann.classify(kann.testmatrix, kann.test);
				System.out.println("correct ratio="+ratio+"\n");
				if (ratio>maxratio)
				{
					maxratio=ratio;
					for (int i=0;i<best_w.length;i++)
					{
						best_w[i]=kann.w[i];
					}
					bestsigma=sigma[g];	 
				}


		} //for (int g=0;g<sigma.length;g++)	
		System.out.println("\nBest sigma found is: "+bestsigma);
		System.out.println("Best correct ratio is: "+maxratio);
		System.out.println("The calculated weights via cross-validation are:");
		for(int j=0;j<best_w.length;j++)
		{
			System.out.println("w["+(j+1)+"]="+best_w[j]);
		}
	}
}
