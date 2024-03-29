/* Copyright 2010 Speech and Language Technologies Lab, The Ohio State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.osu.slate.relatedness.swwr.algorithm;

import java.util.Arrays;

import edu.osu.slate.relatedness.RelatednessInterface;
import edu.osu.slate.relatedness.swwr.data.graph.WikiGraph;

/**
 * Implementation of the Sourced PageRank <b>(SPR)</b> version of vertex relatedness on a graph.
 * <p>
 * <i>Exact</i> relatedness methods omit the jump model from the calculation and only take the link structure into account when calculating values.
 * These run slower and are not guaranteed to converge, but may produce higher-quality results.
 * <p>
 * Source Paper: Y. Ollivier and P. Senellart, <i>Finding Related Pages Using Green Measures: An Illustration with Wikipedia.</i>
 * 
 * @author weale
 * @version 1.0
 */
public class VectSourcedPageRank extends PageRank implements RelatednessInterface {

  /**
   * 
   */
  private static final long serialVersionUID = 6168622709678063605L;

  /**
   * 
   */
  private double [] SPR_old;

  /**
   * 
   */
  private double [] SPR_new;

  /**
   * 
   */
  private double [] PR_init;

  private int fromVertex;
  private double[] fromArray;
  
  /**
   * Indicates the use of approximate (faster) or exact (more accurate) calculations.
   * <br>
   * Approximate calculations are guaranteed to converge, while exact calculations are not.
   */
  protected boolean approximate = false;

  /**
   * Constructor for GreenRelatedness.  Calls the {@link PageRank} constructor.
   * 
   * @param graphFileName {@link java.lang.String} containing the path to the graph file.
   */
  public VectSourcedPageRank(String graphFileName) {
    super(graphFileName);
  }

  /**
   * Constructor for GreenRelatedness.  Calls the {@link PageRank} constructor.
   * 
   * @param graph Previously initialized {@link WikiGraph} structure
   */
  public VectSourcedPageRank(WikiGraph graph) {
    super(graph);
  }

  /**
   * Finds the relatedness value between two vertices (compressed value) using the approximate inference routine.
   * <p>
   * This runs faster than getExactRelatedness, but may return less accurate results.
   * This is guaranteed to converge.
   * <p>
   * Requires relatedness calculations on the full-graph.
   * 
   * @param from Vertex ID number (compressed)
   * @param to Vertex ID number (compressed)
   * @return GreenMeasure resulting from running relatedness measure.
   */
  public double getRelatedness(int from, int to)
  {

    //Get distribution
    double [] GM1 = null;
    if(from != fromVertex)
    {
      GM1 = getExactRelatedness(from);
      fromArray = new double[GM1.length];
      System.arraycopy(GM1, 0, fromArray, 0, GM1.length);
      fromVertex = from;
    }
    else
    {
      GM1 = fromArray;
    }
    
    double [] GM2 = getExactRelatedness(to);

    double num = 0.0;
    double length1 = 0.0;
    double length2 = 0.0;
    for(int i = 0; i < GM1.length; i++)
    {
      num += (GM1[i] * GM2[i]);
      length1 += (GM1[i] * GM1[i]);
      length2 += (GM2[i] * GM2[i]);
    }

    length1 = Math.sqrt(length1);
    length2 = Math.sqrt(length2);

    // Return value at the 'to' index
    return num / (length1 * length2);
  }

  /**
   * Finds the relatedness value between two vertices (compressed value) using the approximate inference routine.
   * <p>
   * This runs faster than getExactRelatedness, but may return less accurate results.
   * This is guaranteed to converge.
   * <p>
   * Requires relatedness calculations on the full-graph.
   * 
   * @param from Vertex ID number (compressed)
   * @param to Vertex ID number (compressed)
   * @return GreenMeasure resulting from running relatedness measure.
   */
  public double getRelatedness(int[] from, int to) {

    // Set Approximate Flag
    approximate = true;

    // Return Results of getRelatedness
    return getRelatedness(from, to);
  }

  /**
   * Finds the relatedness value between two vertices using the exact inference routine.
   * <p>
   * This runs slower than getRelatedness, but should return more accurate results.
   * This is NOT guaranteed to converge.
   * <p>
   * Requires relatedness calculations on the full-graph.
   *
   * @param from Vertex ID number (compressed)
   * @param to Vertex ID number (compressed)
   * @return GreenMeasure resulting from running relatedness measure.
   */
  public double getExactRelatedness(int from, int to) {

    //Get distribution
    double [] GM = getExactRelatedness(from);		

    // Return value at the 'to' index
    return GM[to];
  }

  /**
   * Finds the relatedness distribution sourced at a vertex using the approximate inference routine.
   * <p>
   * This runs faster than getExactRelatedness, but should return less accurate results.
   * <p>
   * This is guaranteed to converge.
   *
   * @param from Vertex ID number (compressed)
   * @return Array containing relatedness distribution
   */
  public double[] getRelatedness(int from) {

    // Set Approximate Flag
    approximate = true;

    // Return Results of getRelatednessDistribution
    return getExactRelatedness(from);
  }

  /**
   * Finds the relatedness distribution sourced at a vertex using the approximate inference routine.
   * <p>
   * This runs faster than getExactRelatedness, but should return less accurate results.
   * <p>
   * This is guaranteed to converge.
   *
   * @param from Vertex ID number (compressed)
   * @return Array containing relatedness distribution
   */
  public double[] getRelatedness(int[] from) {

    // Set Approximate Flag
    approximate = true;

    SPR_old = new double[graph.length];
    SPR_new = new double[graph.length];
    PR_init = new double[graph.length];

    for(int j=0;j<PR_init.length;j++) {
      PR_init[j] = PR[j] * -1;
      SPR_old[j] = PR_init[j];
    }

    for(int i=0; i<from.length; i++) {
      PR_init[from[i]] = PR_init[from[i]] + (1.0/from.length);
      SPR_old[from[i]] = SPR_old[from[i]] + (1.0/from.length);
    }

    // Return Results of getRelatednessDistribution
    return getExactRelatedness();
  }

  public double[] getRelatedness(int[] from, float[] vals) {

    approximate = true;

    SPR_old = new double[graph.length];
    SPR_new = new double[graph.length];
    PR_init = new double[graph.length];

    for(int j=0;j<PR_init.length;j++) {
      PR_init[j] = PR[j] * -1;
      SPR_old[j] = PR_init[j];
    }

    for(int i=0; i<from.length; i++) {
      PR_init[from[i]] = PR_init[from[i]] + vals[i];
      SPR_old[from[i]] = SPR_old[from[i]] + vals[i];
    }

    return getExactRelatedness();
  }

  /**
   * Finds the relatedness distribution sourced at a vertex using the exact inference routine.
   * <p>
   * This runs slower than getRelatedness, but should return more accurate results.
   * <p>
   * This is NOT guaranteed to converge.
   *
   * @param from Vertex ID number (compressed)
   * @return Array containing relatedness distribution
   */
  public double[] getExactRelatedness(int from)
  {
    double [] SPR_old = new double[graph.length];
    double [] SPR_new = new double[graph.length];
    double [] PR_init = new double[graph.length];

    for(int j = 0; j < PR_init.length; j++)
    {
      PR_init[j] = PR[j] * -1;
      SPR_old[j] = PR_init[j];
    }

    PR_init[from] = PR_init[from] + 1;
    SPR_old[from] = SPR_old[from] + 1;

    int numIterations = 0;
    double change;
    do {
      double randomSurfer = 0;

      for(int j = 0; j < graph.length; j++)
      {

        if(graph[j] != null && graph[j].length != 0) {
          // Valid transition array

          for(int k = 0; k < graph[j].length; k++)
          {
            SPR_new[graph[j][k]] += (SPR_old[j] * tProb[j][k]);
          }//end: for(k)
        }
        else
        {
          // Add transition values to randomSurfer
          randomSurfer += SPR_old[j] / graph.length;
        }

      }//end: for(j)

      for(int x = 0; x < SPR_new.length; x++)
      {
        SPR_new[x] = .85 * ((SPR_new[x] + randomSurfer) + PR_init[x]) + (.15 / PR_init.length);
      }

      change = pageRankDiff(SPR_old, SPR_new);
      System.arraycopy(SPR_new, 0, SPR_old, 0, SPR_new.length);
      Arrays.fill(SPR_new, 0.0);

      numIterations++;
    }while(change > 0.002);

    for(int j=0; j<SPR_old.length; j++)
    {
      SPR_old[j] = SPR_old[j] * Math.log10(1.0/PR[j]);
    }

    return SPR_old;
  }

  /**
   * Finds the relatedness distribution sourced at a vertex using the exact inference routine.
   * <p>
   * This runs slower than getRelatedness, but should return more accurate results.
   * <p>
   * This is NOT guaranteed to converge.
   *
   * @param from Vertex ID number (compressed)
   * @return Array containing relatedness distribution
   */
  public double[] getExactRelatedness() {

    int numIterations = 0;
    double change;
    do {
      double randomSurfer = 0;

      for(int j = 0; j < graph.length; j++) {

        if(graph[j] != null && graph[j].length != 0) {
          // Valid transition array

          for(int k=0; k<graph[j].length; k++) {
            //						SPR_new[graph[j][k]] += (SPR_old[j] / graph[j].length);
            SPR_new[graph[j][k]] += (SPR_old[j] * tProb[j][k]);
          }//end: for(k)
        }
        else {
          // Add transition values to randomSurfer
          randomSurfer += SPR_old[j] / graph.length;
        }

      }//end: for(j)

      for(int x=0; x<SPR_new.length; x++) {
        if(approximate) {
          SPR_new[x] = .85 * ((SPR_new[x] + randomSurfer) + PR_init[x]) + (.15 / PR_init.length);
        }
        else {
          SPR_new[x] = (SPR_new[x] + randomSurfer) + PR_init[x];
        }
      }

      change = pageRankDiff(SPR_old, SPR_new);

      //			double tmp = 0.0;
      //			for(int x=0; x<SPR_new.length; x++) {
      //				SPR_old[x] = SPR_new[x];
      //				tmp += SPR_old[x];
      //				SPR_new[x] = 0;
      //			}
      System.arraycopy(SPR_new, 0, SPR_old, 0, SPR_new.length);
      Arrays.fill(SPR_new, 0.0);

      numIterations++;
    }while(change > 0.002);

    for(int j=0; j<SPR_old.length; j++) {
      SPR_old[j] = SPR_old[j] * Math.log10(1.0/PR[j]);
    }

    approximate = false;
    return SPR_old;
  }

}//end: GreenRelatedness