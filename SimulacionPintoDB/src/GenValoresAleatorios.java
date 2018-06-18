
public class GenValoresAleatorios {

    double result;

    double GenerarValNormal(double varianza, double media) {
        double sumRandom = 0;
        for (int i = 0; i < 12; i++) {
            sumRandom += Math.random();
        }
        double Z = sumRandom - 6;
        result = media + (Math.sqrt(varianza) * Z);
        return result;
    }

    double GenerarValExponencial(double media) {
        double random = Math.random();
        result = ((-1) / media) * Math.log(random);
        return result;
    }

    double GenerarValUniforme(double a, double b) {
        double random = Math.random();
        result = a + ((b - a) * random);
        return result;
    }
}
