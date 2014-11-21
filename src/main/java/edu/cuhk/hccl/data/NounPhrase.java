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

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

public class NounPhrase implements Writable{
	private Text adjective;
	private Text noun;
	private Text aspect;
	private IntWritable rating;
	private DoubleWritable similarity;
	
	public NounPhrase(){
		setAdjective(new Text());
		setNoun(new Text());
		setAspect(new Text());
		setRating(new IntWritable());
		setSimilarity(new DoubleWritable());
	}
	
	public NounPhrase(String strAdj, String strNoun, String strAsp, IntWritable rating, DoubleWritable similarity) {
		set(strAdj, strNoun, strAsp, rating, similarity);
	}

	public Text getAdjective() {
		return adjective;
	}
	public void setAdjective(Text adjective) {
		this.adjective = adjective;
	}
	public Text getNoun() {
		return noun;
	}
	public void setNoun(Text noun) {
		this.noun = noun;
	}
	
	public IntWritable getRating() {
		return rating;
	}

	public void setRating(IntWritable rating) {
		this.rating = rating;
	}

	public Text getAspect() {
		return aspect;
	}

	public void setAspect(Text aspect) {
		this.aspect = aspect;
	}

	public DoubleWritable getSimilarity() {
		return similarity;
	}

	public void setSimilarity(DoubleWritable similarity) {
		this.similarity = similarity;
	}

	public void set(String strAdj, String strNoun, String strAsp, IntWritable rating, DoubleWritable similarity) {
		setAdjective(new Text(strAdj));
		setNoun(new Text(strNoun));
		setAspect(new Text(strAsp));
		setRating(rating);
		setSimilarity(similarity);
	}
	
	@Override
	public String toString(){
		return adjective + "\t" + noun + "\t" + rating;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		adjective.readFields(in);
		noun.readFields(in);
		aspect.readFields(in);
		rating.readFields(in);
		similarity.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		adjective.write(out);
		noun.write(out);
		aspect.write(out);
		rating.write(out);
		similarity.write(out);
	}
}
