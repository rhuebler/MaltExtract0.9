package strainMap;
import java.util.ArrayList;
// Store all Combinations for the first 10 and last 10 Positions of each Blast hit
import java.util.HashMap;

import RMAAlignment.Alignment;
import behaviour.Filter;
/**
 * Functions that collects all misMatches and processes them 
 * also serves as a container 
 * @author huebler
 *
 */
public class StrainMisMatchContainer
{
  private HashMap<Integer, HashMap<String, Integer>> container = new HashMap<Integer, HashMap<String, Integer>>();
  private HashMap<Integer, Double> damage;
  private HashMap<Integer, Double> noise;
  private ArrayList<Integer> distances = new ArrayList<>();
  private ArrayList<Double> pIdents = new ArrayList<>();
  private ArrayList<Integer> lengths = new ArrayList<>();
  private int processed = 0;
  private Filter filter;
  
  public StrainMisMatchContainer(Filter filter) {
    this.filter = filter;
  }
  
  public int getProcessed() {
    return processed;
  }
  
  public ArrayList<Integer> getEditDistances() { return distances; }
  
  public ArrayList<Integer> getLengths() {
    return lengths;
  }
  
  public ArrayList<Double> getPercentIdentity() { return pIdents; }
  
  public HashMap<Integer, Double> getDamage() {
    return damage;
  }
  
  public HashMap<Integer, Double> getNoise() { return noise; }
  
  public void processAlignment(Alignment al) {
    if (al.getMlength() >= 20) {
      if (filter == Filter.CRAWL) {
        distances.add(Integer.valueOf(al.getEditDistance()));
        pIdents.add(Double.valueOf(al.getPIdent()));
        lengths.add(Integer.valueOf(al.getMlength()));
      }
      processed += 1;
      String q = al.getQuery();
      String r = al.getReference();
      for (int i = 0; i < 20; i++) {
        if (i < 10) {
        	if(container.containsKey(i)){
			HashMap<String,Integer> misMatches = container.get(i);
            String s = r.charAt(i) +""+ q.charAt(i);
            if (misMatches.containsKey(s)) {
              misMatches.replace(s, Integer.valueOf(((Integer)misMatches.get(s)).intValue() + 1));
            } else {
              misMatches.put(s, Integer.valueOf(1));
            }
          } else {
            HashMap<String, Integer> misMatches = new HashMap<String, Integer>();
            String s = r.charAt(i) +""+ q.charAt(i);
            misMatches.put(s, Integer.valueOf(1));
            container.put(Integer.valueOf(i), misMatches);
          }
        }
        else if (container.containsKey(Integer.valueOf(i))) {
          HashMap<String, Integer> misMatches = container.get(i);
          String s = r.charAt(al.getMlength() + i - 20) +""+ q.charAt(al.getMlength() + i - 20);
          if (misMatches.containsKey(s)) {
            misMatches.replace(s, Integer.valueOf(((Integer)misMatches.get(s)).intValue() + 1));
          } else {
            misMatches.put(s, Integer.valueOf(1));
          }
        }
        else {
          HashMap<String, Integer> misMatches = new HashMap<String, Integer>();
          String s = r.charAt(i) +""+ q.charAt(i);
          misMatches.put(s, Integer.valueOf(1));
          container.put(Integer.valueOf(i), misMatches);
        }
      }
    }
  }
  
  public void processMisMatches() {
    HashMap<Integer, Double> damage = new HashMap<Integer, Double>();
    HashMap<Integer, Double> noise = new HashMap<Integer, Double>();
    for (int i = 0; i < 20; i++) {
      HashMap<String, Integer> positionContainer = container.get(i);
      if (i < 10) {
        double damDivident = 0.0D;
        double damDivisor = 0.0D;
        if (positionContainer.containsKey("CT")) {
          damDivisor = ((Integer)positionContainer.get("CT")).intValue();
          if (positionContainer.containsKey("CC"))
          {
            damDivident = damDivisor + ((Integer)positionContainer.get("CC")).intValue();
            
            double d = damDivisor / damDivident;
            damage.put(Integer.valueOf(i), Double.valueOf(d));
          } else {
            double d = 1.0D;
            damage.put(Integer.valueOf(i), Double.valueOf(d));
          }
        }
        double divisor = 0.0D;
        double divident = 0.0D;
        for (String key : positionContainer.keySet()) {
          if (((key.equals("CT") ? 0 : 1) | (key.contains("-") ? 0 : 1)) != 0) {
            divisor += ((Integer)positionContainer.get(key)).intValue();
          }
        }
        divident += divisor;
        if (positionContainer.containsKey("GG")) {
          divident -= ((Integer)positionContainer.get("GG")).intValue();
        }
        if (positionContainer.containsKey("AA")) {
          divident -= ((Integer)positionContainer.get("AA")).intValue();
        }
        if (positionContainer.containsKey("TT")) {
          divident -= ((Integer)positionContainer.get("TT")).intValue();
        }
        if (positionContainer.containsKey("CC")) {
          divident -= ((Integer)positionContainer.get("CC")).intValue();
        }
        divident /= 11.0D;
        noise.put(Integer.valueOf(i), Double.valueOf(divident / divisor));
      } else {
        double damDivident = 0.0D;
        double damDivisor = 0.0D;
        if (positionContainer.containsKey("GA")) {
          damDivident = ((Integer)positionContainer.get("GA")).intValue();
          if (positionContainer.containsKey("GG")) {
            damDivisor = damDivident + ((Integer)positionContainer.get("GG")).intValue();
            double d = damDivident / damDivisor;
            damage.put(Integer.valueOf(i), Double.valueOf(d));
          } else {
            double d = 1.0D;
            damage.put(Integer.valueOf(i), Double.valueOf(d));
          }
        }
        double divident = 0.0D;
        double divisor = 0.0D;
        for (String key : positionContainer.keySet()) {
          if ((!key.equals("GA")) || (!key.contains("-")))
            divisor += ((Integer)positionContainer.get(key)).intValue();
        }
        divident += divisor;
        if (positionContainer.containsKey("CC")) {
          divident -= ((Integer)positionContainer.get("CC")).intValue();
        }
        if (positionContainer.containsKey("AA")) {
          divident -= ((Integer)positionContainer.get("AA")).intValue();
        }
        if (positionContainer.containsKey("TT")) {
          divident -= ((Integer)positionContainer.get("TT")).intValue();
        }
        if (positionContainer.containsKey("GG")) {
          divident -= ((Integer)positionContainer.get("GG")).intValue();
        }
        divident /= 11.0D;
        noise.put(Integer.valueOf(i), Double.valueOf(divident / divisor));
      }
    }
    
    this.damage = damage;
    this.noise = noise;
  }
}
