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

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import edu.cuhk.hccl.data.DataRecord;
import edu.cuhk.hccl.util.Constant;

public class TripAdvisorMapper extends BaseMapper {

	private final int NUM_LINES = 5;
	private String hotelID;

	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		aspects = Constant.TRIPADVISOR_ASPECTS;
	}

	@Override
	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		hotelID = key.toString();
		
		String[] lines = value.toString().split("\n");
		StringBuilder buffer = new StringBuilder();
		Text fiveLines = new Text();
		for(int i = 1; i <= lines.length; i++){ 
			buffer.append(lines[i-1]+"\n");
			if(i % NUM_LINES == 0){
				fiveLines.set(buffer.toString());
				DataRecord record = parseDataRecord(fiveLines);
				if (record != null)
					processRecord(record, context);
				buffer.setLength(0);;
			}
		}
		
		// Report progress
		context.getCounter(Counters.INPUT_REVIEWS).increment(1);
	}
	
	public DataRecord parseDataRecord(Text value) {
		DataRecord record = new DataRecord();

		record.setItemID(hotelID);
		String[] lines = value.toString().split("\n");
		for (String line : lines) {
			try {
				if (line.startsWith("<Author>")) {
					record.setUserID(line.split(">")[1].trim());
				} else if (line.startsWith("<Content>")) {
					String content = line.split(">")[1].trim();
					if (content == null || content.equals(""))
						return null;
					else
						record.setReview(content);
				} else if (line.startsWith("<Rating>")) {
					String[] rates = line.split(">")[1].trim().split("\t");
					record.setRating(Integer.parseInt(rates[0]));
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.print(e.getMessage());
			}
		}
		return record;
	}
}
