/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package negocio.patrones.observer;

import negocio.abstractas.ObservadorPedido;
import negocio.modelo.Pedido;

/**
 * Observador que representa la "pantalla de estado del cliente"
 * según el diseño UML. En esta implementación básica, simplemente
 * muestra por consola los cambios de estado del pedido.
 *
 * Más adelante se puede adaptar para delegar a un controlador JavaFX.
 */
public class PantallaEstadoCliente implements ObservadorPedido {

    @Override
    public void actualizar(Pedido p) {
        if (p == null) {
            return;
        }
        System.out.println(
            "[PantallaEstadoCliente] Pedido #" + p.getId() +
            " ahora está en estado: " + p.getNombreEstado()
        );
    }
}
