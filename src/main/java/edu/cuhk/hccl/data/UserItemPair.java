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
package edu.cuhk.hccl.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

public class UserItemPair implements WritableComparable<UserItemPair>{
	private Text userID;
	private Text itemID;
	
	public UserItemPair() {
		setUserID(new Text());
		setItemID(new Text());
	}
	
	public UserItemPair(Text user, Text text) {
		setUserID(user);
		setItemID(text);
	}

	public UserItemPair(String user, String item) {
		set(user, item);
	}

	public Text getUserID() {
		return userID;
	}

	public void setUserID(Text userID) {
		this.userID = userID;
	}

	public Text getItemID() {
		return itemID;
	}

	public void setItemID(Text itemID) {
		this.itemID = itemID;
	}

	public void set(String user, String item) {
		setUserID(new Text(user));
		setItemID(new Text(item));
	}
	
	@Override
	public String toString(){
		return userID + "\t" + itemID;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		userID.readFields(in);
		itemID.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		userID.write(out);
		itemID.write(out);
	}

	@Override
	public int compareTo(UserItemPair pair) {
		int userCompare = userID.compareTo(pair.userID);
		return (userCompare == 0) ?  itemID.compareTo(pair.itemID) : userCompare;
	}
}
