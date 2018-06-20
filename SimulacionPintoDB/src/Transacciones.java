
import java.util.Comparator;
import java.util.PriorityQueue;

public class Transacciones extends Modulo {

    static class PQsort implements Comparator<Consulta> {

        @Override
        public int compare(Consulta one, Consulta two) {
            int val1 = 0;
            int val2 = 0;
            switch (one.tipoSentencia) {
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
            switch (two.tipoSentencia) {
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
            if (val1 == val2) {
                return 0;
            } else if (val1 < val2) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    Transacciones(int numConsultas) {
        numMaxServidores = numConsultas;
    }

    boolean espera = false;
    PriorityQueue<Consulta> PQ = new PriorityQueue(new PQsort());

    @Override
    void procesarEntrada(Simulacion s, Evento e) {
        e.consulta.estadistTransacciones.tiempoLlegadaModulo = e.tiempo;
        if (numServOcupados == numMaxServidores) { //Si ya hay una consulta siendo procesada
            PQ.add(e.consulta);
        } else {                       //Si no hay una consulta en cola
            if (espera) {
                PQ.add(e.consulta);
            } else {
                if (e.consulta.tipoSentencia == Consulta.TipoSentencia.DDL) {
                    if (numServOcupados > 0) {
                        PQ.add(e.consulta);
                        espera = true;
                    } else {
                        espera = true;
                        double tiempoTotal = numMaxServidores * 0.03;
                        switch (e.consulta.tipoSentencia) {
                            case JOIN:
                                e.consulta.bloquesCargados = (int) generador.GenerarValUniforme(1, 64);
                                tiempoTotal += e.consulta.bloquesCargados * 0.1;
                                break;
                            case SELECT:
                                e.consulta.bloquesCargados = 1;
                                tiempoTotal += 0.1;
                                break;
                        }
                        e.consulta.estadistTransacciones.tiempoSalidaCola = 0;
                        e.consulta.estadistTransacciones.tiempoSalidaModulo = e.tiempo + tiempoTotal;
                        e.consulta.estadistTransacciones.tiempoEnModulo = tiempoTotal;
                        Evento evento = new Evento(e.consulta);
                        evento.tipoE = e.tipoE.SALIDA;
                        evento.modulo = e.modulo.TRANSACCIONES;
                        evento.tiempo = e.tiempo + tiempoTotal;
                        s.listaE.add(evento);
                        numServOcupados++;
                    }
                } else {
                    double tiempoTotal = numMaxServidores * 0.03;
                    switch (e.consulta.tipoSentencia) {
                        case JOIN:
                            e.consulta.bloquesCargados = (int) generador.GenerarValUniforme(1, 64);
                            tiempoTotal += e.consulta.bloquesCargados * 0.1;
                            break;
                        case SELECT:
                            e.consulta.bloquesCargados = 1;
                            tiempoTotal += 0.1;
                            break;
                    }
                    e.consulta.estadistTransacciones.tiempoSalidaCola = 0;
                    e.consulta.estadistTransacciones.tiempoSalidaModulo = e.tiempo + tiempoTotal;
                    e.consulta.estadistTransacciones.tiempoEnModulo = tiempoTotal;
                    Evento evento = new Evento(e.consulta);
                    evento.tipoE = e.tipoE.SALIDA;
                    evento.modulo = e.modulo.TRANSACCIONES;
                    evento.tiempo = e.tiempo + tiempoTotal;
                    s.listaE.add(evento);
                    numServOcupados++;
                }
            }
        }
    }

    @Override
    void procesarSalida(Simulacion s, Evento e) {
        e.consulta.tiempoSalida = e.tiempo;
        if ((e.tiempo - e.consulta.tiempoLlegada) > s.tiempoMaximo) { //Si hace timeout
            e.consulta.tiempoEnsistema = e.tiempo - e.consulta.tiempoLlegada;
            e.consulta.tiempoSalida = e.tiempo;
            Evento evento = new Evento(e.consulta);
            evento.tipoE = evento.tipoE.RETIRO;
            evento.modulo = evento.modulo.TRANSACCIONES;
            evento.tiempo = e.tiempo;
            numServOcupados--;
        } else {                                     //Si no hace timeout
            Evento evento = new Evento(e.consulta);
            evento.tipoE = e.tipoE.ENTRADA;
            evento.modulo = e.modulo.EJEC_SENTENCIAS;
            evento.tiempo = e.tiempo;
            numServOcupados--;
            s.listaE.add(evento);
        }
        if (e.consulta.tipoSentencia == Consulta.TipoSentencia.DDL) {
            espera = false;
        }
        if (!PQ.isEmpty()) {   //Si despues de una salida hay algo en cola
            Consulta consulta = PQ.remove();
            if (consulta.tipoSentencia == Consulta.TipoSentencia.DDL) {
                espera = true;
            }
            double tiempoTotal = numMaxServidores * 0.03;
            switch (consulta.tipoSentencia) {
                case JOIN:
                    consulta.bloquesCargados = (int) generador.GenerarValUniforme(1, 64);
                    tiempoTotal += e.consulta.bloquesCargados * 0.1;
                    break;
                case SELECT:
                    consulta.bloquesCargados = 1;
                    tiempoTotal += 0.1;
                    break;
            }
            consulta.estadistTransacciones.tiempoSalidaCola = e.tiempo - e.consulta.estadistTransacciones.tiempoLlegadaModulo;
            consulta.estadistTransacciones.tiempoSalidaModulo = e.tiempo + tiempoTotal;
            consulta.estadistTransacciones.tiempoEnModulo = (tiempoTotal + e.tiempo) - consulta.estadistTransacciones.tiempoLlegadaModulo;
            Evento evento = new Evento(e.consulta);
            evento.tipoE = Evento.TipoEvento.SALIDA;
            evento.modulo = Evento.TipoModulo.TRANSACCIONES;
            evento.tiempo = e.tiempo + tiempoTotal;
            s.listaE.add(evento);
            numServOcupados++;
        }
    }
}
