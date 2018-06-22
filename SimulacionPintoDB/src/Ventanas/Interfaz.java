/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ventanas;

/**
 *
 * @author B61144  extends Thread
 */
public class Interfaz{
    public static Inicio inicio;
    public static Datos datos;
    public static Corriendo corr;
    public static double tM;
    public static double t;
    public static int iter;
    public static int k;
    public static int p;
    public static int m;
    public static int n;
    public static boolean seguir;
    public static boolean v3;
    public static boolean modoLento;

    public Interfaz() {
        modoLento = false;
        seguir = true;
        v3 = false;
        inicio = new Inicio();
        datos = new Datos();
        corr = new Corriendo();
        tM = 50.0;
        t = 15.0;
        iter = 1;
        k = 1;
        p = 1;
        m = 1;
        n = 1;
    }
    
    //@Override
    public void run() {
        inicio.setVisible(true);
    }
}
