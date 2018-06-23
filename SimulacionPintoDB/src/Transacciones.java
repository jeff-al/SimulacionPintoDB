
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Iterator;

public class Transacciones extends Modulo {

    static class PQsort implements Comparator<Consulta> {       //Implementado para hacer que se inserten en la cola segun la prioridad establecida en el proyecto

        @Override
        public int compare(Consulta one, Consulta two) {      //Compara 2 consultas
            int val1 = 0;
            int val2 = 0;
            switch (one.tipoSentencia) {     //determinamos la prioridad del primero
                case UPDATE:
                    val1 = 1;
                    break;
                case JOIN:
                    val1 = 2;
                    break;
                case SELECT:
                    val1 = 3;
                    break;
            }
            switch (two.tipoSentencia) { //determinamos la prioridad del segundo
                case UPDATE:
                    val2 = 1;
                    break;
                case JOIN:
                    val2 = 2;
                    break;
                case SELECT:
                    val2 = 3;
                    break;
            }
            if (val1 == val2) {     //Comparamos prioridades
                return 0;
            } else if (val1 < val2) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    Transacciones(int numConsultasMaximas) {
        numMaxServidores = numConsultasMaximas;
    }

    boolean espera = false;                 //Booleano para determinar si un DDL está siendo atendido o está en cola
    PriorityQueue<Consulta> PQ = new PriorityQueue(new PQsort());   //Cola de prioridad del modulo de transacciones
    double tiempoTotalProcesamiento;

    @Override
    void procesarEntrada(Simulacion s, Evento e) {
        e.consulta.estadistTransacciones.tiempoLlegadaModulo = e.tiempo;
        e.consulta.moduloActual = Evento.TipoModulo.TRANSACCIONES;
        if (numServOcupados == numMaxServidores) { //Si todos los "servidores" estan ocupados
            PQ.add(e.consulta);
            if (e.consulta.tipoSentencia == Consulta.TipoSentencia.DDL) {   //Si entro un DDL avisa que está en cola
                espera = true;
            }
            s.estadisticasT.promedioColaT += PQ.size();
        } else {                       //Si hay "servidores" libres
            if (espera) {              //Si hay un DDL en cola o siendo atendido se añaden a la cola
                PQ.add(e.consulta);
                s.estadisticasT.promedioColaT += PQ.size();
            } else {                                                  //Si hay "servidores libres" y no hay DDL adentro del modulo
                if (e.consulta.tipoSentencia == Consulta.TipoSentencia.DDL) {  //Si el que entró es un DDL verifica si todos los servidores están desocupados
                    if (numServOcupados > 0) {    //Si hay al menos uno ocupado se pone en cola
                        PQ.add(e.consulta);
                        s.estadisticasT.promedioColaT += PQ.size();
                    } else {                      //sino pasa a ser atendido
                        e.consulta.estadistTransacciones.tiempoSalidaCola = 0;
                        Evento evento = new Evento(e.consulta);
                        evento.tipoE = Evento.TipoEvento.SALIDA;
                        evento.modulo = Evento.TipoModulo.TRANSACCIONES;
                        tiempoTotalProcesamiento = numMaxServidores * 0.03;
                        evento.tiempo = e.tiempo + tiempoTotalProcesamiento;
                        s.listaE.add(evento);
                        numServOcupados++;
                    }
                    espera = true;
                } else {       //Si el que entro no es un DDL se crea el evento con sus tiempos
                    e.consulta.estadistTransacciones.tiempoSalidaCola = 0;
                    Evento evento = new Evento(e.consulta);
                    evento.tipoE = Evento.TipoEvento.SALIDA;
                    evento.modulo = Evento.TipoModulo.TRANSACCIONES;
                    procesarConsulta(e.consulta);
                    evento.tiempo = e.tiempo + tiempoTotalProcesamiento;
                    s.listaE.add(evento);
                    numServOcupados++;
                }
            }
        }
    }

    @Override
    void procesarSalida(Simulacion s, Evento e) {
        e.consulta.estadistTransacciones.tiempoSalidaModulo = e.tiempo;
        e.consulta.estadistTransacciones.tiempoEnModulo = e.tiempo - e.consulta.estadistTransacciones.tiempoLlegadaModulo;
        Evento evento = new Evento(e.consulta);
        evento.tipoE = Evento.TipoEvento.ENTRADA;
        evento.modulo = Evento.TipoModulo.EJEC_SENTENCIAS;
        evento.tiempo = e.tiempo;
        s.listaE.add(evento);
        if (e.consulta.tipoSentencia == Consulta.TipoSentencia.DDL) {     //Si el que sale es un DDL avisa
            espera = false;
        }
        if (!PQ.isEmpty()) {   //Si despues de una salida hay algo en cola
            Consulta consulta = PQ.peek();          //verificamos que consulta está en la cabeza de la cola
            if (consulta.tipoSentencia == Consulta.TipoSentencia.DDL) {    //Si el que sigue es un DDL
                if ((numServOcupados - 1) == 0) {              //Si el DDL puede ser atendido de una vez
                    consulta = PQ.remove();
                    consulta.estadistTransacciones.tiempoSalidaCola = e.tiempo - e.consulta.estadistTransacciones.tiempoLlegadaModulo;
                    s.estadisticasT.promedioColaT += PQ.size();
                    Evento eventoS = new Evento(consulta);
                    eventoS.tipoE = Evento.TipoEvento.SALIDA;
                    eventoS.modulo = Evento.TipoModulo.TRANSACCIONES;
                    tiempoTotalProcesamiento = numMaxServidores * 0.03;
                    eventoS.tiempo = e.tiempo + tiempoTotalProcesamiento;
                    s.listaE.add(eventoS);
                    espera = true;
                } else {                                  //Si todavia están atendiendo a otras consultas
                    numServOcupados--;
                }
            } else {            //Si el que sigue no es un DDL
                if (e.consulta.tipoSentencia == Consulta.TipoSentencia.DDL) {   //Si el que sale es un DDL
                    numServOcupados--;
                    while (!PQ.isEmpty() && (numServOcupados < numMaxServidores)) {  //Si la cola no está vacia debe meter a todos los que puedan entrar
                        consulta = PQ.remove();
                        consulta.estadistTransacciones.tiempoSalidaCola = e.tiempo - e.consulta.estadistTransacciones.tiempoLlegadaModulo;
                        Evento eventoS = new Evento(consulta);
                        eventoS.tipoE = Evento.TipoEvento.SALIDA;
                        eventoS.modulo = Evento.TipoModulo.TRANSACCIONES;
                        procesarConsulta(consulta);
                        eventoS.tiempo = e.tiempo + tiempoTotalProcesamiento;
                        numServOcupados++;
                    }
                } else {                                      //Si el que sale no es un DDL
                    consulta = PQ.remove();
                    consulta.estadistTransacciones.tiempoSalidaCola = e.tiempo - e.consulta.estadistTransacciones.tiempoLlegadaModulo;
                    Evento eventoS = new Evento(consulta);
                    eventoS.tipoE = Evento.TipoEvento.SALIDA;
                    eventoS.modulo = Evento.TipoModulo.TRANSACCIONES;
                    procesarConsulta(consulta);
                    eventoS.tiempo = e.tiempo + tiempoTotalProcesamiento;
                }
            }
        } else {     //Si no hay ninguna consulta en cola
            numServOcupados--;
        }
    }

    @Override
    void imprimirCola() {
        Iterator<Consulta> it = PQ.iterator();
        System.out.print("C: ");
        while (it.hasNext()) {
            Consulta c = it.next();
            System.out.print(c.id + ", ");
        }
        System.out.print("\n");
    }

    @Override
    void procesarRetiro(Simulacion s, Evento e) {
        boolean enCola = false;                       //Booleano para saber si está en cola
        Iterator<Consulta> it = PQ.iterator();
        if (e.consulta.tipoSentencia == Consulta.TipoSentencia.DDL) {                  //Si el que se saca es un DDL
            espera = false;
        }
        while (it.hasNext()) {                 //Se busca la consulta en la cola
            Consulta c = it.next();
            if (c == e.consulta) {             //Si la consulta e´sta en cola se saca y se le saignan los tiempos de salida
                it.remove();
                s.estadisticasT.promedioColaT += PQ.size();
                e.consulta.tiempoEnsistema = e.tiempo - e.consulta.tiempoLlegada;
                e.consulta.estadistTransacciones.tiempoSalidaCola = e.tiempo - e.consulta.estadistTransacciones.tiempoLlegadaModulo;
                enCola = true;
            }
        }
        if (!enCola && !PQ.isEmpty()) {    //Si no estaba en cola y la cola no está vacia
            Consulta consulta = PQ.peek();          //verificamos que consulta está en la cabeza de la cola
            if (consulta.tipoSentencia == Consulta.TipoSentencia.DDL) {    //Si el que sigue es un DDL
                if ((numServOcupados - 1) == 0) {              //Si el DDL puede ser atendido de una vez
                    consulta = PQ.remove();
                    consulta.estadistTransacciones.tiempoSalidaCola = e.tiempo - consulta.estadistTransacciones.tiempoLlegadaModulo;
                    s.estadisticasT.promedioColaT += PQ.size();
                    Evento eventoS = new Evento(consulta);
                    eventoS.tipoE = Evento.TipoEvento.SALIDA;
                    eventoS.modulo = Evento.TipoModulo.TRANSACCIONES;
                    tiempoTotalProcesamiento = numMaxServidores * 0.03;
                    eventoS.tiempo = e.tiempo + tiempoTotalProcesamiento;
                    s.listaE.add(eventoS);
                    espera = true;
                } else {                                  //Si todavia están atendiendo a otras consultas
                    numServOcupados--;
                }
            } else {            //Si el que sigue no es un DDL
                if (e.consulta.tipoSentencia == Consulta.TipoSentencia.DDL) {   //Si el que sale es un DDL
                    numServOcupados--;
                    while (!PQ.isEmpty() && (numServOcupados < numMaxServidores)) {  //Si la cola no está vacia debe meter a todos los que puedan entrar
                        consulta = PQ.remove();
                        consulta.estadistTransacciones.tiempoSalidaCola = e.tiempo - consulta.estadistTransacciones.tiempoLlegadaModulo;
                        Evento eventoS = new Evento(consulta);
                        eventoS.tipoE = Evento.TipoEvento.SALIDA;
                        eventoS.modulo = Evento.TipoModulo.TRANSACCIONES;
                        procesarConsulta(consulta);
                        eventoS.tiempo = e.tiempo + tiempoTotalProcesamiento;
                        numServOcupados++;
                    }
                } else {                                      //Si el que sale no es un DDL
                    consulta = PQ.remove();
                    consulta.estadistTransacciones.tiempoSalidaCola = e.tiempo - consulta.estadistTransacciones.tiempoLlegadaModulo;
                    Evento eventoS = new Evento(consulta);
                    eventoS.tipoE = Evento.TipoEvento.SALIDA;
                    eventoS.modulo = Evento.TipoModulo.TRANSACCIONES;
                    procesarConsulta(consulta);
                    eventoS.tiempo = e.tiempo + tiempoTotalProcesamiento;
                }
            }
        } else if (!enCola) {
            numServOcupados--;
        }
        e.consulta.tiempoSalida = e.tiempo;
        e.consulta.estadistTransacciones.tiempoSalidaModulo = e.tiempo;
        e.consulta.estadistTransacciones.tiempoEnModulo = e.tiempo - e.consulta.estadistTransacciones.tiempoLlegadaModulo;
        e.consulta.enSistema = false;
    }

    @Override
    int Colasize() {
        return PQ.size();
    }

    void procesarConsulta(Consulta consulta) {
        tiempoTotalProcesamiento = numMaxServidores * 0.03;
        switch (consulta.tipoSentencia) {
            case JOIN:
                consulta.bloquesCargados = (int) generador.GenerarValUniforme(1, 64);
                tiempoTotalProcesamiento += consulta.bloquesCargados * 0.1;
                break;
            case SELECT:
                consulta.bloquesCargados = 1;
                tiempoTotalProcesamiento += 0.1;
                break;
        }
    }
}
