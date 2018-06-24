
import Ventanas.Interfaz;
import java.util.*;

public class Simulacion extends Thread {
    
    static int iter = 1;
    int ids;
    double tiempoMaximoConexion;
    double tiempoM;
    List<Consulta> listaC;
    List<Evento> listaE;
    static List<EstadisticasTotales> listaET;

    int n;  //ProcesosDisponibles
    int p;  //MaximoConsultas
    int m;  //MaximoSentencias
    int k;  //Conexiones maximas Modulo Administracion de Conexiones

    Modulo moduloAC;
    Modulo moduloAP;
    Modulo moduloPC;
    Modulo moduloT;
    Modulo moduloES;

    static Ventanas.Interfaz interfaz = new Ventanas.Interfaz();

    GenValoresAleatorios generador = new GenValoresAleatorios();
    
    EstadisticasTotales estadisticasT;

    private double reloj;
    boolean inicial;

    public Simulacion(){
        inicial = true;
        reloj = 0;
        ids = 0;
        listaC = new ArrayList();
        listaE = new ArrayList();
        tiempoM = interfaz.tM;
        tiempoMaximoConexion = interfaz.t;
        k = interfaz.k;
        p = interfaz.p;
        m = interfaz.m;
        n = interfaz.n;
        moduloAC = new AdministracionConexiones(k);
        moduloAP = new AdministracionProcesos();
        moduloPC = new ProcesamientoConsulta(n);
        moduloT = new Transacciones(p);
        moduloES = new EjecucionDeSentencias(m);
        estadisticasT = new EstadisticasTotales();
    }
    
    void Simular() {
        interfaz.corr.impDurante("Inicia Corrida numero: "+iter+"\n\n");
        boolean pausa = false;
        crearEvento();
        while (reloj < tiempoM) {
            pausa = interfaz.corr.getPausa();
            while (pausa) {
                pausa = interfaz.corr.getPausa();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            }
            if (interfaz.modoLento) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            Evento evento = buscarMenor(listaE);
            reloj = evento.tiempo;
            if (evento.consulta.enSistema) {
                if (evento.tipoE == Evento.TipoEvento.RETIRO) {
                    switch (evento.consulta.moduloActual) {
                        case ADM_PROCESOS:
                            moduloAP.procesarRetiro(this, evento);
                            moduloAC.numServOcupados--;
                            break;
                        case PROC_CONSULTAS:
                            moduloPC.procesarRetiro(this, evento);
                            moduloAC.numServOcupados--;
                            break;
                        case TRANSACCIONES:
                            moduloT.procesarRetiro(this, evento);
                            moduloAC.numServOcupados--;
                            break;
                        case EJEC_SENTENCIAS:
                            moduloES.procesarRetiro(this, evento);
                            moduloAC.numServOcupados--;
                            break;
                    }
                    interfaz.corr.impDurante("SALIO: " + evento.consulta.id + "  -------  " + "Mod Actual: " + evento.consulta.moduloActual + "Tipo: "+evento.consulta.tipoSentencia+"  " );
                } else {
                    interfaz.corr.impDurante("ID Consulta: " + evento.consulta.id + "  -------  Tipo de Sentencia: " + evento.consulta.tipoSentencia + "  -------  Modulo: " + evento.modulo + "  -------  Evento: " + evento.tipoE + "\n");
                    switch (evento.modulo) {
                        case ADM_CONEXIONES:
                            if (evento.tipoE == Evento.TipoEvento.ENTRADA) {
                                moduloAC.procesarEntrada(this, evento);
                            } else if (evento.tipoE == Evento.TipoEvento.SALIDA) {
                                moduloAC.procesarSalida(this, evento);
                            }
                            break;
                        case ADM_PROCESOS:
                            if (evento.tipoE == Evento.TipoEvento.ENTRADA) {
                                moduloAP.procesarEntrada(this, evento);
                                crearEvento();
                            } else if (evento.tipoE == Evento.TipoEvento.SALIDA) {
                                moduloAP.procesarSalida(this, evento);
                            }
                            break;
                        case PROC_CONSULTAS:
                            if (evento.tipoE == Evento.TipoEvento.ENTRADA) {
                                moduloPC.procesarEntrada(this, evento);
                            } else if (evento.tipoE == Evento.TipoEvento.SALIDA) {
                                moduloPC.procesarSalida(this, evento);
                            }
                            break;
                        case TRANSACCIONES:
                            if (evento.tipoE == Evento.TipoEvento.ENTRADA) {
                                moduloT.procesarEntrada(this, evento);
                            } else if (evento.tipoE == Evento.TipoEvento.SALIDA) {
                                moduloT.procesarSalida(this, evento);
                            }
                            break;
                        case EJEC_SENTENCIAS:
                            if (evento.tipoE == Evento.TipoEvento.ENTRADA) {
                                moduloES.procesarEntrada(this, evento);
                            } else if (evento.tipoE == Evento.TipoEvento.SALIDA) {
                                moduloES.procesarSalida(this, evento);
                            }
                            break;
                    }
                }
                imprimirD();
            }
        }
        imprimirF();
        //listaET.add(estadisticasT);
    }

    void crearEvento() {
        double random = Math.random();
        Consulta consulta = new Consulta();
        consulta.id = ids++;
        if (random < 0.30) {
            consulta.tipoSentencia = consulta.tipoSentencia.SELECT;
            consulta.soloLectura = true;
        } else if (random < 0.55) {
            consulta.tipoSentencia = consulta.tipoSentencia.UPDATE;
            consulta.soloLectura = false;
        } else if (random < 0.90) {
            consulta.tipoSentencia = consulta.tipoSentencia.JOIN;
            consulta.soloLectura = true;
        } else {
            consulta.tipoSentencia = consulta.tipoSentencia.DDL;
            consulta.soloLectura = false;
        }
        Evento evento = new Evento(consulta);                     //Evento de entrada al administrador de procesos
        evento.tipoE = Evento.TipoEvento.ENTRADA;
        evento.modulo = Evento.TipoModulo.ADM_PROCESOS;

        Evento eventoTO = new Evento(consulta);                  //Evento de TimeOut
        eventoTO.tipoE = Evento.TipoEvento.RETIRO;

        if (inicial) {
            evento.tiempo = 0;
            inicial = false;
        } else {
            evento.tiempo = reloj + generador.GenerarValExponencial(0.5);
        }
        consulta.tiempoLlegada = evento.tiempo;
        eventoTO.tiempo = evento.tiempo + tiempoMaximoConexion;
        
        listaE.add(evento);                    //Se añaden los eventos a la lista de eventos
        listaE.add(eventoTO);
        //listaC.add(consulta);                  //Se añade la consulta generada a la lista de consultas
    }

    Evento buscarMenor(List<Evento> lista) { 
        double tiempo = 1000000000;
        int index = 0;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).tiempo < tiempo) {
                index = i;
                tiempo = lista.get(i).tiempo;
            } else if (lista.get(i).tiempo == tiempo) {
                if (lista.get(i).tipoE == Evento.TipoEvento.SALIDA) {
                    index = i;
                }
            }
        }
        return lista.remove(index);
    }

    void imprimirD() { // Impresiones durante cada corrida
        interfaz.corr.impReloj("" + String.format("%.2f", reloj) + " seg\n");
        interfaz.corr.impCD("" + estadisticasT.conexionesDescartadas + "\n");
        interfaz.corr.impDurante("Reloj: " + String.format("%.2f", reloj) + " seg\n");
        interfaz.corr.impDurante("Modulo Administracion de Procesos Serv Ocupados : " + moduloAP.numServOcupados + "/" + moduloAP.numMaxServidores);
        interfaz.corr.impDurante("   Cola en modulo: " + moduloAP.colaC.size() + "\n");

        interfaz.corr.impDurante("Modulo Procesamiento de Consultas Serv Ocupados : " + moduloPC.numServOcupados + "/" + moduloPC.numMaxServidores);
        interfaz.corr.impDurante("   Cola en modulo: " + moduloPC.colaC.size() + "\n");

        interfaz.corr.impDurante("Modulo Transacciones Serv Ocupados : " + moduloT.numServOcupados + "/" + moduloT.numMaxServidores);
        interfaz.corr.impDurante("   Cola en modulo: " + moduloT.Colasize() + "\n");

        interfaz.corr.impDurante("Modulo Ejecucion de Sentencias Serv Ocupados : " + moduloES.numServOcupados + "/" + moduloES.numMaxServidores);
        interfaz.corr.impDurante("   Cola en modulo: " + moduloES.colaC.size() + "\n");

        interfaz.corr.impDurante("Modulo Administracion de Conexiones Conexiones Actuales : " + moduloAC.numServOcupados + "/" + moduloAC.numMaxServidores + " Tiempo max: " + tiempoMaximoConexion);
        interfaz.corr.impDurante("\n\n\n");
    }
    
    void imprimirF() {  // Impresiones al final de cada corrida
        estadisticasT.promediarVidaConexión(listaC);
        estadisticasT.promediarCola(listaC);
        estadisticasT.promediarTSxMod(listaC);
        interfaz.corr.impReloj("" + String.format("%.2f", reloj) + " seg\n");
        interfaz.corr.impCD("" + estadisticasT.conexionesDescartadas + "\n");
        interfaz.corr.impFinal("Corrida numero :"+iter + "\n");
        interfaz.corr.impFinal("Conexiones Descartadas: "+estadisticasT.conexionesDescartadas + "\n");
        interfaz.corr.impFinal("Conexiones Totales: "+ids + "\n");
        interfaz.corr.impFinal("Tiempo promedio de vida por conexion: "+ estadisticasT.promedioVidaConexion +" segundos \n");
        interfaz.corr.impFinal("Tamaño Promedio de la cola modulo Administracion de Procesos: " + estadisticasT.promedioColaAP + "\n");
        interfaz.corr.impFinal("Tamaño Promedio de la cola modulo Procesamiento de Consultas: " + estadisticasT.promedioColaPC + "\n");
        interfaz.corr.impFinal("Tamaño Promedio de la cola modulo Transacciones: " + estadisticasT.promedioColaT + "\n");
        interfaz.corr.impFinal("Tamaño Promedio de la cola modulo Ejecucion de Sentencias: " + estadisticasT.promedioColaES + "\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia DDL en el modulo Administracion de Procesos: "+estadisticasT.promediosDDL[0]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia DDL en el modulo Procesamiento de Consultas: "+estadisticasT.promediosDDL[1]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia DDL en el modulo Transacciones: "+estadisticasT.promediosDDL[2]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia DDL en el modulo Ejecucion de Sentencias: "+estadisticasT.promediosDDL[3]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia DDL en el modulo Administracion de Conexiones: "+estadisticasT.promediosDDL[4]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia UPDATE en el modulo Administracion de Procesos: "+estadisticasT.promediosUpdate[0]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia UPDATE en el modulo Procesamiento de Consultas: "+estadisticasT.promediosUpdate[1]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia UPDATE en el modulo Transacciones: "+estadisticasT.promediosUpdate[2]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia UPDATE en el modulo Ejecucion de Sentencias: "+estadisticasT.promediosUpdate[3]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia UPDATE en el modulo Administracion de Conexiones: "+estadisticasT.promediosUpdate[4]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia JOIN en el modulo Administracion de Procesos: "+estadisticasT.promediosJoin[0]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia JOIN en el modulo Procesamiento de Consultas: "+estadisticasT.promediosJoin[1]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia JOIN en el modulo Transacciones: "+estadisticasT.promediosJoin[2]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia JOIN en el modulo Ejecucion de Sentencias: "+estadisticasT.promediosJoin[3]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia JOIN en el modulo Administracion de Conexiones: "+estadisticasT.promediosJoin[4]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia SELECT en el modulo Administracion de Procesos: "+estadisticasT.promediosSelect[0]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia SELECT en el modulo Procesamiento de Consultas: "+estadisticasT.promediosSelect[1]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia SELECT en el modulo Transacciones: "+estadisticasT.promediosSelect[2]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia SELECT en el modulo Ejecucion de Sentencias: "+estadisticasT.promediosSelect[3]+"\n");
        interfaz.corr.impFinal("Promedio de vida de la Sentencia SELECT en el modulo Administracion de Conexiones: "+estadisticasT.promediosSelect[4]+"\n");
        interfaz.corr.impFinal("\n\n\n");
    }
    
    static void imprimirT() {  // Impresiones al final de la serie de corridas
        /*
        interfaz.corr.impFinal("Estadisticas Totales de toda la serie de corridas \n\n");
        for (int i = 0; i < listaET.size(); i++) {
            EstadisticasTotales it = listaET.get(i);
            for(int j = 0; j<5; j++){
                
            }
        }
        interfaz.corr.impFinal("Conexiones Descartadas: "+estadisticasT.conexionesDescartadas);
        interfaz.corr.impFinal("Conexiones Totales: "+ids);
        interfaz.corr.impFinal("Tamaño Promedio de la cola AP: " + estadisticasT.promedioColaAP);
        interfaz.corr.impFinal("Tamaño Promedio de la cola PC: " + estadisticasT.promedioColaPC);
        interfaz.corr.impFinal("Tamaño Promedio de la cola T: " + estadisticasT.promedioColaT);
        interfaz.corr.impFinal("Tamaño Promedio de la cola ES: " + estadisticasT.promedioColaES);
        interfaz.corr.impFinal("\n\n\n");
*/
    }

    public static void main(String[] args) {
        interfaz.run();
        boolean v3 = false;
        while (!interfaz.v3) {
            v3 = interfaz.datos.getV3();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
        int iteraciones = interfaz.iter;
        while (iter <= iteraciones) {
            Simulacion s = new Simulacion();
            s.Simular();
            iter++;
        }
        imprimirT();
    }
}
