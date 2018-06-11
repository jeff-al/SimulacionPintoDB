public class Consulta {
        
    enum tipo {DDL, UPDATE, JOIN, SELECT}; 
    int id;
    tipo tipoSentencia;
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
