package mapreduce;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
/**
 * This class holds the configurations for the cluster.
 * 
 * Singleton pattern is utilized for this class to guarantee only one instance.
 * @author liang 
 */
public class ClusterConfig implements Serializable{

	public int dim;
	public int maxObjectNum;
	public String testArea;
	public double threshold;
	public String srcName;
	public String intermediate;

	/*insert an comment
	 */
	public int numWorkers;
	public String configFile;
	public double[] splitValue;
	public int numDiv;
	transient static final int MaxSplitNum = 10;
	
	private static final ClusterConfig CC = new ClusterConfig();

	public ClusterConfig(String configFile) {
		this.configFile = configFile;
		parseConfigFile();
	}

	private ClusterConfig() {
		
		parseConfigFile();
	}

	public static ClusterConfig getInstance(){
		return CC;	
	}

	/**
	 * Parse the configuration file and populate all the 
	 * fields in configuration
	 */
	public void parseConfigFile() {

		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream("liang.prop"));;

			splitValue = new double[MaxSplitNum];
			FileReader fd = new FileReader("liang.split");

			BufferedReader br = new BufferedReader(fd);
			int split_index = 0;
			String line = br.readLine();
			while(line != null && split_index < MaxSplitNum){

				if(Double.parseDouble(line) >=0 ){
					splitValue[split_index] = Double.parseDouble(line);
					split_index ++;
				}

				line = br.readLine();
			}
			numWorkers = split_index;

		}catch (IOException ex) {
			ex.printStackTrace();
		}

		this.maxObjectNum = Integer.parseInt(prop.getProperty("objectNum"));
		this.dim = Integer.parseInt(prop.getProperty("dim"));
		this.testArea = prop.getProperty("testArea");
		this.numDiv = Integer.parseInt(prop.getProperty("numDiv"));
		threshold = Double.parseDouble(prop.getProperty("threshold"));
		this.srcName= prop.getProperty("srcName");
		this.intermediate= prop.getProperty("intermediate");
	}
}

