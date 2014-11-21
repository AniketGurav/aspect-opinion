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
import java.util.List;

import org.apache.commons.io.FileUtils;

public class TripRealRatingsApp {

	public static void main(String[] args) throws IOException {		
		File dir = new File(args[0]);
		File outFile = new File(args[1]);
		outFile.delete();

		StringBuilder buffer = new StringBuilder();

		for (File file : dir.listFiles()) {
			List<String> lines = FileUtils.readLines(file, "UTF-8");
			String hotelID = file.getName().split("_")[1];
			String author = null;
			boolean noContent = false;
			for (String line : lines) {
				if (line.startsWith("<Author>")) {
					try {
						author = line.split(">")[1].trim();
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println("[ERROR] An error occured on this line:");
						System.out.println(line);
						continue;
					}
				} else if (line.startsWith("<Content>")) { // ignore records if they have no content
					String content = line.split(">")[1].trim();
					if (content == null || content.equals(""))
						noContent = true;
				} else if (line.startsWith("<Rating>")) {
					String[] rates = line.split(">")[1].trim().split("\t");

					if (noContent || rates.length != 8)
						continue;
					
					// Change missing rating from -1 to 0
					for (int i = 0; i < rates.length; i++) {
						if (rates[i].equals("-1"))
							rates[i] = "0";
					}

					buffer.append(author + "\t");
					buffer.append(hotelID + "\t");

					// overall
					buffer.append(rates[0] + "\t");
					// location
					buffer.append(rates[3] + "\t");
					// room
					buffer.append(rates[2] + "\t");
					// service
					buffer.append(rates[6] + "\t");
					// value
					buffer.append(rates[1] + "\t");
					// cleanliness
					buffer.append(rates[4] + "\t");

					buffer.append("\n");
				}
			}
			
			// Write once for each file
			FileUtils.writeStringToFile(outFile, buffer.toString(), true);
			
			// Clear buffer
			buffer.setLength(0);
			System.out.printf("[INFO] Finished processing %s\n", file.getName());
		}
		System.out.println("[INFO] All processinig are finished!");
	}
}
