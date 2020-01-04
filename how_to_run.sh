cd /home/edigley/doutorado_uab/git/two-stage-prediction/
mvn clean
mvn install
cd target
#java -jar two-stage-prediction-0.0.1-SNAPSHOT.jar -f nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction -c ../playpen/fire-scenarios/jonquera/ -m ../playpen/farsite_execution_memoization_900_seconds_with_adj_factor_4_threads.txt -t 900 -s 123 -p 4 | tee ../playpen/two_stage_prediction.txt 
java  -jar two-stage-prediction-0.0.1-SNAPSHOT.jar -f nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction -c ../playpen/fire-scenarios/jonquera/ -m ../playpen/farsite_execution_memoization_7200_seconds_with_fire_error_adjustment_factor_and_sensible_to_layer_extent.txt -t 7200 -p 1 -s 16 | tee ../playpen/two_stage_prediction.txt

~/doutorado_uab/git/two-stage-prediction/target$ mkdir ~/desktop/jonquera_10_generations_adaptive_killing/19
~/doutorado_uab/git/two-stage-prediction/target$ mv ../playpen/fire-scenarios/jonquera/{input,output}  ~/desktop/jonquera_10_generations_adaptive_killing/19/
~/doutorado_uab/git/two-stage-prediction/target$ mv ../playpen/two_stage_prediction_19.txt ~/desktop/jonquera_10_generations_adaptive_killing/19/
~/doutorado_uab/git/two-stage-prediction/target$ mv tsp.log ~/desktop/jonquera_10_generations_adaptive_killing/19/

