/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package negocio.patrones.state;

/**
 *
 * @author fende
 */

import negocio.abstractas.EstadoPedido;
import negocio.modelo.Pedido;

/**
 * Estado CANCELADO.
 * Estado final: no permite avanzar.
 */
public class EstadoCancelado implements EstadoPedido {

    @Override
    public void avanzar(Pedido pedido) {
        // Un pedido cancelado NO avanza a ningún otro estado
    }

    @Override
    public String getNombreEstado() {
        return "CANCELADO";
    }
}
