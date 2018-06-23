
import java.util.LinkedList;
import java.util.Queue;
import java.util.Iterator;
import java.util.List;

public abstract class Modulo {

    GenValoresAleatorios generador = new GenValoresAleatorios();
    int numMaxServidores = 0;
    int numServOcupados = 0;
    Queue<Consulta> colaC;
    Modulo(){
        numMaxServidores = 0;
        numServOcupados = 0;
        colaC = new LinkedList();
    }
    
    List<Consulta> Atendidos = new LinkedList();

    void procesarEntrada(Simulacion s, Evento e) {

    }

    void procesarSalida(Simulacion s, Evento e) {

    }

    void procesarRetiro(Simulacion s, Evento e) {
      
    }

    void imprimirCola() {
        Iterator<Consulta> it = colaC.iterator();
        System.out.print("C: ");
        while (it.hasNext()) {
            Consulta c = it.next();
            System.out.print(c.id + ", ");
        }
        System.out.print("\n");
    }
    
    void imprimirAtend() {
        Iterator<Consulta> it = Atendidos.iterator();
        System.out.print("Atendidos: ");
        while (it.hasNext()) {
            Consulta c = it.next();
            System.out.print(c.id + ", ");
        }
    }
    
    int Colasize() {
        return 0;
    }

}
