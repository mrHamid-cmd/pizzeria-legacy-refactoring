/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package persistencia;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Encargada de I/O auxiliar:
 * 1. Leer el archivo histórico (pedidos.txt) para mostrar especificaciones.
 * 2. Generar el archivo físico del Ticket.
 */
public class RepositorioTickets {

    private static final String PEDIDOS_LOG_FILE = "pedidos.txt";
    private static final String TICKETS_DIR = "tickets";

    // Lógica para leer el archivo de log (lo que tenías en cargarDatosPedido)
    public String leerDatosDelLog(String numeroPedido) {
        File file = new File(PEDIDOS_LOG_FILE);
        if (!file.exists()) return null;

        StringBuilder pedidoActual = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean pedidoEncontrado = false;

            while ((line = reader.readLine()) != null) {
                if (line.contains("PEDIDO " + numeroPedido) || line.contains("=== PEDIDO " + numeroPedido)) {
                    pedidoEncontrado = true;
                }

                if (pedidoEncontrado) {
                    pedidoActual.append(line).append("\n");
                    if (line.startsWith("=============================")) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo log: " + e.getMessage());
            return null;
        }

        return pedidoActual.length() > 0 ? pedidoActual.toString() : null;
    }

    // Lógica para escribir el ticket en disco
    public String guardarTicketEnDisco(String numeroPedido, String estado, List<String> especificaciones, String total) throws IOException {
        File directorio = new File(TICKETS_DIR);
        if (!directorio.exists()) directorio.mkdirs();

        String nombreArchivo = TICKETS_DIR + "/ticket_" + numeroPedido + "_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(nombreArchivo))) {
            writer.println("========================================");
            writer.println("            PIZZERÍA DEL SISTEMA        ");
            writer.println("========================================");
            writer.println("Ticket: " + numeroPedido);
            writer.println("Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            writer.println("Estado: " + estado);
            writer.println("----------------------------------------");
            writer.println("ESPECIFICACIONES DE LA PIZZA:");
            writer.println("----------------------------------------");

            for (String item : especificaciones) {
                writer.println(item);
            }

            writer.println("========================================");
            writer.println("TOTAL: " + total);
            writer.println("========================================");
            writer.println("         ¡GRACIAS POR SU COMPRA!        ");
            writer.println("========================================");
        }
        
        return nombreArchivo; // Retornamos la ruta para avisar al usuario
    }
}