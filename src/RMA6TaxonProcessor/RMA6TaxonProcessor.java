package RMA6TaxonProcessor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.logging.Logger;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import NCBI_MapReader.NCBI_MapReader;
import RMAAlignment.Alignment;
import RMAAlignment.CompositionMap;
import behaviour.Filter;
import megan.data.IMatchBlock;
import strainMap.StrainMisMatchContainer;

/**
 * Extract all information from one Taxon and save the information in the specified slots to be retrieved
 * in RMA6Processor is parent class for all filtering Taxonprocessors now mainly serves as a makeshift interface
 * @author huebler
 *
 */
public class RMA6TaxonProcessor {
	/**
	 * @param int ID, NCBI_MapReader reader, boolean verbose, Logger, log, Logger warning, minPIdent
	 * @return int numMatches, String readDistribution, HashMap EditDistance, HashMap Percent Identity
	 */ 
	  protected String taxName = "unasigned_Name";
	  protected String readDistribution;
	  protected NCBI_MapReader mapReader;
	  protected Integer taxID;
	  protected double minPIdent;
	  protected double topPercent;
	  protected int maxLength;
	  protected HashMap<Integer, Integer> editHistogram;
	  protected HashMap<Integer, Integer> pIdentHistogram;
	  protected boolean verbose;
	  protected Logger log;
	  protected Logger warning;
	  protected ArrayList<String> alignments = new ArrayList<>();
	  protected ArrayList<String> alignmentList;
	  protected ArrayList<String> readList;
	  protected ArrayList<String> lines = new ArrayList<>();
	  protected String damageLine;
	  protected String coverageLine;
	  protected int numOfReads = 0;
	  protected int numMatches = 0;
	  protected int originalNumberOfReads = 0;
	  protected int originalNumberOfAlignments = 0;
	  protected String filterLine = "";
	  protected ArrayList<Integer> distances = new ArrayList<>();
	  protected ArrayList<Double> pIdents = new ArrayList<>();
	  protected HashMap<Integer, ArrayList<Alignment>> taxonMap = new HashMap<>();
	  protected Filter filter = Filter.NON;
	  protected StrainMisMatchContainer container = new StrainMisMatchContainer(filter);
	  protected String readLengthStatistics;
	  protected int refLength = 0;
	  protected ArrayList<Integer> lengths = new ArrayList<>();
	  protected boolean turnedOn = true;
	  protected String additionalEntries = "none";
//constructor
	  public RMA6TaxonProcessor(Integer id, double pID, NCBI_MapReader reader, boolean v, Logger log, Logger warning, double topPercent, int maxLength, Filter f) {
		    mapReader = reader;
		    minPIdent = pID;
		    taxID = id;
		    verbose = v;
		    this.log = log;
		    this.warning = warning;
		    this.topPercent = topPercent;
		    this.maxLength = maxLength;
		    taxName = getName(id.intValue());
		    filter = f;
		    
		    readLengthStatistics = (taxName + "\t0\t0\t0\t0");
		    coverageLine = (taxName + "\tNA\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0");
		    ArrayList<Double> pIdents = new ArrayList<>();
		    ArrayList<Integer> distances = new ArrayList<>();
		    ArrayList<String> reads = new ArrayList<>();
		    ArrayList<String> alignments = new ArrayList<>();
		    reads.add("None");
		    alignments.add("None");
		    pIdents.add(Double.valueOf(0.0D));
		    readList = reads;
		    alignmentList = alignments;
		    setEditDistanceHistogram(distances);
		    setPercentIdentityHistogram(pIdents);
		    String s = taxName;
		    for (int i = 0; i <= 40; i++) {
		      s = s + "\t0";
		    }
		    setDamageLine(s);
		    readDistribution = (taxName + "\tNA\t0\t0\t0\t0\t0\t0");
		  }
public RMA6TaxonProcessor() {
	// TODO Auto-generated constructor stub
}
//setters
protected void setOriginalNumberOfAlignments(int num){
	this.originalNumberOfAlignments = num;
}
protected void setOriginalNumberOfReads(int num){
	this.originalNumberOfReads = num;
}
protected void setReads(ArrayList<String> list){
	this.readList = list;
}
protected void setAlignments(ArrayList<String> list){
	this.alignmentList = list;
}
protected void setDamageLine(String s){
	this.damageLine = s;
}
//setter for compostion map that retrieves information on read distribution and node composition
protected void processCompositionMap(CompositionMap map){
	DecimalFormat df = new DecimalFormat("#.###");
	setTurnedOn(map.wasTurnedOn());
	if(map.getCompositionMap().keySet().size() >0){//check if some alignments are even left after filtering
		map.calculateStatistics();
		//setReadDist
		String maxReference = getName(map.getMaxID());
		String s = taxName +"\t" + maxReference;;
		for(double d : map.getGenaralStatistics()) {
			s += "\t" + df.format(d);
		}	
		this.readDistribution=s;
		//set coverage Line
		HashMap<Integer,Integer> histogram = map.getConverageHistogram();
		String line = taxName + "\t" + maxReference;
		for(int k =0;k<=11;k++)
			line += "\t" + histogram.get(k);
		this.coverageLine = line;
		HashMap<Double, ArrayList<Integer>> list= map.getAllTopReferences();
	      String addEntries = taxName;
	      if (list.size() > 0) {
	        for (double x = 1.0D; x >= 0.0D; x -= 0.1D) {
	          ArrayList<Integer> margin = list.get(x);
	          addEntries = addEntries + "\t";
	          for (int m : margin) 
	          { int reference =m;
	            addEntries = addEntries + getName(reference) + ";";
	          }
	        }
	      } else {
	        addEntries = addEntries += "\tnone";
	      }
	      additionalEntries = addEntries;
	      map = null;
	
		
		
		map=null; // unassign Map at the end 
	}else{
		this.readDistribution = taxName+"\tNA\t0\t0\t0\t0\t0";
		this.coverageLine = taxName+"\tNA\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0";
	}
}
protected void setTurnedOn(boolean wasTurnedOn) {
	// TODO Auto-generated method stub
	
}
protected void setNumberOfReads(int n){
	this.numOfReads=n;
}
protected void setEditDistanceHistogram(ArrayList<Integer> list){
	HashMap<Integer,Integer> histo = new HashMap<Integer,Integer> ();
	for(int i = 0;i < 7;i++){
		histo.put(i, 0);
	}
	if(list != null){
		for(int d : list){
			if(d<=5){
				int value = histo.get(d);
				value++;
				histo.put(d, value);
			}else{
				int value = histo.get(6);
				value++;
				histo.put(6, value);
			}	
		}
	}
	this.editHistogram = histo;
}
protected void setPercentIdentityHistogram(ArrayList<Double> list){
	HashMap<Integer,Integer> histo = new HashMap<Integer,Integer> ();
	for(int i = 0;i < 5; i++){
		histo.put(i, 0);
	}
	if(list != null){
		for(double d: list){
			if(77.5 <= d && d< 82.5){
				int value = histo.get(0);
				value++;
				histo.put(0, value);
			}else if(82.5 <= d && d< 87.5){
				int value = histo.get(1);
				value++;
				histo.put(1, value);
			}else if(87.5 <= d && d< 92.5){
				int value = histo.get(2);
				value++;
				histo.put(2, value);
			}else if(92.5 <= d && d< 97.5){
				int value = histo.get(3);
				value++;
				histo.put(3, value);
			}else if(97.5 <= d){
				int value = histo.get(4);
				value++;
				histo.put(4, value);
			}
		}
	}
	this.pIdentHistogram =  histo;
}

protected void setNumMatches(int matches){
	this.numMatches = matches;
}


//getters
public String getFilterLine(){
	String s = taxName+"\t"+originalNumberOfReads + "\t" +numOfReads +"\t"+originalNumberOfAlignments+"\t" + numMatches;
	return s;
}
public String getCoverageLine(){
	return this.coverageLine;
}
public String getDamageLine(){
	return this.damageLine;
}
protected String getName(int taxId){
	String name;
	if(mapReader.getNcbiIdToNameMap().get(taxId) != null)
		name = mapReader.getNcbiIdToNameMap().get(taxId).replace(' ', '_');
	else if(taxId == 0)
		name="NA";
	else
		name = "unassignedName";
	return name;
}
public ArrayList<String> getAlignments(){
	return this.alignmentList;
}
public ArrayList<String> getReads(){
	return this.readList;
}
public String getEditDistanceHistogram(){
	HashMap<Integer,Integer> histo = this.editHistogram;
	return taxName+"\t"+ histo.get(0)+"\t"+histo.get(1)+"\t"+histo.get(2)+"\t"+histo.get(3)+"\t"+histo.get(4)+"\t"+histo.get(5)+"\t"+histo.get(6);
}
public String getPercentIdentityHistogram(){
	HashMap<Integer,Integer> histo = this.pIdentHistogram;
	return taxName+"\t"+histo.get(0)+"\t"+histo.get(1)+"\t"+histo.get(2)+"\t"+histo.get(3)+"\t"+histo.get(4);
}

protected double getGcContent(String sequence){
	double gcContent = 0;
	char[] chars=sequence.toCharArray();
	for(char c : chars){
		if(c=='g'||c=='G'||c=='c'||c=='C')
			gcContent++;
	}
	if(gcContent !=0){
		gcContent=gcContent/chars.length;
	}
	return gcContent;
}
protected void calculateReadLengthDistribution(){
	if(lengths.size() != 0){
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for(int i : lengths)
			stats.addValue(i);
		String line = taxName;
		line += "\t"+stats.getMean();
		line += "\t"+stats.getGeometricMean();
		line += "\t"+stats.getPercentile(50);
		line += "\t"+stats.getStandardDeviation();
		this.readLengthStatistics = line;
	}
}
//getters
public String getReadLengthStatistics(){
	return this.readLengthStatistics;
}
public int getNumberOfReads(){
	return this.numOfReads;
}

public String getReadDistribution(){
	return this.readDistribution;
}

public void processMatchBlocks(IMatchBlock[] blocks, String readName, int lenght, String readSequence){ 

	}// void 
public void process(){ 

}// void 
}// class 
