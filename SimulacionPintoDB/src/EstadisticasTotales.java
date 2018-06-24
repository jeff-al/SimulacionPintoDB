
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

    // arrays para determinar el tiempo promedio de tipo de sentencia por modulo
    // con 0 = moduloAP
    // con 1 = moduloPC
    // con 2 = moduloT
    // con 3 = moduloES
    // con 4 = moduloAC
    double[] promediosDDL;
    double[] promediosUpdate;
    double[] promediosSelect;
    double[] promediosJoin;
    int[] pasaPorDDL;
    int[] pasaPorUpdate;
    int[] pasaPorSelect;
    int[] pasaPorJoin;

    double promedioVidaConexion;

    public EstadisticasTotales() {
        conexionesDescartadas = 0;
        promedioColaAP = 0;
        promedioColaPC = 0;
        promedioColaT = 0;
        promedioColaES = 0;

        pasoPorAP = 0;
        pasoPorPC = 0;
        pasoPorT = 0;
        pasoPorES = 0;

        promediosDDL = new double[5];
        promediosUpdate = new double[5];
        promediosSelect = new double[5];
        promediosJoin = new double[5];
        pasaPorDDL = new int[5];
        pasaPorUpdate = new int[5];
        pasaPorSelect = new int[5];
        pasaPorJoin = new int[5];
        for (int i = 0; i < 4; i++) {
            promediosDDL[i] = 0.0;
            promediosUpdate[i] = 0.0;
            promediosSelect[i] = 0.0;
            promediosJoin[i] = 0.0;
            pasaPorDDL[i] = 0;
            pasaPorUpdate[i] = 0;
            pasaPorSelect[i] = 0;
            pasaPorJoin[i] = 0;
        }
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
        int x = 0;
        for (int i = 0; i < listaC.size(); i++) {
            if (listaC.get(i).tiempoLlegada <= listaC.get(i).tiempoSalida) {
                listaC.get(i).tiempoEnsistema = listaC.get(i).tiempoSalida - listaC.get(i).tiempoLlegada;
                promedio += listaC.get(i).tiempoEnsistema;
                x++;
            }
        }
        promedio = promedio / x;
        promedioVidaConexion = promedio;
    }

    void promediarTSxMod(List<Consulta> listaC) { // Promediar tipo de sentencia por modulo
        for (int i = 0; i < listaC.size(); i++) {
            Consulta it = listaC.get(i);
            if (it.estadistAdm_Procesos.tiempoEnModulo > -1) {
                if (it.tipoSentencia == Consulta.TipoSentencia.DDL) {
                    pasaPorDDL[0]++;
                    promediosDDL[0] += it.estadistAdm_Procesos.tiempoEnModulo;
                } else if (it.tipoSentencia == Consulta.TipoSentencia.UPDATE) {
                    pasaPorUpdate[0]++;
                    promediosUpdate[0] += it.estadistAdm_Procesos.tiempoEnModulo;
                } else if (it.tipoSentencia == Consulta.TipoSentencia.JOIN) {
                    pasaPorJoin[0]++;
                    promediosJoin[0] += it.estadistAdm_Procesos.tiempoEnModulo;
                } else {
                    pasaPorSelect[0]++;
                    promediosSelect[0] += it.estadistAdm_Procesos.tiempoEnModulo;
                }
            }
            if (it.estadistProc_Consultas.tiempoEnModulo > -1) {
                if (it.tipoSentencia == Consulta.TipoSentencia.DDL) {
                    pasaPorDDL[1]++;
                    promediosDDL[1] += it.estadistProc_Consultas.tiempoEnModulo;
                } else if (it.tipoSentencia == Consulta.TipoSentencia.UPDATE) {
                    pasaPorUpdate[1]++;
                    promediosUpdate[1] += it.estadistProc_Consultas.tiempoEnModulo;
                } else if (it.tipoSentencia == Consulta.TipoSentencia.JOIN) {
                    pasaPorJoin[1]++;
                    promediosJoin[1] += it.estadistProc_Consultas.tiempoEnModulo;
                } else {
                    pasaPorSelect[1]++;
                    promediosSelect[1] += it.estadistProc_Consultas.tiempoEnModulo;
                }
            }
            if (it.estadistTransacciones.tiempoEnModulo > -1) {
                if (it.tipoSentencia == Consulta.TipoSentencia.DDL) {
                    pasaPorDDL[2]++;
                    promediosDDL[2] += it.estadistTransacciones.tiempoEnModulo;
                } else if (it.tipoSentencia == Consulta.TipoSentencia.UPDATE) {
                    pasaPorUpdate[2]++;
                    promediosUpdate[2] += it.estadistTransacciones.tiempoEnModulo;
                } else if (it.tipoSentencia == Consulta.TipoSentencia.JOIN) {
                    pasaPorJoin[2]++;
                    promediosJoin[2] += it.estadistTransacciones.tiempoEnModulo;
                } else {
                    pasaPorSelect[2]++;
                    promediosSelect[2] += it.estadistTransacciones.tiempoEnModulo;
                }
            }
            if (it.estadistEjec_Sentencias.tiempoEnModulo > -1) {
                if (it.tipoSentencia == Consulta.TipoSentencia.DDL) {
                    pasaPorDDL[3]++;
                    promediosDDL[3] += it.estadistEjec_Sentencias.tiempoEnModulo;
                } else if (it.tipoSentencia == Consulta.TipoSentencia.UPDATE) {
                    pasaPorUpdate[3]++;
                    promediosUpdate[3] += it.estadistEjec_Sentencias.tiempoEnModulo;
                } else if (it.tipoSentencia == Consulta.TipoSentencia.JOIN) {
                    pasaPorJoin[3]++;
                    promediosJoin[3] += it.estadistEjec_Sentencias.tiempoEnModulo;
                } else {
                    pasaPorSelect[3]++;
                    promediosSelect[3] += it.estadistEjec_Sentencias.tiempoEnModulo;
                }
            }
            if (it.estadistAdm_Conexiones.tiempoEnModulo > -1) {
                if (it.tipoSentencia == Consulta.TipoSentencia.DDL) {
                    pasaPorDDL[4]++;
                    promediosDDL[4] += it.estadistAdm_Conexiones.tiempoEnModulo;
                } else if (it.tipoSentencia == Consulta.TipoSentencia.UPDATE) {
                    pasaPorUpdate[4]++;
                    promediosUpdate[4] += it.estadistAdm_Conexiones.tiempoEnModulo;
                } else if (it.tipoSentencia == Consulta.TipoSentencia.JOIN) {
                    pasaPorJoin[4]++;
                    promediosJoin[4] += it.estadistAdm_Conexiones.tiempoEnModulo;
                } else {
                    pasaPorSelect[4]++;
                    promediosSelect[4] += it.estadistAdm_Conexiones.tiempoEnModulo;
                }
            }
        }
        for (int i = 0; i < 5; i++) {
            if (promediosDDL[i] != 0) {
                promediosDDL[i] = promediosDDL[i] / pasaPorDDL[i];
            }
            if (promediosUpdate[i] != 0) {
                promediosUpdate[i] = promediosUpdate[i] / pasaPorUpdate[i];
            }
            if (promediosSelect[i] != 0) {
                promediosSelect[i] = promediosSelect[i] / pasaPorSelect[i];
            }
            if (promediosJoin[i] != 0) {
                promediosJoin[i] = promediosJoin[i] / pasaPorJoin[i];
            }
        }
    }
}
