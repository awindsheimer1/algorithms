import java.util.*;
import java.io.*;

/**
 * Creates Apartment objects, which hold data from the input file apartments.txt, 
 * and can be compared against each other using their attributes. 
 */
class Apartment{
	
	public String address;
	public String aptNumber;
	public String city;
	public Integer zip;
	public Integer monthlyRent;
	public Integer sqft;

	/**
	 * Initializes an Apartment object with six attributes, which are initalized from the
	 * input file. monthlyRent can be updated by the user via the menu
	 */
	public Apartment(String address, String aptNumber, String city, int zip, int monthlyRent, int sqft) {		
		this.address = address;
		this.aptNumber = aptNumber;
		this.city = city;
		this.zip = Integer.valueOf(zip);
		this.monthlyRent = Integer.valueOf(monthlyRent);
		this.sqft = Integer.valueOf(sqft);
	}
	
	/**
	 * Prints an Apartment object, which is used to compare with user input
	 */
	public String toString() {
		return ""+address+" "+aptNumber+" "+zip;
	}
}

/**
 * Creates minPQ objects, which in this case are optimized to hold Apartment objects. 
 * Meant to maintain lowest rent Apartment.
 * Contains heap-backed operations such as insert, delete, and changeKey.
 * Uses a series of HashMaps and arrays to complete these operations. 
 */
class MinPQ {
	
	int[] pq; //heap
	int[] qp; //inverse of pq, where pq[qp[i]] = i
	int n; //number of elements in heap
	ArrayList<Integer> avail; //allows insert on top of deleted objects in keys
	int maxN; //initial array size allocation
	Apartment[] keys; //lists apartment objects
	HashMap<String, Apartment> strings = new HashMap<String, Apartment>(); //maps toStrings of Apartments to the objects themselves
	HashMap<Apartment, Integer> indices = new HashMap<Apartment, Integer>(); //maps Apartments to their index in keys
	
	
	/**
	 * Initializes the minPQ object. Accepts an int value from initial input size to 
	 * properly size arrays. Declares HashMaps which allow for fast lookup in the arrays.
	 * An internal size counter is also kept.
	 * @param maxN the initial array sizes
	 */
	@SuppressWarnings("unchecked")
	public MinPQ (int maxN) {
		this.maxN = maxN;
		pq = new int[maxN+1];
		qp = new int[maxN+1];
		n=0;
		keys = new Apartment[maxN+1];
		avail = new ArrayList<Integer>();
		
		for(int i=0; i<maxN+1; i++) {
			qp[i]=-1; //no objects inserted means no objects have any priority
		}
	}
	
	/**
	 * Switches the location of two values in the heap
	 * @param k1, k2 the indices of the objects to be switched
	 */
	private void exch(int k1, int k2) {
		int temp = pq[k1]; //swaps objects' locations in pq
		pq[k1] = pq[k2];
		pq[k2] = temp;
		
		qp[pq[k1]] = k1; //corrects positions in qp
		qp[pq[k2]] = k2;
	}
	
	/**
	 * Moves an object down the heap based on its priority, if necessary
	 * @param k index pointing to object in the heap
	 */
	private void swim(int k) {
		//makes comparisons with "parents" in heap to see if any are of lesser priority
		while(k>1 && less(keys[pq[k/2]],keys[pq[k]])>0) { 
			exch(k, k/2); //if true, swap the objects
			k=k/2; //continue upward
		}
	}
	
	/**
	 * Moves an object up the heap based on its priority, if necessary
	 * @param k index pointing to object in the heap
	 */
	private void sink(int k) {
		//makes comparisons with "children" in heap to see if any are of greater priority
		while(2*k <= n) {
			int j = 2*k;
			if(j+1 <= n && less(keys[pq[j]],keys[pq[j+1]])>0) j++;
			if(less(keys[pq[k]],keys[pq[j]])>0) {
				exch(k,j); //if true, swap on the objects
				k=j; //continue downward
			}
			else break;
		}
	}
	
	/**
	 * Compare two apartments using their monthlyRent attribute.
	 * @return 0 if the rents are equal, <0 if first rent is less, >0 if second rent is less
	 * @param a the first Apartment to be compared
	 * @param b the second Apartment to be compared
	 */
	public int less(Apartment a, Apartment b) {
		return a.monthlyRent.compareTo(b.monthlyRent);
	}
	
	/**
	 * Inserts an object into the heap
	 * @param key the object to be inserted into the heap
	 */
	public void insert(Apartment key) {
		int index=n;
		boolean flag=false;
		//trick to insert values after deletions without resizing arrays as quickly
		if(avail.size()>0) { 
			index=avail.get(0);
			flag=true;
		}
		//if necessary, resize arrays
		if(index>=maxN) resize();
		
		//finally insert value into arrays
		n++;
		qp[index] = n;
		pq[n] = index;
		keys[index] = key;
		swim(n); //check if of higher priority than parents
		
		//insert object into the two Hashmaps
		strings.put(key.toString(), key);
		indices.put(key, index);
		
		//if previously used index was rewritten, pop value from stack
		if(flag) avail.remove(0);
	}
	
	/**
	 * Removes an object from the heap
	 * @param i the location of the object in the heap
	 */
	public void delete(int i) {
		int index = qp[i];
		exch(index, n--); //swap value to end of heap
		swim(index); //adjust swapped object
		sink(index);
		
		//update arrays
		indices.remove(keys[i]);
		keys[i]=null;
		qp[i]=-1;
		avail.add(i); //make space available for new inserts
	}
	
	/**
	 * Returns the minimum key in the heap.
	 * @return Apartment which has the minimum rent
	 */
	public Apartment minKey() {
		return keys[pq[1]];//Object corresponding to priority 1
	}
	
	/**
	 * Changes the value associated with an object in the heap
	 * @param i, the location of the object in the heap
	 * @param key, the new object, which replaces the old one
	 */
	public void changeKey(int i, Apartment key) {
		keys[i]=key; //write on top of old value
		swim(qp[i]); //adjust Object as necessary in heap
		sink(qp[i]);
	}
	
	/**
	 * Upsizes the arrays as necessary
	 */
	public void resize() {
		int newSize = pq.length+maxN; //increase size of arrays
		int[] newPQ = new int[newSize];
		int[] newQP = new int[newSize];
		Apartment[] newKeys = new Apartment[newSize];
		
		for(int i=0; i<pq.length; i++) {//create new arrays
			newPQ[i] = pq[i];
			newQP[i] = qp[i];
			newKeys[i] = keys[i];
		}
		for(int i=pq.length; i<newSize; i++) {
			newQP[i] = -1;
		}
		
		pq = newPQ; //assign new arrays
		qp = newQP;
		keys = newKeys;
	}
	
}

/**
 * Creates maxPQ objects, which in this case are optimized to hold Apartment objects. 
 * Meant to maintain highest square footage Apartment.
 * Contains heap-backed operations such as insert, delete, and changeKey.
 * Uses a series of HashMaps and arrays to complete these operations. 
 */
class MaxPQ {
	
	int[] pq; //heap
	int[] qp; //inverse of pq, where pq[qp[i]] = i
	int n; //number of elements in heap
	ArrayList<Integer> avail; //allows insert on top of deleted objects in keys
	int maxN; //initial array size allocation
	Apartment[] keys; //lists apartment objects
	HashMap<String, Apartment> strings = new HashMap<String, Apartment>(); //maps toStrings of Apartments to the objects themselves
	HashMap<Apartment, Integer> indices = new HashMap<Apartment, Integer>(); //maps Apartments to their index in keys
	
	/**
	 * Initializes the maxPQ object. Accepts an int value from initial input size to 
	 * properly size arrays. Declares HashMaps which allow for fast lookup in the arrays.
	 * An internal size counter is also kept.
	 * @param maxN the initial array sizes
	 */
	@SuppressWarnings("unchecked")
	public MaxPQ (int maxN) {
		this.maxN = maxN;
		pq = new int[maxN+1];
		qp = new int[maxN+1];
		n=0;
		avail=new ArrayList<Integer>();
		keys = new Apartment[maxN+1];
		
		for(int i=0; i<maxN+1; i++) {
			qp[i]=-1; //no objects inserted means no objects have any priority
		}
	}
	
	/**
	 * Switches the location of two values in the heap
	 * @param k1, k2 the indices of the objects to be switched
	 */
	private void exch(int k1, int k2) {
		int temp = pq[k1]; //swaps objects' locations in pq
		pq[k1] = pq[k2];
		pq[k2] = temp;
		
		qp[pq[k1]] = k1; //corrects positions in qp
		qp[pq[k2]] = k2;
	}
	
	/**
	 * Moves an object down the heap based on its priority, if necessary
	 * @param k index pointing to object in the heap
	 */
	private void swim(int k) {
		//makes comparisons with "parents" in heap to see if any are of lesser priority
		while(k>1 && greater(keys[pq[k/2]],keys[pq[k]])>0) {
			exch(k, k/2); //if true, swap the objects
			k=k/2; //continue upward
		}
	}
	
	/**
	 * Moves an object up the heap based on its priority, if necessary
	 * @param k index pointing to object in the heap
	 */
	private void sink(int k) {
		//makes comparisons with "children" in heap to see if any are of greater priority
		while(2*k <= n) {
			int j = 2*k;
			if(j+1 <= n && greater(keys[pq[j]],keys[pq[j+1]])>0) j++;
			if(greater(keys[pq[k]],keys[pq[j]])>0) {
				exch(k,j); //if true, swap on the objects
				k=j; //continue downward
			}
			else break;
		}
	}
	
	/**
	 * Compare two apartments using their sqft attribute.
	 * @return 0 if the values are equal, >0 if a.sqft is greater, <0 if b.sqft is greater
	 * @param a the first Apartment to be compared
	 * @param b the second Apartment to be compared
	 */
	public int greater(Apartment a, Apartment b) {
		return b.sqft.compareTo(a.sqft);
	}
	
	/**
	 * Inserts an object into the heap
	 * @param key the object to be inserted into the heap
	 */
	public void insert(Apartment key) {
		int index=n;
		boolean flag=false;
		//trick to insert values after deletions without resizing arrays as quickly
		if(avail.size()>0) {
			index=avail.get(0);
			flag=true;
		}
		//if necessary, resize arrays
		if(index>=maxN) resize();
		
		//finally insert value into arrays
		n++;
		qp[index] = n;
		pq[n] = index;
		keys[index] = key;
		swim(n);
		
		//insert object into the two Hashmaps
		strings.put(key.toString(), key);
		indices.put(key, index);
		
		//if previously used index was rewritten, pop value from stack
		if(flag) avail.remove(0);
	}
	
	/**
	 * Removes an object from the heap
	 * @param i the location of the object in the heap
	 */
	public void delete(int i) {
		int index = qp[i];
		exch(index, n--); //swap value to end of heap
		swim(index); //adjust swapped object
		sink(index);
		
		//update arrays
		indices.remove(keys[i]);
		keys[i]=null;
		qp[i]=-1;
		avail.add(i); //make space available for new inserts
	}
	
	/**
	 * Returns the max key in the heap.
	 * @return Apartment which has the max sqft
	 */
	public Apartment maxKey() {
		return keys[pq[1]];//Object corresponding to priority 1
	}
	
	/**
	 * Changes the value associated with an object in the heap
	 * @param i, the location of the object in the heap
	 * @param key, the new object, which replaces the old one
	 */
	public void changeKey(int i, Apartment key) {
		keys[i]=key; //write on top of old value
		swim(qp[i]); //adjust Object as necessary in heap
		sink(qp[i]);
	}
	
	/**
	 * Upsizes the arrays as necessary
	 */
	public void resize() {
		int newSize = pq.length+maxN; //increase size of arrays
		int[] newPQ = new int[newSize];
		int[] newQP = new int[newSize];
		Apartment[] newKeys = new Apartment[newSize];
		
		for(int i=0; i<pq.length; i++) { //create new arrays
			newPQ[i] = pq[i];
			newQP[i] = qp[i];
			newKeys[i] = keys[i];
		}
		for(int i=pq.length; i<newSize; i++) {
			newQP[i] = -1;
		}
		
		pq = newPQ; //assign new arrays
		qp = newQP;
		keys = newKeys;
	}
	
}

/**
 * Initializes PQ objects from input and allows user to modify their values.
 * Uses heap-backed operations in conjunction with HashMaps.
 */
public class AptTracker{
	
	/**
	 * Inserts an apartment into its corresponding city-based HashMap, which then
	 * points towards a smaller MaxPQ which outputs the max square footage apartment
	 * in the city.
	 * @param h the corresponding city-based HashMap
	 * @param a the Apartment object to be inserted into the HashMap
	 * @param size the size of the arrays inside a potential new PQ object
	 * @return HashMap the modified cityMax object
	 */
	public static HashMap<String, MaxPQ> cityMaxInsert(HashMap<String, MaxPQ> h, Apartment a, int size) {
		MaxPQ tempMin;
		if(h.containsKey(a.city)) {
			tempMin = h.get(a.city);
			tempMin.insert(a);
		}
		else {
			h.put(a.city, new MaxPQ(size));
			tempMin = h.get(a.city);
			tempMin.insert(a);
		}
		return h;
	}
	
	/**
	 * Inserts an apartment into its corresponding city-based HashMap, which then
	 * points towards a smaller MinPQ which outputs the min rent apartment
	 * in the city.
	 * @param h the corresponding city-based HashMap
	 * @param a the Apartment object to be inserted into the HashMap
	 * @param size the size of the arrays inside a potential new PQ object
	 * @return HashMap the modified cityMax object
	 */
	public static HashMap<String, MinPQ> cityMinInsert(HashMap<String, MinPQ> h, Apartment a, int size) {
		MinPQ tempMin;
		if(h.containsKey(a.city)) {
			tempMin = h.get(a.city);
			tempMin.insert(a);
		}
		else {
			h.put(a.city, new MinPQ(size));
			tempMin = h.get(a.city);
			tempMin.insert(a);
		}
		return h;
	}
	
	/**
	 * The main method, which creates the PQ objects and accepts user input to modify them
	 */
	public static void main(String[] args){
		ArrayList<String> inputs = new ArrayList<String>(); //stores lines from input
		
		try { 
		BufferedReader infile = new BufferedReader(new FileReader("apartments.txt")); //automatically accepts apartments.txt
			infile.readLine();
			while (infile.ready()) {
				inputs.add(infile.readLine());
			}
			infile.close();
		}
		catch(Exception e)
		{
			System.out.println("INPUT ERROR: " + e);
			System.exit(0);
		}
		
		MinPQ minRent = new MinPQ(inputs.size()); //the PQ that will calculate the min rent apartment overall
		MaxPQ maxSQFT = new MaxPQ(inputs.size()); //the PQ that will calculate the max square footage apartment overall
		
		HashMap<String, MinPQ> cityMin = new HashMap<String, MinPQ>(); //the HashMap that will calculate the min rent apartment in each city
		HashMap<String, MaxPQ> cityMax = new HashMap<String, MaxPQ>(); //the HashMap that will calculate the max square footage apartment in each city
		
		MinPQ tempMin; //temporary pq objects used in calculations
		MaxPQ tempMax;

		/**
		 * Considers each line from the input file, creates Apartment objects, and inserts them into the PQs and HashMaps
		 */
		for(int i=0; i<inputs.size(); i++){
			String[] array = inputs.get(i).split(":");
			Apartment a = new Apartment(array[0], array[1], array[2], //new Apartment object
				Integer.valueOf(array[3]), Integer.valueOf(array[4]), Integer.valueOf(array[5]));
				
			if(minRent.strings.containsKey(""+a.address+" "+a.aptNumber+" "+a.zip)) { //prevents addition of redundant object
				continue;
			}
			
			//inserts the Apartment into each PQ and Hashmap
			minRent.insert(a);
			
			cityMin = cityMinInsert(cityMin, a, inputs.size());
			
			maxSQFT.insert(a);
			
			cityMax = cityMaxInsert(cityMax, a, inputs.size());
		}
		
		//Case when input file is empty
		if(inputs.size()==0) {
			System.out.println("\nThe input file had no data.\n");
		}
		
		//various variables used in user input and calculations
		Scanner scan = new Scanner(System.in);
		Character input;
		String bool, address, apt, city, string;
		int zip, rent, sqft;
		Apartment tempApt;
		
		/**
		 * Lists options for user, and based on input, navigates a list of options using
		 * a switch statement
		 */
		while(true) {
			System.out.println("Enter 0 to exit.");
			System.out.println("Enter 1 to add an apartment.");
			System.out.println("Enter 2 to update an apartment.");
			System.out.println("Enter 3 to remove an apartment.");
			System.out.println("Enter 4 to retrieve the apartment with the overall lowest rent.");
			System.out.println("Enter 5 to retrieve the apartment with the overall highest square footage.");
			System.out.println("Enter 6 to retrieve the apartment with the lowest rent in a given city.");
			System.out.println("Enter 7 to retrieve the apartment with the highest square footage in a given city.");
			System.out.println("What would you like to do?");
			input = scan.next().charAt(0);
			scan.nextLine();
			
			switch(input) {
				case '0' :
					System.out.println("\nYou have chosen to exit.\n");
					System.exit(0);
					
				case '1' :
					//Asks for each attribute of an Apartment object, creates object, then adds to all PQs and HashMaps
					System.out.println("\nYou have chosen to add an apartment. Please enter the following attributes:\n");
					System.out.println(" A street address: ");
					address = scan.nextLine();
					System.out.println(" An apartment number: ");
					apt = scan.nextLine();
					System.out.println(" The city the apartment is in: ");
					city = scan.nextLine();
					System.out.println(" The apartment's ZIP code: ");
					zip = scan.nextInt();
					System.out.println(" The monthly cost to rent: ");
					rent = scan.nextInt();
					System.out.println(" The square footage of the apartment: ");
					sqft = scan.nextInt();
					
					Apartment a = new Apartment(address, apt, city, zip, rent, sqft);
					
					if(minRent.strings.containsKey(""+address+" "+apt+" "+zip)) { //again prevents addition of same apartment twice
						System.out.println("You have entered a duplicate value.");
					}
					
					maxSQFT.insert(a);
					minRent.insert(a);
					cityMin = cityMinInsert(cityMin, a, minRent.maxN);
					cityMax = cityMaxInsert(cityMax, a, maxSQFT.maxN);
					
					System.out.println("\nThe apartment has been added.\n");
					break;
					
				case '2' :
					//Prevents user from modifying apartments if there are none in the database
					if(minRent.n==0) {
						System.out.println("\nThere are no records to search.\n");
						break;
					}
					
					//Otherwise, asks for input of three attributes which correspond to a unique apartment object
					System.out.println("\nYou have chosen to update an apartment. Please enter the following attributes:\n");
					System.out.println(" A street address: ");
					address = scan.nextLine();
					System.out.println(" An apartment number: ");
					apt = scan.nextLine();
					System.out.println(" The apartment's ZIP code: ");
					zip = scan.nextInt();
					string = ""+address+" "+apt+" "+zip;
				
					System.out.println(" Would you like to update the rent for this apartment?");
					scan.nextLine();
					
					//based on answer to preceding question, changes rent necessarily and adjusts all data structures
					while(true) {
						System.out.println(" Please enter yes or no.");
						bool = scan.nextLine();
						
						//change rent if the user wants
						if(bool.equals("yes")) {
							System.out.println(" Enter the new rent:");
							rent = scan.nextInt();
							
							//test for existence of apartment first
							if(!minRent.strings.containsKey(string)) {
								System.out.println("You have entered an apartment that is not in the database.");
								break;
							}
							
							tempApt = minRent.strings.get(string); //finds apartment Object from string
							tempApt.monthlyRent = rent; //changes the rent of the object
							
							minRent.changeKey(minRent.indices.get(tempApt), tempApt); //modifies the "overall" PQs
							maxSQFT.changeKey(maxSQFT.indices.get(tempApt), tempApt);

							tempMin = cityMin.get(tempApt.city); //modifies the city-specific PQs
							tempMin.changeKey(tempMin.indices.get(tempApt), tempApt);
							
							tempMax = cityMax.get(tempApt.city);
							tempMax.changeKey(tempMax.indices.get(tempApt), tempApt);
							
							System.out.println("\nThe apartment has been updated.\n");
							break;
						}
						
						//otherwise leave
						if(bool.equals("no")) {
							break;
						}
					}
					break;
					
				case '3' :
					//Prevents user from deleting apartments if there are none in the database
					if(minRent.n==0) {
						System.out.println("\nThere are no records to search.\n");
						break;
					}
					
					//asks for three identifying attributes of an apartment
					System.out.println("\nYou have chosen to remove an apartment. Please enter the following attributes:\n");
					System.out.println(" A street address: ");
					address = scan.nextLine();
					System.out.println(" An apartment number: ");
					apt = scan.nextLine();
					System.out.println(" The apartment's ZIP code: ");
					zip = scan.nextInt();
					string = ""+address+" "+apt+" "+zip;
					
					//if the object doesn't exist, leave
					if(!minRent.strings.containsKey(string)) {
						System.out.println("You have entered an apartment that is not in the database.");
						break;
					}
					
					//otherwise, modify it
					tempApt = minRent.strings.get(string); //find the corresponding object
					
					minRent.delete(minRent.indices.get(tempApt)); //delete the object from the "overall" PQs
					minRent.indices.remove(tempApt); //delete the object from the PQs' Hashmaps
					minRent.strings.remove(string);
					
					maxSQFT.delete(maxSQFT.indices.get(tempApt));
					maxSQFT.indices.remove(tempApt);
					maxSQFT.strings.remove(string);
					
					tempMin = cityMin.get(tempApt.city); //find the correct city-specific PQ based on the apartment's attribute
					tempMin.delete(tempMin.indices.get(tempApt)); //delete from the PQ
					tempMin.indices.remove(tempApt); //delete from the PQ's HashMaps
					tempMin.strings.remove(string);
					
					tempMax = cityMax.get(tempApt.city);
					tempMax.delete(tempMax.indices.get(tempApt));
					tempMax.indices.remove(tempApt);
					tempMax.strings.remove(string);
					
					System.out.println("\nThe apartment has been removed.\n");
					break;
					
				case '4' :
					//prevents the user from searching the apartments if there are none
					if(minRent.n==0) {
						System.out.println("\nThere are no records to search.\n");
						break;
					}
					
					System.out.println("\nYou have chosen to retrieve the apartment with the lowest overall rent.\n");
					System.out.println("\nThe apartment of interest is "+minRent.minKey()+", at $"+minRent.minKey().monthlyRent+" \n");
					break;
					
				case '5' :
					//prevents the user from searching the apartments if there are none
					if(minRent.n==0) {
						System.out.println("\nThere are no records to search.\n");
						break;
					}
					
					System.out.println("\nYou have chosen to retrieve the apartment with the highest overall square footage.\n");
					System.out.println("\nThe apartment of interest is "+maxSQFT.maxKey()+", at "+maxSQFT.maxKey().sqft+" square feet \n");
					break;
					
				case '6' :
					//prevents the user from searching the apartments if there are none
					if(minRent.n==0) {
						System.out.println("\nThere are no records to search.\n");
						break;
					}
					
					System.out.println("\nYou have chosen to retrieve the apartment with the lowest rent in a given city.\n");
					System.out.println("\nPlease enter a city: ");
					
					city = scan.nextLine();
					
					if(!cityMin.containsKey(city)) {//checks if apartment with city exists
						System.out.println("You have entered a city that is not in the database.");
						break;
					}
					
					//locates the PQ based only around the city of interest
					System.out.println("\nThe apartment of interest is "+cityMin.get(city).minKey()+", at $"+cityMin.get(city).minKey().monthlyRent+" \n");
					break;
					
				case '7' :
					//prevents the user from searching the apartments if there are none
					if(minRent.n==0) {
						System.out.println("\nThere are no records to search.\n");
						break;
					}
					
					System.out.println("\nYou have chosen to retrieve the apartment with the highest square footage in a given city.\n"); 
					System.out.println("\nPlease enter a city: ");
					
					city = scan.nextLine();
					
					if(!cityMin.containsKey(city)) {//checks if apartment with city exists
						System.out.println("\nYou have entered a city that is not in the database.\n");
						break;
					}
					
					//locates the PQ based only around the city of interest
					System.out.println("\nThe apartment of interest is "+cityMax.get(city).maxKey()+", at "+cityMax.get(city).maxKey().monthlyRent+" square feet \n");
					break;
					
				default :
					//lets the user enter a value again if they enter something other than 0-7
					System.out.println("\nYou have entered an invalid input. Please enter a number from 1-7 inclusive, to perform an action, or 0 to exit.\n");
			}
		}
	}
}