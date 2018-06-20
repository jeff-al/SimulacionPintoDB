
public class AdministracionProcesos extends Modulo {

    double mediaHilo = 1;
    double varianzaHilo = 0.01;

    AdministracionProcesos() {
        numMaxServidores = 1;
        numServOcupados = 0;
    }

    @Override
    void procesarEntrada(Simulacion s, Evento e) {
        e.consulta.estadistAdm_Procesos.tiempoLlegadaModulo = e.tiempo;
        if (numServOcupados == numMaxServidores) { //Si ya hay una consulta siendo procesada
            colaC.add(e.consulta);
        } else {                                   //Si no hay una consulta en cola
            double tiempoHilo = generador.GenerarValNormal(varianzaHilo, mediaHilo);
            e.consulta.estadistAdm_Procesos.tiempoSalidaCola = 0;
            e.consulta.estadistAdm_Procesos.tiempoSalidaModulo = e.tiempo + tiempoHilo;
            e.consulta.estadistAdm_Procesos.tiempoEnModulo = tiempoHilo;
            Evento evento = new Evento(e.consulta);
            evento.tipoE = e.tipoE.SALIDA;
            evento.modulo = e.modulo.ADM_PROCESOS;
            evento.tiempo = e.tiempo + tiempoHilo;
            s.listaE.add(evento);
            numServOcupados = 1;

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
            evento.modulo = evento.modulo.ADM_PROCESOS;
            evento.tiempo = e.tiempo;
            numServOcupados = 0;
            s.moduloAC.numServOcupados--;
        } else {                                     //Si no hace timeout
            Evento evento = new Evento(e.consulta);
            evento.tipoE = e.tipoE.ENTRADA;
            evento.modulo = e.modulo.PROC_CONSULTAS;
            evento.tiempo = e.tiempo;
            numServOcupados = 0;
            s.listaE.add(evento);
        }
        if (!colaC.isEmpty()) {   //Si despues de una salida hay algo en cola
            Consulta consulta = colaC.remove();
            double tiempoHilo = generador.GenerarValNormal(varianzaHilo, mediaHilo);
            consulta.estadistAdm_Procesos.tiempoSalidaCola = e.tiempo - consulta.estadistAdm_Procesos.tiempoLlegadaModulo;
            consulta.estadistAdm_Procesos.tiempoSalidaModulo = tiempoHilo + e.tiempo;
            consulta.estadistAdm_Procesos.tiempoEnModulo = (tiempoHilo + e.tiempo) - consulta.estadistAdm_Procesos.tiempoLlegadaModulo;
            Evento eventoS = new Evento(consulta);
            eventoS.tipoE = e.tipoE.SALIDA;
            eventoS.modulo = e.modulo.ADM_PROCESOS;
            eventoS.tiempo = e.tiempo + tiempoHilo;
            s.listaE.add(eventoS);
            numServOcupados = 1;
        }
    }
}
