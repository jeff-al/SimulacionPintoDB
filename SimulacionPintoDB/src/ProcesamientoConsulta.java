
public class ProcesamientoConsulta extends Modulo {

    ProcesamientoConsulta(int NumeroServ) {
        numMaxServidores = NumeroServ;
    }

    @Override
    void procesarEntrada(Simulacion s, Evento e) {
        e.consulta.estadistProc_Consultas.tiempoLlegadaModulo = e.tiempo;
        if (numServOcupados == numMaxServidores) { //Si ya hay una consulta siendo procesada
            colaC.add(e.consulta);
        } else {                       //Si no hay una consulta en cola
            double tiempoProc = procesamiento(e.consulta);
            e.consulta.estadistProc_Consultas.tiempoSalidaCola = 0;
            e.consulta.estadistProc_Consultas.tiempoSalidaModulo = e.tiempo + tiempoProc;
            e.consulta.estadistProc_Consultas.tiempoEnModulo = tiempoProc;
            Evento evento = new Evento(e.consulta);
            evento.tipoE = e.tipoE.SALIDA;
            evento.modulo = e.modulo.PROC_CONSULTAS;
            evento.tiempo = e.tiempo + tiempoProc;
            s.listaE.add(evento);
            numServOcupados++;
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
            evento.modulo = evento.modulo.PROC_CONSULTAS;
            evento.tiempo = e.tiempo;
            numServOcupados--;
        } else {                                     //Si no hace timeout
            Evento evento = new Evento(e.consulta);
            evento.tipoE = e.tipoE.ENTRADA;
            evento.modulo = e.modulo.TRANSACCIONES;
            evento.tiempo = e.tiempo;
            numServOcupados--;
            s.listaE.add(evento);
        }
        if (!colaC.isEmpty()) {   //Si despues de una salida hay algo en cola
            Consulta consulta = colaC.remove();
            double tiempoProc = procesamiento(e.consulta);
            consulta.estadistProc_Consultas.tiempoSalidaCola = e.tiempo - consulta.estadistProc_Consultas.tiempoLlegadaModulo;
            consulta.estadistProc_Consultas.tiempoSalidaModulo = tiempoProc + e.tiempo;
            consulta.estadistProc_Consultas.tiempoEnModulo = (tiempoProc + e.tiempo) - consulta.estadistProc_Consultas.tiempoLlegadaModulo;
            Evento eventoS = new Evento(consulta);
            eventoS.tipoE = e.tipoE.SALIDA;
            eventoS.modulo = e.modulo.PROC_CONSULTAS;
            eventoS.tiempo = e.tiempo + tiempoProc;
            s.listaE.add(eventoS);
            numServOcupados++;
        }
    }

    double procesamiento(Consulta c) {
        double result = 0.1;
        result += generador.GenerarValUniforme(0, 1);
        result += generador.GenerarValUniforme(0, 2);
        result += generador.GenerarValExponencial(0.7);
        if (c.soloLectura == true) {
            result += 0.1;
        } else {
            result += 0.25;
        }
        return result;
    }
}
