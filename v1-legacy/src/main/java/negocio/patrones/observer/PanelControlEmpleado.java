/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package negocio.patrones.observer;

import negocio.abstractas.ObservadorPedido;
import negocio.modelo.Pedido;

/**
 * Observador que representa el "panel de control del empleado"
 * de acuerdo al diagrama UML. Aquí se notifica cuando un pedido
 * cambia de estado.
 *
 * En una versión más avanzada, podría actualizar una tabla/lista
 * en la GUI mediante un controlador.
 */
public class PanelControlEmpleado implements ObservadorPedido {

    @Override
    public void actualizar(Pedido p) {
        if (p == null) {
            return;
        }
        System.out.println(
            "[PanelControlEmpleado] Actualización de pedido #" + p.getId() +
            " -> estado: " + p.getNombreEstado()
        );
    }
}
