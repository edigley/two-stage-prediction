/*
 * File:   farsite.h
 * Author: carlos
 *
 * Created on 16 de abril de 2012, 14:17
 */

#ifndef FARSITE_H
#define	FARSITE_H

int runFarsite(INDVTYPE_FARSITE individual, double * predictionError, char * configurationFile, double timeout);

void createInputFiles(INDVTYPE_FARSITE *individual);

#endif	/* FARSITE_H */


