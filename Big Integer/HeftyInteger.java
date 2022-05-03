import java.util.Random;

public class HeftyInteger {

	private final byte[] ONE = {(byte) 1};
	private byte[] NEGATIVE_ONE = {(byte)-1};
	private byte[] ZERO = {(byte)0};

	private byte[] val;

	/**
	 * Construct the HeftyInteger from a given byte array
	 * @param b the byte array that this HeftyInteger should represent
	 */
	public HeftyInteger(byte[] b) {
		val = b;
	}

	/**
	 * Return this HeftyInteger's val
	 * @return val
	 */
	public byte[] getVal() {
		return val;
	}

	/**
	 * Return the number of bytes in val
	 * @return length of the val byte array
	 */
	public int length() {
		return val.length;
	}

	/**
	 * Add a new byte as the most significant in this
	 * @param extension the byte to place as most significant
	 */
	public void extend(byte extension) {
		byte[] newv = new byte[val.length + 1];
		newv[0] = extension;
		for (int i = 0; i < val.length; i++) {
			newv[i + 1] = val[i];
		}
		val = newv;
	}

	/**
	 * If this is negative, most significant bit will be 1 meaning most
	 * significant byte will be a negative signed number
	 * @return true if this is negative, false if positive
	 */
	public boolean isNegative() {
		return (val[0] < 0);
	}

	/**
	 * Computes the sum of this and other
	 * @param other the other HeftyInteger to sum with this
	 */
	public HeftyInteger add(HeftyInteger other) {
		byte[] a, b;
		// If operands are of different sizes, put larger first ...
		if (val.length < other.length()) {
			a = other.getVal();
			b = val;
		}
		else {
			a = val;
			b = other.getVal();
		}

		// ... and normalize size for convenience
		if (b.length < a.length) {
			int diff = a.length - b.length;

			byte pad = (byte) 0;
			if (b[0] < 0) {
				pad = (byte) 0xFF;
			}

			byte[] newb = new byte[a.length];
			for (int i = 0; i < diff; i++) {
				newb[i] = pad;
			}

			for (int i = 0; i < b.length; i++) {
				newb[i + diff] = b[i];
			}

			b = newb;
		}

		// Actually compute the add
		int carry = 0;
		byte[] res = new byte[a.length];
		for (int i = a.length - 1; i >= 0; i--) {
			// Be sure to bitmask so that cast of negative bytes does not
			//  introduce spurious 1 bits into result of cast
			carry = ((int) a[i] & 0xFF) + ((int) b[i] & 0xFF) + carry;

			// Assign to next byte
			res[i] = (byte) (carry & 0xFF);

			// Carry remainder over to next byte (always want to shift in 0s)
			carry = carry >>> 8;
		}

		HeftyInteger res_li = new HeftyInteger(res);

		// If both operands are positive, magnitude could increase as a result
		//  of addition
		if (!this.isNegative() && !other.isNegative()) {
			// If we have either a leftover carry value or we used the last
			//  bit in the most significant byte, we need to extend the result
			if (res_li.isNegative()) {
				res_li.extend((byte) carry);
			}
		}
		// Magnitude could also increase if both operands are negative
		else if (this.isNegative() && other.isNegative()) {
			if (!res_li.isNegative()) {
				res_li.extend((byte) 0xFF);
			}
		}

		// Note that result will always be the same size as biggest input
		//  (e.g., -127 + 128 will use 2 bytes to store the result value 1)
		return res_li;
	}

	/**
	 * Negate val using two's complement representation
	 * @return negation of this
	 */
	public HeftyInteger negate() {
		byte[] neg = new byte[val.length];
		int offset = 0;

		// Check to ensure we can represent negation in same length
		//  (e.g., -128 can be represented in 8 bits using two's
		//  complement, +128 requires 9)
		if (val[0] == (byte) 0x80) { // 0x80 is 10000000
			boolean needs_ex = true;
			for (int i = 1; i < val.length; i++) {
				if (val[i] != (byte) 0) {
					needs_ex = false;
					break;
				}
			}
			// if first byte is 0x80 and all others are 0, must extend
			if (needs_ex) {
				neg = new byte[val.length + 1];
				neg[0] = (byte) 0;
				offset = 1;
			}
		}

		// flip all bits
		for (int i  = 0; i < val.length; i++) {
			neg[i + offset] = (byte) ~val[i];
		}

		HeftyInteger neg_li = new HeftyInteger(neg);

		// add 1 to complete two's complement negation
		return neg_li.add(new HeftyInteger(ONE));
	}

	/**
	 * Implement subtraction as simply negation and addition
	 * @param other HeftyInteger to subtract from this
	 * @return difference of this and other
	 */
	public HeftyInteger subtract(HeftyInteger other) {
		return this.add(other.negate());
	}

	/**
	 * Compute the product of this and other
	 * @param other HeftyInteger to multiply by this
	 * @return product of this and other
	 */
	public HeftyInteger multiply(HeftyInteger other) {
		byte[] a,b;
		
		//put the larger value into a
		if (val.length < other.length()) {
			a = other.getVal();
			b = val;
		}
		else {
			a = val;
			b = other.getVal();
		}

		//make both byte arrays the same size
		if (b.length < a.length) {
			int diff = a.length - b.length;

			byte pad = (byte) 0;
			if (b[0] < 0) {
				pad = (byte) 0xFFFF;
			}

			byte[] newb = new byte[a.length];
			for (int i = 0; i < diff; i++) {
				newb[i] = pad;
			}

			for (int i = 0; i < b.length; i++) {
				newb[i + diff] = b[i];
			}

			b = newb;
		}
	
		int carry, aCurr, bCurr, z;
		byte[] temp;
		HeftyInteger product = new HeftyInteger(new byte[2*a.length]), A = new HeftyInteger(a), B = new HeftyInteger(b);
		boolean flagA=false, flagB=false;
		
		//only work with positive numbers, then negate the result if necessary
		if(A.isNegative()) {
			flagA=true;
			A = A.negate();
		}
		if(B.isNegative()) {
			flagB=true;
			B = B.negate();
		}
		
		//retrieve positive values
		a = A.getVal();
		b = B.getVal();

		//grade school-type algorithm
		for (int i=0; i<a.length;i++) {
            for (int j=0; j<b.length;j++){
				temp = new byte[2*a.length]; //init array to hold partial product
				
				z=i+j+1; //properly shift the partial product
				
				aCurr = a[i]; //consider each array one byte at a time
				bCurr = b[j];
				
				//only work with positive numbers
				if(aCurr<0) {
					aCurr+=256; 
				}
				
				if(bCurr<0) { 
					bCurr+=256;
				}
				
				carry = (aCurr*bCurr); //compute mult
				
				temp[z--] = (byte) carry; //store the product
				temp[z] = (byte) ((carry) >> 8); //and its overflow
				
				//add partial product to total
				product = product.add(new HeftyInteger(temp)); 
            }
		}
		//if exactly one input was negative, make output negative
		if((flagA || flagB) && !(flagA && flagB)) product = product.negate();

		return product;
	}
	
	/**
	 * Run the extended Euclidean algorithm on this and other
	 * @param other another HeftyInteger
	 * @return an array structured as follows:
	 *   0:  the GCD of this and other
	 *   1:  a valid x value
	 *   2:  a valid y value
	 * such that this * x + other * y == GCD in index 0
	 */
	public HeftyInteger[] XGCD(HeftyInteger other) {
		HeftyInteger larger, smaller;
		
		//put the larger value into the correct HeftyInt
		if (val.length < other.length()) {
			larger = new HeftyInteger(other.getVal());
			smaller = new HeftyInteger(val);
		}
		else {
			larger = new HeftyInteger(val);
			smaller = new HeftyInteger(other.getVal());
		}
		
		//only work with positive numbers, then negate the result if necessary
		boolean flagA=false, flagB=false;
		if(larger.isNegative()) {larger = larger.negate(); flagA=true;}
		if(smaller.isNegative()) {smaller = smaller.negate(); flagB=true;}
		
		HeftyInteger temp = larger, temp2, div = new HeftyInteger(ZERO);
		
		//holds the result of a/b at each step in the process
		HeftyInteger[] divisions = new HeftyInteger[larger.length()*8];
		
		byte[] mod;
		int y=0;
		
		//while smaller is non-negative
		while(!(smaller.add(new HeftyInteger(NEGATIVE_ONE)).isNegative())) {
			
			//modulus is the result of repeated subtraction
			while(!(larger.add(smaller.negate()).isNegative())) {
				larger = larger.subtract(smaller);
				div = div.add(new HeftyInteger(ONE)); 
			}
			
			//larger now holds value of a%b
			//div now holds value of a/b
			divisions[y] = div;
			y++; //maintains proper index 
			
			div = new HeftyInteger(ZERO);
			
			//swap values for next step in algorithm
			temp = larger; 
			larger = smaller;
			smaller = temp;
		}
		
		HeftyInteger s = new HeftyInteger(ONE), t = new HeftyInteger(ZERO);
		byte[] rep = t.getVal();
		
		//work back up through to calculate s and t
		for(int i=y-1; i>=0; i--) {
			
			//perform the euclidean ops
			temp = t;
			temp2 = divisions[i];
			t = s.subtract(t.multiply(temp2));
			s = temp;
			
			//prevent the multiplied array from growing in size unnecessarily
			//if there's a leading zero, remove it
			int g = t.length() - 1;
			while(t.getVal()[0]==0 && t.getVal().length>larger.length() && g>0) {
				rep = new byte[g];
				
				//reduce length by one
				for(int p=1; p<t.getVal().length; p++) {
					rep[p-1] = 
					t.getVal()[p];
				}
				
				t = new HeftyInteger(rep); //store result
				g--;
			}
		}
		
		return new HeftyInteger[]{larger, s, t};
	}
}