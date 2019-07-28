/*
 * File:   myutils.h
 * Author: carlos
 *
 * Created on 9 de mayo de 2012, 13:07
 */

#ifndef MYUTILS_H
#define	MYUTILS_H

char *str_replace(char *orig, char *rep, char *with);
int deleteFilesFromFolder(char *folder, char * extension);
int createFolder(char * folder);
int createLinkToFile(char *filename, char *filepath, char *folder);
int setEnvironmentVariable(char * var_name, char * var_value);

#endif	/* MYUTILS_H */


