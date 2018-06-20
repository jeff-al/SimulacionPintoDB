
import java.util.*;

public class EstadisticasTotales {

    int conexionesDescartadas;
    double PromedioColaAC;
    double PromedioColaAP;
    double PromedioColaPC;
    double PromedioColaT;
    double PromedioColaES;

    double promedioVidaConexion;

    double promediarCola(List<Consulta> listaC) {
        double promedio = 0;
        return promedio;
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
