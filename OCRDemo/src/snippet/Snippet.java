package snippet;

public class Snippet {
	public int[] convByteArr2IntArr(byte[] barr)
	    {
	    	int[] iarr = new int[barr.length];
	    	for (int i = 0 ; i <  iarr.length; i++)
	    	{
	    		iarr[i] = barr[i];
	    	}
	    	return iarr;
	    }
}

