edigley@cariri:~/doutorado_uab/git/two-stage-prediction/target$
/opt/java/jre1.8.0_91/bin/java  -jar two-stage-prediction-0.0.1-SNAPSHOT.jar -f nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction -c ../playpen/fire-scenarios/jonquera/ -m ../playpen/farsite_execution_memoization_NSD.txt -t 7200 -p 1 -s 17 -e nsd | tee ../playpen/two_stage_prediction_NSD_fixed_with_shapefile_name.txt
cat farsite_execution_memoization_NSD.txt | sort -k 11 | head


export JAVA_HOME=/opt/java/jdk1.8.0_51
export PATH=$JAVA_HOME/bin:$PATH

cd /home/edigley/doutorado_uab/git/two-stage-prediction
mvn clean 
mvn install
cd target	



/opt/java/jre1.8.0_91/bin/java

rm ../playpen/fire-scenarios/jonquera/{input,output}/*.???
jonqueraScenario="../playpen/fire-scenarios/jonquera"
scenarioFile="${jonqueraScenario}/scenario.ini"
memoizationFile="../playpen/executions/jonquera_farsite_execution_memoization_agof.txt"
outputDir="../playpen/fire-scenarios/jonquera/output"
for seed in `seq 54 55`; do
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

	
mkdir -p                                                                ../playpen/execution_agof_2020-01-14_seed_99/output/
mv ../playpen/fire-scenarios/jonquera/output/*                          ../playpen/execution_agof_2020-01-14_seed_99/output/
mv ../playpen/two_stage_prediction_agof_2020-01-14.txt                  ../playpen/execution_agof_2020-01-14_seed_99/
mv tsp.log                                                              ../playpen/execution_agof_2020-01-14_seed_99/
mv ../playpen/farsite_execution_memoization_agof_2020-01-14.txt         ../playpen/execution_agof_2020-01-14_seed_99/
cp ../playpen/fire-scenarios/jonquera/scenario.ini                      ../playpen/execution_agof_2020-01-14_seed_99/



21:23:40,150 |-INFO in ch.qos.logback.classic.LoggerContext[default] - Could NOT find resource [logback-test.xml]
21:23:40,150 |-INFO in ch.qos.logback.classic.LoggerContext[default] - Could NOT find resource [logback.groovy]
21:23:40,150 |-INFO in ch.qos.logback.classic.LoggerContext[default] - Found resource [logback.xml] at [file:/home/edigley/doutorado_uab/git/two-stage-prediction/target/classes/logback.xml]
21:23:40,285 |-INFO in ch.qos.logback.classic.joran.action.ConfigurationAction - debug attribute not set
21:23:40,287 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - About to instantiate appender of type [ch.qos.logback.core.ConsoleAppender]
21:23:40,304 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - Naming appender as [CONSOLE]
21:23:40,449 |-WARN in ch.qos.logback.core.ConsoleAppender[CONSOLE] - This appender no longer admits a layout as a sub-component, set an encoder instead.
21:23:40,449 |-WARN in ch.qos.logback.core.ConsoleAppender[CONSOLE] - To ensure compatibility, wrapping your layout in LayoutWrappingEncoder.
21:23:40,449 |-WARN in ch.qos.logback.core.ConsoleAppender[CONSOLE] - See also http://logback.qos.ch/codes.html#layoutInsteadOfEncoder for details
21:23:40,451 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - About to instantiate appender of type [ch.qos.logback.core.FileAppender]
21:23:40,460 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - Naming appender as [FILE]
21:23:40,468 |-WARN in ch.qos.logback.core.FileAppender[FILE] - This appender no longer admits a layout as a sub-component, set an encoder instead.
21:23:40,468 |-WARN in ch.qos.logback.core.FileAppender[FILE] - To ensure compatibility, wrapping your layout in LayoutWrappingEncoder.
21:23:40,468 |-WARN in ch.qos.logback.core.FileAppender[FILE] - See also http://logback.qos.ch/codes.html#layoutInsteadOfEncoder for details
21:23:40,468 |-INFO in ch.qos.logback.core.FileAppender[FILE] - File property is set to [tsp.log]
21:23:40,474 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [com.edigley] to DEBUG
21:23:40,474 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting additivity of logger [com.edigley] to false
21:23:40,474 |-INFO in ch.qos.logback.core.joran.action.AppenderRefAction - Attaching appender named [FILE] to Logger[com.edigley]
21:23:40,475 |-INFO in ch.qos.logback.classic.joran.action.RootLoggerAction - Setting level of ROOT logger to ERROR
21:23:40,475 |-INFO in ch.qos.logback.core.joran.action.AppenderRefAction - Attaching appender named [CONSOLE] to Logger[ROOT]
21:23:40,476 |-INFO in ch.qos.logback.classic.joran.action.ConfigurationAction - End of configuration.
21:23:40,477 |-INFO in ch.qos.logback.classic.joran.JoranConfigurator@26ba2a48 - Registering current configuration as safe fallback point

48360.0
33750.0
2020-01-03T21:23:43.277+0100  SEVERE  Could not find 'AREAd' in the FeatureType (jonquera_polygon_from_layer_extent), available attributes are: 

[the_geom, MINX, MINY, MAXX, MAXY, CNTX, CNTY, AREA, PERIM, HEIGHT, WIDTH]

org.geotools.filter.IllegalFilterException: Could not find 'AREAd' in the FeatureType (jonquera_polygon_from_layer_extent), available attributes are: [the_geom, MINX, MINY, MAXX, MAXY, CNTX, CNTY, AREA, PERIM, HEIGHT, WIDTH]
	at org.geotools.renderer.lite.StreamingRenderer.checkAttributeExistence(StreamingRenderer.java:2198)
	at org.geotools.renderer.lite.StreamingRenderer.getFeatures(StreamingRenderer.java:2106)
	at org.geotools.renderer.lite.StreamingRenderer.processStylers(StreamingRenderer.java:1986)
	at org.geotools.renderer.lite.StreamingRenderer.paint(StreamingRenderer.java:857)
	at org.geotools.renderer.lite.StreamingRenderer.paint(StreamingRenderer.java:590)
	at com.edigley.tsp.input.ShapeFileUtil.saveAsJPG(ShapeFileUtil.java:530)
	at com.edigley.tsp.input.ShapeFileUtil.saveFarsiteResultAsJPG(ShapeFileUtil.java:494)
	at com.edigley.tsp.App.main(App.java:58)


/opt/java/jre1.8.0_91/bin/java  -jar two-stage-prediction-0.0.1-SNAPSHOT.jar -f nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction -c ../playpen/fire-scenarios/jonquera/ -m ../playpen/farsite_execution_memoization_7200_seconds_with_fire_error_adjustment_factor_and_sensible_to_layer_extent.txt -t 7200 -p 1 -s 17 -e gof | tee ../playpen/two_stage_prediction_gof.txt
/opt/java/jre1.8.0_91/bin/java  -jar two-stage-prediction-0.0.1-SNAPSHOT.jar -f nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction -c ../playpen/fire-scenarios/jonquera/ -m ../playpen/farsite_execution_memoization_gof.txt -t 7200 -p 1 -s 17 -e gof | tee ../playpen/two_stage_prediction_gof.txt





reset && tail -n 14 execution_agof_2020-01-14_seed_*/two_stage_prediction_*.txt

edigley@cariri:~/doutorado_uab/git/two-stage-prediction/playpen$ reset && tail -n 14 execution_agof_2020-01-14_seed_*/two_stage_prediction_*.txt | grep Best



cat execution_agof_2020-01-14_seed_99/two_stage_prediction_agof_2020-01-14.txt | grep "pool-" | sort -g -k 22
cat execution_agof_2020-01-14_seed_99/two_stage_prediction_agof_2020-01-14.txt | grep "pool-" | sort -g -k 22

cd ~/doutorado_uab/git/two-stage-prediction/
for i in `seq 21 30`; do ./run_tsp.sh $i; done

# copy the best results to a proper best results dir
cd ~/doutorado_uab/git/two-stage-prediction/playpen
for seed in `seq 21 30`; do
	scenarioDir=execution_agof_2020-01-14_seed_${seed}
	scenarioOutputFile=${scenarioDir}/two_stage_prediction_agof_2020-01-14.txt
	scenarioResultsDir=${scenarioDir}/output
	bestResultsDir=${scenarioResultsDir}/best_results
	ls ${scenarioDir}
	ls ${scenarioOutputFile}
	ls ${scenarioResultsDir} | tail -n1
	ls ${bestResultsDir}
	mkdir ${bestResultsDir}
	seed=21; cat ${scenarioOutputFile} | grep "Best R" | awk '{print $18}' | sed "s#shp#shp#g" | sed "s#^#${scenarioResultsDir}/#g" | xargs -n1  cp -t ${bestResultsDir}
	seed=21; cat ${scenarioOutputFile} | grep "Best R" | awk '{print $18}' | sed "s#shp#shx#g" | sed "s#^#${scenarioResultsDir}/#g" | xargs -n1  cp -t ${bestResultsDir}
	seed=21; cat ${scenarioOutputFile} | grep "Best R" | awk '{print $18}' | sed "s#shp#dbf#g" | sed "s#^#${scenarioResultsDir}/#g" | xargs -n1  cp -t ${bestResultsDir}
	echo ${bestResultsDir}
	ls   ${bestResultsDir}
done;

ls execution_agof_2020-01-14_seed_*/output/best_results/*

# Join all the output files in a single file to serve as input to tsp graph overview
# https://raw.githubusercontent.com/edigley/spif/master/results/two_stage_prediction.txt

edigley@cariri:~/doutorado_uab/git/two-stage-prediction/target$ history | grep header.txt
 1801  #echo "calibrationExecNumber generationExecNumber generation individual " > header.txt
 1805  echo "calibrationExecNumber generationExecNumber generation individual t1 t10 t100 t1000 t10000 ws wd th hh adj fireError maxSimulatedTime parallelizationLevel executionTime targetThread" > header.txt
 1806  cat header.txt 
 1823  pico header.txt 
 1824  cat header.txt > two_stage_prediction.txt && for i in `seq 16 35`; do file=two_stage_prediction_seed_${i}.txt ; sed "s/.*/${file}  ${i} &/" ${file} >> two_stage_prediction.txt; done
 2005  history | grep header.txt
edigley@cariri:~/doutorado_uab/git/two-stage-prediction/target$ 

cd /home/edigley/Área de Trabalho/jonquera_adaptive_evaluation/jonquera_10_generations_adaptive_killing
cat header.txt 
cat header.txt > two_stage_prediction.txt && for i in `seq 16 35`; do file=two_stage_prediction_seed_${i}.txt ; sed "s/.*/${file}  ${i} &/" ${file} >> two_stage_prediction.txt; done

resultsDir="agof"
header="/home/edigley/desktop/jonquera_adaptive_evaluation/jonquera_10_generations_adaptive_killing/header.txt"
cd ~/doutorado_uab/git/two-stage-prediction/playpen/
mkdir -p ${resultsDir} 
cp  ${header} ${resultsDir}/
cat ${header} | sed "s/targetThread/shapeFile targetThread/g" > ${resultsDir}/two_stage_prediction.txt

for seed in `seq 21 30`; do
	scenarioDir=execution_agof_2020-01-14_seed_${seed}
	scenarioOutputFile=${scenarioDir}/two_stage_prediction_agof_2020-01-14.txt
	cat ${scenarioOutputFile} | grep " \[ " | sed "s/- CACHED//g" | sed "s/==>//g" | sed "s/->//g" | sed "s/ -//g" | sed "s/\[//g" | sed "s/\]//g" | sed "s/.shp/.shp\"/g" | sed "s#shape_#   \"${scenarioDir}/shape_#g" > ${resultsDir}/two_stage_prediction_seed_${seed}.txt 
done;

cd ${resultsDir}

for i in `seq 21 30`; do file=two_stage_prediction_seed_${i}.txt ; sed "s/.*/${file}  ${i} &/" ${file} >> two_stage_prediction.txt; done

head two_stage_prediction.txt





https://github.com/edigley/spif/blob/master/results/ajustar_dataset_e_plotar_tstp_overview.R
https://github.com/edigley/spif/blob/master/results/plot_calibration_overview.R
resultsTSPAll <- read.table('https://raw.githubusercontent.com/edigley/spif/master/results/two_stage_prediction.txt', header=T)
resultsTSPAgof <- read.table('https://raw.githubusercontent.com/edigley/spif/master/results/two_stage_prediction_agof.txt', header=T, row.names=NULL)






ds21 <- subset(resultsTSPAgof, seed==21)
#summary(ds21)
fitness <- subset(ds21, select=c("generation", "individual", "fireError"))
#summary(fitness)
fitness %>% 
    group_by(generation) %>%
    summarise(nElements=n()) %>%
    mutate(cumulated=cumsum(nElements)+1)
dsMeanFitnessPerGeneration <- fitness %>% 
    group_by(generation) %>%
    summarise(mean=mean(fireError))
head(dsMeanFitnessPerGeneration)
plot(dsMeanFitnessPerGeneration$generation, dsMeanFitnessPerGeneration$mean, type='l')

source("https://github.com/edigley/spif/raw/master/results/dependencies.R")
print(source('https://raw.githubusercontent.com/edigley/spif/master/results/ajustar_dataset_e_plotar_tstp_overview.R'))
print(source("https://raw.githubusercontent.com/edigley/spif/master/results/plot_line_plot_grouped_by_scenario_average_max_cummulated_fitness_per_generation_nsd.R"))





cat farsite_execution_memoization_TheAdjustedGoF3.log | grep "Individual finished" | grep -v CACHED | tr -s [:blank:] | cut -d' ' -f 8-26 | awk '{print $0" shape_"$2"_"$3".shp"}' | cut -d' ' -f 5-18,20 | while read line; do printf "%3s %3s %3s %3s %3s %4s %4s %3s %3s  %.1f  %.6f  %6s %6s %6s %s\n" $line; done | sed "s#shape_#execution_agof_seed_21_1/output/shape_#g" > farsite_execution_memoization_seed_21_1.txt 




Add a prefix in all files
rename 's/^(prefix)?/_bla/g' *
rename 's/(.*)$/theSufix.$1/' *

Add a sufix
rename 's/(.*)$/${1}theSufix/' *
rename 's/(.*)$/${1}_1/' *

edigley@cariri:~/git/two-stage-prediction/playpen$ find . -maxdepth 1 -type d -name "execution*seed_??_*" -exec echo mv {} {}_1 \;
mv ./execution_agof_seed_32_1 ./execution_agof_seed_32_1_1
mv ./execution_agof_seed_26_1 ./execution_agof_seed_26_1_1
mv ./execution_agof_seed_27_1 ./execution_agof_seed_27_1_1
mv ./execution_agof_seed_23_1 ./execution_agof_seed_23_1_1
mv ./execution_agof_seed_24_1 ./execution_agof_seed_24_1_1
mv ./execution_agof_seed_30_1 ./execution_agof_seed_30_1_1
mv ./execution_agof_seed_31_1 ./execution_agof_seed_31_1_1
mv ./execution_agof_seed_29_1 ./execution_agof_seed_29_1_1
mv ./execution_agof_seed_22_1 ./execution_agof_seed_22_1_1
mv ./execution_agof_seed_21_1 ./execution_agof_seed_21_1_1
mv ./execution_agof_seed_21_2 ./execution_agof_seed_21_2_1
mv ./execution_agof_seed_42_1 ./execution_agof_seed_42_1_1
mv ./execution_agof_seed_28_1 ./execution_agof_seed_28_1_1
mv ./execution_agof_seed_99_1 ./execution_agof_seed_99_1_1
mv ./execution_agof_seed_41_1 ./execution_agof_seed_41_1_1
mv ./execution_agof_seed_25_1 ./execution_agof_seed_25_1_1
edigley@cariri:~/git/two-stage-prediction/playpen$ 


export LANG=en_US.UTF-8

# When we need to extract the memoization from the output file
cat execution_agof_seed_??_?/farsite_execution_memoization_*.txt | grep "Individual finished" | grep -v CACHED | tr -s [:blank:] | cut -d' ' -f 8-26 | awk '{print $0" shape_"$2"_"$3".shp"}' | cut -d' ' -f 5-18,20 | while read line; do printf "%3s %3s %3s %3s %3s %4s %4s %3s %3s  %.1f  %.6f  %6s %6s %6s %s\n" $line; done | sed "s#shape_#execution_agof_seed_21_1/output/shape_#g" > farsite_execution_memoization_agof_all_seeds.txt 

# When we need to join the memoization files
cat execution_agof_seed_??_?/farsite_execution_memoization_*.txt | grep "Individual finished" | grep -v CACHED | tr -s [:blank:] | cut -d' ' -f 8-26 | awk '{print $0" shape_"$2"_"$3".shp"}' | cut -d' ' -f 5-18,20 | while read line; do printf "%3s %3s %3s %3s %3s %4s %4s %3s %3s  %.1f  %.6f  %6s %6s %6s %s\n" $line; done | sed "s#shape_#execution_agof_seed_21_1/output/shape_#g" > farsite_execution_memoization_agof_all_seeds.txt 

dir="execution_agof_seed_21_1"
cat ${dir}/farsite_execution_memoization_TheAdjustedGoF3.log | grep "Individual finished" | grep -v CACHED | tr -s [:blank:] | cut -d' ' -f 8-26 | awk '{print $0" shape_"$2"_"$3".shp"}' | cut -d' ' -f 5-18,20 | while read line; do printf "%3s %3s %3s %3s %3s %4s %4s %3s %3s  %.1f  %.6f  %6s %6s %6s %s\n" $line; done | sed "s#shape_#${dir}/output/shape_#g" > execution_memoization_all_seed/farsite_execution_memoization_agof_${dir}.txt 

dir="execution_agof_seed_21_1"
cat farsite_execution_memoization_TheAdjustedGoF_fixed_with_shapefile_name.txt | sed "s#shape_#${dir}/output/shape_#g" > farsite_memoization_${dir}.txt
dir="execution_agof_seed_21_3"
cat farsite_execution_memoization_agof_2020-01-14.txt | sed "s#shape_#${dir}/output/shape_#g" > farsite_memoization_${dir}.txt
dir="execution_agof_seed_99_1"
cd ../${dir}
cat farsite_execution_memoization_agof_2020-01-14.txt | sed "s#shape_#${dir}/output/shape_#g" > farsite_memoization_${dir}.txt
cat farsite_memoization_${dir}.txt

seed=48
orig="execution_agof_2020-01-21_seed_${seed}"
dir="execution_agof_seed_${seed}_1"
origMemoizationFile="farsite_execution_memoization_agof_2020-01-21_${seed}.txt"
memoizationFile="farsite_memoization_${dir}.txt"
cd ~/git/two-stage-prediction/playpen
mv ${orig} ${dir}
cd ${dir}
head ${origMemoizationFile}
cat ${origMemoizationFile} | sed "s#shape_#${dir}/output/shape_#g" > ${memoizationFile}
head ${memoizationFile}

seed=49
dir="execution_agof_seed_${seed}_1"
memoizationFile="farsite_memoization_${dir}.txt"
cp ${dir}/${memoizationFile} execution_memoization_all_seeds/

cd ~/git/two-stage-prediction/playpen/execution_memoization_all_seeds/
cat farsite_memoization_execution_agof_seed_??_?.txt | sort -k 11 | head

memoizationFile="farsite_execution_memoization_agof.txt"
head -n 1 memoization_header.txt | sed "s/executionTime/executionTime predictionFile/" > ${memoizationFile}
execution_agof_seed_32_1
execution_agof_seed_26_1
execution_agof_seed_27_1

#-----------------------------------------------------------------
Passos para quando se quer recalcular fireError
#-----------------------------------------------------------------
seed=51_1
dir="execution_agof_seed_${seed}"
origMemoizationFile="farsite_memoization_execution_agof_seed_${seed}.txt"
memoizationFile="${dir}.txt"
head -n 1 memoization_header.txt > ${memoizationFile}
cat ${origMemoizationFile} | grep -v "t1" | sort -u | sort -k 11 | sed "s#execution_#playpen/execution_#" >> ${memoizationFile}

head ${memoizationFile}
tail ${memoizationFile}

# execução no eclipse e depois executa os próximos comandos

wc -l farsite_execution_memoization_agof.txt
cat ${memoizationFile} | grep -v "playpen" | sed "s#shape_#playpen/${dir}/output/shape_#g" >> farsite_execution_memoization_agof.txt
wc -l farsite_execution_memoization_agof.txt
#-----------------------------------------------------------------

wc -l farsite_execution_memoization_agof.txt
cat ${memoizationFile} | grep -v "t1" | sed "s#shape_#playpen/${dir}/output/shape_#g" >> farsite_execution_memoization_agof.txt
wc -l farsite_execution_memoization_agof.txt
cat ${memoizationFile} | grep -v "t1" | sed "s#execution_#playpen/execution_#"        >> farsite_execution_memoization_agof.txt


print duplicates with counts
sort FILE | uniq -cd
sort FILE | uniq --count --repeated
if you want to print counts for all lines including those that appear only once
sort FILE | uniq -c
In order to sort the output with the most frequent lines on top, you can do the following (to get all results):
sort FILE| uniq -c | sort -nr





cat tsp.log | grep "Individual finished" | grep -v "CACHED" | grep "shape_" | awk '{print $26}' | sort | cut -d'_' -f 3 | sed 's/.shp//' | sort -g | wc -l
ls output/*.shp | wc -l

scenario="execution_agof_seed_97_1"
cd /home/edigley/git/two-stage-prediction/playpen/executions/cached/arkadia/${scenario}/output/
mkdir jpgs
cd    jpgs
cp ../*.jpg .
rename 's/\d+/sprintf("%02d",$&)/e' *.jpg
convert -delay 10 -loop 0 shape_*_*.jpg ${scenario}.gif
eog ${scenario}.gif

# https://seg.bb.com.br/passo-a-passo.html

edigley@cariri:~/doutorado_uab/git/two-stage-prediction/playpen/executions/cached/arkadia/execution_nsd_seed_95_1$ tail -n 15 two_stage_prediction_nsd_95.txt  | grep shape_ | awk '{print $15}' | sed "s#../playpen/fire-scenarios/arkadia/##g" | sed 's/_1_/_01_/g' | sed 's/_2_/_02_/g' | sed 's/_3_/_03_/g' | sed 's/_4_/_04_/g' | sed 's/_5_/_05_/g' | sed 's/_6_/_06_/g' | sed 's/_7_/_07_/g' |sed 's/_8_/_08_/g' | sed 's/_9_/_09_/g' | sed 's/.shp/.jpg/g' | sed 's#output/#output/jpgs/#g' | xargs -n1 cp -t bests/
edigley@cariri:~/doutorado_uab/git/two-stage-prediction/playpen/executions/cached/arkadia/execution_nsd_seed_95_1$ ls bests/

