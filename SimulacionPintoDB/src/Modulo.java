
import java.util.LinkedList;
import java.util.Queue;

public abstract class Modulo {

    GenValoresAleatorios generador = new GenValoresAleatorios();
    int numMaxServidores = 0;
    int numServOcupados = 0;
    Queue<Consulta> colaC = new LinkedList();

    void procesarEntrada(Simulacion s, Evento e) {

    }

    void procesarSalida(Simulacion s, Evento e) {

    }

    void procesarRetiro(Simulacion s, Evento e) {
        
    }
}
