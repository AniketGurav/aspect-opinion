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

import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import edu.cuhk.hccl.data.NounPhrase;
import edu.cuhk.hccl.data.UserItemPair;
import edu.cuhk.hccl.util.Constant;

public class HadoopApp extends Configured implements Tool{

	public static void main(String[] args) throws Exception {
        int status = ToolRunner.run(new Configuration(), new HadoopApp(), args);
        System.exit(status);
    }
	
    @Override
    public int run(String[] args) throws Exception {
    	
    	if (args == null || args.length < 4){
    		System.out.println("Please specify parameters: input, output, domain, num-reducers!");
    		System.exit(-1);
    	}
    	
    	String input = args[0];
    	String output = args[1];
    	String domain = args[2];
    	int numReducers = Integer.parseInt(args[3]);
    	float similarity = Float.parseFloat(args[4]);
    	int range = Integer.parseInt(args[5]);
    	
        Job job = new Job(new Configuration(), this.getClass().getSimpleName());

        // Must below the line of job creation
        Configuration conf = job.getConfiguration();
        // Reuse the JVM
        conf.setInt("mapred.job.reuse.jvm.num.tasks", -1);
        conf.setFloat("SIM_THRESHOLD", similarity);
        conf.setInt("SEARCH_RANGE", range);
        
        if (domain.equalsIgnoreCase("restaurant")){
        	conf.setStrings("ASPECTS", Constant.RESTAURANT_ASPECTS);
            job.setMapperClass(YelpMapper.class);
            job.setInputFormatClass(TextInputFormat.class);
            
            // args[4] is the business file to select matching business_ids to restaurant
            String busiFile = args[6];
            DistributedCache.addCacheFile(new URI(busiFile), conf);
        } else if (domain.equalsIgnoreCase("hotel")){
        	conf.setStrings("ASPECTS", Constant.TRIPADVISOR_ASPECTS);
            job.setMapperClass(TripAdvisorMapper.class);
            job.setInputFormatClass(SequenceFileInputFormat.class);
        } else{
        	System.out.println("Wrong domain type!");
        	System.exit(-1);
        }
        
        job.setJarByClass(HadoopApp.class);
        job.setReducerClass(ReviewReducer.class);
        job.setNumReduceTasks(numReducers);

        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(UserItemPair.class);
        job.setOutputValueClass(NounPhrase.class);

        // Delete output if exists
        Path outputDir = new Path(output);
        FileSystem hdfs = FileSystem.get(conf);
        if (hdfs.exists(outputDir))
            hdfs.delete(outputDir, true);

        FileInputFormat.setInputPaths(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));
        
        job.waitForCompletion(true);
        return 0;
    }
}