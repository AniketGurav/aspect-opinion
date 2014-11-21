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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/*
 *
 * The class SentiWordNet is revised based on the sample code from http://stackoverflow.com/questions/15653091/how-to-use-sentiwordnet
 * 
 */

public class SentiWordNet {
	private String pathToSWN = "wordnet/SentiWordNet.txt";
	private HashMap<String, Integer> dictMap;

	public SentiWordNet() {

		dictMap = new HashMap<String, Integer>();
		HashMap<String, Vector<Double>> tempMap = new HashMap<String, Vector<Double>>();
		InputStream file = SentiWordNet.class.getClassLoader().getResourceAsStream(pathToSWN);
		BufferedReader csv = new BufferedReader(new InputStreamReader(file));
		String line = "";

		try {
			while ((line = csv.readLine()) != null) {
				if (line.startsWith("#")) {
					continue;
				}
				String[] data = line.split("\t");
				Double score;
				try {
					score = Double.parseDouble(data[2]) - Double.parseDouble(data[3]);
				} catch (NumberFormatException e) {
					continue;
				}

				String[] words = data[4].split(" ");
				for (String w : words) {
					String[] w_n = w.split("#");
					w_n[0] += "#" + data[0];
					int index = Integer.parseInt(w_n[1]) - 1;
					if (tempMap.containsKey(w_n[0])) {
						Vector<Double> v = tempMap.get(w_n[0]);
						if (index > v.size())
							for (int i = v.size(); i < index; i++)
								v.add(0.0);
						v.add(index, score);
						tempMap.put(w_n[0], v);
					} else {
						Vector<Double> v = new Vector<Double>();
						for (int i = 0; i < index; i++)
							v.add(0.0);
						v.add(index, score);
						tempMap.put(w_n[0], v);
					}
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Set<String> tempKeys = tempMap.keySet();
		for (Iterator<String> iterator = tempKeys.iterator(); iterator.hasNext();) {
			String word = (String) iterator.next();
			Vector<Double> v = tempMap.get(word);
			double score = 0.0;
			double sum = 0.0;
			for (int i = 0; i < v.size(); i++)
				score += ((double) 1 / (double) (i + 1)) * v.get(i);
			for (int i = 1; i <= v.size(); i++)
				sum += (double) 1 / (double) i;
			score /= sum;
			int rating = 0;
			if (score >= 0.75)
				rating = 5; // "strong_positive";
			else if (score > 0.25 && score <= 0.5)
				rating = 5; // "positive";
			else if (score > 0 && score >= 0.25)
				rating = 4; // "weak_positive";
			else if (score < 0 && score >= -0.25)
				rating = 3; // "weak_negative";
			else if (score < -0.25 && score >= -0.5)
				rating = 2; // "negative";
			else if (score <= -0.75)
				rating = 1; // "strong_negative";

			dictMap.put(word, rating);
		}
	}

	public int extract(String word, String pos) {
		try {
			return dictMap.get(word + "#" + pos).intValue();
		} catch (NullPointerException e) {
			return 0;
		}
	}
}
