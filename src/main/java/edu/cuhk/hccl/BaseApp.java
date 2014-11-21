/**
 * Copyright (C) 2014 Pengfei Liu <pfliu@se.cuhk.edu.hk>
 * The Chinese University of Hong Kong.
 *
 * This file is part of aspect-opinion.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cuhk.hccl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public abstract class BaseApp {
	
	protected Options options = new Options();
	protected CommandLineParser parser = new GnuParser();
	protected CommandLine cmdLine;
	
	protected File outFile;
	protected File phraseFile;
	
	public BaseApp() {
	    options.addOption("o", true, "output file");
	    options.addOption("p", true, "phrase file");
	}

	public abstract StringBuilder processStream(String fileName) throws FileNotFoundException, IOException;
	
	public void run(String[] args) throws IOException {
	    try {
	        cmdLine = parser.parse(options, args);
	        
	        outFile = new File(cmdLine.getOptionValue('o'));
	        phraseFile = new File(cmdLine.getOptionValue('p'));
	    } catch (ParseException e) {
	        System.out.print(e.getMessage());
	    }
	}
}