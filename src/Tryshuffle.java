import java.util.*;

public class Tryshuffle {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
			ArrayList<Integer> list1=new ArrayList<Integer>();
			for (int i=1;i<12;i++)
				list1.add(new Integer(i));
			for (int i=1;i<100;i++)
			{
				Collections.shuffle(list1);
				System.out.println(list1);
			}	

	}

}
