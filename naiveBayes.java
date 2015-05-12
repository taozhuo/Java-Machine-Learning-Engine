import java.util.*;
import java.io.*;

public class naiveBayes {
	
	public static String getlabel(String phrase)
	{
		StringTokenizer st=new StringTokenizer(phrase);
		int i=0;
		while (st.hasMoreTokens()) {
	         String s= st.nextToken();
	         i++;
	         if (i==36) return s;
	     }
		return null;
	}
	
	public static void statistics(TreeMap<String, Double> prob, String phrase)
	{
		StringTokenizer st=new StringTokenizer(phrase);
		int j=0;
		while (st.hasMoreTokens()) 
		{
			String s=st.nextToken();
			j++;
			if (j==8) continue;
			for (int i=0;i<4;i++)
				if (st.hasMoreTokens())
					st.nextToken();
			if (prob.get(s)==null)
				prob.put(s, new Double(1));
			else 
			{
				Double value=new Double(prob.get(s).doubleValue()+1);
				prob.put(s, value);
			}
			//System.out.println(prob);
		}
			
	}
	
	public static void laplace(TreeMap<String, Double> prob, String s)
	{
		Set<String> keyset=prob.keySet();
		Iterator<String> i=keyset.iterator();
		while (i.hasNext())
		{
			String current=i.next();
			Double value=new Double(prob.get(current).doubleValue()+1);
			prob.put(current, value);
		}
		prob.put(s, new Double(1));
	}
	
	public static int classify(TreeMap<String, Double> prob1, 
								TreeMap<String, Double> prob2,
								double prior1, double prior2,
								String phrase)
	{
		StringTokenizer st=new StringTokenizer(phrase);
		int j=0;
		double likelihood1=0.0;
		double likelihood2=0.0;
		double size1=0.0;
		double size2=0.0;
		while (st.hasMoreTokens()) 
		{
			String s=st.nextToken();
			j++;
			if (j==8) continue;
			for (int i=0;i<4;i++)
				if (st.hasMoreTokens())
					st.nextToken();
			
			if (prob1.get(s)==null) laplace(prob1, s);
			double p1=prob1.get(s);
			size1=(double)prob1.size();
			p1=p1/size1;
			likelihood1+=Math.log(p1);
			
			if (prob2.get(s)==null) laplace(prob2, s);
			double p2=prob2.get(s);
			size2=(double)prob2.size();
			p2=p2/size2;
			likelihood2+=Math.log(p2);
			
			//System.out.println(likelihood1+" "+likelihood2);
		}
		likelihood1+=Math.log(prior1);
		likelihood2+=Math.log(prior2);
		//System.out.println(likelihood1+" "+likelihood2);
		if (likelihood1>likelihood2) return 1;
		else return 2;
		
	}
	public static void main(String[] args) {
		String wordX=args[0];
		String wordY=args[1];
		double fraction=Double.valueOf(args[2]).doubleValue();
		String trainfile=args[3];
		String testfile=args[4];
		
		//accept: 2123006  / except: 3341506 = 0.635344063
		//affect: 1395027  / effect: 4228358 = 0.329921686
		//right: 19177722  / write:  5918352 = 3.24038212
		//there: 48380051  / their: 52594052 = 0.919876852
		//too:   12079076  / two:   31500140 = 0.383461026
		double prior2=1/(fraction+1);
		double prior1=1-prior2;
		
		TreeMap<String, Double> prob1=new TreeMap<String, Double>();
		TreeMap<String, Double> prob2=new TreeMap<String, Double>();
		try {
			BufferedReader readtrain=new BufferedReader(new FileReader(trainfile));
			
			//training
			while(true)
			{
				String phrase=readtrain.readLine();
				if (phrase==null) break;
				String label=getlabel(phrase);
				//System.out.println(label);
				if (label.equals(wordX)) statistics(prob1, phrase);
				else if (label.equals(wordY)) statistics(prob2, phrase);
			}
			
			//classifying
			
			BufferedReader readtest=new BufferedReader(new FileReader(testfile));
			double correct=0.0;
			double total=0.0;
			while(true)
			{
				String current1=readtest.readLine();
			//	System.out.println(current1);
				
				String current2=readtest.readLine();
			//	System.out.println(current2);
				
				String current3=readtest.readLine();
			//	System.out.println(current3);
				
				if (current1==null) break;
				int result=classify(prob1,prob2,prior1, prior2,current1);
				if (result==1) 
					{
						System.out.println(getlabel(current1)+" "+wordX);
						if (wordX.equals(getlabel(current1)))
								correct++;
					}
				else 
					{
						System.out.println(getlabel(current1)+" "+wordY);
						if (wordY.equals(getlabel(current1)))
								correct++;
					}
				
				total++;
				
			}
			System.out.println(correct+" "+total);
			System.out.println(correct/total);
			
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
}
