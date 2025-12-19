/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package persistencia;

/**
 *
 * @author dell
 */



import java.io.*;
import java.util.ArrayList;
import java.util.List;

// Importaciones de tu modelo y patrones
import negocio.abstractas.EstadoPedido;
import negocio.modelo.Pedido;
import negocio.modelo.Pizza;
import negocio.patrones.state.*;

/**
 * Clase encargada exclusivamente de la persistencia en archivo de texto.
 * Patrón: DAO (Data Access Object) / Repository simple.
 */
public class RepositorioPedidosTxt {

    private static final String STORE_FILE = "pedidos_store.txt";

    /**
     * Lee el archivo txt y convierte cada línea en un objeto Pedido.
     * @return Lista de pedidos recuperados.
     */
    public List<Pedido> cargarPedidos() {
        List<Pedido> lista = new ArrayList<>();
        File file = new File(STORE_FILE);

        // Si el archivo no existe, retornamos lista vacía
        if (!file.exists()) {
            return lista;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    Pedido p = parsearPedido(linea);
                    if (p != null) {
                        lista.add(p);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo " + STORE_FILE + ": " + e.getMessage());
        }
        return lista;
    }

    /**
     * Recibe la lista actual de memoria y sobrescribe el archivo completo.
     * @param pedidos Lista de todos los pedidos actuales.
     */
    public void guardarPedidos(List<Pedido> pedidos) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(STORE_FILE, false))) {
            for (Pedido p : pedidos) {
                bw.write(serializarPedido(p));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error escribiendo " + STORE_FILE + ": " + e.getMessage());
        }
    }

    // =========================================================
    // Métodos privados de Mapeo (String <-> Objeto)
    // =========================================================

    private Pedido parsearPedido(String linea) {
        // Formato esperado:
        // id;masa;salsa;queso;tipoOrilla;ing1|ing2;con1|con2;total;ESTADO
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
            double total = Double.parseDouble(partes[7].trim());
            String estadoStr = partes[8].trim();

            // Arrays auxiliares para ingredientes y condimentos
            String[] ingArray = ingredientesStr.isEmpty() ? new String[0] : ingredientesStr.split("\\|");
            String[] conArray = condimentosStr.isEmpty() ? new String[0] : condimentosStr.split("\\|");

            // 1. Reconstruir la Pizza
            Pizza pizza = new Pizza();
            pizza.setMasa(masa);
            pizza.setSalsa(salsa);
            pizza.setQueso(queso);
            pizza.setTipoOrilla(tipoOrilla);

            for (String ing : ingArray) {
                if (!ing.isBlank()) pizza.getIngredientes().add(ing.trim());
            }
            for (String con : conArray) {
                if (!con.isBlank()) pizza.getCondimentos().add(con.trim());
            }

            // 2. Reconstruir el Pedido
            Pedido pedido = new Pedido(pizza, total);
            pedido.setId(id);
            // Convertimos el String del estado a la clase Estado correspondiente
            pedido.setEstadoActual(estadoPorNombre(estadoStr));

            return pedido;

        } catch (Exception e) {
            System.err.println("Error parseando línea: " + linea);
            return null;
        }
    }

    private String serializarPedido(Pedido pedido) {
        StringBuilder sb = new StringBuilder();

        // Datos básicos
        sb.append(pedido.getId()).append(";");
        sb.append(nullToEmpty(pedido.getPizza().getMasa())).append(";");
        sb.append(nullToEmpty(pedido.getPizza().getSalsa())).append(";");
        sb.append(nullToEmpty(pedido.getPizza().getQueso())).append(";");
        sb.append(nullToEmpty(pedido.getPizza().getTipoOrilla())).append(";");

        // Ingredientes con pipe |
        List<String> ing = pedido.getPizza().getIngredientes();
        sb.append(String.join("|", ing)).append(";");

        // Condimentos con pipe |
        List<String> con = pedido.getPizza().getCondimentos();
        sb.append(String.join("|", con)).append(";");

        // Total y Estado
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
            case "CANCELADO":  return new EstadoCancelado();
            default: return new EstadoRecibido();
        }
    }

    private String nullToEmpty(String s) {
        return (s == null) ? "" : s;
    }
}