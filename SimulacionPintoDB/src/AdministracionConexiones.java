
public class AdministracionConexiones extends Modulo {

    AdministracionConexiones(int conexionesMax) {
        numMaxServidores = conexionesMax;
    }

    @Override
    void procesarEntrada(Simulacion s, Evento e) {
        e.consulta.estadistAdm_Conexiones.tiempoLlegadaModulo = e.tiempo;                       //Si no hay una consulta en cola
        double tiempoCarga = e.consulta.bloquesCargados / 64;
        e.consulta.estadistAdm_Conexiones.tiempoSalidaCola = 0;
        e.consulta.estadistAdm_Conexiones.tiempoSalidaModulo = e.tiempo + tiempoCarga;
        e.consulta.estadistAdm_Conexiones.tiempoEnModulo = tiempoCarga;
        Evento evento = new Evento(e.consulta);
        evento.tipoE = e.tipoE.SALIDA;
        evento.modulo = e.modulo.ADM_CONEXIONES;
        evento.tiempo = e.tiempo + tiempoCarga;
        s.listaE.add(evento);
    }

    @Override
    void procesarSalida(Simulacion s, Evento e) {
        numServOcupados--;
    }
}
