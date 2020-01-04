#!/bin/bash

SEED=$1

cd ~/git/two-stage-prediction/target

java  -jar two-stage-prediction-0.0.1-SNAPSHOT.jar -f nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction -c ../playpen/fire-scenarios/jonquera/ -m ../playpen/farsite_execution_memoization_7200_seconds_with_fire_error_adjustment_factor_and_sensible_to_layer_extent_18.txt -t 7200 -p 1 -s ${SEED} | tee ../playpen/two_stage_prediction_${SEED}.txt


mkdir ~/desktop/jonquera_10_generations_adaptive_killing/${SEED}/


mv tsp.log ~/desktop/jonquera_10_generations_adaptive_killing/${SEED}/


mv ../playpen/two_stage_prediction_${SEED}.txt ~/desktop/jonquera_10_generations_adaptive_killing/${SEED}/


mv ../playpen/fire-scenarios/jonquera/input   ~/desktop/jonquera_10_generations_adaptive_killing/${SEED}/

mv ../playpen/fire-scenarios/jonquera/output  ~/desktop/jonquera_10_generations_adaptive_killing/${SEED}/


exit 0;
