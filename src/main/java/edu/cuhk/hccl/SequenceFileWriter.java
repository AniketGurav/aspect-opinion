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
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

public class SequenceFileWriter {

	public static void main(String[] args) throws IOException {
		// Check parameters
		if (args.length < 3){
			System.out.print("Please specify three parameters!");
			System.exit(-1);
		}
		
		File filesDir = new File(args[0]);
		String targetDir = args[1];
		final int NUM_SPLITS = Integer.parseInt(args[2]);
		
		// Remove old target directory first
		FileUtils.deleteQuietly(new File(targetDir));

		File[] dataFiles = filesDir.listFiles();
		
		int total = dataFiles.length;
		int range = (int) Math.round(total / NUM_SPLITS + 0.5);
		System.out.printf("[INFO] The number of total files is %d \n.", total);
		
		for (int i = 1; i <= NUM_SPLITS; i++) {
			int start = (i - 1) * range;
			int end = Math.min(start + range, total);
			File[] subFiles = Arrays.copyOfRange(dataFiles, start, end);
			createSeqFile(subFiles, FilenameUtils.normalize(targetDir + "/" + i + ".seq"));
		}

		System.out.println("[INFO] All files have been successfully processed!");
	}

	private static void createSeqFile(File[] files, String seqName) {
		Configuration conf = new Configuration();
		LongWritable key = new LongWritable();
		Text value = new Text();

		SequenceFile.Writer writer = null;

		try {
			FileSystem fs = FileSystem.get(URI.create(seqName), conf);
			writer = SequenceFile.createWriter(fs, conf, new Path(seqName), key.getClass(), value.getClass());

			for (File file : files) {
				//System.out.printf("Processing file: %s \n", file.getPath());
				key.set(Integer.parseInt(file.getName().split("_")[1]));
				value.set(FileUtils.readFileToString(file));
				writer.append(key, value);
			}
			System.out.printf("[INFO] The sequence file %s has been created for %d files! \n", seqName, files.length);

		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			IOUtils.closeStream(writer);
		}
	}
}