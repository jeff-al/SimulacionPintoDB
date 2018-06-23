
import java.util.Iterator;

public class EjecucionDeSentencias extends Modulo {

    double tiempoEjecucion;

    EjecucionDeSentencias(int MaxSentencias) {
        numMaxServidores = MaxSentencias;
    }

    @Override
    void procesarEntrada(Simulacion s, Evento e) {
        e.consulta.estadistEjec_Sentencias.tiempoLlegadaModulo = e.tiempo;
        e.consulta.moduloActual = Evento.TipoModulo.EJEC_SENTENCIAS;
        if (numServOcupados == numMaxServidores) { //Si todos los "Servidores" estan ocupados se añade a la cola
            colaC.add(e.consulta);
            s.estadisticasT.promedioColaES += colaC.size();
        } else {                       //Si hay "Servidores" disponibles se procesa
            e.consulta.estadistEjec_Sentencias.tiempoSalidaCola = 0;
            Evento evento = new Evento(e.consulta);
            evento.tipoE = Evento.TipoEvento.SALIDA;
            evento.modulo = Evento.TipoModulo.EJEC_SENTENCIAS;
            ejecucionSentencia(e.consulta);
            evento.tiempo = e.tiempo + tiempoEjecucion;
            s.listaE.add(evento);
            numServOcupados++;
        }
    }

    @Override
    void procesarSalida(Simulacion s, Evento e) {   //Se ponen los tiempos de la salida
        e.consulta.estadistEjec_Sentencias.tiempoSalidaModulo = e.tiempo;
        e.consulta.estadistEjec_Sentencias.tiempoEnModulo = e.tiempo - e.consulta.estadistEjec_Sentencias.tiempoLlegadaModulo;
        Evento evento = new Evento(e.consulta);
        evento.tipoE = Evento.TipoEvento.ENTRADA;
        evento.modulo = Evento.TipoModulo.ADM_CONEXIONES;
        evento.tiempo = e.tiempo;
        numServOcupados--;
        s.listaE.add(evento);

        if (!colaC.isEmpty()) {   //Si despues de una salida hay algo en cola
            Consulta consulta = colaC.remove();
            consulta.estadistEjec_Sentencias.tiempoSalidaCola = e.tiempo - consulta.estadistEjec_Sentencias.tiempoLlegadaModulo;
            s.estadisticasT.promedioColaES += colaC.size();
            Evento eventoS = new Evento(consulta);
            eventoS.tipoE = Evento.TipoEvento.SALIDA;
            eventoS.modulo = Evento.TipoModulo.EJEC_SENTENCIAS;
            ejecucionSentencia(consulta);
            eventoS.tiempo = e.tiempo + tiempoEjecucion;
            s.listaE.add(eventoS);
            numServOcupados++;
        }

    }

    @Override
    void procesarRetiro(Simulacion s, Evento e) {
        boolean enCola = false;               //Booleano para saber si esta en cola
        Iterator<Consulta> it = colaC.iterator();
        while (it.hasNext()) {      //Lo buscamos en la cola
            Consulta c = it.next();
            if (c == e.consulta) {    //Si esta lo quitamos y se ponen los tiempos de salida
                it.remove();
                s.estadisticasT.promedioColaES += colaC.size();
                e.consulta.tiempoEnsistema = e.tiempo - e.consulta.tiempoLlegada;
                e.consulta.estadistEjec_Sentencias.tiempoSalidaCola = e.tiempo - e.consulta.estadistEjec_Sentencias.tiempoLlegadaModulo;
                enCola = true;
            }
        }

        if (!enCola && !colaC.isEmpty()) {       //Si está siendo atendido y la cola no está vacia se saca el siguiente de la cola y se le ponen los tiempos
            Consulta consulta = colaC.remove();
            consulta.estadistEjec_Sentencias.tiempoSalidaCola = e.tiempo - consulta.estadistEjec_Sentencias.tiempoLlegadaModulo;
            s.estadisticasT.promedioColaES += colaC.size();
            Evento eventoS = new Evento(consulta);
            eventoS.tipoE = Evento.TipoEvento.SALIDA;
            eventoS.modulo = Evento.TipoModulo.EJEC_SENTENCIAS;
            ejecucionSentencia(consulta);
            eventoS.tiempo = e.tiempo + tiempoEjecucion;
            s.listaE.add(eventoS);
            numServOcupados++;
        }
        if (!enCola) {
            numServOcupados--;
        }
        e.consulta.tiempoSalida = e.tiempo;
        e.consulta.estadistEjec_Sentencias.tiempoSalidaModulo = e.tiempo;
        e.consulta.estadistEjec_Sentencias.tiempoEnModulo = e.tiempo - e.consulta.estadistEjec_Sentencias.tiempoLlegadaModulo;
        e.consulta.enSistema = false;      //Se pone la consulta como fuera del sistema
    }

    void ejecucionSentencia(Consulta consulta) {
        tiempoEjecucion = Math.pow(consulta.bloquesCargados, 2) * (1 / 1000); //Milisegundos a segundos  (La ejecucion toma B a la 2 milisegundos en ejecutarse)
        switch (consulta.tipoSentencia) {
            case DDL:
                tiempoEjecucion += 0.5;       //Actualizar el esquema de la base de datos
                break;
            case UPDATE:
                tiempoEjecucion += 1;       //Actualizar el esquema de la base de datos
                break;
        }
    }
}
