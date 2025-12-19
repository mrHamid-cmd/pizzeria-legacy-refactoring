package negocio.servicios;

import java.io.IOException;
import java.util.List;

// Importaciones del modelo y abstractas
import negocio.abstractas.PizzaBuilder;
import negocio.modelo.Pedido;
import negocio.modelo.Pizza;

// Importaciones de patrones
import negocio.patrones.builder.PizzaPersonalizadaBuilder;
import negocio.patrones.observer.PanelControlEmpleado;
import negocio.patrones.observer.PantallaEstadoCliente;
import negocio.patrones.strategy.PrecioEstandar;
import negocio.patrones.strategy.PrecioPromocion;

// IMPORTANTE: Importamos los repositorios del paquete persistencia
import persistencia.RepositorioPedidosTxt;
import persistencia.RepositorioTickets;

/**
 * Capa de servicio para crear y gestionar pedidos.
 * Actúa como fachada (Facade) entre la Vista (Controladores) y:
 * 1. El Modelo de Dominio (GestorPedidos, Builders, State, etc.)
 * 2. La Capa de Persistencia (Repositorios).
 */
public class ServicioPedidos {

    // Singleton del gestor (memoria)
    private final GestorPedidos gestor = GestorPedidos.getInstancia();
    
    // Repositorios (Capa de datos)
    private final RepositorioPedidosTxt repoPedidos;
    private final RepositorioTickets repoTickets;

    // Bandera estática para cargar el archivo principal solo una vez por ejecución
    private static boolean pedidosCargadosDesdeArchivo = false;

    public ServicioPedidos() {
        // Inicializamos los repositorios
        this.repoPedidos = new RepositorioPedidosTxt();
        this.repoTickets = new RepositorioTickets();

        // Al iniciar el servicio, verificamos si ya cargamos los datos del TXT principal
        if (!pedidosCargadosDesdeArchivo) {
            List<Pedido> pedidosDelDisco = repoPedidos.cargarPedidos();
            
            // Los registramos en el Gestor de memoria como "existentes"
            for (Pedido p : pedidosDelDisco) {
                gestor.registrarPedidoExistente(p);
                // Opcional: Si quieres reactivar observadores al reiniciar
                // p.agregarObservador(new PantallaEstadoCliente());
            }
            pedidosCargadosDesdeArchivo = true;
        }
    }

    // =========================================================
    // LÓGICA DE CREACIÓN Y GESTIÓN (Builder, State, Strategy)
    // =========================================================

    /**
     * Crea un pedido nuevo desde la interfaz, lo guarda en memoria y actualiza el TXT.
     */
    public Pedido crearPedidoDesdeDatosSimples(String masa,
                                               String salsa,
                                               String queso,
                                               String tipoOrilla,
                                               List<String> ingredientes,
                                               List<String> condimentos) {

        // 1. Construir la Pizza (Builder)
        PizzaBuilder builder = new PizzaPersonalizadaBuilder()
                .conMasa(masa)
                .conSalsa(salsa)
                .conQueso(queso)
                .conTipoOrilla(tipoOrilla);

        if (ingredientes != null) {
            for (String ing : ingredientes) {
                if (ing != null && !ing.isBlank()) builder.agregarIngrediente(ing.trim());
            }
        }

        if (condimentos != null) {
            for (String con : condimentos) {
                if (con != null && !con.isBlank()) builder.agregarCondimento(con.trim());
            }
        }

        Pizza pizza = builder.build();

        // 2. Registrar en el Gestor (Memoria)
        Pedido pedido = gestor.registrarPedido(pizza);

        // 3. Agregar Observadores
        pedido.agregarObservador(new PantallaEstadoCliente());
        pedido.agregarObservador(new PanelControlEmpleado());

        // 4. Persistir cambios (Guardar en TXT)
        guardarCambiosEnDisco();

        return pedido;
    }

    /**
     * Cambia la estrategia de precios.
     */
    public void cambiarEstrategiaPromocion(boolean activa) {
        if (activa) {
            gestor.setEstrategia(new PrecioPromocion());
        } else {
            gestor.setEstrategia(new PrecioEstandar());
        }
    }

    public List<Pedido> listarPedidos() {
        return gestor.getPedidos();
    }

    public Pedido buscarPedidoPorId(int id) {
        return gestor.buscarPorId(id);
    }

    /**
     * Avanza el estado y guarda en disco.
     */
    public void avanzarEstadoPedido(int idPedido) {
        gestor.avanzarEstado(idPedido);
        guardarCambiosEnDisco();
    }

    /**
     * Cancela el pedido y guarda en disco.
     */
    public void cancelarPedido(int idPedido) {
        gestor.cancelarPedido(idPedido);
        guardarCambiosEnDisco();
    }

    private void guardarCambiosEnDisco() {
        repoPedidos.guardarPedidos(gestor.getPedidos());
    }

    // =========================================================
    // NUEVO: MÉTODOS AUXILIARES PARA EL CONTROLADOR (I/O)
    // =========================================================

    /**
     * Recupera el texto crudo del log antiguo (pedidos.txt) para mostrar detalles.
     * Delega a RepositorioTickets.
     */
    public String obtenerTextoLogPedido(String numeroPedido) {
        return repoTickets.leerDatosDelLog(numeroPedido);
    }

    /**
     * Genera un archivo físico de ticket.
     * Delega a RepositorioTickets.
     */
    public String generarTicketFisico(String numeroPedido, String estado, List<String> especificaciones, String total) throws IOException {
        return repoTickets.guardarTicketEnDisco(numeroPedido, estado, especificaciones, total);
    }
}