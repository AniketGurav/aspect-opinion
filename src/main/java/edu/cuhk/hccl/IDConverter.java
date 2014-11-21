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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import edu.cuhk.hccl.util.StringLongMapping;

public class IDConverter {
	
	private static StringLongMapping mapping = StringLongMapping.Instance();
	
	public static void main(String[] args) {
		if(args.length < 2){
			printUsage();
		}
		
		try {
			File inFile = new File(args[0]);
			File outFile = new File(args[1]);
			
			if (inFile.isDirectory()){
				System.out.println("ID Converting begins...");
				FileUtils.forceMkdir(outFile);
				for (File file : inFile.listFiles()) {
					if (!file.isHidden())
						processFile(file, new File(outFile.getAbsolutePath() + "/" 
								+ FilenameUtils.removeExtension(file.getName()) + "-long.txt"));
	            }
			}else if(inFile.isFile()){
				System.out.println("ID Converting begins...");
				processFile(inFile, outFile);
			}else{
				printUsage();
			}
			
			System.out.println("ID Converting finished.");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void processFile(File inFile, File outFile) throws IOException{
		List<String> lines = FileUtils.readLines(inFile, "UTF-8");
		List<String> buffer = new ArrayList<String>();
		
		for (String line : lines) {
			String[] cols = line.split("\t");
			
			cols[0] = String.valueOf(mapping.getUserID(cols[0]));
			cols[1] = String.valueOf(mapping.getItemID(cols[1]));
			
			buffer.add(StringUtils.join(cols, '\t'));
		}
		
		Collections.sort(buffer);
		FileUtils.writeLines(outFile, buffer, false);
		
		System.out.printf("ID Converting finished for file: %s.\n", inFile.getAbsolutePath());
	}
	
	private static void printUsage(){
		System.out.println("Please specify input file/folder and output file/folder!");
		System.exit(-1);
	}
}
