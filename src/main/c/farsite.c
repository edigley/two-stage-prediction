#include <stdio.h>
#include "population.h"
#include <stdlib.h>
#include "iniparser.h"
#include <sys/types.h>
#include "fitness.h"
#include "myutils.h"

int FireSimLimit, numGenerations;
char * farsite_path, *windninja_path, *input_path, *output_path, * real_fire_map_t0, * real_fire_map_t1,*FuelsToCalibrateFileName,*CustomFuelFile,*real_fire_map_tINI;
char * landscapeFile, *ignitionFile, *ignitionFileType, *wndFile, *wtrFile, *adjFile, *fmsFile;
char * baseWndFile, *baseWtrFile, *baseFmsFile, *baseAdjFile;
char * RasterFileName, *shapefile, *VectorFileName, *doRaster, *doShape, *doVector;
char * ConditMonth, *ConditDay, *StartMonth, *StartDay, *StartHour, *StartMin, *EndMonth, *EndDay, *EndHour, *EndMin;
char * timestep, *visibleStep, *secondaryVisibleStep, *perimeterResolution,*distanceResolution;
char * fmsFileNew, * wndFileNew, * wtrFileNew, *adjFileNew, *atmPath;
char * RasterFileNameNew, FuelsUsedFileName;

int numgen=1, num_threads;
int start_time, end_time;
int doWindFields;
int doMeteoSim;
int CalibrateAdjustments;
int seguir = 1;
float TEMP_VARIATION, HUM_VARIATION;
int FuelsUs[256];
int TotalFuels;

int runFarsite(INDVTYPE_FARSITE individual, double * predictionError, char * configurationFile, double timeout);
void initFarsiteVariables(char * filename, int numgen);
void createInputFiles(INDVTYPE_FARSITE individual);
void createSettingsFile(char * filename, int idInd, int gen, int res);
double getSimulationError(char * simulatedMap);

int runFarsite(INDVTYPE_FARSITE individual, double * predictionError, char * configurationFile, double timeout) {
    printf("INFO: Farsite.runSimFarsite -> Going to run farsite for individual (%d,%d) \n", individual.generation, individual.id);

    numgen = individual.generation;
    printf("INFO: Farsite.runSimFarsite -> numgene (%d) \n", individual.generation);

    char settings_filename[2000]; 

    char syscall[5000];
    double ti,te;
    double execuTime = 0.0f;
  
    initFarsiteVariables(configurationFile, individual.generation);

    sprintf(settings_filename, "%ssettings_%d_%d.txt", output_path, individual.generation, individual.id);
    
    createInputFiles(individual);
    
    int res=100;

    createSettingsFile(settings_filename, individual.id, individual.generation, res);
    
    printf("INFO: Farsite.runSimFarsite -> farsite_path (%s) \n", farsite_path);

	printf("         timeout --signal=SIGXCPU %.0f %sfarsite4P -i %s -f %d -t 1 -g %d -n %d -p %dm\n", 
           timeout, farsite_path, settings_filename, individual.threads, individual.generation, individual.id, res);

	sprintf(syscall,"timeout --signal=SIGXCPU %.0f %sfarsite4P -i %s -f %d -t 1 -g %d -n %d -p %dm", 
           timeout, farsite_path, settings_filename, individual.threads, individual.generation, individual.id, res);

	int err_syscall = system(syscall);
    
	if (err_syscall == 0) {
		char sim_fire_line[5000];
		sprintf(sim_fire_line, "%s%s.toa", output_path, RasterFileNameNew);
		*predictionError = getSimulationError(sim_fire_line);
	} else {
		printf( "FARSITE:%d_%d_%d_%f_%f_%s\n", individual.generation, individual.id, individual.threads, (float) timeout, (float) timeout, perimeterResolution);
		*predictionError = 9999.99f;
	}
   
    return (EXIT_SUCCESS);

}

void initFarsiteVariables(char * configurationFile, int numgen) {

    dictionary * configuration;
    configuration = iniparser_load(configurationFile);
    
    numGenerations = iniparser_getint(configuration, "genetic:numGenerations", 1);
    num_threads    = iniparser_getint(configuration, "main:num_threads", 1);
    doWindFields   = iniparser_getint(configuration, "main:doWindFields", 0);
    doMeteoSim     = iniparser_getint(configuration, "main:doMeteoSim", 0);

    CalibrateAdjustments = iniparser_getint(configuration, "main:CalibrateAdjustments", 0);
    FuelsToCalibrateFileName = iniparser_getstr(configuration, "main:FuelsToCalibrate"); 
   
    CustomFuelFile      = iniparser_getstr(configuration, "farsite:CustomFuelFile");
    farsite_path        = iniparser_getstr(configuration, "farsite:farsite_path");
    input_path          = iniparser_getstr(configuration, "farsite:input_path");
    output_path         = iniparser_getstr(configuration, "farsite:output_path");
    landscapeFile       = iniparser_getstr(configuration, "farsite:landscapeFile");
    adjFile             = iniparser_getstr(configuration, "farsite:adjFile");
    ignitionFile        = iniparser_getstr(configuration, "farsite:ignitionFile");
    ignitionFileType    = iniparser_getstr(configuration, "farsite:ignitionFileType");
    wndFile             = iniparser_getstr(configuration, "farsite:wndFile");
    wtrFile             = iniparser_getstr(configuration, "farsite:wtrFile");
    fmsFile             = iniparser_getstr(configuration, "farsite:fmsFile");
    baseWndFile         = iniparser_getstr(configuration, "farsite:baseWndFile");
    baseWtrFile         = iniparser_getstr(configuration, "farsite:baseWtrFile");
    baseFmsFile         = iniparser_getstr(configuration, "farsite:baseFmsFile");
    baseAdjFile         = iniparser_getstr(configuration, "farsite:baseAdjFile");
    RasterFileName      = iniparser_getstr(configuration, "farsite:RasterFileName");
    shapefile           = iniparser_getstr(configuration, "farsite:shapefile");
    VectorFileName      = iniparser_getstr(configuration, "farsite:VectorFileName");
    doRaster            = iniparser_getstr(configuration, "farsite:doRaster");
    doShape             = iniparser_getstr(configuration, "farsite:doShape");
    doVector            = iniparser_getstr(configuration, "farsite:doVector");

    if (numgen > numGenerations) { // predication step
        ignitionFile        = iniparser_getstr(configuration, "prediction:PignitionFile");
        ignitionFileType    = iniparser_getstr(configuration, "prediction:PignitionFileType");
        ConditMonth         = iniparser_getstr(configuration, "prediction:PConditMonth");
        ConditDay           = iniparser_getstr(configuration, "prediction:PConditDay");
        StartMonth          = iniparser_getstr(configuration, "prediction:PStartMonth");
        StartDay            = iniparser_getstr(configuration, "prediction:PStartDay");
        StartHour           = iniparser_getstr(configuration, "prediction:PStartHour");
        StartMin            = iniparser_getstr(configuration, "prediction:PStartMin");
        EndMonth            = iniparser_getstr(configuration, "prediction:PEndMonth");
        EndDay              = iniparser_getstr(configuration, "prediction:PEndDay");
        EndHour             = iniparser_getstr(configuration, "prediction:PEndHour");
        EndMin              = iniparser_getstr(configuration, "prediction:PEndMin");
        start_time          = iniparser_getint(configuration, "prediction:Pstart_time",1);
        end_time            = iniparser_getint(configuration, "prediction:Pend_time",1);
        real_fire_map_t0    = iniparser_getstr(configuration, "prediction:Preal_fire_map_t0");
        real_fire_map_t1    = iniparser_getstr(configuration, "prediction:Preal_fire_map_t1");
        real_fire_map_tINI  = iniparser_getstr(configuration, "prediction:Preal_fire_map_tINI");
    } else {                       // calibration step
		 ignitionFile        = iniparser_getstr(configuration, "farsite:ignitionFile");
		 ignitionFileType    = iniparser_getstr(configuration, "farsite:ignitionFileType");
		 ConditMonth         = iniparser_getstr(configuration, "farsite:ConditMonth");
		 ConditDay           = iniparser_getstr(configuration, "farsite:ConditDay");
		 StartMonth          = iniparser_getstr(configuration, "farsite:StartMonth");
		 StartDay            = iniparser_getstr(configuration, "farsite:StartDay");
		 StartHour           = iniparser_getstr(configuration, "farsite:StartHour");
		 StartMin            = iniparser_getstr(configuration, "farsite:StartMin");
		 EndMonth            = iniparser_getstr(configuration, "farsite:EndMonth");
		 EndDay              = iniparser_getstr(configuration, "farsite:EndDay");
		 EndHour             = iniparser_getstr(configuration, "farsite:EndHour");
		 EndMin              = iniparser_getstr(configuration, "farsite:EndMin");
		 start_time          = iniparser_getint(configuration, "farsite:start_time",1);
		 end_time            = iniparser_getint(configuration, "farsite:end_time",1);
		 real_fire_map_t0    = iniparser_getstr(configuration, "farsite:real_fire_map_t0");
    	 real_fire_map_t1    = iniparser_getstr(configuration, "farsite:real_fire_map_t1");
	 	 real_fire_map_tINI  = iniparser_getstr(configuration, "farsite:real_fire_map_tINI");		
	}
    
    timestep             = iniparser_getstr(configuration, "farsite:timestep");
    visibleStep          = iniparser_getstr(configuration, "farsite:visibleStep");
    secondaryVisibleStep = iniparser_getstr(configuration, "farsite:secondaryVisibleStep");
    perimeterResolution  = iniparser_getstr(configuration, "farsite:perimeterResolution");
    distanceResolution   = iniparser_getstr(configuration, "farsite:distanceResolution");
    FireSimLimit         = iniparser_getint(configuration, "farsite:ExecutionLimit", 1);

    TEMP_VARIATION       = iniparser_getdouble(configuration, "farsite:TEMP_VARIATION",1.0);
    HUM_VARIATION        = iniparser_getdouble(configuration, "farsite:HUM_VARIATION",1.0);
    
    if (CalibrateAdjustments) {
    	FILE *FuelsToCalibrateFILE;
	    int i, nFuel;
	
	    for (i=0;i<256;i++) {
            FuelsUs[i]=0;
        }

   	    if ((FuelsToCalibrateFILE = fopen(FuelsToCalibrateFileName, "r"))==NULL) {
		    printf("ERROR:Opening fuels used file.\n");
    	} else {
            while (fscanf(FuelsToCalibrateFILE, "%d", &nFuel)!=EOF) {
                FuelsUs[nFuel-1]=1;
            }
    	}
    }	
    
}

void createInputFiles(INDVTYPE_FARSITE individual) {
    char * line = (char*)malloc(sizeof(char) * 200);
    char * newline= (char*)malloc(sizeof(char) * 200);
    char * buffer= (char*)malloc(sizeof(char) * 200);
    
    fmsFileNew = (char*)malloc(sizeof(char) * 200);
    adjFileNew = (char*)malloc(sizeof(char) * 200);

    if (doMeteoSim == 0) {
 	    wndFileNew = (char*)malloc(sizeof(char) * 200);
    	wtrFileNew = (char*)malloc(sizeof(char) * 200);
    }

    FILE * fFMS, *fWND, *fWTR, *fADJ, *fFMSnew, *fWNDnew, *fWTRnew,*fADJnew;
    char * tmp = (char*)malloc(sizeof(char) * 400);
    sprintf(tmp,"%d",individual.generation);
    
    fmsFileNew = str_replace(fmsFile, "$1", tmp);

    if (CalibrateAdjustments) {
	    adjFileNew = str_replace(adjFile, "$1", tmp);
    }

    if (doMeteoSim == 0) {
	 	wndFileNew = str_replace(wndFile,"$1", tmp);
    	wtrFileNew = str_replace(wtrFile,"$1", tmp);
	}
    sprintf(tmp,"%d",individual.id);
    fmsFileNew = str_replace(fmsFileNew, "$2", tmp);
    if (CalibrateAdjustments) {
        adjFileNew = str_replace(adjFileNew, "$2", tmp);
    }

    if (doMeteoSim == 0) {
		wndFileNew = str_replace(wndFileNew,"$2", tmp);
    	wtrFileNew = str_replace(wtrFileNew,"$2", tmp);
    }

    if ((fFMS = fopen(baseFmsFile, "r")) == NULL) {
        printf("Unable to open FMS file");
        seguir = 0;
    }
    
    if (doMeteoSim == 0) {
        if (((fWND = fopen(baseWndFile, "r")) == NULL) || ((fWTR = fopen(baseWtrFile, "r")) == NULL) ) {
            printf("Unable to open WND or WTR files\n");
            seguir = 0;
        }
	 }

	 if (seguir == 1) {
            if ((fFMSnew = fopen(fmsFileNew, "w")) == NULL) {
                printf("Unable to create FMS temp file\n");
                seguir = 0;
            }
			if (doMeteoSim == 0){
				if (((fWNDnew = fopen(wndFileNew, "w")) == NULL) || ((fWTRnew = fopen(wtrFileNew, "w")) == NULL) ) {
                    printf("Unable to create WND or WTR temp files\n");
                    seguir = 0;
				}
			}
        if(seguir == 1) {
            while (fgets( line, 100, fFMS ) != NULL) {                     
                sprintf(buffer,"%1.0f",individual.parameters[0]);                      
                newline = str_replace(line, "1h", buffer);
                sprintf(buffer,"%1.0f",individual.parameters[1]);
                newline = str_replace(newline, "10h", buffer);
                sprintf(buffer,"%1.0f",individual.parameters[2]);
                newline = str_replace(newline, "100h", buffer);
                sprintf(buffer,"%1.0f",individual.parameters[3]);
                newline = str_replace(newline, "herb", buffer);
                fprintf(fFMSnew,"%s", newline);
            }

            fclose(fFMSnew);

            if (CalibrateAdjustments) {
                if (((fADJnew = fopen(adjFileNew, "w")) == NULL) || (fADJ = fopen(baseAdjFile, "r")) == NULL) {
                    printf("Unable to create ADJ temp file\n");
                    seguir = 0;
                }

                int nfuel,param=9;
                float adjust=0.0f;

                while (fscanf(fADJ,"%d %f",&nfuel,&adjust)!= EOF ) {
                    if (FuelsUs[nfuel-1]) {
                        fprintf(fADJnew,"%d %1.6f\n",nfuel,individual.parameters[param]);
                        param++;
                    } else {
                        fprintf(fADJnew,"%d 1.000000\n",nfuel);
                    }
                }
                fclose(fADJnew);
                fclose(fADJ);
            }

            if (doMeteoSim == 0) {
                fgets( line, 100, fWND );
                fprintf(fWNDnew,"%s", line);
                while(fgets( line, 100, fWND ) != NULL) {
                    sprintf(buffer,"%1.0f",individual.parameters[5]);
                    newline = str_replace(line, "ws", buffer);
                    sprintf(buffer,"%1.0f",individual.parameters[6]);
                    newline = str_replace(newline, "wd", buffer);
                    sprintf(buffer,"%d",0);
                    newline = str_replace(newline, "wc", buffer);
                    fprintf(fWNDnew,"%s", newline);
                }
                if (doMeteoSim == 0) {
                    fclose(fWNDnew);
                }

                fgets( line, 100, fWTR );
                fprintf(fWTRnew,"%s", line);
                float tl = individual.parameters[7] - TEMP_VARIATION;
                float hl = individual.parameters[8] - HUM_VARIATION;
                    
                while (fgets( line, 100, fWTR ) != NULL) {
                    sprintf(buffer,"%1.0f",tl);
                    newline = str_replace(line, "tl", buffer);
                    
                    sprintf(buffer,"%1.0f",individual.parameters[6]);
                    newline = str_replace(newline, "th", buffer);
                    
                    sprintf(buffer,"%1.0f", individual.parameters[7]);
                    newline = str_replace(newline, "hh", buffer);
                    
                    sprintf(buffer,"%1.0f",hl);
                    newline = str_replace(newline, "hl", buffer);
                    
                    fprintf(fWTRnew,"%s", newline);
                }

                if (doMeteoSim == 0) {
                    fclose(fWTRnew);
                }
            }
        }
        fclose(fFMS);
        if (doMeteoSim == 0){
            fclose(fWND);
            fclose(fWTR);
        }
    }     
}

void createSettingsFile(char * filename, int idInd, int generation, int res) {
    char * shapefileNew = (char*)malloc(sizeof(char) * 400);
    RasterFileNameNew = (char*)malloc(sizeof(char) * 400);
    char * VectorFileNameNew = (char*)malloc(sizeof(char) * 400);
    char * tmp = (char*)malloc(sizeof(char) * 400);
    
    FILE * file;

    if ( (file = fopen(filename, "w")) == NULL ) {
        printf("Unable to open settings file");
    } else {
        sprintf(tmp,"%d",numgen);
        shapefileNew = str_replace(shapefile, "$1", tmp);
        RasterFileNameNew = str_replace(RasterFileName,"$1", tmp);
        VectorFileNameNew = str_replace(VectorFileName,"$1", tmp);
        sprintf(tmp,"%d",idInd);
        shapefileNew = str_replace(shapefileNew, "$2", tmp);
        RasterFileNameNew = str_replace(RasterFileNameNew,"$2", tmp);
        VectorFileNameNew = str_replace(VectorFileNameNew,"$2", tmp);

        fprintf(file, "version = 43\n");

        fprintf(file, "landscapeFile = %s\n", landscapeFile);
        fprintf(file, "FUELMOISTUREFILE =  %s\n", fmsFileNew);
        if (CustomFuelFile != NULL) {
            fprintf(file, "fuelmodelfile = %s\n",CustomFuelFile);
        }
        
        if (doWindFields == 0) {
            if(doMeteoSim == 0) {
                fprintf(file, "windFile0 =  %s\n", wndFileNew);
            } else {
                fprintf(file, "windFile0 =  %s\n", wndFile);
            }
        } else {
            if (doMeteoSim == 0) {
                fprintf(file, "windFile0 =  %s\n", atmPath);
            }
            if (doMeteoSim == 1) {
                fprintf(file, "windFile0 =  %s\n", wndFile);
            }
        } if (CalibrateAdjustments) {
            fprintf(file, "adjustmentFile = %s\n", adjFileNew);
        } else {
            fprintf(file, "adjustmentFile = %s\n", baseAdjFile);
        }
        if(doMeteoSim == 0) {
            fprintf(file, "weatherFile0 = %s\n", wtrFileNew);
        } else {
            fprintf(file, "weatherFile0 = %s\n", wtrFile);
        }
        // OUTPUTS CREATION
        fprintf(file, "vectMake = %s\n", doVector);
        fprintf(file, "rastMake = %s\n", doRaster);
        fprintf(file, "shapeMake = %s\n", doShape);
        // RESOLUTION
        fprintf(file, "timestep = %s\n", timestep);
        fprintf(file, "visibleStep = %s\n", visibleStep);
        fprintf(file, "secondaryVisibleStep = %s\n", secondaryVisibleStep);
        //fprintf(file,"perimeterResolution = %s\n", perimeterResolution);
        fprintf(file, "perimeterResolution = %dm\n", res);
        fprintf(file, "distanceResolution = %s\n", distanceResolution);
        // IGNITION DATA & TYPE
        fprintf(file, "ignitionFile = %s\n", ignitionFile);
        fprintf(file, "ignitionFileType = %s\n", ignitionFileType);
        if (strcmp(doShape, "true") == 0) {
            fprintf(file, "shapefile=%s%s.shp\n", output_path, shapefileNew);	
        }
        if (strcmp(doRaster,"true") == 0) {
            fprintf(file, "RasterFileName=%s%s\n", output_path, RasterFileNameNew);
        }
        if (strcmp(doVector,"true") == 0) {
            fprintf(file, "VectorFileName=%s%s\n", output_path, VectorFileNameNew);
        }

        fprintf(file, "enableCrownfire = false\n");
        fprintf(file, "linkCrownDensityAndCover = false\n");
        fprintf(file, "embersFromTorchingTrees = false\n");
        fprintf(file, "enableSpotFireGrowth = false\n");
        fprintf(file, "nwnsBackingROS = false\n");
        fprintf(file, "fireacceleration=false\n");
        fprintf(file, "accelerationtranstion=1m\n");
        fprintf(file, "distanceChecking = fireLevel\n");
        fprintf(file, "simulatePostFrontalCombustion = false\n");
        fprintf(file, "fuelInputOption = absent\n");
        fprintf(file, "calculationPrecision = normal\n");

        fprintf(file, "useConditioningPeriod = false\n");
        fprintf(file, "ConditMonth = %s\n",ConditMonth);
        fprintf(file, "ConditDay = %s\n", ConditDay);

        fprintf(file, "StartMonth = %s\n", StartMonth);
        fprintf(file, "StartDay = %s\n", StartDay);
        fprintf(file, "StartHour = %s\n", StartHour);
        fprintf(file, "StartMin = %s\n", StartMin);
        fprintf(file, "EndMonth = %s\n", EndMonth);

        //if (generation > numGenerations) {
            //fprintf(file, "EndDay = %d\n", atoi(StartDay)+1);
        //    fprintf(file, "EndDay = %d\n", EndDay);
        //} else {
            fprintf(file, "EndDay = %s\n", EndDay);
        //}

        fprintf(file, "EndHour = %s\n", EndHour);
        fprintf(file, "EndMin = %s\n", EndMin);

        fprintf(file, "rast_arrivaltime = true\n");
        fprintf(file, "rast_fireIntensity = false\n");
        fprintf(file, "rast_spreadRate = false\n");
        fprintf(file, "rast_flameLength = false\n");
        fprintf(file, "rast_heatPerArea = false\n");
        fprintf(file, "rast_crownFire = false\n");
        fprintf(file, "rast_fireDirection = false\n");
        fprintf(file, "rast_reactionIntensity = false\n");

        fclose(file);
    }
}

double getSimulationError(char * simulatedMap) {

    FILE *fd,*fd2;
    char tmp;
	char name[20];
    int i,n,j,srows,scols,rrows=0,rcols=0,aux;
    double *mapaReal, *mapaSim,fitness, error = 9999.0, doubleValue,val;
        
    if (((fd = fopen(real_fire_map_t1, "r")) == NULL) || ((fd2 = fopen(real_fire_map_tINI, "r")) == NULL) ) {
        printf("Unable to open real map file");
    } else {
        fscanf(fd,"%7s%f\n%7s%f\n%7s%f\n%7s%f\n%7s%d\n%7s%d\n",name,&val,name,&val,name,&val,name,&val,name,&rrows,name,&rcols);
		fscanf(fd2,"%7s%f\n%7s%f\n%7s%f\n%7s%f\n%7s%d\n%7s%d\n",name,&val,name,&val,name,&val,name,&val,name,&aux,name,&aux);

		mapaReal=(double *)calloc(rrows*rcols,sizeof(double));

        for (i=0;i<rrows*rcols;i++) {
		    fscanf(fd2, "%lf\n", &doubleValue);
            if (doubleValue==1.0f) { 
			    fscanf(fd,"%lf\n",&mapaReal[i]);
			    mapaReal[i]=-1.0f;
            } else {
			    fscanf(fd,"%lf\n",&mapaReal[i]);
            }
		}
        fclose(fd);
		fclose(fd2);
    }

    if ((fd=fopen(simulatedMap,"r")) == NULL) {
        printf("Unable to open simulated map file");
    } else {
        fscanf(fd,"%7s%f\n%7s%f\n%7s%f\n%7s%f\n%7s%d\n%7s%d\n",name,&val,name,&val,name,&val,name,&val,name,&srows,name,&scols);
        mapaSim=(double *)calloc(srows*scols,sizeof(double));
    
        if ( (srows!=rrows) || (scols!=rcols) ) {
            printf("ERROR: Different map dimensions!! Real: %dx%d Simulated: %dx%d\n",rrows,rcols,srows,scols);		
        } else {
            for (i=0;i<srows*scols;i++) {
                fscanf(fd,"%lf\n",&mapaSim[i]);
            }
            fclose(fd);
            fitness=fitnessYError(mapaReal, mapaSim, rrows, rcols, start_time, end_time, &error);
            free(mapaReal);
            free(mapaSim);
        }          
    }

    return error;
}
