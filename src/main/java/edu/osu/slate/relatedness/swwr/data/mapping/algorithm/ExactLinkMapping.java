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

package edu.osu.slate.relatedness.swwr.data.mapping.algorithm;

import java.util.Arrays;

import edu.osu.slate.relatedness.swwr.data.mapping.TermToVertexCount;
import edu.osu.slate.relatedness.swwr.data.mapping.TermToVertexCountComparator;
import edu.osu.slate.relatedness.swwr.data.mapping.VertexCount;

/**
 * Strictest lookup class using link information only.
 * <p>
 * This mapping class requires the input term to match the
 * seen mappings <i>exactly</i> in order to get a vertex mapping.
 * 
 * @author weale
 * @version 1.01
 */
public class ExactLinkMapping extends TermToVertexMapping
{
  private static final long serialVersionUID = 5395182204888235246L;

 /**
  * Constructor.
  * 
  * @param tvc Array of {@link TermToVertexCount} object.
  */
  public ExactLinkMapping(TermToVertexCount[] tvc)
  {
    super(tvc);
  }//end: ExactLinkMapping(TermToVertexCount[])
  
 /**
  * Constructor.
  * <p>
  * Reads the {@link TermToVertexCount} array from the given <i>.tvc file</i>.
  * 
  * @param filename Name of the input file.
  */
  public ExactLinkMapping(String filename)
  {
    super(filename);
  }//end: ExactLinkMapping(String)
  
 /**
  * Gets the vertices mapped to a given term.
  * <p>
  * Returns null if the term is not found in the mapping function.
  *  
  * @param term Term to be mapped.
  * @return An array of {@link VertexCount} objects.
  */
  public VertexCount[] getVertexMappings(String term)
  {
    int pos = Arrays.binarySearch(terms, new TermToVertexCount(term),
                                  new TermToVertexCountComparator());

    if(pos >= 0)
    { // FOUND!
      return terms[pos].getVertexCounts();
    }
     
    return null;
  }//end: getVertexMappings(String)
}