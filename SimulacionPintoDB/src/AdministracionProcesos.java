
import java.util.Iterator;

public class AdministracionProcesos extends Modulo {

    double mediaHilo = 1;
    double varianzaHilo = 0.01;

    AdministracionProcesos() {
        numMaxServidores = 1;
        numServOcupados = 0;
    }

    @Override
    void procesarEntrada(Simulacion s, Evento e) {
        if (s.moduloAC.numServOcupados < s.moduloAC.numMaxServidores) {
            e.consulta.estadistAdm_Procesos.tiempoLlegadaModulo = e.tiempo;
            e.consulta.moduloActual = Evento.TipoModulo.ADM_PROCESOS;
            if (numServOcupados == numMaxServidores) { //Si ya hay una consulta siendo procesada
                colaC.add(e.consulta);
                s.estadisticasT.promedioColaAP += colaC.size();
            } else {                                   //Si no hay una consulta en cola
                double tiempoHilo = generador.GenerarValNormal(varianzaHilo, mediaHilo);
                e.consulta.estadistAdm_Procesos.tiempoSalidaCola = 0;
                Evento evento = new Evento(e.consulta);
                evento.tipoE = e.tipoE.SALIDA;
                evento.modulo = e.modulo.ADM_PROCESOS;
                evento.tiempo = e.tiempo + tiempoHilo;
                s.listaE.add(evento);
                numServOcupados = 1;
                Atendidos.add(e.consulta);
            }
            s.moduloAC.numServOcupados++;
        } else {
            s.estadisticasT.conexionesDescartadas++;
            e.consulta.enSistema = false;
        }

    }

    @Override
    void procesarSalida(Simulacion s, Evento e) {
        e.consulta.estadistAdm_Procesos.tiempoSalidaModulo = e.tiempo;
        e.consulta.estadistAdm_Procesos.tiempoEnModulo = e.tiempo - e.consulta.estadistAdm_Procesos.tiempoLlegadaModulo;
        Evento evento = new Evento(e.consulta);
        evento.tipoE = e.tipoE.ENTRADA;
        evento.modulo = e.modulo.PROC_CONSULTAS;
        evento.tiempo = e.tiempo;
        numServOcupados = 0;
        s.listaE.add(evento);
        Atendidos.remove(e.consulta);

        if (!colaC.isEmpty()) {   //Si despues de una salida hay algo en cola
            Consulta consulta = colaC.remove();
            s.estadisticasT.promedioColaAP += colaC.size();
            double tiempoHilo = generador.GenerarValNormal(varianzaHilo, mediaHilo);
            consulta.estadistAdm_Procesos.tiempoSalidaCola = e.tiempo - consulta.estadistAdm_Procesos.tiempoLlegadaModulo;
            Evento eventoS = new Evento(consulta);
            eventoS.tipoE = e.tipoE.SALIDA;
            eventoS.modulo = e.modulo.ADM_PROCESOS;
            eventoS.tiempo = e.tiempo + tiempoHilo;
            s.listaE.add(eventoS);
            numServOcupados = 1;
            Atendidos.add(eventoS.consulta);
        }
    }

    @Override
    void procesarRetiro(Simulacion s, Evento e) {
        boolean enCola = false;
        Iterator<Consulta> it = colaC.iterator();
        while (it.hasNext()) {
            Consulta c = it.next();
            if (c == e.consulta) {
                it.remove();
                s.estadisticasT.promedioColaAP += colaC.size();
                e.consulta.tiempoEnsistema = e.tiempo - e.consulta.tiempoLlegada;
                e.consulta.tiempoSalida = e.tiempo;
                e.consulta.estadistAdm_Procesos.tiempoSalidaCola = e.tiempo - e.consulta.estadistAdm_Procesos.tiempoLlegadaModulo;
                enCola = true;
            }
        }
        if (!enCola && !colaC.isEmpty()) {
            Consulta consulta = colaC.remove();
            s.estadisticasT.promedioColaAP += colaC.size();
            double tiempoHilo = generador.GenerarValNormal(varianzaHilo, mediaHilo);
            consulta.estadistAdm_Procesos.tiempoSalidaCola = e.tiempo - consulta.estadistAdm_Procesos.tiempoLlegadaModulo;
            Evento eventoS = new Evento(consulta);
            eventoS.tipoE = e.tipoE.SALIDA;
            eventoS.modulo = e.modulo.ADM_PROCESOS;
            eventoS.tiempo = e.tiempo + tiempoHilo;
            s.listaE.add(eventoS);
            numServOcupados = 1;
            Atendidos.remove(e.consulta);
        } 
        if (!enCola) {
            numServOcupados = 0;
            Atendidos.remove(e.consulta);
        }
        e.consulta.enSistema = false;
    }
}
