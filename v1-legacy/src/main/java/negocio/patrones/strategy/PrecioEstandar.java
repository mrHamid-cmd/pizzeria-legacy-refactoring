/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package negocio.patrones.strategy;

import negocio.abstractas.EstrategiaPrecio;
import negocio.modelo.Pizza;

/**
 *
 * @author ARTX
 */
public class PrecioEstandar implements EstrategiaPrecio {

    @Override
    public double calcularTotal(Pizza pizza) {
        double total = 100.0; // precio base de la pizza

        // reglas muy sencillas a modo de ejemplo
        if (pizza.getMasa() != null && pizza.getMasa().equalsIgnoreCase("rellena")) {
            total += 20.0;
        }

        total += pizza.getIngredientes().size() * 15.0;

        return total;
    }
}