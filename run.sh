#!/bin/bash

#echo "dry run"
#exit 0;

rm ../playpen/fire-scenarios/arkadia/{input,output}/*.???

evaluation="agof"
arkadiaScenario="../playpen/fire-scenarios/arkadia"
scenarioFile="${arkadiaScenario}/scenario.ini"
memoizationFile="../playpen/executions/arkadia_farsite_execution_memoization_${evaluation}.txt"
outputDir="../playpen/fire-scenarios/arkadia/output"

for seed in 90 91 92 93 94 98 99; do
	scenario="execution_${evaluation}_seed_${seed}_1"
	outputFile="../playpen/two_stage_prediction_${evaluation}_${seed}.txt"
	cacheDir="../playpen/executions/cached/arkadia/${scenario}"
	java \
		-jar  two-stage-prediction-0.0.1-SNAPSHOT.jar \
		-f    nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction \
		-c    ${arkadiaScenario} \
		-m    ${memoizationFile} \
		-t    900 \
		-p      1 \
		-s    ${seed} \
		-e    ${evaluation} \
		| tee ${outputFile}
	mkdir -p               ${cacheDir}/output/
	mv ${outputDir}/*      ${cacheDir}/output/
	mv ${outputFile}       ${cacheDir}/
	mv tsp.log             ${cacheDir}/
	cp ${scenarioFile}     ${cacheDir}/
	sed -i "s#../playpen/fire-scenarios/arkadia/#${cacheDir}/#" ${memoizationFile}

	cd ${cacheDir}/output/
	mkdir jpgs
	cd    jpgs
	cp ../*.jpg .
	rename 's/\d+/sprintf("%02d",$&)/e' *.jpg
	convert -delay 10 -loop 0 shape_*_*.jpg ${scenario}.gif
	#eog ${scenario}.gif
	cd ~/git/two-stage-prediction/target
done;

exit 0;
