import java.util.*;

public class HW3 {


	public static void main(String[] args) {
		int len=args.length;
		String[] newargs=new String[3];
		for (int i=0;i<3;i++)
		{
			newargs[i]=args[i];
		}
		if (len==4)
		
			KernelANN.main(newargs);
			else ANN.main(newargs);

	}

}
