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
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.cuhk.hccl.util.Constant;
import edu.cuhk.hccl.util.DatasetUtil;

/**
 * @author Pengfei Liu (pfliu@se.cuhk.edu.hk)
 * 
 */
public class YelpApp extends BaseApp {

	private Gson gson = new GsonBuilder().create();
	private HashSet<String> itemSet = new HashSet<String>();
	
	public YelpApp(){
		options.addOption("f", true, "data file");
		options.addOption("b", true, "business file");
		options.addOption("c", true, "category name");
	}
	
	public static void main(String[] args) throws IOException {
		BaseApp app = new YelpApp();
		app.run(args);
	}

	@Override
	public void run(String[] args) throws IOException {
		super.run(args);
		
		// Put into itemSet once all business_ids for some category (e.g. restaurant) from a file
		String businessFile = cmdLine.getOptionValue('b');
		String category = cmdLine.getOptionValue('c');
		
		if (itemSet.isEmpty()) {
			try {
				List<String> lines = FileUtils.readLines(new File(businessFile), "UTF-8");
				for (String line : lines) {
					Business business = gson.fromJson(line, Business.class);

					boolean isIn = false;
					for (String cate : business.categories) {
						if (category.equals(cate.toLowerCase())) {
							isIn = true;
							break;
						}
					}

					if (isIn) {
						if (!itemSet.contains(business.business_id)) {
							itemSet.add(business.business_id);
						}
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Process data file and save result
		String dataFileName = cmdLine.getOptionValue('f');
		String result = processStream(dataFileName).toString();
		FileUtils.writeStringToFile(outFile, result, false);
		
		System.out.println("Processing is fininshed!");
	}

	@Override
	public StringBuilder processStream(String fileName) throws FileNotFoundException, IOException {
		StringBuilder result = new StringBuilder();

		List<String> lines = FileUtils.readLines(new File(fileName), "UTF-8");

		String[] rates = new String[4];

		for (String line : lines) {
			Review review = gson.fromJson(line, Review.class);

			if (review.text.isEmpty() || !itemSet.contains(review.getBusinessId()))
				continue;

			String author = review.user_id;
			String itemId = review.business_id;
			rates[0] = review.stars;
			rates[1] = review.votes.funny;
			rates[2] = review.votes.useful;
			rates[3] = review.votes.cool;

			ArrayList<String> selectedPhrase = DatasetUtil.processRecord(
					itemId, result, review.text, author, rates, Constant.RESTAURANT_ASPECTS, 2);
			
			for(String phrase : selectedPhrase){
        		FileUtils.writeStringToFile(phraseFile, phrase + "\n", true);
        	}
		}

		return result;
	}

	public class Review {
		private String business_id;
		private String user_id;
		private String stars;
		private String text;
		private String date;
		private Votes votes;

		@Override
		public String toString() {
			return String.format("{user_id: %s, business_id: %s, stars: %s, text: %s, date: %s, votes: %s}", 
					user_id, getBusinessId(), stars, text, date, votes.toString());
		}

		public String getBusinessID() {
			return getBusinessId();
		}

		public String getUserID() {
			return user_id;
		}

		public String getText() {
			return text;
		}

		public void setBusinessID(String business_id) {
			this.setBusinessId(business_id);
		}

		public void setUserID(String user_id) {
			this.user_id = user_id;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getStars() {
			return stars;
		}

		public void setStars(String stars) {
			this.stars = stars;
		}

		public String getBusinessId() {
			return business_id;
		}

		public void setBusinessId(String business_id) {
			this.business_id = business_id;
		}
	}

	public class Votes {
		private String funny;
		private String useful;
		private String cool;

		@Override
		public String toString() {
			return String.format("{funny: %s, useful: %s, cool: %s}", funny, useful, cool);
		}
	}

	public class Business {
		public String business_id;
		public String[] categories;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			for (String cate : categories) {
				builder.append(cate + ", ");
			}

			return String.format("{business_id: %s, categories: %s}", business_id, builder.toString());
		}
	}
}
