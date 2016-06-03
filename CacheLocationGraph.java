package SomeImportant;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * The following piece of code generates a minimum spanning tree of distributed cache units
 * This identifies minimum cost to reach to a particular cache unit given a source node.
 *
 * Extracted from the project - cache data mobilization among cache units
 *
 */
public class CacheLocationGraph {
    public static long INFINITY= 999999999;

    /**
     * Constructor to read data from a configfile and set cache nodes
     * @param dataFile
     */
    public CacheLocationGraph(File dataFile){

        try {
            BufferedReader br = new BufferedReader(new FileReader(dataFile)) ;
            String firstLine = br.readLine();
            this.numNodes = Integer.parseInt( firstLine.split(" ")[0]);
            for (int i = 0; i < numNodes; i++) {
                CacheNode pn = new CacheNode(i);
                allNode.add(pn);
            }

            numEdges = Integer.parseInt( firstLine.split(" ")[1]);
            weightMatrix = new long[numNodes][numNodes];
            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numNodes ; j++) {
                    weightMatrix[i][j] = INFINITY;
                }
            }

            for (int i = 0; i < numEdges; i++) {
                String line = br.readLine();
                int a = Integer.parseInt( line.split(" ")[0]);
                int b = Integer.parseInt( line.split(" ")[1]);
                long weight = Integer.parseInt( line.split(" ")[2]);

                //to avoid duplicate edges, storing the shorter distances
                if (weightMatrix[a-1][b-1] != INFINITY && (weight <weightMatrix[a-1][b-1])){
                    weight = weightMatrix[a-1][b-1];
                }

                weightMatrix[a-1][b-1] = weight;
                weightMatrix[b-1][a-1] = weight;
            }

            int source_pos =  Integer.parseInt(br.readLine())-1;
            this.sourceLocation = getNode(source_pos);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to generate minimum spanning tree
     */
    public void minimumSpanningTree(){
         pred_List = new ArrayList<>(); /* initiate a list of all predecessors */

        //set curr node to be source node
        CacheNode curr = sourceLocation;
        curr.setCost(0);

        ArrayList<CacheNode> copy_AllNode = (ArrayList<CacheNode>)allNode.clone();// copy list


        while(true){

            if (!(copy_AllNode.size()>0) ) break;
            CacheNode min_neighbour = null;
            boolean hasNeighbor = false;


            for (int i = 0; i < numNodes; i++) {
                long min = INFINITY;
                CacheNode node_i = getNode(i);

                // System.out.println(curr.getName());
                int currNodename = curr.getName();

                if (weightMatrix[currNodename][i] != INFINITY){ // adjacent node

                    curr.addToSuccessorList(node_i);
                    //set reachable costs to minimum
                    if(node_i.getCost() > curr.getCost()+weightMatrix[currNodename][i]){
                        node_i.setCost( curr.getCost() + weightMatrix[currNodename][i]);
                        //System.out.println(node_i.getName()+" "+node_i.getCost());
                        node_i.setPred(curr);

                    }

                    //find minimum adjacent node
                    if (weightMatrix[currNodename][i] < min && (!pred_List.contains(node_i))) {
                        min = weightMatrix[currNodename][i];
                        min_neighbour = node_i;
                        hasNeighbor = true;
                    }
                }
            }

            if (!hasNeighbor && copy_AllNode.size()>0){
                min_neighbour = curr.getPred();
            }

            if (!pred_List.contains(curr)) pred_List.add(curr);

            if (copy_AllNode.contains(curr)){
                copy_AllNode.remove(curr);
            }
            curr = min_neighbour; // search for minimum using greedy

        }

    }

    /**
     * Function to calculate minimum span of the cache location graph
     * @return
     */
    public long getCost(){
        long totalCost = 0;
        Iterator<CacheNode> itr = pred_List.iterator();
        while(itr.hasNext()){
            CacheNode pn = itr.next();

            if (pn.isHasPred()) {
                totalCost += (pn.getCost() - pn.getPred().getCost());

            }
            else {
                totalCost += pn.getCost();

            }
        }

        System.out.println("Total minimum cost = "+ totalCost);
        return  totalCost;
    }

    /**
     * This function is a handy one to return node given a name
     * @param i
     * @return
     */
    private  CacheNode getNode(int i){
        Iterator<CacheNode> itr = allNode.iterator();
        while(itr.hasNext()){
            CacheNode pn = itr.next();
            if (i == pn.getName()){
                return  pn;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        /**
         *   Above is the complete code for dynamic generation of cache location graph using minimum spanning tree.
         *   Should be immplemented in the following order. A sample config file and steps are given below.
         *   Conditions and restrictions :
         *      - number of cache units (N) tested upto <= 4000
         *      - generates undirected graph. Hence, minimum of the duplicate edge weights are considered
         *      - number of edges (E) can be upto N*(N-1)/2
         *      - since this is implemented for single source, source can be any cache unit
         *      - weight on edges is an integer here.
         *
         *   Steps to generate sample location graph:
         *   1. specify data config file
         *   2. create cache location graph
         *   3. Generate minimum spanning tree
         *   4. find cost
         */
        File inputFile = new File("input.txt");
        CacheLocationGraph clg = new CacheLocationGraph(inputFile);
        clg.minimumSpanningTree();
        clg.getCost();

    }


    /**
     * Source code- variables
     */
    private  class CacheNode{
        int name;
        CacheNode pred; //predecessor to the current cache node
        ArrayList<CacheNode> succList; //List of successors for a given node
        long cost; 
        boolean hasPred; 


        public CacheNode(int name) {
            this.name = name;
            this.pred = null;
            this.succList = new ArrayList<>();
            this.cost = INFINITY;
            this.hasPred = false;
        }

        public int getName() {
            return name;
        }

        public CacheNode getPred() {
            return pred;
        }

        public void setPred(CacheNode pred) {
            this.pred = pred;
            this.hasPred = true;
        }

        public void addToSuccessorList(CacheNode pn){
            this.succList.add(pn);
        }

        public long getCost() {
            return cost;
        }

        public void setCost(long cost) {
            this.cost = cost;
        }

        public boolean isHasPred() {
            return hasPred;
        }
    } // CacheNode is an abstraction of cache unit
    private ArrayList<CacheNode> allNode = new ArrayList<>();
    private long[][] weightMatrix;
    private int numNodes;   /* Number of cache units present in a single location */
    private int numEdges;  /* Edges are dynamic - depends upon the network availability */
    private CacheNode sourceLocation; /*  no need for a minimum spannin tree, but required for source data transfers */
    private ArrayList<CacheNode>  pred_List;  /* predecessor list generated for minimum spanning tree */
}
