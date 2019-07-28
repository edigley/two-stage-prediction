# Two-Stage Prediction Framework

~~~~
git clone https://github.com/edigley/two-stage-prediction.git
cd two-stage-prediction/
mvn install
ls target/two-stage-prediction-0.0.1-SNAPSHOT.jar target/nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction

cd ../
FARSITE_DIR=/path/to/farsite/
git clone https://github.com/edigley/fire-scenarios.git
cd fire-scenarios
sed -i "s#<farsite_path>#${FARSITE_DIR}#g" jonquera/scenario_jonquera.ini

cd ../two-stage-prediction/target
java -jar two-stage-prediction-0.0.1-SNAPSHOT.jar -f nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction -s ../../fire-scenarios/jonquera/
~~~~
