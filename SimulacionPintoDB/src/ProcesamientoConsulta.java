
import java.util.Iterator;

public class ProcesamientoConsulta extends Modulo {

    ProcesamientoConsulta(int NumeroServ) {
        numMaxServidores = NumeroServ;
    }

    @Override
    void procesarEntrada(Simulacion s, Evento e) {
        e.consulta.estadistProc_Consultas.tiempoLlegadaModulo = e.tiempo;
        e.consulta.moduloActual = Evento.TipoModulo.PROC_CONSULTAS;
        if (numServOcupados == numMaxServidores) { //Si ya hay una consulta siendo procesada
            colaC.add(e.consulta);
        } else {                       //Si no hay una consulta en cola
            double tiempoProc = procesamiento(e.consulta);
            e.consulta.estadistProc_Consultas.tiempoSalidaCola = 0;
            Evento evento = new Evento(e.consulta);
            evento.tipoE = e.tipoE.SALIDA;
            evento.modulo = e.modulo.PROC_CONSULTAS;
            evento.tiempo = e.tiempo + tiempoProc;
            s.listaE.add(evento);
            numServOcupados++;
            Atendidos.add(e.consulta);
        }

    }

    @Override
    void procesarSalida(Simulacion s, Evento e) {

        e.consulta.estadistProc_Consultas.tiempoSalidaModulo = e.tiempo;
        e.consulta.estadistProc_Consultas.tiempoEnModulo = e.tiempo - e.consulta.estadistProc_Consultas.tiempoLlegadaModulo;
        Evento evento = new Evento(e.consulta);
        evento.tipoE = e.tipoE.ENTRADA;
        evento.modulo = e.modulo.TRANSACCIONES;
        evento.tiempo = e.tiempo;
        numServOcupados--;
        s.listaE.add(evento);
        Atendidos.remove(e.consulta);
        if (!colaC.isEmpty()) {   //Si despues de una salida hay algo en cola
            Consulta consulta = colaC.remove();
            double tiempoProc = procesamiento(e.consulta);
            consulta.estadistProc_Consultas.tiempoSalidaCola = e.tiempo - consulta.estadistProc_Consultas.tiempoLlegadaModulo;
            Evento eventoS = new Evento(consulta);
            eventoS.tipoE = e.tipoE.SALIDA;
            eventoS.modulo = e.modulo.PROC_CONSULTAS;
            eventoS.tiempo = e.tiempo + tiempoProc;
            s.listaE.add(eventoS);
            numServOcupados++;
            
            Atendidos.add(eventoS.consulta);
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

    @Override
    void procesarRetiro(Simulacion s, Evento e) {
        boolean enCola = false;
            Iterator<Consulta> it = s.moduloPC.colaC.iterator();
            while (it.hasNext()) {
                Consulta c = it.next();
                if (c == e.consulta) { //Lo busca en la cola
                    it.remove();
                    e.consulta.tiempoEnsistema = e.tiempo - e.consulta.tiempoLlegada;
                    e.consulta.tiempoSalida = e.tiempo;
                    e.consulta.estadistProc_Consultas.tiempoSalidaCola = e.tiempo - e.consulta.estadistProc_Consultas.tiempoLlegadaModulo;
                    enCola = true;
                }
            }
            if (!enCola && !colaC.isEmpty()) {
                Consulta consulta = colaC.remove();
                double tiempoProc = procesamiento(e.consulta);
                consulta.estadistProc_Consultas.tiempoSalidaCola = e.tiempo - consulta.estadistProc_Consultas.tiempoLlegadaModulo;
                Evento eventoS = new Evento(consulta);
                eventoS.tipoE = e.tipoE.SALIDA;
                eventoS.modulo = e.modulo.PROC_CONSULTAS;
                eventoS.tiempo = e.tiempo + tiempoProc;
                s.listaE.add(eventoS);
                Atendidos.remove(e.consulta);
            }else if(!enCola){
                numServOcupados--;
                Atendidos.remove(e.consulta);
            }
            e.consulta.enSistema = false;
    }
}
