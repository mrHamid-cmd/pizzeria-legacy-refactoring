/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package negocio.abstractas;

import negocio.modelo.Pizza;

/**
 *
 * @author ARTX
 */
public interface EstrategiaPrecio {
    double calcularTotal(Pizza pizza);
}