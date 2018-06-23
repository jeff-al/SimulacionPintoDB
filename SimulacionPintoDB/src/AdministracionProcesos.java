
import java.util.Iterator;

public class AdministracionProcesos extends Modulo {

    double mediaHilo = 1;
    double varianzaHilo = 0.01;
    double tiempoHilo;

    AdministracionProcesos() {
        numMaxServidores = 1;
        numServOcupados = 0;
    }

    @Override
    void procesarEntrada(Simulacion s, Evento e) {
        if (s.moduloAC.numServOcupados < s.moduloAC.numMaxServidores) {   //Si el modulo Administracion de conexiones permite una nueva entrada 
            e.consulta.estadistAdm_Procesos.tiempoLlegadaModulo = e.tiempo;
            e.consulta.moduloActual = Evento.TipoModulo.ADM_PROCESOS;
            if (numServOcupados == numMaxServidores) { //Si ya hay una consulta siendo procesada
                colaC.add(e.consulta);
                s.estadisticasT.promedioColaAP += colaC.size();
            } else {                                   //Si no hay una consulta siendo atendido
                e.consulta.estadistAdm_Procesos.tiempoSalidaCola = 0;
                Evento evento = new Evento(e.consulta);
                evento.tipoE = Evento.TipoEvento.SALIDA;
                evento.modulo = Evento.TipoModulo.ADM_PROCESOS;
                tiempoHilo = generador.GenerarValNormal(varianzaHilo, mediaHilo);
                evento.tiempo = e.tiempo + tiempoHilo;
                s.listaE.add(evento);
                numServOcupados = 1;
            }
            s.moduloAC.numServOcupados++;
        } else {
            s.estadisticasT.conexionesDescartadas++;   //Se añade una conexion decartada a la estadistica
            e.consulta.enSistema = false;              //Se saca la colsulta del sistema
        }

    }

    @Override
    void procesarSalida(Simulacion s, Evento e) {
        e.consulta.estadistAdm_Procesos.tiempoSalidaModulo = e.tiempo;
        e.consulta.estadistAdm_Procesos.tiempoEnModulo = e.tiempo - e.consulta.estadistAdm_Procesos.tiempoLlegadaModulo;
        Evento evento = new Evento(e.consulta);
        evento.tipoE = Evento.TipoEvento.ENTRADA;
        evento.modulo = Evento.TipoModulo.PROC_CONSULTAS;
        evento.tiempo = e.tiempo;
        numServOcupados = 0;
        s.listaE.add(evento);

        if (!colaC.isEmpty()) {   //Si despues de una salida hay algo en cola
            Consulta consulta = colaC.remove();
            consulta.estadistAdm_Procesos.tiempoSalidaCola = e.tiempo - consulta.estadistAdm_Procesos.tiempoLlegadaModulo;
            s.estadisticasT.promedioColaAP += colaC.size();
            Evento eventoS = new Evento(consulta);
            eventoS.tipoE = Evento.TipoEvento.SALIDA;
            eventoS.modulo = Evento.TipoModulo.ADM_PROCESOS;
            tiempoHilo = generador.GenerarValNormal(varianzaHilo, mediaHilo);
            eventoS.tiempo = e.tiempo + tiempoHilo;
            s.listaE.add(eventoS);
            numServOcupados = 1;
        }

    }

    @Override
    void procesarRetiro(Simulacion s, Evento e) {
        boolean enCola = false;                    //Booleano para saber si se sacó o no de la cola
        Iterator<Consulta> it = colaC.iterator();
        while (it.hasNext()) {                      //Buscamos si la consulta está en la cola
            Consulta c = it.next();
            if (c == e.consulta) {                    //Si esta la sacamos y le asignamos los tiempos correspondientes a las salidas para estadisticas
                it.remove();
                s.estadisticasT.promedioColaAP += colaC.size();
                e.consulta.tiempoEnsistema = e.tiempo - e.consulta.tiempoLlegada;
                e.consulta.estadistAdm_Procesos.tiempoSalidaCola = e.tiempo - e.consulta.estadistAdm_Procesos.tiempoLlegadaModulo;
                enCola = true;
            }
        }

        if (!enCola && !colaC.isEmpty()) {       //Si la consulta estaba siendo atendida y hay consultas en cola
            Consulta consulta = colaC.remove();
            consulta.estadistAdm_Procesos.tiempoSalidaCola = e.tiempo - consulta.estadistAdm_Procesos.tiempoLlegadaModulo;
            s.estadisticasT.promedioColaAP += colaC.size();
            Evento eventoS = new Evento(consulta);
            eventoS.tipoE = Evento.TipoEvento.SALIDA;
            eventoS.modulo = Evento.TipoModulo.ADM_PROCESOS;
            tiempoHilo = generador.GenerarValNormal(varianzaHilo, mediaHilo);
            eventoS.tiempo = e.tiempo + tiempoHilo;
            s.listaE.add(eventoS);
        } else if (!enCola) {                  //Si estaba siendo atendida y no habia consultas en cola
            numServOcupados = 0;
        }
        e.consulta.tiempoSalida = e.tiempo;
        e.consulta.estadistAdm_Procesos.tiempoSalidaModulo = e.tiempo;
        e.consulta.estadistAdm_Procesos.tiempoEnModulo = e.tiempo - e.consulta.estadistAdm_Procesos.tiempoLlegadaModulo;
        e.consulta.enSistema = false;        //Se pone a la consulta como fuera del sistema
    }
}
