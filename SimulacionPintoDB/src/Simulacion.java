
import java.util.*;

public class Simulacion extends Thread {

    int ids = 0;
    double tiempoMaximo = 15;
    double tiempoM;
    List<Consulta> listaC = new ArrayList();
    List<Evento> listaE = new ArrayList();

    int n; //ProcesosDisponibles
    int p; //MaximoConsultas
    int m; //MaximoSentencias
    int c;

    Modulo moduloAC = new AdministracionConexiones(c);
    Modulo moduloAP = new AdministracionProcesos();
    Modulo moduloPC = new ProcesamientoConsulta(n);
    Modulo moduloT = new Transacciones(p);
    Modulo moduloES = new EjecucionDeSentencias(m);
    
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
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
        tiempoM = interfaz.tM;
        tiempoMaximo = interfaz.t;
        c = interfaz.k;
        p = interfaz.p;
        m = interfaz.m;
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
            System.out.print("\033[H\033[2J");
            //System.out.println("Reloj: " + reloj + " ID: " + evento.consulta.id + " Sentencia: " + evento.consulta.tipoSentencia + " Modulo: " + evento.modulo + " Evento: " + evento.tipoE);
            if (evento.consulta.enSistema) {
                if (evento.tipoE == Evento.TipoEvento.RETIRO) {
                    switch (evento.consulta.moduloActual) {
                        case ADM_PROCESOS:
                            moduloAP.procesarRetiro(this, evento);
                            break;
                        case ADM_CONEXIONES:
                            moduloAC.procesarRetiro(this, evento);
                            break;
                        case PROC_CONSULTAS:
                            moduloPC.procesarRetiro(this, evento);
                            break;
                        case TRANSACCIONES:
                            moduloT.procesarRetiro(this, evento);
                            break;
                        case EJEC_SENTENCIAS:
                            moduloES.procesarRetiro(this, evento);
                            break;
                    }
                    //System.out.println("SALIO: " + evento.consulta.id);
                    interfaz.corr.impDurante("SALIO: " + evento.consulta.id);
                } else {
                    interfaz.corr.impDurante("ID Consulta: " + evento.consulta.id + " Tipo de Sentencia: " + evento.consulta.tipoSentencia + " Modulo: " + evento.modulo + " Evento: " + evento.tipoE + "\n");
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
                                if (moduloAC.numMaxServidores != moduloAC.numServOcupados) {
                                    crearEvento();
                                } else {
                                    estadisticasT.conexionesDescartadas++;
                                }
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
            if (listaE.isEmpty()) {
                crearEvento();
            }
        }
        System.out.println("Descartadas: " + estadisticasT.conexionesDescartadas);
        System.out.println("Totales: " + ids);
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
        consulta.tiempoLlegada = reloj;
        consulta.estadistAdm_Conexiones.tiempoLlegadaModulo = reloj;  //Se puede eliminar todo
        consulta.estadistAdm_Conexiones.tiempoSalidaModulo = reloj;
        consulta.estadistAdm_Conexiones.tiempoSalidaCola = 0;
        consulta.estadistAdm_Conexiones.tiempoEnModulo = 0; // Asumiendo que dura algo en el modulo
        Evento evento = new Evento(consulta);
        evento.tipoE = Evento.TipoEvento.ENTRADA;
        evento.modulo = evento.modulo.ADM_PROCESOS;

        Evento eventoTO = new Evento(consulta);
        eventoTO.tipoE = Evento.TipoEvento.RETIRO;
        eventoTO.tiempo = reloj + tiempoMaximo;
        if (inicial) {
            evento.tiempo = 0;
            inicial = false;
        } else {
            evento.tiempo = reloj + generador.GenerarValExponencial(0.5);
        }
        evento.consulta.bloquesCargados = 0;
        listaE.add(evento);
        listaE.add(eventoTO);
        listaC.add(consulta);
        moduloAC.numServOcupados++;
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
        interfaz.corr.impReloj("" + reloj + "\n");
        interfaz.corr.impCD("" + estadisticasT.conexionesDescartadas + "\n");
        interfaz.corr.impDurante("Reloj: " + reloj + "\n");
        interfaz.corr.impDurante("Cola en modulo AP: " + moduloAP.colaC.size() + "\n");
        interfaz.corr.impDurante("Cola en modulo PC: " + moduloPC.colaC.size() + "\n");
        interfaz.corr.impDurante("Cola en modulo TR: " + moduloT.colaC.size() + "\n");
        interfaz.corr.impDurante("Cola en modulo ES: " + moduloES.colaC.size() + "\n");
        interfaz.corr.impDurante("\n\n\n");
        /*
        System.out.print("AP ");
        moduloAP.imprimirAtend();
        System.out.print("     -------------    ");
        moduloAP.imprimirCola();
        System.out.print("PC ");
        moduloPC.imprimirAtend();
        System.out.print("     -------------    ");
        moduloPC.imprimirCola();
        System.out.print("TR ");
        moduloT.imprimirAtend();
        System.out.print("     -------------    ");
        moduloT.imprimirCola();
        System.out.print("ES ");
        moduloES.imprimirAtend();
        System.out.print("     -------------    ");
        moduloES.imprimirCola();
        System.out.print("AC ");
        moduloAC.imprimirAtend();
        System.out.print("     -------------    ");
        moduloAC.imprimirCola();
        */
    }

    public static void main(String[] args) {
        Simulacion s = new Simulacion();
        s.Simular();
    }
}

    //Falta Maim
