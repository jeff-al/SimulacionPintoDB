
import java.util.*;

public class Simulacion extends Thread {
    
    static int iter = 1;
    int ids;
    double tiempoMaximoConexion;
    double tiempoM;
    List<Consulta> listaC;
    List<Evento> listaE;

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
            //System.out.println("Reloj: " + reloj + " ID: " + evento.consulta.id + " Sentencia: " + evento.consulta.tipoSentencia + " Modulo: " + evento.modulo + " Evento: " + evento.tipoE);
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
                    //System.out.println("Reloj: " + reloj + " ID: " + evento.consulta.id + " Sentencia: " + evento.consulta.tipoSentencia + " Modulo: " + evento.modulo + " Evento: " + evento.tipoE);
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
        /*
        estadisticasT.promediarVidaConexión(listaC);
        estadisticasT.promediarCola(listaC);
        System.out.println("Descartadas: " + estadisticasT.conexionesDescartadas);
        System.out.println("Totales: " + ids);
        System.out.println("Tamaño Promedio de la cola AP: " + estadisticasT.promedioColaAP);
        System.out.println("Tamaño Promedio de la cola PC: " + estadisticasT.promedioColaPC);
        System.out.println("Tamaño Promedio de la cola T: " + estadisticasT.promedioColaT);
        System.out.println("Tamaño Promedio de la cola ES: " + estadisticasT.promedioColaES);
        */
        
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
        listaC.add(consulta);                  //Se añade la consulta generada a la lista de consultas
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
        interfaz.corr.impDurante("Modulo AP  Serv Ocupados : " + moduloAP.numServOcupados + "/" + moduloAP.numMaxServidores);
        interfaz.corr.impDurante("   Cola en modulo: " + moduloAP.colaC.size() + "\n");

        interfaz.corr.impDurante("Modulo PC  Serv Ocupados : " + moduloPC.numServOcupados + "/" + moduloPC.numMaxServidores);
        interfaz.corr.impDurante("   Cola en modulo: " + moduloPC.colaC.size() + "\n");

        interfaz.corr.impDurante("Modulo TR  Serv Ocupados : " + moduloT.numServOcupados + "/" + moduloT.numMaxServidores);
        interfaz.corr.impDurante("   Cola en modulo: " + moduloT.Colasize() + "\n");

        interfaz.corr.impDurante("Modulo ES  Serv Ocupados : " + moduloES.numServOcupados + "/" + moduloES.numMaxServidores);
        interfaz.corr.impDurante("   Cola en modulo: " + moduloES.colaC.size() + "\n");

        interfaz.corr.impDurante("Modulo AC  Conexiones Actuales : " + moduloAC.numServOcupados + "/" + moduloAC.numMaxServidores + " Tiempo max: " + tiempoMaximoConexion);
        interfaz.corr.impDurante("\n\n\n");
    }
    
    void imprimirF() {  // Impresiones al final de cada corrida
        estadisticasT.promediarVidaConexión(listaC);
        estadisticasT.promediarCola(listaC);
        interfaz.corr.impReloj("" + String.format("%.2f", reloj) + " seg\n");
        interfaz.corr.impCD("" + estadisticasT.conexionesDescartadas + "\n");
        interfaz.corr.impFinal("Corrida numero :"+iter + "\n");
        interfaz.corr.impFinal("Conexiones Descartadas: "+estadisticasT.conexionesDescartadas + "\n");
        interfaz.corr.impFinal("Conexiones Totales: "+ids + "\n");
        interfaz.corr.impFinal("Tamaño Promedio de la cola AP: " + estadisticasT.promedioColaAP + "\n");
        interfaz.corr.impFinal("Tamaño Promedio de la cola PC: " + estadisticasT.promedioColaPC + "\n");
        interfaz.corr.impFinal("Tamaño Promedio de la cola T: " + estadisticasT.promedioColaT + "\n");
        interfaz.corr.impFinal("Tamaño Promedio de la cola ES: " + estadisticasT.promedioColaES + "\n");
        interfaz.corr.impFinal("\n\n\n");
    }
    
    void imprimirT() {  // Impresiones al final de la serie de corridas
        estadisticasT.promediarVidaConexión(listaC);
        estadisticasT.promediarCola(listaC);
        interfaz.corr.impReloj("" + String.format("%.2f", reloj) + " seg\n");
        interfaz.corr.impCD("" + estadisticasT.conexionesDescartadas + "\n");
        interfaz.corr.impFinal("Corrida numero :"+iter);
        interfaz.corr.impFinal("Conexiones Descartadas: "+estadisticasT.conexionesDescartadas);
        interfaz.corr.impFinal("Conexiones Totales: "+ids);
        interfaz.corr.impFinal("Tamaño Promedio de la cola AP: " + estadisticasT.promedioColaAP);
        interfaz.corr.impFinal("Tamaño Promedio de la cola PC: " + estadisticasT.promedioColaPC);
        interfaz.corr.impFinal("Tamaño Promedio de la cola T: " + estadisticasT.promedioColaT);
        interfaz.corr.impFinal("Tamaño Promedio de la cola ES: " + estadisticasT.promedioColaES);
        interfaz.corr.impFinal("\n\n\n");
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
    }
}
