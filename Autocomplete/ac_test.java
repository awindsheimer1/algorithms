  import java.io.*;
import java.util.*;
import java.math.*;

/**
 * Creates a Node object, which is the building block of the DLB
 */
class Node {
	/**
	 * Each objects consists of a character value, a pointer to both the next and child nodes, 
	 * and an optional counter, which is used to keep track of frequency in the user history trie. 
	 */
	private Character value;
	private Node next, child;
	private int counter;


	/* The following methods initialize a Node object based on the number of inputted parameters */
	public Node(){
		this(null, null, null, 0);
	}

	public Node(Character value){
		this(value, null, null, 0);
	}

	public Node(Character value, Node child){
		this(value, child, null, 0);
	}	
	
	public Node(Character value, Node child, Node next){
		this(value, child, next, 0);
	}

	public Node(Character value, Node child, Node next, int counter){
		setValue(value);
		setChild(child);
		setNext(next);
		setCounter(counter);
	}

	public Character getValue(){
		return value;
	}
	
	public Node getChild() {
		return child;
	}

	public Node getNext(){
		return next;
	}
	
	public int getCounter(){
		return counter;
	}

	public void setValue(Character value){
		this.value = value;
	}
	
	public void setChild(Node child){
		this.child = child;
	}

	public void setNext(Node next){
		this.next = next;
	}
	
	public void setCounter(int counter){
		this.counter = counter;
	}
	
	public void incCounter(){
		counter++;
	}

	public String toString(){
		return ""+getValue();
	}
}

/**
 * Word: Creates a Word object, used to create predictions from user history
 */
class Word {
	public String word;
	public int value;
	
	/**
	 * Initializes a Word with specified word and frequency
	 */
	public Word(String word, int value){
		this.word=word;
		this.value=value;
	}
	
	public String getWord(){
		return word;
	}
	
	public int getValue(){
		return value;
	}
}

/**
 * Creates a DLB object, used to store both the dictionary and user history words, albeit in separate instances.
 * Contains search and insert operations. 
 */
class DLB {
	public Node root;
	public static Node pref, pref2;

	/**
	 * Initializes a DLB object
	 * NOTE: this method produces a structure built entirely in this file: no java built-in
	 * 		objects such as Java LinkedList are used; in particular, the following three methods
	 *		are the ones that perform this operation
	 * @param filename String name of text file in the directory which contains a word on each line
	 * @param flag Boolean dependent on whether or not frequency of the words matters, which is true
	 * for the user history DLB but not for the dictionary DLB
	 * @throws Exception if something is wrong with the file
	 */
	public DLB(String fileName, boolean flag) {
		root=new Node();
		try
		{
			BufferedReader infile = new BufferedReader(new FileReader(fileName));
			while (infile.ready()) {
				insert(infile.readLine(), flag);
			}
			
			infile.close();
		}
		catch( Exception e )
		{
			System.out.println( "Error from dictionary file: " + e );
			System.exit(0);
		}
	}
	
	/**
	 * Calls an insert method using the root of the dlb, and sends the word in the correct manner, which
	 * is appended with "^".
	 * @param exp String containing the word to be inserted
	 * @param flag Boolean which is true for the history DLB
	 */
	public void insert(String exp, boolean flag) {
		exp+="^";
		insert(this.root, exp, flag);
	}

	/**
	 * Inserts a character of a given word at a root, and is recursively called to move down the trie
	 * as more letters are inserted. 
	 * @param root Node which is initialy this.root, but changes as the method is recursively called
	 * @param word String to be inserted char by char into the trie
	 * @param flag Boolean which manipulates the counter variable for the history DLB
	 */
	private void insert(Node root, String word, boolean flag) {
		//FIRST CASE: the root has no value attached to it
		if(root.getValue()==null) {
			//insert and then recurse downwards
			if(word.length()>=2) {
				root.setValue(word.charAt(0));
				root.setChild(new Node());
				word=word.substring(1, word.length());
				insert(root.getChild(), word, flag);
			}
			//o/w just insert and return
			else { 
				root.setValue(word.charAt(0));
				word=word.substring(1, word.length());
				if(flag){
					root.incCounter();
				}
				return; 
			}
		}
		//SECOND CASE: the root's value equals that of the char
		else if(((Character)word.charAt(0)).equals(root.getValue())){
			word=word.substring(1, word.length());
			//return if all chars have been inserted
			if(word.length()==0) {
				if(flag) { 
					root.incCounter(); 
				} 
				return;
			}
			//recurse downwards
			if(root.getChild()==null){
				root.setChild(new Node());
			}
			insert(root.getChild(), word, flag);
		}
		//THIRD CASE: the root's value doesn't equal that of the char
		else {
			//must move to next Node if it exists
			if(root.getNext()!=null) {
				insert(root.getNext(), word, flag);
			}
			else{ 
				root.setNext(new Node());
				insert(root.getNext(), word, flag);
			}
		}
		return;
	}
	
	////////////////////////////////////////////////////////////
	
	/**
	 * Any methods from this point on in the class no longer deal with creating the DLB object from the dictionary. 
	 * Instead, the following deal with searching the DLB, or creating and maintaining the user_history file. 
	 * The three methods immediately following are related to the search operation of the DLB.
	 */
	
	/**
	 * Search method called from main, which uses two other methods to locate the deepest Node of a word
	 * and then generate predictions off of that Node
	 * @param dlb DLB which contains the dictionary
	 * @param exp String which is the word to be searched for
	 * @param arr ArrayList that the words will be stored in 
	 * @return arr ArrayList containing the predicted words
	 */
	public static ArrayList<String> search(DLB dlb, String exp, ArrayList<String> arr) {
		ArrayList<Character> list = new ArrayList<Character>();
		for(int i=0; i<exp.length(); i++) {
			list.add(exp.charAt(i));
		}
		pref = searchPref(histDLB.root, list);
		//stores the deepest Node from the given word, if it exists in the trie
		
		list = new ArrayList<Character>();
		for(int i=0; i<exp.length(); i++) {
			list.add(exp.charAt(i));
		}
		pref2 = searchPref(dlb.root, list);
		
		boolean flag = true; 
		exp=exp.substring(0,exp.length()-1); //will eventually return words, without their ending char attached
		ArrayList<Node> nodes = new ArrayList<Node>();
		
		if(pref==null&&pref2==null) return arr; //word wasn't found in either trie
		
		else if(pref==null&&pref2!=null) {//word was only found in dictionary
			dlb.searchWords(pref2, nodes, arr, exp, false); 
			return arr;
		}
		else{//word at least appears in history
			histDLB.searchWords(pref, nodes, arr, exp, flag);
			ArrayList<Word> words = new ArrayList<Word>();//stores words to be sorted based on frequency
			for(int i=0; i<arr.size(); i++) {
				words.add(new Word(arr.get(i), nodes.get(i).getCounter()));
			}
			
			//sorts based on frequency
			Collections.sort(words, new Comparator<Word>(){
			public int compare(Word a, Word b) {
				return Integer.compare(b.getValue(), a.getValue());
			}
			});
			
			arr.clear();
			if(words.size()>=5) {//limits number of returned words to at most 5
				for(int i=0; i<5; i++) {
					arr.add(words.get(i).getWord());
				}
				return arr;
			}
			else for(Word w : words) {
					arr.add(w.getWord());
				}
			
			if(pref2!=null) {//word may also appear in dictionary
				dlb.searchWords(pref2, nodes, arr, exp, !flag);
			}
			return arr;
		}
	}
	
	/**
	 * Finds the prefix in the trie, and returns the Node pointing to the last char of it
	 * @param root Node which maintains a pointer to the current root
	 * @param list ArrayList of the characters in the word
	 * @return Node which points to the last char of the word
	 */
	public static Node searchPref(Node root, ArrayList<Character> list) {
		//FIRST CASE: if there's nothing there return
		if(root.getValue()==null) return null;
		
		//SECOND CASE: the root's value equals the char
		else if(list.get(0).equals(root.getValue())){
			list.remove(0);
			if(list.size()==0) return root; //recurse downward if possible
			if(root.getChild()!=null) return searchPref(root.getChild(), list);
			else return null;
			}
			
		//THIRD CASE: the root's value doesn't equal the char
		else {
			if(root.getNext()==null) return null; //recurse rightward if possible
			else return searchPref(root.getNext(), list);
		}
	}

	/**
	 * Searches the given trie for predictions from a given word
	 * @param base Node that is passed in as the root that represents the word
	 * @param nodes If the history trie is being searched, contains the nodes that represent the predicted words
	 * @param words Contains the predicted words
	 * @param word Holds a String that will be considered for inclusion in the prediction set
	 * @param f If the history trie is being searched, permits the storage of nodes into nodes
	 */
	public static void searchWords(Node base, ArrayList<Node> nodes, ArrayList<String> words, String word, boolean f){
		//base cases
		if(words.size()>=5 || base==null || (base.getNext()==null && base.getChild()==null)) return;
		
		word+=base.getValue();

		//if current word exists in dictionary
		if(!words.contains(word) && base.getChild()!=null && base.getChild().getValue().equals('^')){
			if(f) {
				nodes.add(base.getChild());
			}
			words.add(word);
			searchWords(base.getChild(), nodes, words, word, f);
			
			word=word.substring(0, word.length()-1);
			if(!(f&&base.equals(pref))||(!f&&base.equals(pref2))) {//prevents words with inocrrect prefixes from being saved
				if(base.getNext()!=null) {
					searchWords(base.getNext(), nodes, words, word, f);
				}	
			}
			return;
		}
		
		//recurse downwards if possible
		if(base.getChild()!=null) {
			searchWords(base.getChild(), nodes, words, word, f);
			
		}
		
		//recurse rightward if possible
		if(!(f&&base.equals(pref))||(!f&&base.equals(pref2))) {//prevents words with inocrrect prefixes from being saved
			if(base.getNext()!=null){
				if(word.length()>0) {
					word=word.substring(0, word.length()-1);
				}
				searchWords(base.getNext(), nodes, words, word, f);
			}
		}
	}
	
	/**
	 * The final three methods of the class and variables declared below directly relate to the creation and maintainence 
	 * of user_history.txt. The creation of such a file is called from main after the dictionary DLB is created. 
	 */
	 
	public static ArrayList<String> histList;
	public static DLB histDLB;
	public static File hist;
	public static String u_h = "user_history.txt";
	
	/**
	 * Creates the user history file if it doesn't already exist
	 */
	public static void createFile() {
		try {
			if((new File(u_h)).isFile()) {
				histList = new ArrayList<String>();
				BufferedReader readHist = new BufferedReader(new FileReader(u_h));
				
				while(readHist.ready()) { 
					histList.add(readHist.readLine());
				}
				
				//sorts list of words from history
				Collections.sort(histList);
				readHist.close();
				
				//reads the sorted list back into the history file
				updateFile();
				hist = new File(u_h);
				hist.createNewFile();
			}
			else {
				histList = new ArrayList<String>();
				hist = new File(u_h);
				hist.createNewFile();
			}
			histDLB = new DLB(u_h, true); //creates a dlb that stores the word from history
		}	
		catch(Exception e)
		{
			System.out.println( "Error from creating history file: " + e );
			System.exit(0);
		}
	}
	
	/**
	 * Recreates user history file, with new entries encluded
	 */	
	public static void updateFile() {
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(new File(u_h)));
			for(int i=0; i<histList.size(); i++) {
				output.write(histList.get(i));
				output.newLine();
			}
			output.close();
		}
		catch (Exception e){
			System.out.println( "Error from history file update: " + e );
			System.exit(0);
		}
	}
	
	/**
	 * Adds a word to the history file, also maintaining a list of words separately from the file
	 * @param exp The word to be added to history
	 */
	public static void addHistory(String exp){
		histList.add(exp);
		Collections.sort(histList);
		updateFile(); //adds word to history file
		histDLB = new DLB(u_h, true); //recreates dlb with new word
	}
}

/**
 * Produces a list of autocompleted predictions based on user input
 */
public class ac_test {
	
	/**
	 * Main method, which creates a DLB object from the dictionary file and a separate one from the user history. 
	 * Also accepts user input one at a time, and allows user to select a recommended word and store any inputted string. 
	 * Tells user how long search operation took, and at conclusion of program lists an average time taken. 
	 * The method is suppressing deprecation warnings from use of BigDecimal, which is part of the timekeeping operation.
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args){
		//false since word frequency doesn't matter for dictionary
		DLB dlb = new DLB("dictionary.txt", false);
		DLB.createFile();
		
		int count = 0, totalInputs=0, intput=0;
		double averageTime=0, totalTime=0, diffDouble;
		long diff, first, last;
		ArrayList<String> current = new ArrayList<String>(), previous = new ArrayList<String>(), test = new ArrayList<String>();
		String value="", input="";
		Scanner scan = new Scanner(System.in);
		
		//to avoid effects associated with lazy class loading:
		//simply running through the search methods in this manner will greatly
		//reduce time spent in future calls
		for(char alph='a'; alph<='z'; alph++) {
			DLB.addHistory(String.valueOf(alph));
			test = DLB.search(dlb, String.valueOf(alph), test);
			test = new ArrayList<String>();
			DLB.histList.remove(String.valueOf(alph));
			DLB.updateFile();
		}
		for(char alph='A'; alph<='Z'; alph++) {
			DLB.addHistory(String.valueOf(alph));
			test = DLB.search(dlb, String.valueOf(alph), test);
			test = new ArrayList<String>();
			DLB.histList.remove(String.valueOf(alph));
			DLB.updateFile();
		}
		
		//continue taking input until user says otherwise
		while(true) {
			if(count==0) {
				System.out.print("Enter your first character: "); 
				value = String.valueOf(scan.next().charAt(0));
				
				//termination char
				if(value.equals("!")) break;
				
				count++;
			}
			
			//for every char after first of word
			else {
				System.out.print("Enter another character: ");
				input = String.valueOf(scan.next().charAt(0));
				
				if(input.equals("!")) break;
				
				if(input.equals("$")){ //$ saves current word into user history, then resets input
					DLB.addHistory(value);
					count=0;
					continue;
				}
				
				try{
					//if value is number, choose corresponding word from previous predictions
					intput=Integer.parseInt(input);
					if(intput>=1 && intput<=5){ 
						System.out.println("\n\nChosen word: "+previous.get(intput-1)+"\n\n");
						DLB.addHistory(previous.get(intput-1));
						count=0;
						continue;
					}
				}
				catch(Exception e){}
				
				value+=input;
			}
			
			first = System.nanoTime(); //store time taken to build predictions
			current = DLB.search(dlb, value, current);//produce predictions, first from history and then from dict
			last = System.nanoTime();
			diff = last-first;
			diffDouble = (double)diff/1000000000.0;
			totalTime+=diffDouble;
			totalInputs++;
			System.out.println("\nTime taken: "+BigDecimal.valueOf(diffDouble).setScale(6, BigDecimal.ROUND_HALF_UP)+"s"); 
			
			if(current.isEmpty()) {
				System.out.println("No predictions were found\n");//if returned array is empty, then say no predictions were found
			}
			else{ 
				System.out.print("| ");
				for(int i=0; i<current.size(); i++) {
					System.out.print(i+1+": "+current.get(i)+" | ");
				}
				System.out.println("\n");
			}
			
			previous = current; //so old predictions can be chosen
			current = new ArrayList<String>(5); //and new predictions can be stored
		}
	
		averageTime = totalTime/totalInputs;
		if(totalInputs==0) {//prevents error in time from immediately exiting
			averageTime=0;
		}
		System.out.println("\nAverage time "+BigDecimal.valueOf(averageTime).setScale(6, BigDecimal.ROUND_HALF_UP)+"s");

		DLB.updateFile();//forces last update of history into file before exiting

		System.exit(0);
	}
}