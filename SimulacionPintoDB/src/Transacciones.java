
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Iterator;

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
        e.consulta.moduloActual = Evento.TipoModulo.TRANSACCIONES;
        if (numServOcupados == numMaxServidores) { //Si ya hay una consulta siendo procesada
            PQ.add(e.consulta);
            s.estadisticasT.promedioColaT += PQ.size();
        } else {                       //Si no hay una consulta en cola
            if (espera) {
                PQ.add(e.consulta);
                s.estadisticasT.promedioColaT += PQ.size();
            } else {                                                  //Si hay servidores libres y no hay DDL adentro
                if (e.consulta.tipoSentencia == Consulta.TipoSentencia.DDL) {
                    if (numServOcupados > 0) {
                        PQ.add(e.consulta);
                        s.estadisticasT.promedioColaT += PQ.size();
                        espera = true;
                    } else {
                        espera = true;
                        double tiempoTotal = numMaxServidores * 0.03;
                        e.consulta.estadistTransacciones.tiempoSalidaCola = 0;
                        Evento evento = new Evento(e.consulta);
                        evento.tipoE = e.tipoE.SALIDA;
                        evento.modulo = e.modulo.TRANSACCIONES;
                        evento.tiempo = e.tiempo + tiempoTotal;
                        s.listaE.add(evento);
                        numServOcupados++;
                        Atendidos.add(e.consulta);
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
                    Evento evento = new Evento(e.consulta);
                    evento.tipoE = e.tipoE.SALIDA;
                    evento.modulo = e.modulo.TRANSACCIONES;
                    evento.tiempo = e.tiempo + tiempoTotal;
                    s.listaE.add(evento);
                    numServOcupados++;
                    Atendidos.add(e.consulta);
                }
            }
        }
    }

    @Override
    void procesarSalida(Simulacion s, Evento e) {
        e.consulta.estadistTransacciones.tiempoSalidaModulo = e.tiempo;
        e.consulta.estadistTransacciones.tiempoEnModulo = e.tiempo - e.consulta.estadistTransacciones.tiempoLlegadaModulo;                           //Si no hace timeout
        Evento evento = new Evento(e.consulta);
        evento.tipoE = e.tipoE.ENTRADA;
        evento.modulo = e.modulo.EJEC_SENTENCIAS;
        evento.tiempo = e.tiempo;
        numServOcupados--;
        s.listaE.add(evento);
        Atendidos.remove(e.consulta);
        if (e.consulta.tipoSentencia == Consulta.TipoSentencia.DDL) {
            espera = false;
        }
        if (!PQ.isEmpty()) {   //Si despues de una salida hay algo en cola
            Consulta consulta = PQ.peek();
            if (consulta.tipoSentencia == Consulta.TipoSentencia.DDL) {
                if (numServOcupados == 0) {
                    double tiempoTotal = numMaxServidores * 0.03;
                    consulta = PQ.remove();
                    Evento eventoS = new Evento(consulta);
                    eventoS.tipoE = e.tipoE.SALIDA;
                    eventoS.modulo = e.modulo.TRANSACCIONES;
                    s.listaE.add(eventoS);
                    s.estadisticasT.promedioColaT += PQ.size();
                    eventoS.tiempo = e.tiempo + tiempoTotal;
                    Atendidos.add(e.consulta);
                    numServOcupados++;
                }
                espera = true;
            } else {
                while (!PQ.isEmpty() && (numServOcupados < numMaxServidores)) {
                    double tiempoTotal = numMaxServidores * 0.03;
                    consulta = PQ.remove();
                    Evento eventoS = new Evento(consulta);
                    eventoS.tipoE = e.tipoE.SALIDA;
                    eventoS.modulo = e.modulo.TRANSACCIONES;
                    switch (consulta.tipoSentencia) {
                        case JOIN:
                            consulta.bloquesCargados = (int) generador.GenerarValUniforme(1, 64);
                            tiempoTotal += consulta.bloquesCargados * 0.1;
                            break;
                        case SELECT:
                            consulta.bloquesCargados = 1;
                            tiempoTotal += 0.1;
                            break;
                    }
                    consulta.estadistTransacciones.tiempoSalidaCola = e.tiempo - e.consulta.estadistTransacciones.tiempoLlegadaModulo;
                    eventoS.tiempo = e.tiempo + tiempoTotal;
                    numServOcupados++;
                }
            }
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
        boolean enCola = false;
        Iterator<Consulta> it = colaC.iterator();
        if (e.consulta.tipoSentencia == Consulta.TipoSentencia.DDL) {
            espera = false;
        }
        while (it.hasNext()) {
            Consulta c = it.next();
            if (c == e.consulta) {
                it.remove();
                s.estadisticasT.promedioColaT += PQ.size();
                e.consulta.tiempoEnsistema = e.tiempo - e.consulta.tiempoLlegada;
                e.consulta.tiempoSalida = e.tiempo;
                e.consulta.estadistTransacciones.tiempoSalidaCola = e.tiempo - e.consulta.estadistTransacciones.tiempoLlegadaModulo;
                enCola = true;
            }
        }
        if (!enCola) {
            numServOcupados--;
            Atendidos.remove(e.consulta);
        }
        if (!enCola && !PQ.isEmpty()) {
            Consulta consulta = PQ.peek();
            if (consulta.tipoSentencia == Consulta.TipoSentencia.DDL) {
                if ((numServOcupados) == 0) {
                    double tiempoTotal = numMaxServidores * 0.03;
                    consulta = PQ.remove();
                    Evento eventoS = new Evento(consulta);
                    eventoS.tipoE = e.tipoE.SALIDA;
                    eventoS.modulo = e.modulo.TRANSACCIONES;
                    s.listaE.add(eventoS);
                    s.estadisticasT.promedioColaT += PQ.size();
                    eventoS.tiempo = e.tiempo + tiempoTotal;
                    Atendidos.add(e.consulta);
                    numServOcupados++;
                }
                espera = true;
            } else {
                while (!PQ.isEmpty() && (numServOcupados < numMaxServidores)) {
                    double tiempoTotal = numMaxServidores * 0.03;
                    consulta = PQ.remove();
                    Evento eventoS = new Evento(consulta);
                    eventoS.tipoE = e.tipoE.SALIDA;
                    eventoS.modulo = e.modulo.TRANSACCIONES;
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
                    eventoS.tiempo = e.tiempo + tiempoTotal;
                    numServOcupados++;
                }
            }
            Atendidos.remove(e.consulta);
        }
        e.consulta.enSistema = false;
    }

    @Override
    int Colasize() {
        return PQ.size();
    }
}
