/************************************************************************************************************/
 /* ACLARACION: esta funcion retorna el error, tiene un margen de error de como estan representados          */
 /* los mapas: si t2 == 4.00 y en el mapa simulado tengo 4.001 ya no lo toma como celda quemada, en          */
 /* cambio, si en el mapa real esta 4.00 si lo incluye como quemado, de aqui que podamos encontrar           */
 /* diferencias entre sumIni y sumReal que no esperabamos encontrar. REPRESENTACION INTERNA DE LOS MAPAS.....*/

 /*MODIFICACIONES ANDRES
 Celdas no quemadas en Farsite se representan con -001.000, por eso cambio
 >>>>> mapaReal[cell] = (mapaReal[cell] == 0.0)?INFINITY:mapaReal[cell];
 por
 >>>>>	if(mapaReal[cell] == -1.0)
 >>>>>		mapaReal[cell]=INFINITY;
 */
 
/************************************************************************************************************/

#include <stdio.h>

#define INFINITY 999999

double fitnessYError(double * mapaReal, double * mapaSim, int Rows, int Cols, double t1, double t2, double * error);
double errorXor(double * mapaReal, double * mapaSim, int Rows, int Cols, double t1, double t2, double * error);

double fitnessYError(double * mapaReal, double * mapaSim, int Rows, int Cols, double t1, double t2, double * error) {
    int Cells = Rows * Cols, cell;
    int sumI=0, sumU=0, sumIni=0, sumReal = 0, sumSim=0;
    int a,b;

    double fit;

    for (cell = 0; cell < Cells ; cell ++) {
        // paso el mapa real a infinity las celdas no quemadas
        if (mapaReal[cell] <= -1.0) {
            mapaReal[cell]=INFINITY;
        }

        if (mapaSim[cell] <= -1.0) {
            mapaSim[cell]=INFINITY;
        }

        a = mapaSim[cell] <= t2 ? 1 : 0;
        b = mapaReal[cell] <= t2 ? 1 : 0;

        sumIni += mapaReal[cell] <= t1 ? 1 : 0;
        sumReal += mapaReal[cell] <= t2 ? 1: 0;
        sumSim += mapaSim[cell] <= t2 ? 1 : 0;

        sumI += ((a==1) && (b==1)) ? 1 : 0;
        sumU += ((a==1) || (b==1)) ? 1 : 0;
    }

    if (sumU != sumIni) {
        fit = ((float)(sumI - sumIni)) / ((float)(sumU - sumIni));
    } else {
        fit = 0.0f;
    }

    if ( sumReal != sumIni ) {
    //BIAS'
        * error = (((float)(sumU - sumIni)) - ((float)(sumI - sumIni))) / ((float)(sumReal - sumIni));

    //BIAS'+FAR
    //  * error = (((((float)(sumU - sumIni)) - ((float)(sumI - sumIni))) / ((float)(sumReal - sumIni)))+((((float)(sumU - sumIni)) - ((float)(sumReal - sumIni))) / ((float)(sumSim - sumIni))))/2;

    //POD'+FAR
    //* error = (((((float)(sumReal-sumIni))-((float)(sumI-sumIni)))/((float)(sumReal-sumIni)))+((((float)(sumSim-sumIni))-((float)(sumI-sumIni)))/((float)(sumSim-sumIni))))/2;

    //CAOS
    // * error = (((((float)(sumU - sumIni)) - ((float)(sumI - sumIni))) / ((float)(sumReal - sumIni)))+((((float)(sumU-sumIni))-((float)(sumI-sumIni))) / ((float)(sumSim-sumIni))))/2;

    //BIAS'-FAR
    // * error = (((((float)(sumU - sumIni)) - ((float)(sumI - sumIni))) / ((float)(sumReal - sumIni)))-((((float)(sumU-sumIni))-((float)(sumReal-sumIni)))/((float)(sumSim-sumIni))))/2;

    //NEW1

    //   float alfa=1, beta=2;

    //* error = (alfa*(((float)(sumU-sumIni))-((float)(sumReal-sumIni))))+(beta*(((float)(sumU-sumIni))-((float)(sumSim-sumIni))));

    //NEW2
    // float alfa=1, beta=2, gamma=1;

    //     * error = (alfa*(((float)(sumU-sumIni))-((float)(sumReal-sumIni))))+(beta*(((float)(sumU-sumIni))-((float)(sumSim-sumIni))))-(gamma*((float)(sumI-sumIni)));

    //NEW3
    //alfa=(((float)(sumI-sumIni))/((float)(sumSim-sumIni)))
    //beta=(((float)(sumI-sumIni))/((float)(sumReal-sumIni)))
    //
    // * error = ((((float)(sumI-sumIni))/((float)(sumSim-sumIni)))*(((float)(sumU-sumIni))-((float)(sumReal-sumIni))))+((((float)(sumI-sumIni))/((float)(sumReal-sumIni)))*(((float)(sumU-sumIni))-((float)(sumSim-sumIni))));
    //
    } else {
        * error = 9999.0f;
    }

    //   printf("sumU= %d sumI= %d sumIni= %d sumReal=%d  fitness:%f error:%f t1:%f t2:%f \n", sumU, sumI, sumIni, sumReal, fit, * error, t1, t2);
    fit=1234.0f;
    return fit;

}

double errorXor(double * mapaReal, double * mapaSim, int Rows, int Cols, double t1, double t2, double * error) {
    int Cells = Rows * Cols, cell;
    int sumI=0, sumU=0, sumIni=0;
    float sumReal=0, sumXOR=0;
    int a,b;
    double fit;

    for (cell = 0; cell < Cells ; cell ++) {
        // paso el mapa real a infinity las celdas no quemadas
        if (mapaReal[cell] == -1.0) {
            mapaReal[cell]=INFINITY;
        }

        if (mapaSim[cell] == -1.0) {
            mapaSim[cell]=INFINITY;
        }

        a = mapaSim[cell] <= t2 ? 1 : 0;
        b = mapaReal[cell] <= t2 ? 1 : 0;

        if ( a^b ) {
            sumXOR++;
        }

        sumIni += mapaReal[cell] <= t1 ? 1 : 0;
        sumReal += mapaReal[cell] <= t2 ? 1: 0;

        sumI += ((a==1) && (b==1)) ? 1 : 0;
        sumU += ((a==1) || (b==1)) ? 1 : 0;
    }

    * error =  (float) (sumXOR / sumReal);

    return (double)fit;

}
