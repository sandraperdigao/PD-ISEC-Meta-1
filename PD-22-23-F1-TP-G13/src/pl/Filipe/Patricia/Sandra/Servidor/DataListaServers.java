package pl.Filipe.Patricia.Sandra.Servidor;

import java.io.Serial;
import java.io.Serializable;

public class DataListaServers implements Serializable {
    @Serial
    private final static long serialVersionUID = 2L;
    private int port;
    private String ip;

    public DataListaServers(int port, String ip) {
        this.port = port;
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


    @Override
    public String
    toString() {
        return "DataListaServers{" +
                "port=" + port +
                ", ip=" + ip +
                '}';
    }
}
