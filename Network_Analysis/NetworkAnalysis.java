import java.util.*;
import java.io.*;

/**
 * Code has been adapted in some places from the following programs in Sedgewick:
 * IndexMinPQ.java
 * Biconnected.java
 * DijkstraSP.java
 * PrimMST.java
 * DepthFirstSearch.java
 */
 
 
/**
 * Creates Edge objects, which holds information about the an edge between two vertices 
 * in a Graph object. They can be compared and printed to the screen.
 */
class Edge {
	public int from;
	public int to;
	public String type;
	public int bandwidth;
	public double length;
	public double weight;
	
	/**
	 * Initializes an Edge object from the given attributes, which are mostly provided from the
	 * input file. Weight is conditionally assigned based on the type of connection. 
	 * @param f the starting vertex of the Edge
	 * @param t the ending vertex of the Edge
	 * @param y the type of connection
	 * @param b the bandwidth traveling through the Edge
	 * @param l the physical length of the connection
	 */
	public Edge(int f, int t, String y, int b, double l) {
		from=f;
		to=t;
		type=y;
		bandwidth=b;
		length=l;
		if(y.equals("copper")) weight=length/Graph.COPPER_SPEED;
		else weight=length/Graph.FIBER_SPEED;
	}
	
	/**
	 * Provided with a vertex that is part of an Edge, returns the other vertex that
	 * the Edge points to.
	 * @param int a vertex in an Edge.
	 * @return int the other vertex in the Edge.
	 */
	public int other(int vertex) {
        if (vertex == from) return to;
        else return from;
    }
	
	/**
	 * Returns the vertices that make up the Edge.
	 * @return A visualization of the Edge.
	 */
	public String toString() {
		return from+" <-> "+to;
	}
	
	/**
	 * Compares the weights of two Edge objects.
	 * @return 0 if the Edge weights are equal, >0 if this.weight is greater, and <0 e.weight is greater
	 */
	public int compareTo(Edge e) {
		return ((Double)this.weight).compareTo((Double)e.weight);
	}
}

/**
 * A min Priority Queue that in this case will contain Edge objects and
 * prioritize them based on weight.
 */
class IndexMinPQ<Key extends Comparable<Key>> {
    private int maxN;        // maximum number of elements on PQ
    private int n;           // number of elements on PQ
    private int[] pq;        // binary heap using 1-based indexing
    private int[] qp;        // inverse of pq - qp[pq[i]] = pq[qp[i]] = i
    private Key[] keys;      // keys[i] = priority of i

    /**
     * Initializes an empty indexed priority queue
     * @param  maxN the keys on this priority queue are index from 0 to maxN-1
     */
	@SuppressWarnings("unchecked")
    public IndexMinPQ(int maxN) {
        this.maxN = maxN;
        n = 0;
        keys = (Key[]) new Comparable[maxN + 1];
        pq   = new int[maxN + 1];
        qp   = new int[maxN + 1];                  
        for (int i = 0; i <= maxN; i++)
            qp[i] = -1;
    }
	
    /**
     * Returns true if this priority queue is empty.
     * @return {@code true} if this priority queue is empty;
     *         {@code false} otherwise
     */
    public boolean isEmpty() {
        return n == 0;
    }

    /**
     * Is {@code i} an index on this priority queue?
     * @param  i an index
     * @return {@code true} if {@code i} is an index on this priority queue;
     *         {@code false} otherwise
     */
    public boolean contains(int i) {
        return qp[i] != -1;
    }

    /**
     * Associates key with index {@code i}.
     * @param  i an index
     * @param  key the key to associate with index {@code i}
     */
    public void insert(int i, Key key) {
        n++;
        qp[i] = n;
        pq[n] = i;
        keys[i] = key;
        swim(n);
    }

    /**
     * Removes a minimum key and returns its associated index.
     * @return an index associated with a minimum key
     */
    public int delMin() {
        int min = pq[1];
        exch(1, n--);
        sink(1);
        assert min == pq[n+1];
        qp[min] = -1;        // delete
        keys[min] = null;    // to help with garbage collection
        pq[n+1] = -1;        // not needed
        return min;
    }

    /**
     * Decrease the key associated with index {@code i} to the specified value.
     * @param  i the index of the key to decrease
     * @param  key decrease the key associated with index {@code i} to this key
     */
    public void decreaseKey(int i, Key key) {
        keys[i] = key;
        swim(qp[i]);
    }
	
	/**
	 * Returns if one object's value is greater than another'scan
	 * @param The indices in the queue of two objects to be compared
	 * @return true if the value corresponding with i is greater, false otherwise
	 */
    private boolean greater(int i, int j) {
        return keys[pq[i]].compareTo(keys[pq[j]]) > 0;
    }

	/**
	 * Swaps the position of two objects in the queue
	 * @param i the position of the first object
	 * @param j the position of the second object
	 */
    private void exch(int i, int j) {
        int swap = pq[i];
        pq[i] = pq[j];
        pq[j] = swap;
        qp[pq[i]] = i;
        qp[pq[j]] = j;
    }
	
	/**
	 * Increases the priority of an object in the queue based on its value
	 * @param k the current position of the object in the queue
	 */
    private void swim(int k) {
        while (k > 1 && greater(k/2, k)) {
            exch(k, k/2);
            k = k/2;
        }
    }
	
	/**
	 * Decreases the priority of an object in the queue based on its value
	 * @param k the current position of the object in the queue
	 */
    private void sink(int k) {
        while (2*k <= n) {
            int j = 2*k;
            if (j < n && greater(j, j+1)) j++;
            if (!greater(k, j)) break;
            exch(k, j);
            k = j;
        }
    }
}

/**
 * Finds the minimum weight path between two vertices.
 */
class MinPath {
	public double[] distTo;
	public ArrayList<Edge>[] path;
	public IndexMinPQ<Double> pq;
	public int[] minBandwidth;
	
	/**
	 * Finds a path from a given vertex to every other vertex in the Graph.
	 * @param g the Graph to search
	 * @param z the vertex to find paths from
	 */
	@SuppressWarnings("unchecked")
	public MinPath(Graph g, int z) {
		path = (ArrayList<Edge>[]) new ArrayList[g.V];
		for(int v=0; v<g.V; v++) 
			path[v] = new ArrayList<Edge>(); //holds path of edges from vertex z to every other, if there is one
		
		minBandwidth = new int[g.V]; //holds min bandwidth from vertex z to every other
		for(int v=0; v<g.V; v++) 
			minBandwidth[v] = Integer.MAX_VALUE;
		
		distTo = new double[g.V];
		for (int v = 0; v < g.V; v++)
            distTo[v] = Double.POSITIVE_INFINITY; //holds min distance from vertex z to every other
		distTo[z] = 0.0;
		
		pq = new IndexMinPQ<Double>(g.V); //will hold the current min weight path from z to another vertex
		pq.insert(z, distTo[z]);
		
		while (!pq.isEmpty()) {
            int v = pq.delMin();
            for (Edge e : g.adj[v]) { //iterate over all possible Edges in the graph that can be reached by z
                relax(e);
			}
        }
	}
	
	/**
	 * Adjusts the min weight path from z to another vertex if a shorter one
	 * can be found. 
	 * @param e the current Edge to be considered
	 */
	public void relax(Edge e) {
		int v = e.from, w = e.to;
        if (distTo[w] > distTo[v] + e.weight) {//if a shorter path can be found
            distTo[w] = distTo[v] + e.weight; //store it as the shortest
			if(path[w].size()>0){ //resets the path of edges 
				if(path[w].get(path[w].size() - 1).to!=v) {
					path[w] = new ArrayList<Edge>();
					minBandwidth[w] = Integer.MAX_VALUE;
				}
			}
			for(Edge edge : path[v]) {//adds the new edges to the path
				path[w].add(edge);
				if(edge.bandwidth<minBandwidth[w]) minBandwidth[w] = edge.bandwidth;
			}
            path[w].add(e);
			if(e.bandwidth<minBandwidth[w]) minBandwidth[w] = e.bandwidth; //updates the min bandwidth along this path
            if (pq.contains(w)) pq.decreaseKey(w, distTo[w]); //adjusts the value associatd with w in the pq
            else                pq.insert(w, distTo[w]);
        }
    }
}

/**
 * Runs Prim's Algorithm on a Graph to find the minimum spanning tree.
 */
class PrimMST {
    public Edge[] edgeTo;        // edgeTo[v] = shortest edge from tree vertex to non-tree vertex
    public double[] distTo;      // distTo[v] = weight of shortest such edge
    public boolean[] marked;     // marked[v] = true if v on tree, false otherwise
    public IndexMinPQ<Double> pq;

    /**
     * Compute a minimum spanning tree (or forest) of an edge-weighted graph.
     * @param G the Graph of interest
     */
    public PrimMST(Graph G) {
        edgeTo = new Edge[G.V];
        distTo = new double[G.V];
        marked = new boolean[G.V];
        pq = new IndexMinPQ<Double>(G.V);
        for (int v = 0; v < G.V; v++)
            distTo[v] = Double.POSITIVE_INFINITY;

        for (int v = 0; v < G.V; v++)      // run from each vertex to find
            if (!marked[v]) prim(G, v);      // minimum spanning forest
    }
	
	/**
     * Runs Prim's algorithm in graph G, starting from vertex s
     * @param G the Graph of interest
	 * @param s the vertex of interest
     */

    private void prim(Graph G, int s) {
        distTo[s] = 0.0;
        pq.insert(s, distTo[s]);
        while (!pq.isEmpty()) {
            int v = pq.delMin();
            scan(G, v);
        }
    }

    /**
	 * Consider all edges branching from Vertex v as a part of Prim's
	 * @param G the Graph of interest
	 * @param v the vertex of interest
	 */
    private void scan(Graph G, int v) {
        marked[v] = true;
        for (Edge e : G.adj[v]) {
            int w = e.other(v);
            if (marked[w]) continue;         // v-w is obsolete edge
            if (e.weight < distTo[w]) { //if a shorter path is found, save it
                distTo[w] = e.weight;
                edgeTo[w] = e;
                if (pq.contains(w)) pq.decreaseKey(w, distTo[w]);
                else                pq.insert(w, distTo[w]);
            }
        }
    }

    /**
     * Returns the edges in a minimum spanning tree (or forest).
     * @return the edges in a minimum spanning tree as a Stack
     */
    public Stack<Edge> edges() {
        Stack<Edge> mst = new Stack<Edge>();
        for (int v = 0; v < edgeTo.length; v++) {
            Edge e = edgeTo[v];
            if (e != null) {
                mst.push(e);
            }
        }
        return mst;
    }

    /**
     * Returns the sum of the edge weights in a minimum spanning tree (or forest).
     * @return the sum of the weights in the minimum spanning tree
     */
    public double weight() {
        double weight = 0.0;
        for (Edge e : edges())
            weight += e.weight;
        return weight;
    }
}

/**
 * Considers if a Graph would remain connected if ANY two vertices were to be removed. 
 */
class Triconnected {
    private int[] low;
    private int[] pre;
    private int cnt;
    private static boolean articulation;

	/**
	 *
	 */
    public Triconnected(Graph G) {
		articulation = false;
        low = new int[G.V];
        pre = new int[G.V];
        for (int v = 0; v < G.V; v++)
            low[v] = -1;
        for (int v = 0; v < G.V; v++)
            pre[v] = -1;
        
		if(G.V<=2) articulation = true;//handles an "Edge" case
		else {
			for(int i = 0; i < G.V; i++) {
				for (int v = 0; v < G.V && v!=i; v++)
					if (pre[v] == -1)
						dfs(G, v, v, i);
			}
		}
    }

	/**
	 * Performs a DFS to search for articulation points
	 * @param G the Graph of interest
	 * @param u,v vertices in the Graph
	 * @param i the root of the DFS, which is to be avoided the rest of the way
	 */
    private void dfs(Graph G, int u, int v, int i) {
		if(articulation) return;
        int children = 0;
        pre[v] = cnt++;
        low[v] = pre[v];
        for (Edge e : G.adj[v]) {
			if(e.to!=i) {
				if (pre[e.to] == -1) {
					children++;
					dfs(G, v, e.to, i);

					// update low number
					low[v] = Math.min(low[v], low[e.to]);

					// non-root of DFS is an articulation point if low[w] >= pre[v]
					if (low[e.to] >= pre[v] && u != v) 
						articulation = true;
				}

				// update low number - ignore reverse of edge leading to v
				else if (e.to != u)
					low[v] = Math.min(low[v], pre[e.to]);
			}
        }

        // root of DFS is an articulation point if it has more than 1 child
        if (u == v && children > 1)
            articulation = true;
    }
	
	/**
	 * Returns if any articulation points are present in the Graph, even after one vertex is removed
	 * @return boolean false if no articulation points are present in the Graph, even after one vertex is removed
	 */
	public static boolean containsArticulationPoint() {
		return articulation;
	}
}

/**
 * Considers if a Graph would remain connected if all non-copper connection were to be removed.
 */
class CopperConnected {
    private boolean[] marked;
    private int count; 
	private int vertices;

	/**
	 * Initializes a CopperConnected object, given a graph G
	 * @param G the graph of interest
	 */
    public CopperConnected(Graph G) {
		vertices = G.V;
        marked = new boolean[vertices];
        dfs(G, 0);
    }

	/**
	 * Performs a DFS on the graph, in an attempt to reach all vertices if possible
	 * @param G the graph of interest
	 * @param v the root vertex of the DFS
	 */
    private void dfs(Graph G, int v) {
        count++;
        marked[v] = true;
        for (Edge e : G.adj[v]) {
			if(!e.type.equals("copper")) continue; //ignore connections that aren't copper
            if (!marked[e.to]) { //if the vertex hasn't been visited yet, visit it
                dfs(G, e.to);
            }
        }
    }
	
	/**
	 * Returns if all vertices could be accessed through the DFS using only
	 * copper connections
	 * @return boolean true if all vertices were reached
	 */
	public boolean isCopperConnected() {
		return (count==vertices);
	}
}

/**
 * Creates a Graph object which contains vertices and edges. Can perform operations to find
 * minimum path from any vertex to any vertex, if it exists, the minimum spanning tree, and 
 * whether or not the Graph remains connected if certain vertices/edges are removed. 
 */
class Graph {
	final public static double COPPER_SPEED = 23.0, FIBER_SPEED = 20.0;
	
	public int V; //graph attributes
	public int E;
	public boolean copperConnected;
	public LinkedList<Edge>[] adj; 
	
	public MinPath[] all; //related to graph operations
	public CopperConnected copperConn;
	public PrimMST prim;
	public Triconnected triConn;
	
	/**
	 * Initalizes a Graph object given a number of vertices.
	 * @param V the number of vertices in the Graph. 
	 */
	@SuppressWarnings("unchecked") 
	public Graph(int V) {
		this.V = V; //initialize all attribute variables, since this can be done before all edges are inputted
		this.E = 0;
		adj = (LinkedList<Edge>[]) new LinkedList[V];
		for(int v=0; v<V; v++) {
			adj[v] = new LinkedList<Edge>();
		}
		copperConnected = false;
	}
	
	/**
	 * Initializes the MinPath objects, after input has been completed.
	 */
	public void initMinPaths() {
		all = new MinPath[V];
		for(int v=0; v<V; v++)
			all[v] = new MinPath(this, v);
	}
	
	/**
	 * Initializes the PrimMST object, after input has been completed.
	 */
	public void initPrim() {
		prim = new PrimMST(this);
	}
	
	/**
	 * Initializes the TriConnected object, after input has been completed.
	 */
	public void initTriConn() {
		triConn = new Triconnected(this);
	}
	
	/**
	 * Initializes the CopperConnected object, after input has been completed.
	 */
	public void initCopperConn() {
		copperConn = new CopperConnected(this);
	}
	
	/**
	 * Adds an edge into the Graph 
	 * @param v one vertex that is an endpoint for the edge
	 * @param w another vertex that is an endpoint for the edge
	 * @param t the type of connection
	 * @param b bandwidth available along the edge
	 * @param l the physical length of the connection
	 */
	public void addEdge(int v, int w, String t, int b, int l) {
		E++;
		if(v==w) adj[v].add(new Edge(v, w, t, b, l));
		else {
			adj[v].add(new Edge(v, w, t, b, l));
			adj[w].add(new Edge(w, v, t, b, l));
		}
	}
	
	/**
	 * Returns whether or not a path exists between two vertices in the graph
	 * @param vertex1 a vertex in the graph
	 * @param vertex2 a vertex in the graph
	 * @return boolean true if the path exists, false otherwise
	 */
	public boolean pathExists(int vertex1, int vertex2) {
		return (all[vertex1].distTo[vertex2]<Double.POSITIVE_INFINITY);
	}
	
	/**
	 * Returns a path comprised of a list of edges between two vertices
	 * @param vertex1 a vertex in the graph
	 * @param vertex2 a vertex in the graph
	 * @return ArrayList<Edge> the edges that make up the path
	 */
	public ArrayList<Edge> pathBetween(int vertex1, int vertex2) {
		return all[vertex1].path[vertex2];
	}
	
	/**
	 * Returns the bandwidth available between two vertices in the graph
	 * @param vertex1 a vertex in the graph
	 * @param vertex2 a vertex in the graph
	 * @return int the bandwidth available between vertex1 and vertex2 in the graph
	 */
	public int minBandwidthBetween(int vertex1, int vertex2) {
		return all[vertex1].minBandwidth[vertex2];
	}
	
	/**
	 * Returns whether or not the Graph can be connected using only copper connections
	 * @return boolean true if the Graph can be connected using only copper connections, false otherwise
	 */
	public boolean isCopperConnected() {
		return copperConn.isCopperConnected();
	}
}

/**
 * Accepts input from a command line file, creates a Graph object, and permits
 * the user to perform operations on it.
 */
public class NetworkAnalysis {
	public static void main(String[] args) {
		
		Graph g=null;

		//reads input from the command line file
		try {
		BufferedReader infile = new BufferedReader(new FileReader(args[0]));
		g = new Graph(Integer.parseInt(infile.readLine()));
			while (infile.ready()) {
				String[] line = infile.readLine().split(" "); //consider each successive line as 5 different inputs
				
				g.addEdge(
				(int)Integer.parseInt(line[0]), 
				(int)Integer.parseInt(line[1]), 
				line[2], 
				(int)Integer.parseInt(line[3]), 
				(int)Integer.parseInt(line[4]));
				
			}
			infile.close();
		}
		catch( Exception e )
		{
			System.out.println("Error uploading file: "+e);
			System.exit(0);
		}
		
		Character input;
		int vertex1, vertex2;
		Scanner scan = new Scanner(System.in);
		
		//considers input from user to permit them to perform actions on the input graph
		while(true) {
			System.out.println("Enter 0 to exit.");
			System.out.println("Enter 1 to find the lowest latency path between two vertices.");
			System.out.println("Enter 2 to find out whether or not the network is copper-only connected.");
			System.out.println("Enter 3 to find the lowest average latency spanning tree.");
			System.out.println("Enter 4 to find out if the network remains connected if any two vertices fail.");
			input = scan.next().charAt(0);
			scan.nextLine();
			
			//diverts program to perform different actions based on input
			switch(input) {
				case '0' :
					System.out.println("\nYou have chosen to exit.\n");
					
					System.exit(0);
				case '1' :
					System.out.println("\nYou have chosen to find the lowest latency path between two vertices.\n");
					System.out.println("Please enter two vertex numbers between 0 and "+(g.V-1)+" inclusive: ");
										
					g.initMinPaths(); //initializes minPath objects now that input has completed
				
					vertex1 = scan.nextInt();
					vertex2 = scan.nextInt();
					
					if(vertex1==vertex2) { //catches case where same vertex is listed twice
						System.out.println("\nPath: "+vertex1+" <-> "+vertex2);
						System.out.println("Bandwidth available: "+Double.POSITIVE_INFINITY+" gigabits per second\n");
						break;
					}
					
					if(g.pathExists(vertex1, vertex2)) {
						System.out.println("\nPath: "+g.pathBetween(vertex1, vertex2));
						System.out.println("Bandwidth available: "+g.minBandwidthBetween(vertex1, vertex2)/1000.0+" gigabits per second\n");
						break;
					}
					
					else System.out.println("No path was found.\n");
					
					break;
				case '2' :
					System.out.println("\nYou have chosen to find out whether or the not the network is completely copper-connected.\n");
					
					g.initCopperConn(); //initalizes CopperConnected object now that input has completed
					
					if(g.isCopperConnected()) {
						System.out.println("The network is completely copper-connected.\n");
						break;
					}
					
					System.out.println("The network is not completely copper-connected.\n");
					
					break;
				case '3' :
					System.out.println("\nYou have chosen to find the lowest average latency spanning tree.\n");
					
					g.initPrim(); //initalizes PrimMST object now that input has completed
					
					System.out.println("Weight of the tree: "+g.prim.weight()+"E-7 seconds\n Edges in the tree: "+g.prim.edges()+"\n");
					
					break;
				case '4' :
					System.out.println("\nYou have chosen to find out if the network remains network if any two vertices fail.\n");
					
					g.initTriConn(); //initalizes TriConnected object now that input has completed
					
					if(g.triConn.containsArticulationPoint()) System.out.println("The system would not remain connected.\n");
					else System.out.println("The system would remain connected.\n");
					
					break;
				default:
					System.out.println("\nYou have entered an invalid input. Please enter a number from 1-4 inclusive, to perform an action, or 0 to exit.\n");
			}
		}
	}
}