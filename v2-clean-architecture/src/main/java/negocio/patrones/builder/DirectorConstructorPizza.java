/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package negocio.patrones.builder;

import negocio.abstractas.PizzaBuilder;
import negocio.modelo.Pizza;

/**
 * Director del patrón Builder.
 * Se encarga de orquestar los pasos de construcción de una pizza
 * usando un PizzaBuilder.
 */
public class DirectorConstructorPizza {

    private PizzaBuilder builder;

    public DirectorConstructorPizza(PizzaBuilder builder) {
        this.builder = builder;
    }

    public void setBuilder(PizzaBuilder builder) {
        this.builder = builder;
    }

    /**
     * Construye una pizza "básica" con valores por defecto.
     * Corresponde al método construirPizzaBasica() del diagrama UML.
     */
    public Pizza construirPizzaBasica() {
        return builder
                .conMasa("Tradicional")
                .conSalsa("Tomate")
                .conQueso("Mozzarella")
                .conTipoOrilla("Normal")
                .build();
    }

    /**
     * Si quisieras, puedes añadir otros métodos para construir
     * variaciones predefinidas (por ejemplo: vegetariana, de carnes, etc.).
     */
}
