
public class Consulta {

    enum TipoSentencia {
        DDL, UPDATE, JOIN, SELECT
    };
    int id;
    TipoSentencia tipoSentencia;
    boolean soloLectura;
    int bloquesCargados;
    double tiempoLlegada;
    double tiempoSalida;
    double tiempoEnsistema;
    EstadisticasConsulta estadistAdm_Conexiones;
    EstadisticasConsulta estadistAdm_Procesos;
    EstadisticasConsulta estadistProc_Consultas;
    EstadisticasConsulta estadistTransacciones;
    EstadisticasConsulta estadistEjec_Sentencias;
}
