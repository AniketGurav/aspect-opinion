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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.cuhk.hccl.util.Constant;
import edu.cuhk.hccl.util.DatasetUtil;

public class TripAdvisorApp extends BaseApp {

	public static void main(String[] args) throws IOException {
    	BaseApp app = new TripAdvisorApp();
		app.run(args);
    }

    public TripAdvisorApp(){
	    options.addOption("d", true, "data directory");
	}

    @Override
    public void run(String[] args) throws IOException {
    	super.run(args);
    	
        if (cmdLine.hasOption('d')) {
            File dir = new File(cmdLine.getOptionValue('d'));
            for (File file : dir.listFiles()) {
                FileUtils.writeStringToFile(outFile, processStream(file.getAbsolutePath()).toString(), true);
            }
            
            System.out.println("Processing is fininshed!");
        }
    }

	@Override
	public StringBuilder processStream(String fileName) throws FileNotFoundException, IOException {
	    StringBuilder result = new StringBuilder();
	    List<String> lines = FileUtils.readLines(new File(fileName), "UTF-8");

	    String hotelID = fileName.split("_")[1];
	    String author = null;
	    String review = null;
	    String[] rates = null;

	    for (String line : lines){
	        boolean recordEnd = false;
	        try {
	            if (line.startsWith("<Author>")) {
	                author = line.split(">")[1].trim();
	            } else if (line.startsWith("<Content>")) {
	            	review = line.split(">")[1].trim();
	            	if (review.isEmpty())
	            		continue;
	            } else if (line.startsWith("<Rating>")) {
	                rates = line.split(">")[1].trim().split("\t");
	                // Change missing rating from -1 to 0
	                for (int i = 0; i < rates.length; i++){
	                	if (rates[i].equals("-1"))
	                			rates[i] = "0";
	                }
	                recordEnd = true;
	            }
	        } catch (ArrayIndexOutOfBoundsException e) {
	            System.out.print(e.getMessage());
	            continue;
	        }

	        if (recordEnd) {
	        	ArrayList<String> selectedPhrase = DatasetUtil.processRecord(hotelID, result, review,
	        			author, rates, Constant.TRIPADVISOR_ASPECTS, 2);

	        	for(String phrase : selectedPhrase){
	        		FileUtils.writeStringToFile(phraseFile, phrase + "\n", true);
	        	}
	        }
	    }

	    return result;
	}

}
