import java.util.*;

public class Statistics {

	//produce the mean feature vectors
	public static double[] mean(Dataset target)
	{
		int len=target.types.size();
		double[] meanList=new double[len];
		Arrays.fill(meanList, 0);
		for (int i=0;i<target.data.size();i++)
		{
			for (int j=0;j<len;j++)
			{
				double x=target.data.get(i).features.get(j).doubleValue();
				meanList[j]+=x;				
			}
		}
		for (int j=0;j<len;j++)
			meanList[j]=meanList[j]/(double)target.data.size();
		return meanList;
	}
	
	//produce the variation vector of features
	public static double[] variation(Dataset target)
	{
		int len=target.data.get(0).features.size();
		double[] meanList=new double[len];
		double[] variationList=new double[len];
		Arrays.fill(meanList, 0.0);
		Arrays.fill(variationList, 0.0);
		for (int i=0;i<target.data.size();i++)
		{
			for (int j=0;j<len;j++)
			{
				double x=target.data.get(i).features.get(j).doubleValue();
				meanList[j]+=x;
				variationList[j]+=Math.pow(x,2);
			}
		}
		for (int j=0;j<len;j++)
		{
			meanList[j]=meanList[j]/(double)target.data.size();
			variationList[j]=variationList[j]/(double)target.data.size();
		}
		for (int j=0;j<len;j++)
		{
			variationList[j]=variationList[j]-Math.pow(meanList[j],2);
		}
		return variationList;
	}
	
	//normalization
	public static void nomalize(Dataset target, double[] meanList, 
								double[] variationList)
	{
		int len=target.types.size();
		for(int i=0;i<target.data.size();i++)
		{
			for(int j=0;j<len;j++)
			{
				double d=target.data.get(i).features.get(j);
				if (target.data.get(i).category==3) continue;
				target.data.get(i).features.set(j,
							(d-meanList[j])/Math.sqrt(variationList[j]));
			}
		}
	}
		
	//main function for testing
	public static void main(String[] args) {
		Dataset nasa=new Dataset();
		nasa.readData(args[0], args[1]);
		double[] meanList=Statistics.mean(nasa);
		double[] varianceList=Statistics.variation(nasa);
		Statistics.nomalize(nasa, meanList, varianceList);

	}

}
