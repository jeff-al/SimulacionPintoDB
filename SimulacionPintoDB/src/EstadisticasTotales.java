
import java.util.*;

public class EstadisticasTotales {

    int conexionesDescartadas = 0;
    double promedioColaAP = 0;
    double promedioColaPC = 0;
    double promedioColaT = 0;
    double promedioColaES = 0;

    double pasoPorAP = 0;
    double pasoPorPC = 0;
    double pasoPorT = 0;
    double pasoPorES = 0;

    double promedioVidaConexion;

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
