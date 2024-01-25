package model;

import com.google.gson.annotations.SerializedName; // Certifique-se de importar a classe correta

import java.text.SimpleDateFormat;
import java.util.Date;

public class Periodo {
    @SerializedName("inicio")
    private Date inicio;
    
    @SerializedName("fim")
    private Date fim;

    public Periodo(Date inicio, Date fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    public Date getInicio() {
        return inicio;
    }

    public Date getFim() {
        return fim;
    }
    
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return "[" + sdf.format(inicio) + " - " + sdf.format(fim) + "]";
    }
}
