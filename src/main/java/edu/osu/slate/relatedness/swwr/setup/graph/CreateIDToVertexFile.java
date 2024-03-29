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

package edu.osu.slate.relatedness.swwr.setup.graph;

import java.io.*;
import java.util.*;

import edu.osu.slate.relatedness.Configuration;
import edu.osu.slate.relatedness.swwr.data.graph.IDVertexTranslation;

/**
 * This program generates a list of ID-to-vertex mappings for a wiki data set.
 * <p>
 * The input is a wiki </i>"page.sql"</i> file.
 * <p>
 * The output of this program is a .vid file containing an
 * {@link IDVertexTranslation} object.  This is used to
 * determine the valid IDs (non-redirect, in the given namespace)
 * for our graph.
 * <p>
 * Additionally, this file will be used to convert the IDs into
 * our graph vertices.
 * <p>
 * In the Graph creation pipeline, this program is:
 * <ul>
 *   <li>Preceded by -none-
 *   <li>Followed by {@link CreateRedirectFiles}
 * </ul>
 * 
 * @author weale
 * @version 2.0-alpha
 */
public class CreateIDToVertexFile
{

  /* Name of the input file generated by Wikipedia (page.sql) */
  private static String inputFileName;

  /* Name of the output file (.vid) */
  private static String outputFileName;

  /**
   * Checks and opens the input file.
   * 
   * @return Scanner for reading the input file
   */
  private static Scanner openInputFile()
  {
    try
    {
      inputFileName = Configuration.baseDir + "/" +
                      Configuration.sourceDir + "/" +
                      Configuration.type + "/" +
                      Configuration.date + "/" +
                      Configuration.type + "-" +
                      Configuration.date + "-" +
                      "page.sql";
      Scanner in = new Scanner(new FileReader(inputFileName));
      return in;		
    }
    catch (FileNotFoundException e) 
    {
      System.out.println("File not found: " + inputFileName);
      e.printStackTrace();
      System.exit(1);
    }
    return null;	
  }//end: openInputFile()

  /**
   * Checks and opens the output file.
   * 
   * @return ObjectOutputStream for writing
   */
  private static ObjectOutputStream openOutputFile() {
    try
    {
      outputFileName = Configuration.baseDir + "/" +
                       Configuration.binaryDir + "/" +
                       Configuration.type + "/" +
                       Configuration.date + "/" +
                       Configuration.type + "-" +
                       Configuration.date + "-" +
                       Configuration.graph + ".vid";
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outputFileName));
      return out;		
    }//end: try {}
    catch (FileNotFoundException e)
    {
      System.out.println("File not found: " + outputFileName);
      e.printStackTrace();
    }
    catch (IOException e)
    {
      System.out.println("Problem with file: " + outputFileName);
      e.printStackTrace();
    }

    System.exit(1);
    return null;
  }//end: openOutputFile()

  /**
   * Runs the program.
   * 
   * @param args Command-line parameters
   * 
   */
  public static void main(String[] args)
  {
    if(args.length == 1)
    {
      Configuration.parseConfigurationFile(args[0]);
    }
    else
    {
      Configuration.parseConfigurationFile("/scratch/weale/data/config/enwiktionary/CreateMappings.xml");
    }

    /* Open input and output files */
    Scanner in = openInputFile();
    ObjectOutputStream out = openOutputFile();

    /* STEP 1
     * 
     * Create the linked list of valid ids.
     * 
     * IDs are valid if they are:
     * 1. In the main namespace
     * 2. Not redirect pages
     */
    LinkedList<Integer> ll = new LinkedList<Integer>();

    String str = in.nextLine();
    while(str.indexOf("INSERT INTO") == -1)
    {
      str = in.nextLine();
    }

    int tmp = 0;
    while(tmp < 3 && str != null && !str.trim().equals(""))
    {
      str = str.substring(str.indexOf("(")+1, str.length()-3);

      // Split the String into the page information
      String [] arr = str.split("\\d\\),\\(");

      for(int i = 0; i < arr.length; i++)
      {
        //System.out.println(arr[i]);
        String [] info = arr[i].split(",");

        // Check if the information is in the correct format
        if(info.length >= 11) {

          // Extract page, namespace and redirect information
          String page = info[0];
          String namespace = info[1];
          String redirect = info[info.length-6];

          // Add the ID if it's in the needed namespace and not a redirect
          if(namespace.equals("0") && redirect.equals("0"))
          {
            ll.add(new Integer(page));
          }
        }//end: if(info.length)
      }//end: for(i)

      str = in.nextLine();
    }//end: while()

    /* STEP 2
     * 
     * Create new integer array of identical length.
     * Copy values into array and sort array.
     */
    int[] arr = new int[ll.size()];
    Iterator<Integer> it = ll.iterator();
    for(int i = 0; i < arr.length; i++)
    {
      arr[i] = it.next();
    }
    Arrays.sort(arr);

    /* STEP 3
     * 
     * Write int [] to object file.
     */
    try
    {
      IDVertexTranslation vid = new IDVertexTranslation(arr);
      out.writeObject(vid);
    }//end: try {}
    catch (IOException e)
    {
      System.err.println("Problem writing IDs to file.");
      e.printStackTrace();
    }

    //Close files
    try
    {
      in.close();
      out.close();
    }//end: try {}
    catch (IOException e)
    {
      System.err.println("Problem closing input/output files.");
      e.printStackTrace();
    }
  }//end: main(args)
}//end: CreateIDToVertexFile