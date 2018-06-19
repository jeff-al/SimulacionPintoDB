
import java.util.*;

public class Simulacion {

    int ids = 0;
    double tiempoMaximo;
    int conexionesMaximas;
    int procesosDisponibles;
    int maximoConsultas;
    int sentenciasMaximas;
    List<Consulta> listaC = new ArrayList();
    List<Evento> listaE = new ArrayList();

    int n; //ProcesosDisponibles
    int p; //MaximoConsultas
    
    Modulo moduloAC = new AdministracionConexiones();
    Modulo moduloAP = new AdministracionProcesos();
    Modulo moduloPC = new ProcesamientoConsulta(n);
    Modulo moduloT = new Transacciones(p);
    Modulo moduloES = new EjecucionDeSentencias();

    private double reloj;

    void procesarEvento() {

    }

    void agregarEvento() {

    }

    void crearEvento() {
        double random = Math.random();
        Consulta consulta = new Consulta();
        consulta.id = ids++;
        if (random < 30) {
            consulta.tipoSentencia = consulta.tipoSentencia.SELECT;
            consulta.soloLectura = true;
        } else if (random < 55) {
            consulta.tipoSentencia = consulta.tipoSentencia.UPDATE;
            consulta.soloLectura = false;
        } else if (random < 90) {
            consulta.tipoSentencia = consulta.tipoSentencia.JOIN;
            consulta.soloLectura = true;
        } else {
            consulta.tipoSentencia = consulta.tipoSentencia.DDL;
            consulta.soloLectura = false;
        }
        consulta.tiempoLlegada = reloj;
        consulta.estadistAdm_Conexiones.tiempoLlegadaModulo = reloj;
        consulta.estadistAdm_Conexiones.tiempoSalidaModulo = reloj;
        consulta.estadistAdm_Conexiones.tiempoSalidaCola = 0;
        consulta.estadistAdm_Conexiones.tiempoEnModulo = 0.1; // Asumiendo que dura algo en el modulo
        Evento evento = new Evento(consulta);
        evento.tipoE = evento.tipoE.ENTRADA;
        evento.modulo = evento.modulo.ADM_PROCESOS;
        evento.consulta = consulta;
        evento.tiempo = reloj;
        listaE.add(evento);
        listaC.add(consulta);
    }
}

    //Falta Maim
