package pl.Filipe.Patricia.Sandra.Cliente;

import java.io.Serial;
import java.io.Serializable;

public class MsgCliServer implements Serializable {
    @Serial
    private final static long serialVersionUID = 2L;

    private String sqlQuery;
    private int opcao;

    public MsgCliServer(String sqlQuery, int opcao) {
        this.sqlQuery = sqlQuery;
        this.opcao = opcao;
    }


    public String getSqlQuery() {
        return sqlQuery;
    }

    public int getOpcao() {
        return opcao;
    }

}
