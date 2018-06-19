
import java.util.*;

public class Simulacion {

    int ids = 0;
    double tiempoMaximo = 15;
    List<Consulta> listaC = new ArrayList();
    List<Evento> listaE = new ArrayList();

    int n = 5; //ProcesosDisponibles
    int p = 3; //MaximoConsultas
    int m = 6; //MaximoSentencias
    int c = 7;

    Modulo moduloAC = new AdministracionConexiones(c);
    Modulo moduloAP = new AdministracionProcesos();
    Modulo moduloPC = new ProcesamientoConsulta(n);
    Modulo moduloT = new Transacciones(p);
    Modulo moduloES = new EjecucionDeSentencias(m);

    GenValoresAleatorios generador = new GenValoresAleatorios();

    private double reloj = 0;

    boolean inicial = true;

    void Simular() {

        tiempoMaximo = 15;
        crearEvento();
        while (reloj < 50) {
            Evento evento = buscarMenor(listaE);
            reloj = evento.tiempo;
            switch (evento.modulo) {
                case ADM_CONEXIONES:
                    if (evento.tipoE == Evento.TipoEvento.ENTRADA) {
                        moduloAC.procesarEntrada(this, evento);
                    } else if (evento.tipoE == Evento.TipoEvento.SALIDA) {
                        moduloAC.procesarSalida(this, evento);
                    } else {
                        moduloAC.procesarRetiro(this, evento);
                    }
                    break;
                case ADM_PROCESOS:
                    if (evento.tipoE == Evento.TipoEvento.ENTRADA) {
                        moduloAP.procesarEntrada(this, evento);
                        //if(moduloAC.numMaxServidores != moduloAC.numServOcupados){
                            crearEvento();
                        //}
                    } else if (evento.tipoE == Evento.TipoEvento.SALIDA) {
                        moduloAP.procesarSalida(this, evento);
                    } else {
                        moduloAP.procesarRetiro(this, evento);
                    }
                    break;
                case PROC_CONSULTAS:
                    if (evento.tipoE == Evento.TipoEvento.ENTRADA) {
                        moduloPC.procesarEntrada(this, evento);
                    } else if (evento.tipoE == Evento.TipoEvento.SALIDA) {
                        moduloPC.procesarSalida(this, evento);
                    } else {
                        moduloPC.procesarRetiro(this, evento);
                    }
                    break;
                case TRANSACCIONES:
                    if (evento.tipoE == Evento.TipoEvento.ENTRADA) {
                        moduloT.procesarEntrada(this, evento);
                    } else if (evento.tipoE == Evento.TipoEvento.SALIDA) {
                        moduloT.procesarSalida(this, evento);
                    } else {
                        moduloT.procesarRetiro(this, evento);
                    }
                    break;
                case EJEC_SENTENCIAS:
                    if (evento.tipoE == Evento.TipoEvento.ENTRADA) {
                        moduloES.procesarEntrada(this, evento);
                    } else if (evento.tipoE == Evento.TipoEvento.SALIDA) {
                        moduloES.procesarSalida(this, evento);
                    } else {
                        moduloES.procesarRetiro(this, evento);
                    }
                    break;
            }
        }
        imprimir();
    }

    void crearEvento() {
        double random = Math.random();
        System.out.println("random: "+random);
        Consulta consulta = new Consulta();
        consulta.id = ids++;
        if (random < 30) {
            consulta.tipoSentencia = consulta.tipoSentencia.SELECT;
            consulta.soloLectura = true;
        } else if (random < 55) {
            consulta.tipoSentencia = consulta.tipoSentencia.UPDATE;
            consulta.soloLectura = false;
        } else if (random < 90) {
            consulta.tipoSentencia = consulta.tipoSentencia.JOIN;
            consulta.soloLectura = true;
        } else {
            consulta.tipoSentencia = consulta.tipoSentencia.DDL;
            consulta.soloLectura = false;
        }
        consulta.tiempoLlegada = reloj;
        consulta.estadistAdm_Conexiones.tiempoLlegadaModulo = reloj;
        consulta.estadistAdm_Conexiones.tiempoSalidaModulo = reloj;
        consulta.estadistAdm_Conexiones.tiempoSalidaCola = 0;
        consulta.estadistAdm_Conexiones.tiempoEnModulo = 0; // Asumiendo que dura algo en el modulo
        Evento evento = new Evento(consulta);
        evento.tipoE = evento.tipoE.ENTRADA;
        evento.modulo = evento.modulo.ADM_PROCESOS;
        evento.consulta = consulta;
        if (inicial) {
            evento.tiempo = 0;
            inicial = false;
        } else {
            evento.tiempo = reloj + generador.GenerarValExponencial(0.5);
        }
        evento.consulta.bloquesCargados = 0;
        listaE.add(evento);
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
        for (int i = 0; i < listaC.size(); i++) {

            System.out.print("Nombre significativo: " + listaC.get(i).id + " ");
            System.out.print("bloquesCargados: " + listaC.get(i).bloquesCargados + " ");
            System.out.print("tiempoLlegada: " + listaC.get(i).tiempoLlegada + " ");
            System.out.print("tiempoSalida: " + listaC.get(i).tiempoSalida + " ");
            System.out.print("tiempoSistema: " + listaC.get(i).tiempoEnsistema + "\n\n");

        }

    }

    public static void main(String[] args) {
        Simulacion s = new Simulacion();
        s.Simular();
    }
}

    //Falta Maim
