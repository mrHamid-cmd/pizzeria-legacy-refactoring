package negocio.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una pizza personalizada.
 * Esta clase se alinea con el modelo conceptual del diagrama UML.
 */
public class Pizza {

    private int id;
    private String nombre;

    private String masa;
    private String salsa;
    private String queso;
    private String tipoOrilla;

    private List<String> ingredientes = new ArrayList<>();
    private List<String> condimentos = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() { 
        return nombre; 
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMasa() { 
        return masa; 
    }

    public void setMasa(String masa) { 
        this.masa = masa; 
    }

    public String getSalsa() { 
        return salsa; 
    }

    public void setSalsa(String salsa) { 
        this.salsa = salsa; 
    }

    public String getQueso() { 
        return queso; 
    }

    public void setQueso(String queso) { 
        this.queso = queso; 
    }

    public String getTipoOrilla() { 
        return tipoOrilla; 
    }

    public void setTipoOrilla(String tipoOrilla) { 
        this.tipoOrilla = tipoOrilla; 
    }

    public List<String> getIngredientes() { 
        return ingredientes; 
    }

    public List<String> getCondimentos() {
        return condimentos;
    }
}
