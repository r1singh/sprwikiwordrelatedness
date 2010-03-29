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

package edu.osu.slate.experiments.synonym;

import java.io.*;

public class RelID implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double rel;
	private int ID;
	
	public double getRel() {
		return rel;
	}
	
	public void setRel(double r) {
		rel = r;
	}
	
	public int getID() {
		return ID;
	}
	
	public RelID(double r, int i) {
		rel = r;
		ID = i;
	}
	
	public int compareTo(RelID o) {
		if( (this.rel-o.rel) < 0)
			return -1;
		else if( (this.rel-o.rel) > 0)
			return 1;
		else
			return 0;
	}
	
	 private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeDouble(rel);
		out.writeInt(ID);
	 }
	 
	 private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		 rel = in.readDouble();
		 ID = in.readInt();
	 }

}
