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
package edu.cuhk.hccl.evaluation;

import java.util.List;

public class Metric {

	public static final int RMSE = 1;
	public static final int MAE = 2;

	public static double computeRMSE(List<Double> reals, List<Double> estimates) {
		
		double error = 0D;
		int total = reals.size();
		
		for (int i=0; i < total; i++){
			error += Math.pow(reals.get(i) - estimates.get(i), 2);
		}
		
		return Math.sqrt(error / total);
	}

	public static double computeMAE(List<Double> reals, List<Double> estimates) {
		
		double error = 0D;
		int total = reals.size();
		
		for (int i=0; i < total; i++){
			error += Math.abs(reals.get(i) - estimates.get(i));
		}
		
		return error / total;
	}
}
