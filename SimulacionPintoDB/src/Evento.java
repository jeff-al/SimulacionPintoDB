
public class Evento {

    enum TipoEvento {
        ENTRADA, SALIDA, RETIRO
    };

    enum TipoModulo {
        ADM_CONEXIONES, ADM_PROCESOS, PROC_CONSULTAS, TRANSACCIONES, EJEC_SENTENCIAS
    };

    TipoEvento tipoE;
    TipoModulo modulo;
    double tiempo;
    Consulta consulta;

    Evento(Consulta consulta) {
        this.consulta = consulta;
    }
}
