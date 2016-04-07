/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag_imp_exp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;


/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class MemoryBufferedReader extends BufferedReader{
	
	public MemoryBufferedReader(Reader in) {
		super(in);
	}

	public MemoryBufferedReader(Reader in, int sz) {
		super(in, sz);
	}

	String bufferedLine=null;

	@Override
	public String readLine() throws IOException {
		bufferedLine=super.readLine();
		return bufferedLine;
	}
	
	public String lastLineRead() {
		return bufferedLine;
	}
	

}
