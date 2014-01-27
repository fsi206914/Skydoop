package ProbSkyline;
import mapreduce.ClusterConfig;
import ProbSkyline.DataStructures.instance;
import ProbSkyline.DataStructures.item;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * This class is in charge of the partitioning after first phase of MapReduce.
 */

public class Intermediate{

	ClusterConfig CC;
	String inputPath;
	List<item> itemList;
	List<instance> instList;
	ArrayList<ArrayList<instance>> divList;
	Set<Integer> cands;

	public Intermediate(ClusterConfig CC, String inputPath){
		this.CC=CC;	
		cands = new HashSet<Integer>();
		this.inputPath = inputPath;
	}

	public String getSrcInstanceFile(){
		return CC.srcName;	
	}


	public instance stringToInstance(String instString){

		String [] div = instString.split(" ");
		instance inst = null;
		if(div.length == CC.dim+3){

			inst= new instance(Util.getInstID(div[1]), Util.getObjectID(div[0]), Util.getProb(div[div.length-1]), CC.dim);
			inst.setPoint(Util.getPoint(div, CC.dim));
		}
		else
			System.out.println("Sth Wrong in creating instance.");

		return inst;
	}


	public void getItemList(String inputFile){

		SkyClient client = new SkyClient(ClusterConfig.getInstance());
		HashMap<Integer, item> aMap = new HashMap<Integer, item>();

		try{
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String line = br.readLine();
			while( line != null){

				instance curr = stringToInstance(line);
				int objectID = curr.objectID;
				if(aMap.containsKey((Integer)objectID) == false){

					item aItem = new item(objectID);         
					aMap.put(objectID, aItem);
				}
				item currItem = aMap.get(objectID);
				currItem.addInstance(curr);
				line = br.readLine();
			}
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
		this.itemList = new ArrayList<item>(aMap.values());
	}


	/*
	 * Remove all items which is unrelated to candidates in the next phase.
	 */
	public void readCandidates(){
		File f = new File(this.inputPath);	
		if(f.isDirectory()){
			File[] files = f.listFiles();	
			for(File file: files)
				readCandidates(file);	
		}	
	}

	public void readCandidates(File file){
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while( line != null){
				String[] oneLine = line.split("\t");
				cands.add( Integer.parseInt(oneLine[0]));
				line = br.readLine();
			}
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}

	}

	void removeUnrelatedObjects(){

		System.out.println("Before removing unrelated object the num is "+ itemList.size());
		Iterator it = itemList.iterator();
		while(it.hasNext()){
			item obj = (item)it.next();
			if(cands.contains(obj.objectID) == false)
				it.remove();
		}
		System.out.println("Finish removing, Now, the num is "+ itemList.size());
	}
	
	/*
	 * Create an instance list to store all instances as a list to help partition.
	 */
	public void itemsToInstances(){
		this.instList = new ArrayList<instance>();

		for(int i=0; i<itemList.size(); i++){
			item aItem = itemList.get(i);
			for(int j=0; j<aItem.instances.size();j++){
				instList.add(aItem.instances.get(j));
			}
		}
	}

	public void partition(){
		int numDiv = CC.numDiv;
		double[] separator= new double[numDiv];
		for(int i=1; i<=numDiv; i++)
			separator[i-1] = ((double)1/numDiv)*i;

		divList = new ArrayList<ArrayList<instance> >();
		for(int i=0; i<numDiv; i++)
			divList.add(new ArrayList<instance>());
		/*
		 * paritition instances to numdiv groups, based on
		 * the evenly distribution of x-axis;
		 */	
		for(instance inst:instList){
			double x = inst.a_point.__coordinates[0];		
			divList.get(findLocOfSeparator(separator,x)).add(inst);
		}

		/*
		 * After parition objects into several colums, we print the data based on
		 * <Area> <L>/<R> regular instance information.
		 */


	}

	int findLocOfSeparator(double[] separ, double x){
		
		int ret =0;
		for(ret=0; ret<separ.length; ret++){
			if(x<separ[ret]) break;
		}
		return ret;
	}

	public static void main(String[] args){
		Intermediate inter = new Intermediate(ClusterConfig.getInstance(), "../output");
		inter.getItemList(inter.getSrcInstanceFile());
		inter.readCandidates();
		inter.removeUnrelatedObjects();
		inter.itemsToInstances();
		inter.partition();
	}
}
