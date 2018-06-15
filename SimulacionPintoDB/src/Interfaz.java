
public interface Interfaz {

    boolean modoLento = false;
    int numeroVeces = 5;

    int insertarMaxConexiones(int num);

    double insertarTiempoMaximo(double num);

    int insertarProcDisponibles(int num);

    int insertarMaxConsultas(int num);

    int insertarMaxSentencias(int num);

    void mostrarCola(Modulo modulo);

    void mostrarEstTotales(Simulacion simulacion);

    void mostrarDatosIniciales();

    void mostrarDatosIteracion();
}
