
import java.util.LinkedList;
import java.util.Queue;

public abstract class Modulo {

    GenValoresAleatorios generador;
    int numMaxServidores;
    int numServOcupados;
    Queue<Consulta> colaC = new LinkedList();

    void ProcesarEntrada(Simulacion s, Evento e) {

    }

    void ProcesarSalida(Simulacion s, Evento e) {

    }

    void ProcesarRetiro(Simulacion s, Evento e) {

    }
}
