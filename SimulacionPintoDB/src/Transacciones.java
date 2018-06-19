
import java.util.Comparator;
import java.util.PriorityQueue;

public class Transacciones extends Modulo {

    static class PQsort implements Comparator<Consulta> {

        @Override
        public int compare(Consulta one, Consulta two) {
            int val1 = 0;
            int val2 = 0;
            switch (one.tipoSentencia) {
                case UPDATE:
                    val1 = 1;
                    break;
                case JOIN:
                    val1 = 2;
                    break;
                case SELECT:
                    val1 = 3;
                    break;
            }
            switch (two.tipoSentencia) {
                case UPDATE:
                    val1 = 1;
                    break;
                case JOIN:
                    val1 = 2;
                    break;
                case SELECT:
                    val1 = 3;
                    break;
            }
            if (val1 == val2) {
                return 0;
            } else if (val1 < val2) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    Transacciones(int numConsultas) {
        numMaxServidores = numConsultas;
    }

    double tiempoTransaccion;
    double duracionBloque;
    PriorityQueue<Consulta> PQ = new PriorityQueue(new PQsort());
//falta agregar el comparador de la prioridad

}
