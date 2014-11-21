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
import java.util.ArrayList;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cuhk.hccl.data.DataRecord;
import edu.cuhk.hccl.data.NounPhrase;
import edu.cuhk.hccl.data.UserItemPair;
import edu.cuhk.hccl.util.NLPUtil;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public abstract class BaseMapper extends Mapper<LongWritable, Text, UserItemPair, NounPhrase> {
	static enum Counters {
		INPUT_REVIEWS
	}

	// Constants
	//public static final double THRESHOLD = 0.3;
	public static final double DEFAULT_SIMILARITY = -10;

	// For MapReduce framework
	private UserItemPair outputKey = new UserItemPair();
	private NounPhrase phrase = new NounPhrase();
	private IntWritable rating = new IntWritable();
	private DoubleWritable similarity = new DoubleWritable();

	private StanfordCoreNLP coreNLP = NLPUtil.createCoreNLP();
	private RelatednessCalculator calc = NLPUtil.createCalculator();

	// To be override
	protected String[] aspects;
	protected float simThreshold = 0.3f;
	protected int searchRange = 2;

	@Override
	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		DataRecord record = parseDataRecord(value);

		if (record != null)
			processRecord(record, context);

		// Report progress
		context.getCounter(Counters.INPUT_REVIEWS).increment(1);
	}

	public void processRecord(DataRecord record, Context context) throws IOException, InterruptedException {
		String rText = record.getReview();
		simThreshold = context.getConfiguration().getFloat("SIM_THRESHOLD", simThreshold);
		searchRange = context.getConfiguration().getInt("SEARCH_RANGE", searchRange);
		
		if (rText != null && !rText.isEmpty()) {

			ArrayList<String[]> pairs = NLPUtil.extractNounPhrases(coreNLP, rText, searchRange);

			outputKey.set(record.getUserID(), record.getItemID());
			rating.set(record.getRating());

			for (String[] pair : pairs) {
				// Find the most similar aspect for each pair, which matches exactly one aspect
				int maxIdx = -1;
				double maxSim = DEFAULT_SIMILARITY;
				for (int idx = 0; idx < aspects.length; idx++) {
					double sim = calc.calcRelatednessOfWords(pair[1], aspects[idx]);
					if (sim > simThreshold && sim > maxSim) {
						maxIdx = idx;
						maxSim = sim;
					}
				}
				// If find the aspect for current pair, output for Reducer
				if (maxIdx != -1) {
					similarity.set(maxSim);
					phrase.set(pair[0], pair[1], aspects[maxIdx], rating, similarity);
					context.write(outputKey, phrase);
				}
			}
		}
	}

	public abstract DataRecord parseDataRecord(Text value);

}
