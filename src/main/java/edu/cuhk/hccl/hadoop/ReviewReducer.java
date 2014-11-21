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
package edu.cuhk.hccl.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import edu.cuhk.hccl.data.NounPhrase;
import edu.cuhk.hccl.data.UserItemPair;
import edu.cuhk.hccl.util.Constant;
import edu.cuhk.hccl.util.NLPUtil;
import edu.cuhk.hccl.util.SentiWordNet;

public class ReviewReducer extends Reducer<UserItemPair, NounPhrase, Text, Text> {
	
	private SentiWordNet swn = new SentiWordNet();
	private Text txtKey = new Text();
	private Text txtValue = new Text();

	private String[] aspects;
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException{
		aspects = context.getConfiguration().getStrings("ASPECTS");
	}
	
	protected void reduce(UserItemPair key, Iterable<NounPhrase> values, Context context) throws IOException, InterruptedException{
		int[] ratings = new int[aspects.length];
		int[] counter = new int[aspects.length];
		for (int i = 0; i < aspects.length; i++) {
			ratings[i] = 0;
			counter[i] = 0;
		}
		
		int overall = 0;
		boolean assigned = false;
		while (values.iterator().hasNext()) {
			NounPhrase phrase = values.iterator().next();
			int aspectIdx = getAspectIndex(phrase.getAspect().toString());
			if(aspectIdx != -1){
				String adjective = phrase.getAdjective().toString();
				boolean hasNegation = false;
				if (adjective.startsWith(NLPUtil.NOT_PREFIX)){
					adjective = adjective.substring(NLPUtil.NOT_PREFIX.length());
					hasNegation = true;
				}
				
				int sentiRate = swn.extract(adjective, "a");
				
				if (hasNegation){
					sentiRate = Constant.MAX_RATING - sentiRate;
				}
				
				if (sentiRate > 0){
					ratings[aspectIdx] += sentiRate;
					counter[aspectIdx] += 1;
				}
				
				if(!assigned){
					overall = phrase.getRating().get();
					assigned = true;
				}
			}
		}

		// Avoid zero division error
		for (int i = 0; i < aspects.length; i++) {
			if (counter[i] == 0)
				counter[i] = 1;
		}

		StringBuilder buffer = new StringBuilder();
		buffer.append(overall + "\t");
		
		// Get aspect ratings
		for (int j = 0; j < aspects.length; j++) {
			int asp_rate = (int) Math.ceil(ratings[j] * 1.0 / counter[j]);
			buffer.append(asp_rate + "\t"); // Set aspect rating even it is zero
		}

		if (buffer.length() > 0) {
			txtValue.set(buffer.toString());
			txtKey.set(key.toString());
			context.write(txtKey, txtValue);
		}
	}
	
	private int getAspectIndex(String aspect){
		for (int idx=0; idx < aspects.length; idx++){
			String asp = aspects[idx];
			if (asp.equalsIgnoreCase(aspect)){
				return idx;
			}
		}
		return -1;
	}
}