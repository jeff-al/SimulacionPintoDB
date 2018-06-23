
import java.util.*;

public class EstadisticasTotales {

    int conexionesDescartadas;
    double promedioColaAP;
    double promedioColaPC;
    double promedioColaT;
    double promedioColaES;

    double pasoPorAP;
    double pasoPorPC;
    double pasoPorT;
    double pasoPorES;

    double promedioVidaConexion;
    
    public EstadisticasTotales(){
        conexionesDescartadas = 0;
        promedioColaAP = 0;
        promedioColaPC = 0;
        promedioColaT = 0;
        promedioColaES = 0;

        pasoPorAP = 0;
        pasoPorPC = 0;
        pasoPorT = 0;
        pasoPorES = 0;
    }

    void promediarCola(List<Consulta> listaC) {
        for (int i = 0; i < listaC.size(); i++) {
            Consulta it = listaC.get(i);
            if (it.estadistAdm_Procesos.tiempoSalidaCola > -1) {
                pasoPorAP++;
            }
            if (it.estadistProc_Consultas.tiempoSalidaCola > -1) {
                pasoPorPC++;
            }
            if (it.estadistTransacciones.tiempoSalidaCola > -1) {
                pasoPorT++;
            }
            if (it.estadistEjec_Sentencias.tiempoSalidaCola > -1) {
                pasoPorES++;
            }
        }
        promedioColaAP = Math.round(promedioColaAP / pasoPorAP);
        promedioColaPC = Math.round(promedioColaPC / pasoPorPC);
        promedioColaT = Math.round(promedioColaT / pasoPorT);
        promedioColaES = Math.round(promedioColaES / pasoPorES);
    }

    void promediarVidaConexión(List<Consulta> listaC) {
        double promedio = 0;
        for (int i = 0; i < listaC.size(); i++) {
            listaC.get(i).tiempoEnsistema = listaC.get(i).tiempoSalida - listaC.get(i).tiempoLlegada;
            promedio += listaC.get(i).tiempoEnsistema;
        }
        promedio /= 2;
        promedioVidaConexion = promedio;
    }
}
