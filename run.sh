#!/bin/bash

#echo "dry run"
#cd ../ && mvn clean && mvn install && cd target && ./../run.sh arkadia nsd "133" run &&  ./../run.sh arkadia agof "133" run

scenario=$1
evaluation=$2

scenarioDir="../playpen/fire-scenarios/${scenario}"
scenarioFile="${scenarioDir}/scenario.ini"
memoizationFile="../playpen/executions/${scenario}_farsite_execution_memoization_${evaluation}.txt"
outputDir="../playpen/fire-scenarios/${scenario}/output"

#continues if scenario file exists; leaves otherwise
ls ${scenarioFile} || exit 1;

for seed in $3; do
	scenarioSpec="execution_${evaluation}_seed_${seed}_1"
	outputFile="../playpen/two_stage_prediction_${evaluation}_${seed}.txt"
	cacheDir="../playpen/executions/cached/arkadia/${scenarioSpec}"
	jpgsDir="${cacheDir}/output/jpgs"
	if [ "$4" = "run" ]
	then
		rm ../playpen/fire-scenarios/${scenario}/{input,output}/*.???
		java \
			-jar  two-stage-prediction-0.0.1-SNAPSHOT.jar \
			-f    nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction \
			-c    ${scenarioDir} \
			-m    ${memoizationFile} \
			-t    900 \
			-p      1 \
			-s    ${seed} \
			-e    ${evaluation} \
			-calibrate \
			-predict \
			| tee ${outputFile}
	fi

	ls ${outputDir}/shape_1_1.shp || exit 2;

	mkdir -p               ${cacheDir}/output/
	mv ${outputDir}/*      ${cacheDir}/output/
	mv ${outputFile}       ${cacheDir}/
	mv tsp.log             ${cacheDir}/
	cp ${scenarioFile}     ${cacheDir}/
	sed -i "s#../playpen/fire-scenarios/${scenario}/#${cacheDir}/#" ${memoizationFile}

	mkdir -p                    ${jpgsDir}/bests
	cp ${cacheDir}/output/*.jpg ${jpgsDir}/
	cd                          ${jpgsDir}
	cd ../../
	tail -n 13 two_stage_prediction_*.txt | head -n 10 | grep -v "NaN" | awk '{print $15}' | sed "s#shp#jpg#g" | sed "s#../playpen/fire-scenarios/arkadia/##g" | xargs -n1  cp -t output/jpgs/bests
	cd output/jpgs/bests
	convert -delay 10 -loop 0 shape_*_*.jpg ../generation_12.gif
	cd ~/git/two-stage-prediction/target
	cd                          ${jpgsDir}
	rename 's/\d+/sprintf("%02d",$&)/e' *.jpg
	rename 's/\d+.jpg/sprintf("%03d.jpg",$&)/e' *.jpg > /dev/null 2>&1
	for g in `seq 1 11`; do $(printf "convert -delay 10 -loop 0 shape_%02d_*.jpg generation_%02d.gif\n" $g $g); done
	#convert -delay 10 -loop 0 shape_*_*.jpg ${scenario}.gif
	mkdir generations/
	mv generation_??.gif generations/
	cp ~/git/two-stage-prediction/src/main/resources/*.html generations/
	cp ~/git/two-stage-prediction/src/main/resources/*.css  generations/
	#echo "eog ${jpgsDir}/${scenario}.gif"
	echo "nautilus ${jpgsDir}/generations/"
	cd ~/git/two-stage-prediction/target
done;

exit 0;
