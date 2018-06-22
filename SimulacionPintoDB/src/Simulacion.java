
import java.util.*;

public class Simulacion extends Thread {

    int ids = 0;
    double tiempoMaximo;
    double tiempoM;
    List<Consulta> listaC = new ArrayList();
    List<Evento> listaE = new ArrayList();

    int n; //ProcesosDisponibles
    int p; //MaximoConsultas
    int m; //MaximoSentencias
    int c;

    Modulo moduloAC;
    Modulo moduloAP;
    Modulo moduloPC;
    Modulo moduloT;
    Modulo moduloES;

    Ventanas.Interfaz interfaz = new Ventanas.Interfaz();

    GenValoresAleatorios generador = new GenValoresAleatorios();
    EstadisticasTotales estadisticasT = new EstadisticasTotales();

    private double reloj = 0;

    boolean inicial = true;

    void Simular() {
        interfaz.run();
        boolean v3 = false;
        while (!interfaz.v3) {
            v3 = interfaz.datos.getV3();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
        }
        tiempoM = interfaz.tM;
        tiempoMaximo = interfaz.t;
        c = interfaz.k;
        p = interfaz.p;
        m = interfaz.m;
        n = interfaz.n;
        moduloAC = new AdministracionConexiones(c);
        moduloAP = new AdministracionProcesos();
        moduloPC = new ProcesamientoConsulta(n);
        moduloT = new Transacciones(p);
        moduloES = new EjecucionDeSentencias(m);
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
                    Thread.sleep(1500);
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
                    interfaz.corr.impDurante("SALIO: " + evento.consulta.id + "  -------  "+"Mod Actual: "+evento.consulta.moduloActual);
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
                imprimir();
            }
        }
        estadisticasT.promediarVidaConexión(listaC);
        estadisticasT.promediarCola(listaC);
        System.out.println("Descartadas: " + estadisticasT.conexionesDescartadas);
        System.out.println("Totales: " + ids);
        System.out.println("Tamaño Promedio de la cola AP: " + estadisticasT.promedioColaAP);
        System.out.println("Tamaño Promedio de la cola PC: " + estadisticasT.promedioColaPC);
        System.out.println("Tamaño Promedio de la cola T: " + estadisticasT.promedioColaT);
        System.out.println("Tamaño Promedio de la cola ES: " + estadisticasT.promedioColaES);
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
        consulta.estadistAdm_Conexiones.tiempoLlegadaModulo = reloj;  //Se puede eliminar todo
        consulta.estadistAdm_Conexiones.tiempoSalidaModulo = reloj;
        consulta.estadistAdm_Conexiones.tiempoSalidaCola = 0;
        consulta.estadistAdm_Conexiones.tiempoEnModulo = 0; // Asumiendo que dura algo en el modulo
        Evento evento = new Evento(consulta);
        evento.tipoE = Evento.TipoEvento.ENTRADA;
        evento.modulo = evento.modulo.ADM_PROCESOS;

        Evento eventoTO = new Evento(consulta);
        eventoTO.tipoE = Evento.TipoEvento.RETIRO;
        if (inicial) {
            evento.tiempo = 0;
            consulta.tiempoLlegada = reloj;
            eventoTO.tiempo = reloj + tiempoMaximo;
            inicial = false;
        } else {
            evento.tiempo = reloj + generador.GenerarValExponencial(0.5);
            consulta.tiempoLlegada = evento.tiempo;
            eventoTO.tiempo = evento.tiempo + tiempoMaximo;
        }
        evento.consulta.bloquesCargados = 0;
        listaE.add(evento);
        listaE.add(eventoTO);
        listaC.add(consulta);
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

    void imprimir() {
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

        interfaz.corr.impDurante("Modulo AC  Conexiones Actuales : " + moduloAC.numServOcupados + "/" + moduloAC.numMaxServidores + "  " + tiempoMaximo);
        interfaz.corr.impDurante("\n\n\n");
        
    }

    public static void main(String[] args) {
        Simulacion s = new Simulacion();
        s.Simular();
    }
}
