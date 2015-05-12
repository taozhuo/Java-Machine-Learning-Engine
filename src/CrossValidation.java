import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class CrossValidation {
	
	public static void divide(Dataset d, String fileName)
	{
		List<Sample> target=new ArrayList<Sample>(d.data);
		List<Integer> positiveList=new ArrayList<Integer>();
		List<Integer> negativeList=new ArrayList<Integer>();
		for (Sample s : target)
		{
			if (s.category==0) 
				negativeList.add(new Integer(target.indexOf(s)));
			else positiveList.add(new Integer(target.indexOf(s)));
		}
		Collections.shuffle(negativeList);
		Collections.shuffle(positiveList);
		long numNeg=(int)((float)negativeList.size()/10.0);
		long numPos=(int)((float)positiveList.size()/10.0);
		try{
			//read data file into List<String>
            BufferedReader in = 
            	new BufferedReader(new FileReader(fileName));
            List<String> dataString=new ArrayList<String>();
            String line;
            while (true)
            {
                    line=in.readLine();
                    if (line==null) break;
                    if (line.equals("")) continue;
                    if (line.startsWith("//")) continue;
                    dataString.add(line);
            }                              
		for (int i=0;i<10;i++)
		{
			//construct negative portion
			long start=i*numNeg;
			long end=(i+1)*numNeg;
		    BufferedWriter outputTraining = 
		    	new BufferedWriter(new FileWriter("training"+(i+1)+".data"));
		    BufferedWriter outputTest = 
		    	new BufferedWriter(new FileWriter("test"+(i+1)+".data"));
			for (int j=0;j<negativeList.size();j++)
			{
				int p=negativeList.get(j);
				if (j>=start && j<end) 
				{
					outputTest.write(dataString.get(p));
					outputTest.newLine();
				}
				else 
				{
					outputTraining.write(dataString.get(p));
					outputTraining.newLine();
				}
			}
			//construct positive portion
			start=i*numPos;
			end=(i+1)*numPos;
			for (int j=0;j<positiveList.size();j++)
			{
				int p=positiveList.get(j);
				if (j>=start && j<end) 
				{
					outputTest.write(dataString.get(p));
					outputTest.newLine();
				}
				else 
				{
					outputTraining.write(dataString.get(p));
					outputTraining.newLine();
				}
			}
			outputTraining.close();
			outputTest.close();
		}
		}//try block
		catch (Exception e)
        {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Dataset data=new Dataset();
		data.readData(args[0], args[1]);
		CrossValidation.divide(data, args[1]);	
	}

}
