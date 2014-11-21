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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.cuhk.hccl.YelpApp.Business;
import edu.cuhk.hccl.YelpApp.Review;
import edu.cuhk.hccl.data.DataRecord;
import edu.cuhk.hccl.util.Constant;

public class YelpMapper extends BaseMapper {

	public static final String CATEGORY = "restaurants";
	private static HashSet<String> itemSet = new HashSet<String>();
	private Gson gson = new GsonBuilder().create();
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException{
		 aspects = Constant.RESTAURANT_ASPECTS;
		
	     try {
            Path[] cacheFiles = DistributedCache.getLocalCacheFiles(context.getConfiguration());
           	readItemSet(cacheFiles[0], CATEGORY);
         } catch (IOException ioe) {
             System.err.println("Caught exception while getting cached files: " + StringUtils.stringifyException(ioe));
         }
	}
	
	@Override
	public DataRecord parseDataRecord(Text value) {
		Review review = gson.fromJson(value.toString(), Review.class);
		
		if (review.getText().isEmpty() || itemSet.contains(review.getBusinessId())){
			
			DataRecord record = new DataRecord();
			
			record.setUserID(review.getUserID());
			record.setItemID(review.getBusinessID());
			record.setReview(review.getText());
			record.setRating((int)Math.floor(Double.parseDouble(review.getStars())));
			
			return record;
			
		}else{
			return null;
		}
	}
	 
	private void readItemSet(Path filePath, String category) {
		if (itemSet.isEmpty()) {
			try {
				List<String> lines = FileUtils.readLines(new File(filePath.toString()), "UTF-8");
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
	}
}

	
