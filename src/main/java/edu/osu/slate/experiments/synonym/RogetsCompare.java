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

/* 
 * ************************************************************************
 * 	  Adapted from Version 1.3 of the Open Roget's Thesaurus
 *    ELKB for our evaluation framework by:
 *    Timothy Weale
 *    Department of Computer Science
 *    The Ohio State University
 *    Columbus, Ohio 43214
 *    
 * ***********************************************************************
 * 
 *    Copyright (C) 2009
 *    Mario Jarmasz, Alistair Kennedy, Tad Stach and Stan Szpakowicz
 *    School of Information Technology and Engineering (SITE)
 *    University of Ottawa, 800 King Edward Avenue
 *    Ottawa, Ontario, Canada, K1N 6N5
 *    
 *    and
 *    
 *    Olena Medelyan
 *    Department of Computer Science,
 *    The University of Waikato
 *    Private Bag 3105, Hamilton, New Zealand
 *    
 *    This file is part of Open Roget's Thesaurus ELKB.
 *    
 *    Copyright (c) 2009, Mario Jarmasz, Alistair Kennedy, Stan Szpakowicz and Olena Medelyan
 *    All rights reserved.
 *    
 *    Redistribution and use in source and binary forms, with or without
 *    modification, are permitted provided that the following conditions are met:
 *        * Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 *        * Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 *        * Neither the name of the University of Ottawa nor the
 *          names of its contributors may be used to endorse or promote products
 *          derived from this software without specific prior written permission.  
 *        * All advertising materials mentioning features or use of this software
 *          must display the following acknowledgement:
 *          This product includes software developed by the University of
 *          Ottawa and its contributors.
 *    
 *    THIS SOFTWARE IS PROVIDED BY Mario Jarmasz, Alistair Kennedy, Stan Szpakowicz 
 *    and Olena Medelyan ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
 *    BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 *    A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Mario Jarmasz, Alistair 
 *    Kennedy, Stan Szpakowicz and Olena Medelyan BE LIABLE FOR ANY DIRECT, 
 *    INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 *    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 *    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY 
 *    OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 *    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 *    EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.osu.slate.experiments.synonym;

import java.util.*;
import java.io.*;


import ca.site.elkb.RogetELKB;
import edu.osu.slate.experiments.Common;

/*******************************************************************************
 * Original Author: Tad Stach (1587688) Date: Saturday, March 2, 2002
 * 
 * Original Program Title: WordPower
 * Program Title: RogetsCompare
 * 
 * Program Description: This program uses the Roget's Electronic
 * Thesaurus to calculate the results of Reader's Digest Word Power game
 * questions. The Electronic Thesaurus has been designed and implemented by
 * Mario Jarmasz. Also note that this program is based highly on Mario Jarmasz's
 * Semantic Distance program.
 * 
 * Modified: Oct. 29, 2002 - Mario Jarmasz Nov. 7, 2003 - Mario Jarmasz
 * Modified: March 19, 2010 - Timothy Weale
 * 
 * Usage : java RogetsCompare <ELKBDirectory> <SynonymFile>
 * 
 * Format of input file: <problem word> | <solution word> | <other word> |
 * <other word> | <other word> A line beginning with a # is considered a comment
 * 
 * @author Mario Jarmasz and Alistsair Kennedy and Timothy Weale
 * @version 1.2OSU Mar 2010
 ******************************************************************************/
public class RogetsCompare {

	private static boolean verbose = true;
	private static String taskFile;
	private static RogetELKB elkb;
	
	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String [] args) throws IOException {

		/* Set parameters */
		if(args.length == 0) {
			//Common.task = "ESL";
			//Common.task = "TOEFL";
			Common.task = "RDWP300";
		}
		
		if(verbose) {
			System.out.println("Opening Alias File");
		}
		setFiles();
		
		elkb = new RogetELKB();
		
		wp(taskFile);		
	}
	
	/**
	 * Loads a file and finds the best synonym for a given word.
	 * 
	 * @param fileName
	 */
	private static void wp(String fileName) {
		// 1. read a line
		// 2. parse the 4 words
		// 3. save answer
		// 4. calculate paths
		// 5. find shortest
		// 6. compare to answer

		int index = 1;
		int correct = 0;
		int tieBroken = 0;
		int tieLost = 0;
		int tieUnsplit = 0;
		int notInIndex = 0;
		double finalScore = 0;
		// String not_found[] = new String [500];

		ArrayList<String> qWNotFound = new ArrayList<String>(); // question words
		ArrayList<String> aWNotFound = new ArrayList<String>(); // answer words
		ArrayList<String> oWNotFound = new ArrayList<String>(); // other words

		int nfcount = 0;
		int nf;

		// variables used for keeping stats:
		int questnotfound = 0; // Question not found in Index
		int answernotfound = 0; // Answer not found in Index
		int othernotfound = 0; // Other word not found in Index

		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			for (;;) {
				String line = br.readLine();
				// line = line.trim();

				if (line == null) {
					br.close();
					index--;
					break;
				}

				// do not read blank lines or line that start with #line
				else if ((line.startsWith("#") || line.equals("")) == false) {

					System.out.println("Question " + index + ": " + line);
					StringTokenizer st = new StringTokenizer(line, "|");

					String problem = st.nextToken().trim();
					String solution = st.nextToken().trim();
					String word2 = st.nextToken().trim();
					String word3 = st.nextToken().trim();
					String word4 = st.nextToken().trim();
					
					int[] probToSol = getMinDistance(problem, solution);
					int[] probToW2 = getMinDistance(problem, word2);
					int[] probToW3 = getMinDistance(problem, word3);
					int[] probToW4 = getMinDistance(problem, word4);
					
					nf = 0;

					if (inindex(problem) == 0) {
						System.out.println(problem + " (PROBLEM) not found in the index!!");
						qWNotFound.add(problem);
						nfcount++;
						questnotfound++;
						nf++;
					} else {

						/**
						 * Obtain the shortest path for each pair **
						 */

						/**
						 * Problem to Solution 
						 */

						if (probToSol[1] != 0) { //the word/phrase is found
							System.out.println(problem + " to " + solution + ", length = " + probToSol[0] + ", " + probToSol[1] + " path(s) of this length");
						} else { //the word/phrase is not found
							if (findbest(problem, solution).equals("NO GOOD")) { //divided word/phrase is not found
								System.out.println(solution + " (ANSWER) is NOT IN THE INDEX");
								aWNotFound.add(solution);
								answernotfound++;
								notInIndex++;
								nf++;
							} else { //divided word/phrase is found
								String bestWord = findbest(problem, solution);
								probToSol = getMinDistance(problem, bestWord);
								System.out.println(problem + " to " + solution + " (" + bestWord + "), length = " + probToSol[0] + ", " + probToSol[1] + " path(s) of this length");
							}
						}

						/** * Problem to Word2 ** */
						if (probToW2[1] != 0) {
							System.out.println(problem + " to " + word2 + ", length = " + probToW2[0] + ", " + probToW2[1] + " path(s) of this length");
						} else {
							if (findbest(problem, word2).equals("NO GOOD")) {
								System.out.println(word2 + " is NOT IN THE INDEX");
								othernotfound++;
								oWNotFound.add(word2);
								notInIndex++;
								nf++;
							} else {
								String bestWord = findbest(problem, word2);
								probToW2 = getMinDistance(problem, bestWord);
								System.out.println(problem + " to " + word2 + " (" + bestWord + "), length = " + probToW2[0] + ", " + probToW2[1] + " path(s) of this length");
							}
						}

						/** * Problem to Word3 ** */
						if (probToW3[1] != 0) {
							System.out.println(problem + " to " + word3 + ", length = " + probToW3[0] + ", " + probToW3[1] + " path(s) of this length");
						} else {
							if (findbest(problem, word3).equals("NO GOOD")) {
								System.out.println(word3 + " is NOT IN THE INDEX");
								othernotfound++;
								oWNotFound.add(word3);
								notInIndex++;
								nf++;
							} else {
								String bestWord = findbest(problem, word3);
								probToW3 = getMinDistance(problem, bestWord);
								System.out.println(problem + " to " + word3 + " (" + bestWord + "), length = " + probToW3[0] + ", " + probToW3[1] + " path(s) of this length");
							}
						}

						/** * Problem to Word4 ** */
						if (probToW4[1] != 0) {
							System.out.println(problem + " to " + word4 + ", length = " + probToW4[0] + ", " + probToW4[1] + " path(s) of this length");
						} else {
							if (findbest(problem, word4).equals("NO GOOD")) {
								System.out.println(word4 + " is NOT IN THE INDEX");
								othernotfound++;
								oWNotFound.add(word4);
								notInIndex++;
								nf++;
							} else {
								String bestWord = findbest(problem, word4);
								probToW4 = getMinDistance(problem, bestWord);
								System.out.println(problem + " to " + word4 + " (" + bestWord + "), length = " + probToW4[0] + ", " + probToW4[1] + " path(s) of this length");
							}
						}
						
						String[] words = {solution, word2, word3, word4};
						int[] dist = {probToSol[0], probToW2[0], probToW3[0], probToW4[0]};
						int[] tieBreak = {probToSol[1], probToW2[1], probToW3[1], probToW4[1]};
						boolean tie = false;
						int max = 0;
						for(int i = 0; i < words.length; i++){
							if(dist[i] > max){
								max = dist[i];
								tie = false;
							}
							else if(dist[i] == max){
								tie = true;
							}
						}
						int tieBreakMax = 0;
						int tieBreakMaxCount = 0;
						for(int i = 0; i < words.length; i++){
							if(dist[i] == max){
								if(tieBreak[i] > tieBreakMax){
									tieBreakMax = tieBreak[i];
									tieBreakMaxCount = 1;
								}
								else if(tieBreak[i] == tieBreakMax){
									tieBreakMaxCount++;
								}
							}
						}
						int bestWord = -1;
						boolean stillTied = false;
						for(int i = 0; i < words.length; i++){
							if(dist[i] == max && tieBreak[i] == tieBreakMax){
								if(bestWord != -1){
									stillTied = true;
								}
								bestWord = i;
							}
						}

						System.out.println("Roget thinks that " + problem + " means " + words[bestWord]);
						
						if(tie && dist[0] == max){
							if(stillTied){
								System.out.println("UNSPLIT TIE");
								tieUnsplit++;
								System.out.println(tieBreakMaxCount);
								finalScore += 1.0/(double)tieBreakMaxCount;
								System.out.println("INCORRECT");
							}
							if(!stillTied){
								if(bestWord == 0){
									System.out.println("TIE BROKEN");
									tieBroken++;
									System.out.println("CORRECT");
									correct++;
									finalScore += 1.0;
								}
								else{
									System.out.println("TIE LOST");
									tieLost++;
									System.out.println("INCORRECT");
								}
							}
						}
						else{
							if(bestWord == 0){
								System.out.println("CORRECT");
								correct++;
								finalScore += 1.0;
							}
							else{
								System.out.println("INCORRECT");
							}
						}
					}
					System.out.println();
					index++;
				}
			}
			System.out.println("Final score: " + correct + "/" + index + ". "
					+ tieBroken + " ties broken, " + tieLost + " ties lost, " 
					+ tieUnsplit + " ties unsplit.");
			System.out.println();
			double finalScoreOutput = finalScore/(double)index*100.0;
			System.out.println("Score: " + finalScoreOutput);
			System.out.println();
			//System.out.println("A word was not in the index " + notInIndex + " times. ");
			//System.out.println();
			//System.out.println("The question was not in the index " + questnotfound + " times.");
			//System.out.println();

			// New stats - MJ
			System.out.println("-- NEW STATS --");
			System.out.println("Question word not in index: " + questnotfound
					+ " times.");
			System.out.println("Answer word not in index: " + answernotfound
					+ " times.");
			System.out.println("Other word not in index: " + othernotfound
					+ " times.");

			// This loop can be added to list words and phrases not found in
			// 	
			System.out.println();

			System.out.println("The following question words were not found"
					+ " in Roget: " + qWNotFound);

			System.out.println("The following answer words were not found"
					+ " in Roget: " + aWNotFound);

			System.out.println("Other words that were not found"
					+ " in Roget: " + oWNotFound);

		} catch (Exception e) {
			System.out.println("Error:" + e);
		}
	}
	
	/**
	 * This procedure cuts up a phrase in relation to the solution to determine
	 * which word in the phrase corresponds best
	 * 
	 * @param prob
	 * @param phrase
	 * @return String representing best word from Roget's to represent a phrase.
	 */
	private static String findbest(String prob, String phrase) {
		String st[] = new String[10];
		int i = 0;
		StringTokenizer subst = new StringTokenizer(phrase, " ");
		int bestVal = 0;
		String bestWord = "";
		while (subst.hasMoreTokens()) {
			st[i] = subst.nextToken().trim();
			if (inindex(st[i]) == 1 && !st[i].equals("to")
					&& !st[i].equals("be") && !st[i].equals("and")) {
				//set[i] = elkb.getAllPaths(prob, st[i]);
				int[] wordDist = getMinDistance(prob,st[i]);
				if(wordDist[0] > bestVal){
					bestVal = wordDist[0];
					bestWord = st[i];
				}
			}
			i++;
		}
		if (bestWord.equals("")) {
			return "NO GOOD";
		}
		else{
			return bestWord;
		} 
	}

	/**
	 * This procedure returns 1 if the word is in the index, 0 otherwise
	 * 
	 * @param x
	 * @return 1 if word is in index, 0 otherwise.
	 */
	private static int inindex(String x) {
		int y = 1;
		ArrayList<String> inIndex = elkb.index.getStrRefList(x);
		if (inIndex.size() == 0) {
			y = 0;
		}
		return y;
	}

	/**
	 * Obtains the maximum similarity between two strings, passed as parameters.  
	 * All Parts of speech are considered.  The returned value is an integer valued
	 * 0, 2, ..., 16, where 16 is the most similar. It also returns the count of how
	 * many times the best score is computed for the pair of words.  These are returned
	 * in an integer array where the distance is in position 0, and the count of best
	 * distances is in position 1.
	 * 
	 * @param word1
	 * @param word2
	 * @return int array containing minimum distance in position 0 and count of min distances in position 1.
	 */
	private static int[] getMinDistance(String word1, String word2) {
		ArrayList<int[]> list1 = elkb.index.getEntryListNumerical(word1);
		ArrayList<int[]> list2 = elkb.index.getEntryListNumerical(word2);
		if(list1.size() == 0 || list2.size() == 0){
			int[] toReturn = { 0, 0 };
			return toReturn;
		}
		
		int best = 0;
		int bestCount = 0;
		for (int i = 0; i < list1.size(); i++) {
			int[] entry1 = list1.get(i);
			for (int j = 0; j < list2.size(); j++) {
				int[] entry2 = list2.get(j);
				int diff = 16;
				for (int k = 0; k < 8; k++) {
					if (entry1[k] != entry2[k]){
						if(2*k < diff){
							diff = 2*k;
						}
					}
				}
				if(best < diff){
					best = diff;
					bestCount = 1;
				}
				else if(best == diff){
					bestCount++;
				}
			}
		}
		int[] toReturn = { best, bestCount };
		return toReturn;
	}
	
	/**
	 * Sets the name of:<br>
	 * 
	 * <ul>
	 * <li> Synonym Task file</li>
	 * </ul>
	 */
	private static void setFiles() {
		/* Set directory, data source */
		String taskDir = "/u/weale/data/wordpair/";
		taskFile = taskDir + Common.task + ".txt";
	}
}
