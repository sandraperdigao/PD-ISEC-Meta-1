package pl.Filipe.Patricia.Sandra.Servidor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;

public class HeardBeat implements Serializable {
    @Serial
    private final static long serialVersionUID = 2L;
    private int portTCP;
    private boolean disponivel;
    private int versaoBD;
    private int numLigacoesTCP;
    private int portTCPClient;
    private String alteracao;
    private String alteracaoDB;
    private Calendar calendar;

    public HeardBeat(int portTCP, boolean disponivel, int versaoBD, int numLigacoesTCP) {
        this.portTCP = portTCP;
        this.disponivel = disponivel;
        this.versaoBD = versaoBD;
        this.numLigacoesTCP = numLigacoesTCP;
        this.alteracao = "Sem alteração";
        this.portTCPClient = -1;
    }

    public int getPortTCP() {
        return portTCP;
    }

    public void setPortTCP(int portTCP) {
        this.portTCP = portTCP;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    public int getVersaoBD() {
        return versaoBD;
    }

    public void setVersaoBD(int versaoBD) {
        this.versaoBD = versaoBD;
    }

    public int getNumLigacoesTCP() {
        return numLigacoesTCP;
    }

    public void setNumLigacoesTCP(int numLigacoesTCP) {
        this.numLigacoesTCP = numLigacoesTCP;
    }

    public String getAlteracao() {
        return alteracao;
    }

    public void setAlteracao(String alteracao) {
        this.alteracao = alteracao;
    }

    public Calendar getCalendar() {
       return calendar;
   }

    public void setCalendar(Calendar calendar) {
       this.calendar = calendar;
    }

    public String getAlteracaoDB() {
        return alteracaoDB;
    }

    public void setAlteracaoDB(String alteracaoDB) {
        this.alteracaoDB = alteracaoDB;
    }


    public int getPortTCPClient() {
        return portTCPClient;
    }

    public void setPortTCPClient(int portTCPClient) {
        this.portTCPClient = portTCPClient;
    }

    @Override
    public String toString() {
        return "HeardBeat{" +
                "portTCP=" + portTCP +
                ", portTCPClient=" + portTCPClient +
                ", disponivel=" + disponivel +
                ", versaoBD=" + versaoBD +
                ", numLigacoesTCP=" + numLigacoesTCP +
                ", alteracao='" + alteracao + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeardBeat heardBeat = (HeardBeat) o;
        return portTCP == heardBeat.portTCP;
    }

    @Override
    public int hashCode() {
        return Objects.hash(portTCP);
    }



}
