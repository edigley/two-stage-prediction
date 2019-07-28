/*********************************************************************/
/* population.c     			  			                                 */
/* rutinas para manejo de la poblacion y de los individuos           */
/*********************************************************************/

#include <stdlib.h>
#include "population.h"

/**
 * - population (output): pointer to store the population
 * - populationFileName: path to the population file
 */
int readPopulation(POPULATIONTYPE * population, char * populationFileName) {
    
    FILE * populationFile;
    if ((populationFile = fopen(populationFileName, "r")) == NULL) {
        printf("ERROR: Farsite.readPopulation -> Population file can't be found or opened: %s \n", populationFileName);
        return -1;
    }

    //read the header: populationSize currentGeneration numberOfParams
    fscanf(populationFile,"%d %d %d\n", &population->popuSize, &population->currentGen, &population->nParams);

    population->popu_fs = (INDVTYPE_FARSITE *)malloc(sizeof(INDVTYPE_FARSITE) * population->popuSize);
    if( (population->nParams-2) > population->maxparams ) {
        printf("WARNING: Farsite.readPopulation -> The number of parameters specified in population file is greater than maxparams used in compilation.\n");
    };

    // read each individual
    int i,j;
    for (i = 0; i < population->popuSize; i++) {
        population->popu_fs[i].id = i;
        population->popu_fs[i].class_ind = 'A';
        population->popu_fs[i].threads = 1;
        for (j=0; j < (population->nParams-2); j++) {
            fscanf(populationFile,"%f ", &(population->popu_fs[i].parameters[j]));
        }
        population->popu_fs[i].nparams_farsite = population->nParams-2;
        fscanf(populationFile, "%f %f %d %d %d",
            &population->popu_fs[i].error, 
            &population->popu_fs[i].errorc,
            &population->popu_fs[i].executed,
            &population->popu_fs[i].oldid,
            &population->popu_fs[i].generation
        );
    }

    fclose(populationFile);

    return 0;
}

void individualToString(int generation, INDVTYPE_FARSITE individual, char * pszIndividual, int buffersize) {
    if (!pszIndividual || buffersize<1) {
        *pszIndividual = '\0'; // return an 'empty' string 
    } else {
        //sprintf(pszIndividual, "%d %d %1.6f %1.6f %1.6f %1.6f %1.6f %1.6f %1.6f %1.6f %1.6f %1.6f %1.6f %1.6f %1.6f %1.6f %1.6f %1.6f %1.6f %1.6f %1.6f %1.6f %1.6f", 
        sprintf(pszIndividual, "%d %d %1.0f %1.0f %1.0f %1.0f %1.0f %1.0f %1.0f %1.0f %1.0f %1.6f %1.0f %1.0f %1.0f %1.0f %1.0f %1.0f %1.0f %1.0f %1.0f %1.0f %1.0f", 
            generation, individual.id, 
            individual.parameters[0], individual.parameters[1], individual.parameters[2], individual.parameters[3],
            individual.parameters[4],
            individual.parameters[5], individual.parameters[6],
            individual.parameters[7], individual.parameters[8],
            individual.parameters[9], individual.parameters[10], individual.parameters[11], individual.parameters[12], individual.parameters[13], 
            individual.parameters[14], individual.parameters[15],
            individual.parameters[16], individual.parameters[17],
            individual.parameters[18], individual.parameters[19], individual.parameters[20]
        );
    }
    //pszIndividual[buffersize-1] = '\0'; // ensure a valid terminating zero! Many people forget this!
}

