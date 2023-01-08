package pl.Filipe.Patricia.Sandra.Servidor;

import java.sql.*;
import java.util.Scanner;

public class CriaBaseDeDados {
    Scanner myObj;

    public CriaBaseDeDados() {
        myObj = new Scanner(System.in);

    }

    public String criar() {
        System.out.println("Insert filename for new database file: ");
        String filename = myObj.nextLine(); //Read user input
        //Sqlite connection string
        filename = filename.trim();
        filename += ".db";
        String DATABASE_URL = "jdbc:sqlite:" + filename;

        //Sql statement for creating a new table
        String espetaculo = "CREATE TABLE espetaculo (\n"
                + " id                   INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + " descricao            TEXT    NOT NULL,\n"
                + " tipo                 TEXT    NOT NULL,\n"
                + " data_hora            TEXT    NOT NULL,\n"
                + " duracao              INTEGER NOT NULL,\n"
                + " local                TEXT    NOT NULL,\n"
                + " localidade           TEXT    NOT NULL,\n"
                + " pais                 TEXT    NOT NULL,\n"
                + " classificacao_etaria TEXT    NOT NULL,\n"
                + " visivel              INTEGER NOT NULL DEFAULT (0)\n"
                + ");";

        String lugar = "CREATE TABLE lugar (\n"
                + "id            INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + "fila          TEXT    NOT NULL,\n"
                + "assento       TEXT    NOT NULL,\n"
                + "preco         REAL    NOT NULL,\n"
                + "espetaculo_id INTEGER REFERENCES espetaculo (id) NOT NULL\n"
                + ");";

        String reserva = "CREATE TABLE reserva (\n"
                + "id            INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + "data_hora     TEXT    NOT NULL,\n"
                + "pago          INTEGER NOT NULL DEFAULT (0),\n"
                + "id_utilizador INTEGER REFERENCES utilizador (id) NOT NULL,\n"
                + "id_espetaculo INTEGER REFERENCES espetaculo (id) NOT NULL\n"
                +");";

        String reservaLugar = " CREATE TABLE reserva_lugar (\n"
                + "id_reserva INTEGER REFERENCES reserva (id) NOT NULL,\n"
                + "id_lugar   INTEGER REFERENCES lugar (id) NOT NULL,\n"
                + "PRIMARY KEY ( id_reserva, id_lugar)\n"
                + ");";

        String utilizador = "CREATE TABLE utilizador (\n"
                + "id            INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + "username      TEXT    UNIQUE NOT NULL,\n"
                + "nome          TEXT    UNIQUE NOT NULL,\n"
                + "password      TEXT    NOT NULL,\n"
                + "administrador INTEGER NOT NULL DEFAULT (0),\n"
                + "autenticado   INTEGER NOT NULL DEFAULT (0)\n"
                + ");";

        String versao = "CREATE TABLE versao (\n"
                + "id INTEGER NOT NULL,\n"
                + "versao_db INTEGER,\n"
                + "PRIMARY KEY (id)\n"
                + ");";

        String versao_Id = "INSERT INTO  versao (versao_db)"
                + "VALUES\n"
                + "(1);";
        try {
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement();
            stmt.execute(espetaculo);
            stmt.execute(lugar);
            stmt.execute(reserva);
            stmt.execute(reservaLugar);
            stmt.execute(utilizador);
            stmt.execute(versao);
            stmt.execute(versao_Id);

            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is: " + meta.getDriverName());
                System.out.println("A new database has been created named " + filename);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return filename;
    }



}
