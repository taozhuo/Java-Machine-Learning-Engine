import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class Dataset {
	
	public ArrayList<Sample> data;
	public List<Integer> types;
	public String cat0;
	public String cat1;
	public List<List<String>> specs; 
	public List<String> featureNames;
	public Dataset(Dataset training)
	{
		data=new ArrayList<Sample>(training.data);
		types=new ArrayList<Integer>(training.types);
		specs=new ArrayList<List<String>>(training.specs);
		featureNames=new ArrayList<String>(training.featureNames);
	}
	
	public static Dataset makeCopy(Dataset oldSet)
	{
		Dataset newSet=new Dataset();
		
		// copy cat0 and cat1
		newSet.cat0=oldSet.cat0;
		newSet.cat1=oldSet.cat1;
		
		//copy data field
		newSet.data=new ArrayList<Sample>(oldSet.data.size());
		for (int i=0;i<oldSet.data.size();i++)
		{
			List<Double> newList=new ArrayList<Double>();
			List<Double> oldList=oldSet.data.get(i).features;
			for (Double d : oldList)
			{
				double newd=d.doubleValue();
				newList.add(new Double(newd));
			}
			Sample s=new Sample(newList, oldSet.data.get(i).category);
			newSet.data.add(s);
		}
		
		//copy types field
		newSet.types=new ArrayList<Integer>();
		for (Integer in : oldSet.types)
		{
			int iv=in.intValue();
			newSet.types.add(new Integer(iv));
		}
		
		//copy featureNames field
		newSet.featureNames=new ArrayList<String>();
		for (String str : oldSet.featureNames)
		{
			newSet.featureNames.add(new String(str));
		}
		
		//copy specs field
		newSet.specs=new ArrayList<List<String>>();
		for (List<String> oldStringList : oldSet.specs)
		{
			List<String> newStringList=new ArrayList<String>();
			for (String str : oldStringList)
			{
				newStringList.add(new String(str));
			}
			newSet.specs.add(newStringList);
		}
		return newSet;
		
	}
	public Dataset() {
		data=new ArrayList<Sample>();
		types=new ArrayList<Integer>();
		specs=new ArrayList<List<String>>();
		featureNames=new ArrayList<String>();
	}
	
	public double[][] transfer()
	{
		int row=data.size();
		int col=types.size();
		double[][] target=new double[row][col];
		for (int i=0;i<row;i++)
			for (int j=0;j<col;j++)
				target[i][j]=data.get(i).features.get(j);
		return target;
	}
	
	public void readData(String fileName1, String fileName2)
	{
		try{
		BufferedReader in1
        = new BufferedReader(new FileReader(fileName1));
		String line;
		int numFeatures=0;
		//read specification from examples.names
		while (true)
		{
			line=in1.readLine();
			if (line==null) break;
			if (line.equals(""))
            {
                    //System.out.println("it's a blank line");
                    continue;
            }
			if (line.startsWith("//")) 
            {
                    //System.out.println("it's a //");
                    continue;
            }
			numFeatures++;
			StringTokenizer token
            	= new StringTokenizer(line, " ,\t\n\r");
			String name;
            name=token.nextToken();
			featureNames.add(name);
			String type=token.nextToken();
			if (type.equals("continuous"))
				types.add(new Integer(1));
			else if (type.equals("discrete"))
				types.add(new Integer(2));
			else if (type.equals("output")) 
				types.add(new Integer(3));
			List<String> v=new ArrayList<String>();
			while(token.hasMoreTokens())
			{
				v.add(token.nextToken());
			}
			specs.add(v);
				
		} // end while
		
		BufferedReader in2
        = new BufferedReader(new FileReader(fileName2));
		while (true)
		{
			List<Double> row=new ArrayList<Double>();
			line=in2.readLine();
			if (line==null) break;
			if (line.equals(""))
            {
                    //System.out.println("it's a blank line");
                    continue;
            }
			if (line.startsWith("//")) 
            {
                    //System.out.println("it's a //");
                    continue;
            }
			StringTokenizer token
            = new StringTokenizer(line, " ,\t");
			Double value;
			int category=0;
			for (int i=0;i<numFeatures;i++)
			{
				String str=token.nextToken();
				if (types.get(i).intValue()==1)
				{
					value=Double.valueOf(str);
					row.add(new Double(value));	
				}
				else if (types.get(i).intValue()==2)
				{
					value=new Double(
							specs.get(i).indexOf(str));
					row.add(new Double(value));	
				}
				else
				{
					value=new Double(
							specs.get(i).indexOf(str));
					row.add(new Double(value));	
					category=specs.get(i).indexOf(str);
				}
					
			}
			data.add(new Sample(row, category));
		}//end while
		numFeatures--;
		/*for (int k=0;k<data.size();k++)
		{
			for (int p=0;p<numFeatures;p++)
				System.out.print(data.get(k).features.get(p)+" ");
			System.out.println(" category is: "+data.get(k).category);
		}*/
		} //try block
		catch (Exception e)
        {
			e.printStackTrace();
		}

	}
	public static void main(String[] args) {
		Dataset nasa=new Dataset();
		nasa.readData(args[0], args[1]);
		//CrossValidation.divide(nasa);
		//Dataset newset=Dataset.makeCopy(nasa);
	}

}
