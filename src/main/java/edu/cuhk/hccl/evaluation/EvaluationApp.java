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
package edu.cuhk.hccl.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.mahout.cf.taste.common.TasteException;

import edu.cuhk.hccl.util.StringLongMapping;

public class EvaluationApp {
	
	private static StringLongMapping mapping = StringLongMapping.Instance();
	
	public static void main(String[] args) throws IOException, TasteException{
		File realFile = new File(args[0]);
		File estimateFile = new File(args[1]);
		
		// Build real-rating map
		Map<String, long[]> realMap = buildRatingMap(realFile);
		
		// Build estimate-rating map
		Map<String, long[]> estimateMap = buildRatingMap(estimateFile);
		
		// Compare realMap with estimateMap
		Map<Integer, List<Double>> realList = new HashMap<Integer, List<Double>>();
		Map<Integer, List<Double>> estimateList = new HashMap<Integer, List<Double>>();
		
		// Use set to store non-duplicate pairs only
		Set<String> noRatingList = new HashSet<String>();
		
		for(String pair : realMap.keySet()){
			long[] realRatings = realMap.get(pair);
			long[] estimateRatings = estimateMap.get(pair);
			
			if (realRatings==null || estimateRatings == null)
				continue;
			
			for (int i = 0; i < realRatings.length; i++){
				long real = realRatings[i];
				long estimate = estimateRatings[i];
				
				// continue if the aspect rating can not be estimated due to incomplete reviews
				if(estimate <= 0){
					noRatingList.add(pair.replace("@", "\t"));
					continue;
				}
				
				if (real > 0 && estimate > 0){
					if (!realList.containsKey(i))
						realList.put(i, new ArrayList<Double>());
					
					realList.get(i).add((double) real);
					
					if (!estimateList.containsKey(i))
						estimateList.put(i, new ArrayList<Double>());
					
					estimateList.get(i).add((double) estimate);
				}
			}
		}
		
		System.out.println("[INFO] RMSE, MAE for estimate ratings: "); 
		System.out.println("------------------------------");
		System.out.println("Index \t RMSE \t MAE");
		for (int i = 1; i < 6; i++){
			double rmse = Metric.computeRMSE(realList.get(i), estimateList.get(i));
			double mae = Metric.computeMAE(realList.get(i), estimateList.get(i));
		
			System.out.printf("%d \t %.3f \t %.3f \n", i, rmse, mae);
		}
		
		System.out.println("------------------------------");
		
		if (noRatingList.size() > 0){
			String noRatingFileName = "evaluation-no-ratings.txt";
			FileUtils.writeLines(new File(noRatingFileName), noRatingList, false);
		
			System.out.println("[INFO] User-item pairs with no ratings are saved in file: " + noRatingFileName);
		} else {
			System.out.println("[INFO] All user-item pairs have ratings.");
		}
	}

	public static Map<String, long[]> buildRatingMap(File ratingFile) throws IOException {
		List<String> ratingLines = FileUtils.readLines(ratingFile, "UTF-8");
		Collections.sort(ratingLines);
		
		Map<String, long[]> dataMap = new HashMap<String, long[]>(); 
		
		for (String line : ratingLines) {
			String[] cols = line.split("\t");
			
			String pair = mapping.getUserID(cols[0].trim()) + "@" + mapping.getItemID(cols[1].trim());
			long[] ratings = new long[cols.length - 2];
				
			for (int j = 2; j < cols.length; j++){
				ratings[j-2] = Long.parseLong(cols[j]);
			}
			dataMap.put(pair, ratings);
		}
		
		return dataMap;
	}
}