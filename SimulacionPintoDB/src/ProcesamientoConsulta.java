
import java.util.Iterator;

public class ProcesamientoConsulta extends Modulo {

    double tiempoProcesamiento;

    ProcesamientoConsulta(int ProcesosDisponibles) {
        numMaxServidores = ProcesosDisponibles;
    }

    @Override
    void procesarEntrada(Simulacion s, Evento e) {
        e.consulta.estadistProc_Consultas.tiempoLlegadaModulo = e.tiempo;
        e.consulta.moduloActual = Evento.TipoModulo.PROC_CONSULTAS;
        if (numServOcupados == numMaxServidores) { //Si ya todos los procesos están ocupados
            colaC.add(e.consulta);
            s.estadisticasT.promedioColaPC += colaC.size();
        } else {                              //Si hay procesos libres
            e.consulta.estadistProc_Consultas.tiempoSalidaCola = 0;
            Evento evento = new Evento(e.consulta);
            evento.tipoE = Evento.TipoEvento.SALIDA;
            evento.modulo = Evento.TipoModulo.PROC_CONSULTAS;
            procesamiento(e.consulta);                // Se procesa la consulta
            evento.tiempo = e.tiempo + tiempoProcesamiento;
            s.listaE.add(evento);
            numServOcupados++;
        }

    }

    @Override
    void procesarSalida(Simulacion s, Evento e) {
        e.consulta.estadistProc_Consultas.tiempoSalidaModulo = e.tiempo;
        e.consulta.estadistProc_Consultas.tiempoEnModulo = e.tiempo - e.consulta.estadistProc_Consultas.tiempoLlegadaModulo;
        Evento evento = new Evento(e.consulta);
        evento.tipoE = Evento.TipoEvento.ENTRADA;
        evento.modulo = Evento.TipoModulo.TRANSACCIONES;
        evento.tiempo = e.tiempo;
        numServOcupados--;
        s.listaE.add(evento);

        if (!colaC.isEmpty()) {   //Si despues de una salida hay algo en cola
            Consulta consulta = colaC.remove();
            consulta.estadistProc_Consultas.tiempoSalidaCola = e.tiempo - consulta.estadistProc_Consultas.tiempoLlegadaModulo;
            s.estadisticasT.promedioColaPC += colaC.size();
            Evento eventoS = new Evento(consulta);
            eventoS.tipoE = e.tipoE.SALIDA;
            eventoS.modulo = e.modulo.PROC_CONSULTAS;
            procesamiento(consulta);        //Se procesa la consulta
            eventoS.tiempo = e.tiempo + tiempoProcesamiento;
            s.listaE.add(eventoS);
            numServOcupados++;
        }

    }

    @Override
    void procesarRetiro(Simulacion s, Evento e) {
        boolean enCola = false;                             //Booleano para saber si se sacó o no de la cola
        Iterator<Consulta> it = s.moduloPC.colaC.iterator();
        while (it.hasNext()) {                      //Se busca en la cola
            Consulta c = it.next();
            if (c == e.consulta) {             //Si lo encuentra lo saca y se ponen los tiempos de salidas para las estadisticas
                it.remove();
                s.estadisticasT.promedioColaPC += colaC.size();
                e.consulta.tiempoEnsistema = e.tiempo - e.consulta.tiempoLlegada;
                e.consulta.estadistProc_Consultas.tiempoSalidaCola = e.tiempo - e.consulta.estadistProc_Consultas.tiempoLlegadaModulo;
                enCola = true;
            }
        }
        if (!enCola && !colaC.isEmpty()) {                        //Si estaba siendo atendido y hay consultas en cola
            Consulta consulta = colaC.remove();                   //Se saca una colsulta de la cola para ser atendida
            consulta.estadistProc_Consultas.tiempoSalidaCola = e.tiempo - consulta.estadistProc_Consultas.tiempoLlegadaModulo;
            s.estadisticasT.promedioColaPC += colaC.size();
            Evento eventoS = new Evento(consulta);
            eventoS.tipoE = Evento.TipoEvento.SALIDA;
            eventoS.modulo = Evento.TipoModulo.PROC_CONSULTAS;
            procesamiento(consulta);                               //Se procesa la consulta 
            eventoS.tiempo = e.tiempo + tiempoProcesamiento;
            s.listaE.add(eventoS);
            numServOcupados++;
        } else if (!enCola) {                         //Si estaba siendo atendido y no habia nadie en cola
            numServOcupados--;
        }
        e.consulta.tiempoSalida = e.tiempo;
        e.consulta.estadistProc_Consultas.tiempoSalidaModulo = e.tiempo;
        e.consulta.estadistProc_Consultas.tiempoEnModulo = e.tiempo - e.consulta.estadistProc_Consultas.tiempoLlegadaModulo;
        e.consulta.enSistema = false;                //Se pone a la consulta como fuera del sistema
    }

    void procesamiento(Consulta c) {
        tiempoProcesamiento = 0.1;         //Tiempo de validacion lexica
        tiempoProcesamiento += generador.GenerarValUniforme(0, 1);   //Tiempo de validacion sintactica
        tiempoProcesamiento += generador.GenerarValUniforme(0, 2);    //Tiempo de validacion semantica
        tiempoProcesamiento += generador.GenerarValExponencial(0.7);   //Verificacion de permisos
        if (c.soloLectura == true) {                                   //Optimizacion de consulta
            tiempoProcesamiento += 0.1;
        } else {
            tiempoProcesamiento += 0.25;
        }
    }
}
