cd /home/edigley/doutorado_uab/git/two-stage-prediction/
mvn clean
mvn install
cd target
java -jar two-stage-prediction-0.0.1-SNAPSHOT.jar -f nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction -c ../playpen/fire-scenarios/jonquera/ -m ../playpen/farsite_execution_memoization_900_seconds_with_adj_factor_4_threads.txt -t 900 -s 123 -p 4 | tee ../playpen/two_stage_prediction.txt 
