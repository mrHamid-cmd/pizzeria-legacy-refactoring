/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package negocio.patrones.strategy;

import negocio.patrones.strategy.PrecioEstandar;
import negocio.abstractas.EstrategiaPrecio;
import negocio.modelo.Pizza;

/**
 *
 * @author ARTX
 */
public class PrecioPromocion implements EstrategiaPrecio {

    @Override
    public double calcularTotal(Pizza pizza) {
        EstrategiaPrecio base = new PrecioEstandar();
        double totalBase = base.calcularTotal(pizza);

        // ejemplo: 10% de descuento
        return totalBase * 0.90;
    }
}