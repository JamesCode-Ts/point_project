package model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Periodo {
    private Date inicio;
    private Date fim;

    public Periodo(Date inicio, Date fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    public Date getInicio() {
        return inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return "[" + sdf.format(inicio) + " - " + sdf.format(fim) + "]";
    }
}