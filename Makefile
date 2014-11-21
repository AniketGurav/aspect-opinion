trip-single-process:
	mvn -q compile exec:java -Dexec.mainClass=edu.cuhk.hccl.TripAdvisorApp \
		-Dexec.args="-d ${HOME}/data/TripAdvisor/Texts/ -o trip-rating.txt -p trip-phrase.txt"

yelp-single-process:
	mvn -q compile exec:java -Dexec.mainClass=edu.cuhk.hccl.YelpApp \
		-Dexec.args="-f ${HOME}/data/yelp/yelp_phoenix_academic_dataset/yelp_academic_dataset_review.json -o yelp-rating.txt -b ${HOME}/data/yelp/yelp_phoenix_academic_dataset/yelp_academic_dataset_business.json -c restaurants -p yelp-phrase.txt"
	make statistic file=yelp-rating.txt

yelp-hdfs:
	hadoop dfs -copyFromLocal yelp/ yelp/

compile-hadoopapp:
	mvn clean compile assembly:single

yelp-hadoop:
	hadoop jar target/HadoopApp-jar-with-dependencies.jar yelp/yelp_academic_dataset_review.json \
		yelp-out restaurant 6 ${threshold} ${range} yelp/yelp_academic_dataset_business.json
	rm -f yelp-hadoop-${threshold}-${range}.txt
	hadoop dfs -getmerge trip-seq-out/ yelp-hadoop-${threshold}-${range}.txt
	make statistic file=yelp-hadoop-${threshold}-${range}.txt

yelp-vary-reducer:
	hadoop jar target/HadoopApp-jar-with-dependencies.jar yelp/yelp_academic_dataset_review.json \
		yelp-out restaurant ${reducer} 0.3 3 yelp/yelp_academic_dataset_business.json

trip-sequence-file:
	rm -rf trip-seq
	mvn -q compile exec:java -Dexec.mainClass=edu.cuhk.hccl.SequenceFileWriter -Dexec.args="${HOME}/data/TripAdvisor/Texts/ trip-seq 10"
	hadoop dfs -rmr trip-seq
	hadoop dfs -copyFromLocal trip-seq trip-seq

trip-hadoop:
	hadoop jar target/HadoopApp-jar-with-dependencies.jar trip-seq trip-seq-out hotel 6 ${threshold} ${range}
	rm -f trip-hadoop-${threshold}-${range}.txt
	hadoop dfs -getmerge trip-seq-out/ trip-hadoop-${threshold}-${range}.txt
	make statistic file=trip-hadoop-${threshold}-${range}.txt

trip-vary-reducer:
	hadoop jar target/HadoopApp-jar-with-dependencies.jar trip-seq trip-seq-out hotel ${reducer} 0.3 3

statistic:
	@echo "Number of users:"
	cut -f1 ${file} | sort -u | wc
	@echo "Number of items:"
	cut -f2 ${file} | sort -u | wc
	@echo "Number of reviews:"
	cat ${file} | wc
	@echo "Count missing:"
	cat ${file} | cut -f3 | grep "0" | wc -l
	cat ${file} | cut -f4 | grep "0" | wc -l
	cat ${file} | cut -f5 | grep "0" | wc -l
	cat ${file} | cut -f6 | grep "0" | wc -l
	cat ${file} | cut -f7 | grep "0" | wc -l
	cat ${file} | cut -f8 | grep "0" | wc -l

trip-real-ratings:
	mvn -q compile exec:java -Dexec.mainClass=edu.cuhk.hccl.TripRealRatingsApp -Dexec.args="${HOME}/data/TripAdvisor/Texts/ trip-real-ratings.txt"
	make statistic file=trip-real-ratings.txt

trip-experiment:
	make trip-real-ratings
	make trip-hadoop threshold=${threshold} range=${range}
	mvn -q compile exec:java -Dexec.mainClass=edu.cuhk.hccl.evaluation.EvaluationApp -Dexec.args="trip-real-ratings.txt trip-hadoop-${threshold}-${range}.txt"

trip-distribution:
	 make trip-experiment threshold=0.3 range=3 > 0.3-3-trip.txt
	 python count_ratings.py ./trip-real-ratings.txt 6
	 python count_ratings.py ./trip-hadoop-0.3-3.txt 6

declare-license:
	mvn license:check
	mvn license:format
	
install-ws4j:
	wget https://ws4j.googlecode.com/files/ws4j-1.0.1.jar
	mvn install:install-file -Dfile=ws4j-1.0.1.jar -DgroupId=edu.cmu.lti -DartifactId=ws4j -Dversion=1.0.1 -Dpackaging=jar
