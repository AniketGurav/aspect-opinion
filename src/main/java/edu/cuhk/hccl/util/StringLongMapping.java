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

import java.util.HashMap;

public class StringLongMapping {
	private long userCounter = 0;
	private long itemCounter = 0;
	private HashMap<String, Long> userMap = new HashMap<String, Long>();
	private HashMap<String, Long> itemMap = new HashMap<String, Long>();

	private StringLongMapping() {
	}

	private static class SingletonHolder {
		public static final StringLongMapping INSTANCE = new StringLongMapping();
	}

	public static StringLongMapping Instance() {
		return SingletonHolder.INSTANCE;
	}

	public long getUserID(String user) {
		if (userMap.containsKey(user))
			return userMap.get(user);
		else {
			userCounter += 1;
			userMap.put(user, userCounter);
			return userCounter;
		}
	}

	public long getItemID(String item) {
		if (itemMap.containsKey(item))
			return itemMap.get(item);
		else {
			itemCounter += 1;
			itemMap.put(item, itemCounter);
			return itemCounter;
		}
	}

	public HashMap<String, Long> getUserMap() {
		return userMap;
	}

	public HashMap<String, Long> getItemMap() {
		return itemMap;
	}

}
