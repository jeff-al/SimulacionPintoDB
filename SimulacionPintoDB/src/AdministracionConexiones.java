
public class AdministracionConexiones extends Modulo {

    double tiempoCarga;

    AdministracionConexiones(int conexionesMax) {
        numMaxServidores = conexionesMax;
    }

    @Override
    void procesarEntrada(Simulacion s, Evento e) {
        e.consulta.estadistAdm_Conexiones.tiempoLlegadaModulo = e.tiempo;
        e.consulta.moduloActual = Evento.TipoModulo.ADM_CONEXIONES;
        e.consulta.estadistAdm_Conexiones.tiempoSalidaCola = 0;
        Evento evento = new Evento(e.consulta);
        evento.tipoE = Evento.TipoEvento.SALIDA;
        evento.modulo = Evento.TipoModulo.ADM_CONEXIONES;
        tiempoCarga = e.consulta.bloquesCargados / 64;
        evento.tiempo = e.tiempo + tiempoCarga;
        s.listaE.add(evento);
        Atendidos.add(e.consulta);
    }

    @Override
    void procesarSalida(Simulacion s, Evento e) {
        e.consulta.estadistAdm_Conexiones.tiempoSalidaModulo = e.tiempo;
        e.consulta.estadistAdm_Conexiones.tiempoEnModulo = e.tiempo - e.consulta.estadistAdm_Conexiones.tiempoLlegadaModulo;
        e.consulta.tiempoSalida = e.tiempo;
        numServOcupados--;
        e.consulta.enSistema = false;
    }
}
