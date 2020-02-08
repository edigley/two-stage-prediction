#!/bin/bash

#echo "dry run"
#exit 0;

rm ../playpen/fire-scenarios/jonquera/{input,output}/*.???

jonqueraScenario="../playpen/fire-scenarios/jonquera"
scenarioFile="${jonqueraScenario}/scenario.ini"
memoizationFile="../playpen/executions/jonquera_farsite_execution_memoization_agof.txt"
outputDir="../playpen/fire-scenarios/jonquera/output"

for seed in `seq 58 59`; do
outputFile="../playpen/two_stage_prediction_agof_${seed}.txt"
cacheDir="../playpen/executions/cached/execution_agof_seed_${seed}_1"
	java \
		-jar two-stage-prediction-0.0.1-SNAPSHOT.jar \
		-f   nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction \
		-c   ${jonqueraScenario} \
		-m   ${memoizationFile} \
		-t    900 \
		-p      1 \
		-s  ${seed} \
		-e   agof \
		| tee ${outputFile}
	mkdir -p               ${cacheDir}/output/
	mv ${outputDir}/*      ${cacheDir}/output/
	mv ${outputFile}       ${cacheDir}/
	mv tsp.log             ${cacheDir}/
	cp ${scenarioFile}     ${cacheDir}/
	sed -i "s#playpen/fire-scenarios/jonquera/#${cacheDir}/#" ${memoizationFile}
done;

exit 0;
