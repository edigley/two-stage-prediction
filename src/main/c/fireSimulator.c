#include <stdio.h>
#include <time.h>
#include "population.h"
#include <stdlib.h>
#include "iniparser.h"
#include <sys/types.h>
#include "fitness.h"
#include "myutils.h"
#include "farsite.h"

char * baseWndFile, * baseWtrFile, * baseFmsFile, * baseAdjFile;
char * adjFile, * fmsFile, * wndFile, * wtrFile;

char * FuelsToCalibrateFileName;

int CalibrateAdjustments;

int doMeteoSim;

float TEMP_VARIATION, HUM_VARIATION;

int FuelsUs[256];

const char * getCurrentTime() {
    time_t t = time(NULL);
    struct tm tm = *localtime(&t);
    char * currentTime = malloc(20);
    sprintf(currentTime, "%04d-%02d-%02d %02d:%02d:%02d\0", tm.tm_year + 1900, tm.tm_mon + 1, tm.tm_mday, tm.tm_hour, tm.tm_min, tm.tm_sec);
    return currentTime;
}

void readConfiguration(char * configurationFile);
void createFarsiteInputFiles(INDVTYPE_FARSITE individual, int generation);

void readConfiguration(char * configurationFile) {
    dictionary * configuration = iniparser_load(configurationFile);

    FuelsToCalibrateFileName = iniparser_getstr(configuration, "main:FuelsToCalibrate");

    adjFile          = iniparser_getstr(configuration, "farsite:adjFile");
    fmsFile          = iniparser_getstr(configuration, "farsite:fmsFile");
    wndFile          = iniparser_getstr(configuration, "farsite:wndFile");
    wtrFile          = iniparser_getstr(configuration, "farsite:wtrFile");

    baseAdjFile	     = iniparser_getstr(configuration, "farsite:baseAdjFile");
    baseFmsFile      = iniparser_getstr(configuration, "farsite:baseFmsFile");
    baseWndFile      = iniparser_getstr(configuration, "farsite:baseWndFile");
    baseWtrFile      = iniparser_getstr(configuration, "farsite:baseWtrFile");

    TEMP_VARIATION   = iniparser_getdouble(configuration, "farsite:TEMP_VARIATION",1.0);
    HUM_VARIATION    = iniparser_getdouble(configuration, "farsite:HUM_VARIATION",1.0);

    FILE *FuelsToCalibrateFILE;
    int i, nFuel;

    for (i=0; i<256; i++) {
        FuelsUs[i]=0;
    }
    if ((FuelsToCalibrateFILE = fopen(FuelsToCalibrateFileName, "r"))==NULL) {
        printf("ERROR: FireSimulator.initFarsiteVariables -> Opening fuels used file.\n");
    } else {
        while(fscanf(FuelsToCalibrateFILE,"%d",&nFuel)!=EOF) {
            FuelsUs[nFuel-1]=1;
        }
    }

}

void createWeatherFile(char *baseWtrFile, char * wtrFileNew, INDVTYPE_FARSITE individual, int temperatureVariation, int humidityVariation) {
    // create weather file for the individual
    FILE *fWTR, *fWTRnew;

    if ( ((fWTR = fopen(baseWtrFile, "r"))   == NULL) ) {
        printf("ERROR: FireSimulator.createWeatherFile -> Unable to open WTR file. \n");
        return;
    }
    if ( ((fWTRnew = fopen(wtrFileNew, "w")) == NULL) ) {
        printf("ERROR: FireSimulator.createWeatherFile -> Unable to create WTR temp file. \n");
        return;
    }

    char * line = (char*)malloc(sizeof(char) * 200);
    char * newline = (char*)malloc(sizeof(char) * 200);
    char * buffer = (char*)malloc(sizeof(char) * 200);

    fgets( line, 100, fWTR );
    fprintf(fWTRnew, "%s", line);
    float tl = individual.parameters[7] - temperatureVariation;
    float hl = individual.parameters[8] - humidityVariation;

    while(fgets( line, 100, fWTR ) != NULL) {
        sprintf(buffer, "%1.0f", tl);
        newline = str_replace(line, "tl", buffer);

        sprintf(buffer, "%1.0f", individual.parameters[7]);
        newline = str_replace(newline, "th", buffer);

        sprintf(buffer, "%1.0f", individual.parameters[8]);
        newline = str_replace(newline, "hh", buffer);

        sprintf(buffer,"%1.0f", hl);
        newline = str_replace(newline, "hl", buffer);

        fprintf(fWTRnew, "%s", newline);
    }

    fclose(fWTR);
    fclose(fWTRnew);
}

void createWindFile(char *baseWndFile, char *wndFileNew, INDVTYPE_FARSITE individual) {
    // create wind file for the individual

    FILE *fWND, *fWNDnew;

    if ( ((fWND = fopen(baseWndFile, "r"))   == NULL) ) {
        printf("ERROR: FireSimulator.createWindFile -> Unable to open WND file. \n");
        return;
    }
    if ( ((fWNDnew = fopen(wndFileNew, "w")) == NULL) ) {
        printf("ERROR: FireSimulator.createWindFile -> Unable to create WND temp file. \n");
        return;
    }

    char * line = (char*)malloc(sizeof(char) * 200);
    char * newline = (char*)malloc(sizeof(char) * 200);
    char * buffer = (char*)malloc(sizeof(char) * 200);

    fgets( line, 100, fWND );
    fprintf(fWNDnew, "%s", line);
    while(fgets( line, 100, fWND ) != NULL) {

        sprintf(buffer, "%1.0f", individual.parameters[5]);
        newline = str_replace(line, "ws", buffer);

        sprintf(buffer, "%1.0f", individual.parameters[6]);
        newline = str_replace(newline, "wd", buffer);

        sprintf(buffer, "%d", 0);
        newline = str_replace(newline, "wc", buffer);

        fprintf(fWNDnew, "%s", newline);
    }

    fclose(fWND);
    fclose(fWNDnew);
}

void createFuelMoistureFile(char *baseFmsFile, char *fmsFileNew, INDVTYPE_FARSITE individual) {

    FILE * fFMS, *fFMSnew;

    if ( ((fFMS = fopen(baseFmsFile, "r"))   == NULL) ) {
        printf("ERROR: FireSimulator.createFuelMoistureFile -> Unable to open FMS file. \n");
        return;
    }
    if ( ((fFMSnew = fopen(fmsFileNew, "w")) == NULL) ) {
        printf("ERROR: FireSimulator.createFuelMoistureFile -> Unable to create FMS temp file. \n");
        return;
    }

    char * line = (char*)malloc(sizeof(char) * 200);
    char * newline = (char*)malloc(sizeof(char) * 200);
    char * buffer = (char*)malloc(sizeof(char) * 200);

        // create fuel moisture file for the individual
    while( fgets( line, 100, fFMS ) != NULL ) {

        sprintf(buffer, "%1.0f", individual.parameters[0]);
        newline = str_replace(line, "1h", buffer);

        sprintf(buffer, "%1.0f", individual.parameters[1]);
        newline = str_replace(newline, "10h", buffer);

        sprintf(buffer, "%1.0f", individual.parameters[2]);
        newline = str_replace(newline, "100h", buffer);

        sprintf(buffer, "%1.0f", individual.parameters[3]);
        newline = str_replace(newline, "herb", buffer);

        fprintf(fFMSnew,"%s", newline);
    }

    fclose(fFMS);
    fclose(fFMSnew);
}

void createAdjustmentFile(char *baseAdjFile, char *adjFileNew, INDVTYPE_FARSITE individual, int *FuelsUs) {
    FILE *fADJ, *fADJnew;

    if ( ((fADJ    = fopen(baseAdjFile, "r")) == NULL) ) {
        printf("ERROR: FireSimulator.createAdjustmentFile -> Unable to open ADJ file. \n");
        return;
    }
    if ( ((fADJnew =  fopen(adjFileNew, "w")) == NULL) ) {
        printf("ERROR: FireSimulator.createAdjustmentFile -> Unable to create ADJ temp file. \n");
        return;
    }

    // create fuel moisture file for the individual
    int nfuel, param = 9;
    float adjust = 0.0f;
    while(fscanf(fADJ, "%d %f", &nfuel, &adjust) != EOF ) {
        if (FuelsUs[nfuel-1]) {
            fprintf(fADJnew, "%d %1.6f\n", nfuel, individual.parameters[param]);
            param++;
        } else {
            fprintf(fADJnew, "%d 1.000000\n", nfuel);
        }
    }

    fclose(fADJnew);
    fclose(fADJ);
}

void genFarsiteInputFiles(char * configurationFile, INDVTYPE_FARSITE individual) {
    printf("INFO: FireSimulator.genFarsiteInputFiles -> Gonna generate all farsite input files.\n"); 
    initFarsiteVariables(configurationFile, individual.generation);
    createFarsiteInputFiles(individual, individual.generation);
    printf("INFO: FireSimulator.genFarsiteInputFiles -> All farsite input files generated into \"input\" folder.\n"); 
}

void runIndividual(char * configurationFile, INDVTYPE_FARSITE individual, int timeout) {
    char individualAsString[256]; // 256 bytes allocated here on the stack.
    double adjustmentError;
    individual.threads=1;
    char * atmPath;
    individualToString(individual.generation, individual, individualAsString, sizeof(individualAsString));
    printf("%s - INFO: FireSimulator.runIndividual -> Gonna run farsite for individual:\n", getCurrentTime());
    printf("%s - INFO: FireSimulator.runIndividual -> %s\n", getCurrentTime(), "gen ind 1h 10h 100h herb 1000h ws wd th hh adj...");
    printf("%s - INFO: FireSimulator.runIndividual -> %s\n", getCurrentTime(), individualAsString);
    runFarsite(individual, &adjustmentError, configurationFile, timeout);//86400);//61);//300);//3600);
    printf("%s - INFO: FireSimulator.runIndividual -> Finished for individual (%d,%d).\n", getCurrentTime(), individual.generation, individual.id);
    printf("%s - INFO: FireSimulator.runIndividual -> adjustmentError: (%d,%d): %f\n", getCurrentTime(), individual.generation, individual.id, adjustmentError);
    printf("%s - INFO: FireSimulator.runIndividual -> &adjustmentError: (%d,%d): %f\n", getCurrentTime(), individual.generation, individual.id, &adjustmentError);
}

/**
 * Create input files to be used in farsite simulation (fms, adj, wnd, wtr).
 * - individual
 * - configurationFile
 */
void createFarsiteInputFiles(INDVTYPE_FARSITE individual, int generation) {
    printf("INFO: FireSimulator.createFarsiteInputFiles -> Going to create input files for individual (%d,%d) \n", generation, individual.id);

    char * adjFileNew, * fmsFileNew, * wndFileNew, * wtrFileNew;

    fmsFileNew = (char*)malloc(sizeof(char) * 200);
    adjFileNew = (char*)malloc(sizeof(char) * 200);
    wndFileNew = (char*)malloc(sizeof(char) * 200);
    wtrFileNew = (char*)malloc(sizeof(char) * 200);

    char * generationAsString = (char*)malloc(sizeof(char) * 400);
    sprintf(generationAsString, "%d", generation);
    char * individualIdAsString = (char*)malloc(sizeof(char) * 400);
    sprintf(individualIdAsString, "%d", individual.id);

    // Create corresponding fms, wnd & wtr filename for each individual

    adjFileNew = str_replace(adjFile, "$1", generationAsString);
    adjFileNew = str_replace(adjFileNew, "$2", individualIdAsString);
    createAdjustmentFile(baseAdjFile, adjFileNew, individual, FuelsUs);

    fmsFileNew = str_replace(fmsFile, "$1", generationAsString);
    fmsFileNew = str_replace(fmsFileNew, "$2", individualIdAsString);
    createFuelMoistureFile(baseFmsFile, fmsFileNew, individual);

    wndFileNew = str_replace(wndFile, "$1", generationAsString);
    wndFileNew = str_replace(wndFileNew, "$2", individualIdAsString);
    createWindFile(baseWndFile, wndFileNew, individual);

    wtrFileNew = str_replace(wtrFile, "$1", generationAsString);
    wtrFileNew = str_replace(wtrFileNew, "$2", individualIdAsString);
    createWeatherFile(baseWtrFile, wtrFileNew, individual, TEMP_VARIATION, HUM_VARIATION);

}

/**
 * - argv[1] file path: spif configuration file
 * - argv[2] file path: population file
 * - argv[3] string [ run | generateFarsiteInputFiles ]: "run" if should run farsite or "generateFarsiteInputFiles" if should only generate farsite input files
 * - argv[4] int [optional]: identifier of the individual to be simulated. It should range between 0 to (population_size - 1).
 */
int main(int argc, char *argv[]) {

    if (argc == 1 ) { //binary valid
    	return 0;
    }

    if (argc < 3 ) { //error: number of args invalid
        printf("%s - ERROR: FireSimulator.main -> number of args invalid. Please inform at least a configuration and a population files.", getCurrentTime());
        printf("You can optionally inform the individual to be executed.\n");
        return 1;
    }

    POPULATIONTYPE population;
    if  (argc <= 5) { //must read population file
        readPopulation(&population, argv[2]);
        //print_population_farsite(population);
        //print_individuo(0, population.popu_fs[0]);
    }

    double adjustmentError;
    char * configurationFile = argv[1];
    char * atmPath;
    int generation = 0;
    int individuoId;
    int timeout;
    INDVTYPE_FARSITE * individual;
    int begin, end;
    char individualAsString[256]; // 256 bytes allocated here on the stack.
    char * adjustmentErrorsFileName = "output_individuals_adjustment_result.txt";
    FILE * adjustmentErrors;

    if  (argc > 5) { //specify the individual directly 
        printf("%s - INFO: FireSimulator.main -> Gonna run farsite for individual specified directly: %s\n", getCurrentTime(), argv[2]);
        printf("%s - INFO: FireSimulator.main -> argc: %d\n", getCurrentTime(), argc);
        individual = (INDVTYPE_FARSITE *)malloc(sizeof(INDVTYPE_FARSITE));
        individual->generation = atoi(argv[3]);
        individual->id = atoi(argv[4]);
        printf("%s - INFO: FireSimulator.main -> Gonna read all the individual params...\n", getCurrentTime());
        int i;
        //for the main parameters
        for (i=0; i < 9; i++) {
            individual->parameters[i] = atof(argv[i+5]);
        }
        //for the adjustment factors
        for (i=9; i < 21; i++) {
            individual->parameters[i] = (i+5 < argc) ? atof(argv[i+5]) : 1.0;
        }
        timeout = atoi(argv[15]);
        printf("%s - INFO: FireSimulator.main -> timeout: %d\n", getCurrentTime(), timeout); 
        printf("%s - INFO: FireSimulator.main -> Gonna call runIndividual funtion...\n", getCurrentTime());
        runIndividual(configurationFile, *individual, timeout);
    } else if (argc == 5) { //run only the population specified individual
        individuoId = atoi(argv[4]);
        begin = individuoId;
        end = individuoId + 1;
    } else if (argc == 4) { //run all individuals in population
        begin = 0;
        end = population.popuSize;
    } else { //error
        printf("%s - ERROR: FireSimulator.main -> Provide the right arguments to the program \n", getCurrentTime());
        printf(" * - argv[1] file path: spif configuration file \n");
        printf(" * - argv[2] file path: population file \n");
        printf(" * - argv[3] string [ run | gen ]: \"run\" if should run farsite or \"gen\" if should only generate farsite input files \n");
        printf(" * - argv[4] int [optional]: identifier of the individual to be simulated. It should range between 0 to (population_size - 1) \n");
        // ~/doutorado_uab/git/spif/fireSimulator scenario_case_1_central_point.ini run 0 0 2 2 3 48 96 45 141 30 76 0.061901
        // ~/doutorado_uab/git/spif/fireSimulator scenario_case_1_central_point.ini farsite_individuals_1000_formatted.txt run 0
        // cd ~/doutorado_uab/git/spif && make fire && scenario=jonquera && cd playpen/${scenario} && ~/doutorado_uab/git/spif/fireSimulator scenario_${scenario}.ini run 0 99 2 2 3 48 96 45 141 30 76 0.06
        return;
    }

    if ((adjustmentErrors = fopen(adjustmentErrorsFileName, "w")) == NULL) { 
        printf("%s - ERROR: FireSimulator.main -> Error opening output adjustment errors file 'w' %s.\n", getCurrentTime(), adjustmentErrorsFileName);
        return;
    } else {
        int i;
        for (i=begin; i < end; i++) {

            printf("%s - INFO: FireSimulator.main -> Going to start for individual (%d,%d)...\n", getCurrentTime(), generation, i);
            individualToString(generation, population.popu_fs[i], individualAsString, sizeof(individualAsString));
            printf("%s - INFO: FireSimulator.main -> %s\n", getCurrentTime(), individualAsString); 

            if (strcmp(argv[3], "gen") == 0) { // only generate farsite input files
                genFarsiteInputFiles(configurationFile, population.popu_fs[i]);
            } else if (strcmp(argv[3], "run") == 0) { // run farsite generating input files
                runIndividual(configurationFile, population.popu_fs[i], timeout);
            } else {
                printf("%s - ERROR: FireSimulator.main -> %s is not a valid action. Please specify what to do: [ run | gen ] \n", getCurrentTime(), argv[3]);
            }
        }
        fclose(adjustmentErrors);
    }

}
