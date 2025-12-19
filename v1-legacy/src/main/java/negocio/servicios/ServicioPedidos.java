package negocio.servicios;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import negocio.patrones.state.EstadoCancelado;
import negocio.abstractas.EstadoPedido;
import negocio.abstractas.PizzaBuilder;
import negocio.modelo.Pedido;
import negocio.modelo.Pizza;
import negocio.patrones.builder.PizzaPersonalizadaBuilder;
import negocio.patrones.observer.PanelControlEmpleado;
import negocio.patrones.observer.PantallaEstadoCliente;
import negocio.patrones.state.EstadoEntregado;
import negocio.patrones.state.EstadoHorneando;
import negocio.patrones.state.EstadoPreparando;
import negocio.patrones.state.EstadoRecibido;
import negocio.patrones.state.EstadoTerminado;
import negocio.patrones.strategy.PrecioEstandar;
import negocio.patrones.strategy.PrecioPromocion;

/**
 * Capa de servicio para crear y gestionar pedidos.
 *
 * Híbrido:
 * - En ejecución: objetos en memoria (GestorPedidos).
 * - Persistencia: TXT simple (solo para recargar al reiniciar).
 */
public class ServicioPedidos {

    // ===== Persistencia simple (1 archivo) =====
    private static final String STORE_FILE = "pedidos_store.txt";

    private final GestorPedidos gestor = GestorPedidos.getInstancia();

    // Para evitar cargar el archivo varias veces si se crean varias instancias
    private static boolean pedidosCargadosDesdeArchivo = false;

    public ServicioPedidos() {
        if (!pedidosCargadosDesdeArchivo) {
            cargarDesdeArchivo();
            pedidosCargadosDesdeArchivo = true;
        }
    }

    /**
     * Crea un pedido a partir de datos simples (GUI) usando Builder + Strategy + Observer.
     * Luego persiste (reescribe) el TXT.
     *
     * ✅ Ahora separa ingredientes y condimentos (como tu UML y el enunciado).
     */
    public Pedido crearPedidoDesdeDatosSimples(String masa,
                                               String salsa,
                                               String queso,
                                               String tipoOrilla,
                                               List<String> ingredientes,
                                               List<String> condimentos) {

        PizzaBuilder builder = new PizzaPersonalizadaBuilder()
                .conMasa(masa)
                .conSalsa(salsa)
                .conQueso(queso)
                .conTipoOrilla(tipoOrilla);

        // ✅ Ingredientes
        if (ingredientes != null) {
            for (String ing : ingredientes) {
                if (ing != null && !ing.isBlank()) {
                    builder.agregarIngrediente(ing.trim());
                }
            }
        }

        // ✅ Condimentos
        if (condimentos != null) {
            for (String con : condimentos) {
                if (con != null && !con.isBlank()) {
                    builder.agregarCondimento(con.trim());
                }
            }
        }

        Pizza pizza = builder.build();

        // Registrar en memoria (objeto real)
        Pedido pedido = gestor.registrarPedido(pizza);

        // Observadores “de demo” (si los quieres conservar por patrón)
        pedido.agregarObservador(new PantallaEstadoCliente());
        pedido.agregarObservador(new PanelControlEmpleado());

        // Persistir snapshot
        reescribirArchivoDesdeMemoria();

        return pedido;
    }

    /**
     * Cambia la estrategia de precios (normal o promoción).
     */
    public void cambiarEstrategiaPromocion(boolean activa) {
        if (activa) {
            gestor.setEstrategia(new PrecioPromocion());
        } else {
            gestor.setEstrategia(new PrecioEstandar());
        }
    }

    /**
     * Lista pedidos en memoria (solo lectura).
     */
    public List<Pedido> listarPedidos() {
        return gestor.getPedidos();
    }

    /**
     * Buscar pedido por ID.
     */
    public Pedido buscarPedidoPorId(int id) {
        return gestor.buscarPorId(id);
    }

    /**
     * Avanza estado en memoria (objeto real) y persiste.
     */
    public void avanzarEstadoPedido(int idPedido) {
        gestor.avanzarEstado(idPedido);
        reescribirArchivoDesdeMemoria();
    }

    /**
     * Cancela pedido en memoria (objeto real) y persiste.
     */
    public void cancelarPedido(int idPedido) {
        gestor.cancelarPedido(idPedido);
        reescribirArchivoDesdeMemoria();
    }

    // =========================================================
    // ============  PERSISTENCIA TXT (cargar/guardar) =========
    // Formato por línea:
    // id;masa;salsa;queso;tipoOrilla;ingredientes;condimentos;total;estado
    //
    // ingredientes = ing1|ing2|ing3
    // condimentos  = con1|con2|con3
    // =========================================================

    private void cargarDesdeArchivo() {
        File file = new File(STORE_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                Pedido pedido = parsearPedido(linea);
                if (pedido != null) {
                    // Importante: lo registramos como existente (no recalcular total)
                    gestor.registrarPedidoExistente(pedido);
                }
            }

        } catch (IOException e) {
            System.err.println("Error leyendo " + STORE_FILE + ": " + e.getMessage());
        }
    }

    private Pedido parsearPedido(String linea) {
        // id;masa;salsa;queso;tipoOrilla;ingredientes;condimentos;total;estado
        String[] partes = linea.split(";");
        if (partes.length < 9) return null;

        try {
            int id = Integer.parseInt(partes[0].trim());

            String masa = partes[1].trim();
            String salsa = partes[2].trim();
            String queso = partes[3].trim();
            String tipoOrilla = partes[4].trim();

            String ingredientesStr = partes[5].trim();
            String condimentosStr = partes[6].trim();

            String[] ingArray = ingredientesStr.isEmpty() ? new String[0] : ingredientesStr.split("\\|");
            String[] conArray = condimentosStr.isEmpty() ? new String[0] : condimentosStr.split("\\|");

            double total = Double.parseDouble(partes[7].trim());
            String estadoStr = partes[8].trim();

            // Reconstruir Pizza
            Pizza pizza = new Pizza();
            pizza.setMasa(masa);
            pizza.setSalsa(salsa);
            pizza.setQueso(queso);
            pizza.setTipoOrilla(tipoOrilla);

            for (String ing : ingArray) {
                if (ing != null && !ing.isBlank()) {
                    pizza.getIngredientes().add(ing.trim());
                }
            }

            for (String con : conArray) {
                if (con != null && !con.isBlank()) {
                    pizza.getCondimentos().add(con.trim());
                }
            }

            // Reconstruir Pedido
            Pedido pedido = new Pedido(pizza, total);
            pedido.setId(id);
            pedido.setEstadoActual(estadoPorNombre(estadoStr));

            // (Opcional) observadores “de demo”
            pedido.agregarObservador(new PantallaEstadoCliente());
            pedido.agregarObservador(new PanelControlEmpleado());

            return pedido;

        } catch (Exception e) {
            System.err.println("Línea inválida en " + STORE_FILE + ": " + linea);
            return null;
        }
    }

    private void reescribirArchivoDesdeMemoria() {
        File file = new File(STORE_FILE);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (Pedido p : gestor.getPedidos()) {
                bw.write(serializarPedido(p));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error escribiendo " + STORE_FILE + ": " + e.getMessage());
        }
    }

    private String serializarPedido(Pedido pedido) {
        StringBuilder sb = new StringBuilder();

        sb.append(pedido.getId()).append(";");
        sb.append(nullToEmpty(pedido.getPizza().getMasa())).append(";");
        sb.append(nullToEmpty(pedido.getPizza().getSalsa())).append(";");
        sb.append(nullToEmpty(pedido.getPizza().getQueso())).append(";");
        sb.append(nullToEmpty(pedido.getPizza().getTipoOrilla())).append(";");

        // ingredientes separados por '|'
        List<String> ingredientes = pedido.getPizza().getIngredientes();
        for (int i = 0; i < ingredientes.size(); i++) {
            sb.append(ingredientes.get(i));
            if (i < ingredientes.size() - 1) sb.append("|");
        }
        sb.append(";");

        // ✅ condimentos separados por '|'
        List<String> condimentos = pedido.getPizza().getCondimentos();
        for (int i = 0; i < condimentos.size(); i++) {
            sb.append(condimentos.get(i));
            if (i < condimentos.size() - 1) sb.append("|");
        }
        sb.append(";");

        sb.append(pedido.getTotal()).append(";");
        sb.append(nullToEmpty(pedido.getNombreEstado()));

        return sb.toString();
    }

    private EstadoPedido estadoPorNombre(String estado) {
        if (estado == null) return new EstadoRecibido();

        switch (estado.trim().toUpperCase()) {
            case "RECIBIDO":   return new EstadoRecibido();
            case "PREPARANDO": return new EstadoPreparando();
            case "HORNEANDO":  return new EstadoHorneando();
            case "TERMINADO":  return new EstadoTerminado();
            case "ENTREGADO":  return new EstadoEntregado();
            case "CANCELADO": return new EstadoCancelado();
            default: return new EstadoRecibido();
        }
    }

    private String nullToEmpty(String s) {
        return (s == null) ? "" : s;
    }
}
