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
package edu.cuhk.hccl.util;

import java.util.ArrayList;

import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class DatasetUtil {
	
    public static final double THRESHOLD = 0.3;
    
    // Initialize tools
    static StanfordCoreNLP coreNLP = NLPUtil.createCoreNLP();
    static RelatednessCalculator calc = NLPUtil.createCalculator();
    static SentiWordNet swn = new SentiWordNet();

    public static ArrayList<String> processRecord(String itemId, StringBuilder result,
			String review, String author, String[] rates, final String[] ASPECTS, int searchRange) {
		ArrayList<String[]> pairs = NLPUtil.extractNounPhrases(coreNLP, review, searchRange);
		
		ArrayList<String> selectedPhrase = new ArrayList<String>();
		
		int[] ratings = new int[ASPECTS.length];
		int[] counter = new int[ASPECTS.length];
		for (int i = 0; i < ASPECTS.length; i++) {
		    ratings[i] = 0;
		    counter[i] = 0;
		}
	
		for (String[] pair : pairs) {
		    int maxIdx = -1;
		    double similarity = -10;
		    // Find the most similar aspect for each pair
		    for (int idx = 0; idx < ASPECTS.length; idx++) {
		        double s = calc.calcRelatednessOfWords(pair[1], ASPECTS[idx]);
		        if (s > THRESHOLD && s > similarity) {
		            maxIdx = idx;
		            similarity = s;
		        }
		    }
		    // If find the aspect for current pair, add its sentiment
		    // score
		    if (maxIdx > -1) {
		    	int pairRate = swn.extract(pair[0], "a");
		        ratings[maxIdx] += pairRate;
		        counter[maxIdx] += 1;
		        
		        selectedPhrase.add(pair[0] + "\t" + pair[1] + "\t" + pairRate);
		    }
		}
	
		// Avoid zero division error
		for (int i = 0; i < ASPECTS.length; i++) {
		    if (counter[i] == 0)
		        counter[i] = 1;
		}
	
		result.append(itemId + "\t");
		result.append(author + "\t");
		for (String rate : rates){
		    result.append(rate + "\t");
		}
		for (int j = 0; j < ASPECTS.length; j++) {
		    int asp_rate = (int) (ratings[j] / counter[j] + 0.5);
		    result.append(asp_rate + "\t"); // Set aspect rating even it is zero
		}
		result.append(System.getProperty("line.separator"));
		
		return selectedPhrase;
	}
}
