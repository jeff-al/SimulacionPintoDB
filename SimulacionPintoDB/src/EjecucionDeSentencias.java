
import java.util.Iterator;

public class EjecucionDeSentencias extends Modulo {

    EjecucionDeSentencias(int MaxSentencias) {
        numMaxServidores = MaxSentencias;
    }

    @Override
    void procesarEntrada(Simulacion s, Evento e) {
        e.consulta.estadistEjec_Sentencias.tiempoLlegadaModulo = e.tiempo;
        e.consulta.moduloActual = Evento.TipoModulo.EJEC_SENTENCIAS;
        if (numServOcupados == numMaxServidores) { //Si ya hay una consulta siendo procesada
            colaC.add(e.consulta);
        } else {                       //Si no hay una consulta en cola
            double tiempoEjec = Math.pow(e.consulta.bloquesCargados, 2) * (1 / 1000);
            switch (e.consulta.tipoSentencia) {
                case DDL:
                    e.consulta.bloquesCargados = (int) generador.GenerarValUniforme(1, 64);
                    tiempoEjec += 0.5;
                    break;
                case UPDATE:
                    e.consulta.bloquesCargados = 1;
                    tiempoEjec += 1;
                    break;
            }
            e.consulta.estadistEjec_Sentencias.tiempoSalidaCola = 0;
            Evento evento = new Evento(e.consulta);
            evento.tipoE = e.tipoE.SALIDA;
            evento.modulo = e.modulo.EJEC_SENTENCIAS;
            evento.tiempo = e.tiempo + tiempoEjec;
            s.listaE.add(evento);
            numServOcupados++;
            Atendidos.add(e.consulta);
        }
    }

    @Override
    void procesarSalida(Simulacion s, Evento e) {
        e.consulta.tiempoSalida = e.tiempo;
        e.consulta.estadistEjec_Sentencias.tiempoSalidaModulo = e.tiempo;
        e.consulta.estadistEjec_Sentencias.tiempoEnModulo = e.tiempo - e.consulta.estadistEjec_Sentencias.tiempoLlegadaModulo;
        Evento evento = new Evento(e.consulta);
        evento.tipoE = e.tipoE.ENTRADA;
        evento.modulo = e.modulo.ADM_CONEXIONES;
        evento.tiempo = e.tiempo;
        numServOcupados--;
        s.listaE.add(evento);
        Atendidos.remove(e.consulta);
        if (!colaC.isEmpty()) {   //Si despues de una salida hay algo en cola
            Consulta consulta = colaC.remove();
            double tiempoEjec = Math.pow(e.consulta.bloquesCargados, 2) * (1 / 1000);
            switch (e.consulta.tipoSentencia) {
                case DDL:
                    e.consulta.bloquesCargados = (int) generador.GenerarValUniforme(1, 64);
                    tiempoEjec += 0.5;
                    break;
                case UPDATE:
                    e.consulta.bloquesCargados = 1;
                    tiempoEjec += 1;
                    break;
            }
            consulta.estadistEjec_Sentencias.tiempoSalidaCola = e.tiempo - consulta.estadistAdm_Procesos.tiempoLlegadaModulo;
            Evento eventoS = new Evento(consulta);
            eventoS.tipoE = e.tipoE.SALIDA;
            eventoS.modulo = e.modulo.EJEC_SENTENCIAS;
            eventoS.tiempo = e.tiempo + tiempoEjec;
            s.listaE.add(eventoS);
            numServOcupados++;

            Atendidos.add(eventoS.consulta);
        }
    }

    @Override
    void procesarRetiro(Simulacion s, Evento e) {
        boolean enCola = false;
        Iterator<Consulta> it = s.moduloES.colaC.iterator();
        while (it.hasNext()) {
            Consulta c = it.next();
            if (c == e.consulta) {
                it.remove();
                e.consulta.tiempoEnsistema = e.tiempo - e.consulta.tiempoLlegada;
                e.consulta.tiempoSalida = e.tiempo;
                e.consulta.estadistEjec_Sentencias.tiempoSalidaCola = e.tiempo - e.consulta.estadistEjec_Sentencias.tiempoLlegadaModulo;
                enCola = true;
            }
        }
        if (!enCola && !colaC.isEmpty()) {
            Consulta consulta = colaC.remove();
            double tiempoEjec = Math.pow(e.consulta.bloquesCargados, 2) * (1 / 1000);
            switch (e.consulta.tipoSentencia) {
                case DDL:
                    e.consulta.bloquesCargados = (int) generador.GenerarValUniforme(1, 64);
                    tiempoEjec += 0.5;
                    break;
                case UPDATE:
                    e.consulta.bloquesCargados = 1;
                    tiempoEjec += 1;
                    break;
            }
            consulta.estadistEjec_Sentencias.tiempoSalidaCola = e.tiempo - consulta.estadistAdm_Procesos.tiempoLlegadaModulo;
            Evento eventoS = new Evento(consulta);
            eventoS.tipoE = e.tipoE.SALIDA;
            eventoS.modulo = e.modulo.EJEC_SENTENCIAS;
            eventoS.tiempo = e.tiempo + tiempoEjec;
            s.listaE.add(eventoS);
            Atendidos.remove(e.consulta);
        } else if (!enCola) {
            s.moduloES.numServOcupados--;
            Atendidos.remove(e.consulta);
        }
        e.consulta.enSistema = false;
    }
}
