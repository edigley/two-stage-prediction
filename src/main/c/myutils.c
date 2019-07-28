#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>

char *str_replace(char *orig, char *rep, char *with) {
    char *result; // the return string
    char *ins;    // the next insert point
    char *tmp;    // varies
    int len_rep;  // length of rep
    int len_with; // length of with
    int len_front; // distance between rep and end of last rep
    int count;    // number of replacements

    if (!orig)
        return NULL;
    if (!rep || !(len_rep = strlen(rep)))
        return NULL;
    if (!(ins = strstr(orig, rep))) {
        tmp = malloc(strlen(orig));
        strcpy(tmp,orig);
        return tmp;
    }
    if (!with)
        with = "";
    len_with = strlen(with);

    for (count = 0; tmp = strstr(ins, rep); ++count) {
        ins = tmp + len_rep;
    }

    tmp = result = malloc(strlen(orig) + (len_with - len_rep) * count + 1);

    if (!result)
        return NULL;

    while (count--) {
        ins = strstr(orig, rep);
        len_front = ins - orig;
        tmp = strncpy(tmp, orig, len_front) + len_front;
        tmp = strcpy(tmp, with) + len_with;
        orig += len_front + len_rep; // move to next "end of rep"
    }
    strcpy(tmp, orig);
    return result;
}

int deleteFilesFromFolder(char *folder, char * extension) {//usage deleteFilesFromFolder(folder,["asc,exe,*,..."])
    char syscall[200];
    sprintf(syscall,"rm %s*.%s",folder,extension);

    int err_syscall = system(syscall);

    return err_syscall;
}

int createFolder(char * folder) {
    mkdir(folder, S_IRWXU | S_IRWXG | S_IRWXO);
    return 0;
}

int createLinkToFile(char *filename, char *filepath, char *folder) {
    char syscall[200];
    //printf("ELEVFILENAME: %s\n",filepath);
    sprintf(syscall,"ln %s%s %s%s",filepath,filename,folder,filename);

    int err_syscall = system(syscall);

    return err_syscall;
}

int setEnvironmentVariable(char * var_name, char * var_value) {
    char syscall[200];
    sprintf(syscall,"%s=%s",var_name, var_value);

    return putenv(syscall);
}
