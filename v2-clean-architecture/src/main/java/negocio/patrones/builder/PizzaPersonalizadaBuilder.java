package negocio.patrones.builder;

import negocio.abstractas.PizzaBuilder;
import negocio.modelo.Pizza;

/**
 * Implementación concreta del Builder para pizzas personalizadas.
 * Aplica el patrón Builder descrito en el diagrama UML.
 */
public class PizzaPersonalizadaBuilder implements PizzaBuilder {

    private Pizza pizza;

    public PizzaPersonalizadaBuilder() {
        this.pizza = new Pizza();
    }

    @Override
    public PizzaBuilder conMasa(String masa) {
        pizza.setMasa(masa);
        return this;
    }

    @Override
    public PizzaBuilder conSalsa(String salsa) {
        pizza.setSalsa(salsa);
        return this;
    }

    @Override
    public PizzaBuilder conQueso(String queso) {
        pizza.setQueso(queso);
        return this;
    }

    @Override
    public PizzaBuilder agregarIngrediente(String ingrediente) {
        pizza.getIngredientes().add(ingrediente);
        return this;
    }

    @Override
    public PizzaBuilder agregarCondimento(String condimento) {
        pizza.getCondimentos().add(condimento);
        return this;
    }

    @Override
    public PizzaBuilder conTipoOrilla(String tipoOrilla) {
        pizza.setTipoOrilla(tipoOrilla);
        return this;
    }

    @Override
    public Pizza build() {
        // Podrías clonar si quisieras que el builder sea reusable;
        // por ahora devolvemos la instancia que se fue armando.
        return pizza;
    }
}
