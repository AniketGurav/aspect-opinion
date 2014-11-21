/**
 * Copyright (C) 2013 Pengfei Liu <pfliu@se.cuhk.edu.hk>
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

 The main applications in use for this project:

 1. NLP based approach
    1.1 Single-process Java program (package edu.cuhk.hccl)
        TripAdvisorApp.java (for TripAdvisor dataset)
        YelpApp.java (for Yelp dataset)
    1.2 Distributed MapReduce implementation (package edu.cuhk.hccl.hadoop)
        TripAdvisorSeqFileApp.java (for TripAdvisor dataset)
        TripAdvisorMapper.java

        YelpApp.java (for Yelp dataset)
        YelpMapper.java

        ReviewReducer.java

 2. Collaborative filtering to handle incomplete reviews
    2.1 CollaborativeFilitering.java
        baseline methods: user-average, item-average
        memory-based methods: user-based, item-based
        model-based methods: SVD
    2.2 CompleteMatrixApp.java
        Entry program to fill missing aspect ratings

 3. Evaluation Metrics (RMSE and MAE)
    EvaluationApp.java
        Support both RMSE and MAE metric

 4. How to compile the code
    Compile:
        mvn compile (You have to install Maven first)

 5. Datasets in use
    5.1 TripAdvisor
        http://sifaka.cs.uiuc.edu/~wang296/Data/LARA/TripAdvisor/
    5.2 Yelp's Academic Dataset
        https://www.yelp.com/academic_dataset/

 For any issues regarding the source code, please contact Pengfei Liu (pfliu@se.cuhk.edu.hk).
