/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package negocio.abstractas;

import negocio.modelo.Pizza;

/**
 * Interfaz Builder para la construcción de pizzas personalizadas.
 * Coincide con el diseño del diagrama UML:
 *  - conMasa
 *  - conSalsa
 *  - conQueso
 *  - agregarIngrediente
 *  - agregarCondimento
 *  - conTipoOrilla
 *  - build
 */
public interface PizzaBuilder {

    PizzaBuilder conMasa(String masa);

    PizzaBuilder conSalsa(String salsa);

    PizzaBuilder conQueso(String queso);

    PizzaBuilder agregarIngrediente(String ingrediente);

    PizzaBuilder agregarCondimento(String condimento);

    PizzaBuilder conTipoOrilla(String tipoOrilla);

    Pizza build();
}
