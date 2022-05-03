/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/
import java.lang.*;

/**
* Handles both compression and expansion operations for file of varying types. 
* Uses three options for both: normal mode, reset mode, monitor mode. 
*/
public class MyLZW {
    private static final int R = 256; // number of input chars
    private static int L = 65536;     // number of codewords = 2^W
    private static int W = 9;         // codeword width


	/** 
	 * Reads in the bits that make up a file, and checks for patterns within
	 * which, if are numerous enough, allow for compression of the file
	 * Uses three different modes: normal, which detects a maximum of 2^16 prefix codewords,
	 * reset, which throws out all existing codewords after 2^16 of them are compiled, and starts
	 * anew, and monitor, which waits to reset until a certain compression ratio is reached.
	 * @param in String which represents the mode to be used to compress the file
	 */
    public static void compress(String in) { 
		BinaryStdOut.write(in.charAt(0), W); //stores the current mode
	
        String input = BinaryStdIn.readString();

		boolean flag=false;
		double compRatioRatio=0, lastCodeword=0, afterCodewords=0, uncompData=0, compData=0;
		
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;  // R is codeword for EOF
		
        while (input.length() > 0) {
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
			BinaryStdOut.write(st.get(s), W);    // Print s's encoding.
			if(in.equals("m")) { //used to calculate compression ratios
				uncompData+=lengthOfBinary(s);
				compData+=W;
			}
            int t = s.length();
            if (t < input.length() && code < L){
				st.put(input.substring(0, t+1), code++); // Add s to symbol table.
				if(st.size()==Math.pow(2, W)) W++;
			}
			if(in.equals("r")) {
				if(code==L) { //once codebook is full, reset immediately
					st = new TST<Integer>();
					for (int i = 0; i < R; i++)
						st.put("" + (char) i, i);
					code = R+1;  // R is codeword for EOF
					W=9;
				}
			}
			else if(in.equals("m")) {
				if(code==L && !flag) {//store compression ratio once codebook fills
					lastCodeword = uncompData / compData;
					flag=true;
				}
				if(flag) { 
					afterCodewords = uncompData / compData; //calculate successive compression ratio
					compRatioRatio = lastCodeword / afterCodewords;
					if(compRatioRatio>1.1) {//consider first ratio / second ratio
						st = new TST<Integer>();
						for (int i = 0; i < R; i++)
							st.put("" + (char) i, i);
						code = R+1;  // R is codeword for EOF
						W=9;
						uncompData=0; //reset all compression-related data
						compData=0;
						lastCodeword=0;
						afterCodewords=0;
						flag=false;
					}
				}
			}
            input = input.substring(t); // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 
	
	/**
	 * Reads in a String and returns its number of bits, which is used in measuring compression ratio
	 * @param input String which is measured by its number of bits
	 * @return int length of String, in bits
	 */
	public static int lengthOfBinary(String input) {
		StringBuilder build = new StringBuilder();
		
		for(char c : input.toCharArray())
			build.append(Integer.toBinaryString(c));
		
		return build.toString().length();
	}
	
	/**
	 * Directs the file to the appropriate expansion mode, which corresponds to the mode used
	 * to compress it. This is pulled from the first value written to the compressed file
	 */
    public static void expand() {
		Character method = BinaryStdIn.readChar(W);
		
		if(method.equals('n')) doNothingMode();
		else if(method.equals('r')) resetMode();
		else if(method.equals('m')) monitorMode();
		
	}

	/**
	 * Reads in W bits of the compressed file at a time, creating the same codebook used during compression and incrementing
	 * W as appropriate until a maximum value of 2^16 codewords is reached. At this point, the method will continue to used
	 * these same codewords to expand the rest of the document.
	 */
	public static void doNothingMode() {
		String[] st = new String[L];
        int i; // next available codeword value

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // lookahead for EOF

        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];

        while (true) {
            BinaryStdOut.write(val);
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String s = st[codeword];
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < L) {
				st[i++] = val + s.charAt(0);
				if(i==Math.pow(2, W)) W++;
				if(W==17) W=16; //increment W as long as it is less than 17
			}
            val = s;
        }
        BinaryStdOut.close();
	}
	
	/**
	 * Initially similar to doNothingMode(). However, instead of using the full, unchanged codebook for the rest of the file
	 * once 65536 codewords are stored, this method will delete them all and start over with 0 once the limit is reached. 
	 * This process could repeat many times, or zero times. 
	 */
	public static void resetMode() {
		String[] st = new String[L];
        int i; // next available codeword value

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // lookahead for EOF

        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];

        while (true) {
            BinaryStdOut.write(val);
			if(i==L-1) { //reset if codebook is full
				st = new String[L];
				for (i = 0; i < R; i++)
					st[i] = "" + (char) i;
				W=9;
			}
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String s = st[codeword];
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < L) {
				st[i++] = val + s.charAt(0);
				if(i==Math.pow(2, W)) W++;
			}
			val = s;
        }
        BinaryStdOut.close();
	}
	
	/**
	 * Takes elements of both doNothingMode() and resetMode() to expand a file. Once the codebook is filled with prefixes, the 
	 * method will use the full codebook, like doNothingMode(), until a certain compression ratio is reached. At that point, the 
	 * codebook will reset as in resetMode(), and the file again store codewords to the codebook. 
	 */
	public static void monitorMode() {
		String[] st = new String[L];
        int i; // next available codeword value
		boolean flag=false;
		double compRatioRatio=0, lastCodeword=0, afterCodewords=0, uncompData=0, compData=0;

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // lookahead for EOF

        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];
		compData+=W;

        while (true) {
            BinaryStdOut.write(val);
			uncompData+=lengthOfBinary(val);
			if(i==L-1&&!flag) {
				lastCodeword = uncompData / compData;
				flag=true;
			}
			if(flag) { //store compression ratio into other variable
				afterCodewords = uncompData / compData;
				compRatioRatio = lastCodeword / afterCodewords;
				if(compRatioRatio>1.1) { //reset only if ratio of ratio reaches 1.1
					st = new String[L];
					for (i = 0; i < R; i++)
						st[i] = "" + (char) i;
					W=9;
					uncompData=0;
					compData=0;
					lastCodeword=0;
					afterCodewords=0;
					flag=false;
				}
			}
            codeword = BinaryStdIn.readInt(W);
			compData+=W;
			if (codeword == R) break;
			String s = st[codeword];
			if (i == codeword) s = val + val.charAt(0);   // special case hack
			if(!flag) { //increment W normally as long as it will be less than 17
				if (i < L) {
					st[i++] = val + s.charAt(0);
					if(i==Math.pow(2, W)) {
						W++;
					}
				}
			}
			val = s;
        }
        BinaryStdOut.close();
	}
	
	/**
	 * Main method
	 * Diverts file to compression or expansion based on input
	 * Throws exception if input is illegal
	 */
    public static void main(String[] args) {
        if      (args[0].equals("-")) compress(args[1]);
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}
