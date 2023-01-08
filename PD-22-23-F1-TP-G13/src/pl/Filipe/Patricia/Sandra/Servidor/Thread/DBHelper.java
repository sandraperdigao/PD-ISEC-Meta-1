package pl.Filipe.Patricia.Sandra.Servidor.Thread;

import pl.Filipe.Patricia.Sandra.Cliente.MsgCliServer;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class DBHelper {

    public static int getDBVersion(String nomeDB) {
        Connection dbConn;
        try {
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + nomeDB);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        Statement statement;
        int id = -1;
        if (dbConn != null)
            try {
                statement = dbConn.createStatement();
                String sqlQuery = "SELECT versao_db FROM versao";

                ResultSet resultSet = statement.executeQuery(sqlQuery);

                id = resultSet.getInt("versao_db");

                resultSet.close();
                statement.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        return  id;
    }

    private static void registerUser(String nomeDB, MsgCliServer msgRec){
        Connection dbConn;
        try {
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + nomeDB);
            Statement statement = dbConn.createStatement();
            statement.executeUpdate(msgRec.getSqlQuery());
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean verifyIfEspetaculoExistsAndHappens24HrLatter(String nomeDB, MsgCliServer msgRec){
        Connection dbConn;
        try{
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + nomeDB);
            Statement statement = dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(msgRec.getSqlQuery());
            String dataEspetaculoAux;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            if(resultSet.next()){
                dataEspetaculoAux = resultSet.getString(1);
                Date dataEspetaculo = simpleDateFormat.parse(dataEspetaculoAux);
                dbConn.close();
                Date dataAtual = new Date();
                System.out.println("Data Espetaculo: " + dataEspetaculo + "; Data Atual: " + dataAtual);
                if(dataEspetaculo.getTime() >= dataAtual.getTime() + 86400000){
                    return true;
                }
            }
            else {
                dbConn.close();
                System.out.println("Failed");
                return false;
            }
        } catch (SQLException | ParseException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    public static boolean verifyIfUserExistsForRegistration(String nomeDB, MsgCliServer msgRec){
        Connection dbConn;
        try{
            StringTokenizer getUsern = new StringTokenizer(msgRec.getSqlQuery(), ",");
            getUsern.nextToken();
            String usernameAux = getUsern.nextToken();
            String username = usernameAux.substring(1,usernameAux.length()-1);
            System.out.println("Username: "+username);
            String nameAux = getUsern.nextToken();
            String name = nameAux.substring(1,nameAux.length()-1);
            System.out.println("Name: "+name);
            String passAux = getUsern.nextToken();
            String pass = passAux.substring(1,passAux.length()-1);
            System.out.println("Pass: "+pass);
            final String queryCheck = "SELECT * from utilizador WHERE username = '" + username + "' AND nome = '" + name + "'";
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + nomeDB);
            final Statement statement = dbConn.createStatement();
            final ResultSet resultSet = statement.executeQuery(queryCheck);
            if (resultSet.next()){
                System.out.println("User already exists in Database.");
                dbConn.close();
                return true;
            }
            else {
                System.out.println("User doesn't exist in Database.");
                dbConn.close();
                return false;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean verifyIfUserExistsForAuthentication(String nomeDB, MsgCliServer msgRec){
        Connection dbConn;
        try{
            StringTokenizer getUsern = new StringTokenizer(msgRec.getSqlQuery(), "'");
            getUsern.nextToken();
            String username = getUsern.nextToken();
            System.out.println("Username: "+username);
            getUsern.nextToken();
            String pass = getUsern.nextToken();
            System.out.println("Pass: "+pass);
            if(username.equals("admin")){
                System.out.println("This authentication process is for other users, not administrator.");
                return false;
            }
            final String queryCheck = "SELECT * from utilizador WHERE username = '" + username + "' AND password = '" + pass+ "'";
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + nomeDB);
            final Statement statement = dbConn.createStatement();
            final ResultSet resultSet = statement.executeQuery(queryCheck);
            if (resultSet.next()){
                System.out.println("User is registered in Database.");
                dbConn.close();
                return true;
            }
            else {
                System.out.println("User isn't registered in Database.");
                dbConn.close();
                return false;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getUserName(String nomeDB, MsgCliServer msgRec){
        Connection dbConn;
        try{
            StringTokenizer getUsern = new StringTokenizer(msgRec.getSqlQuery(), "'");
            getUsern.nextToken();
            String username = getUsern.nextToken();
            System.out.println("Username: "+username);
            getUsern.nextToken();
            String pass = getUsern.nextToken();
            System.out.println("Pass: "+pass);
            final String queryCheck = "SELECT nome from utilizador WHERE username = '" + username + "' AND password = '" + pass+ "'";
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + nomeDB);
            final Statement statement = dbConn.createStatement();
            final ResultSet resultSet = statement.executeQuery(queryCheck);
            String nomeAux;
            if (resultSet.next()){
                nomeAux = resultSet.getString(1);
                System.out.println("Nome do utilizador: " + nomeAux);
                dbConn.close();
                return nomeAux;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return " ";
    }

    public static boolean verifyIfReservaExistsForDelete(String nomeDB, MsgCliServer msgRec){
        Connection dbConn;
        try{
            final String queryCheck = msgRec.getSqlQuery();
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + nomeDB);
            final Statement statement = dbConn.createStatement();
            final ResultSet resultSet = statement.executeQuery(queryCheck);
            if (resultSet.next()){
                System.out.println("Reserva exists in Database and is unpaid.");
                dbConn.close();
                return true;
            }
            else {
                System.out.println("Reserva doesn't exist in Database or is paid.");
                dbConn.close();
                return false;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static String queryDBGetReservations(String nomeDB, MsgCliServer sqlQuery){
        Connection dbConn;
        try {
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + nomeDB);
            Statement statement = dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery.getSqlQuery());
            StringBuilder sb = new StringBuilder();
            while (resultSet.next()){
                sb.append("\nReserva: ID="+resultSet.getString(1) + " Data=" + resultSet.getString(2) +
                        " ID do User=" + resultSet.getString(4) + " ID do espetaculo=" + resultSet.getString(5));
            }
            statement.close();
            dbConn.close();
            return sb.toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return " ";
    }

    public static String queryDBGetEspetaculos(String nomeDB, MsgCliServer sqlQuery){
        Connection dbConn;
        try {
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + nomeDB);
            Statement statement = dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery.getSqlQuery());
            StringBuilder sb = new StringBuilder();
            while (resultSet.next()){
                sb.append("\nEspetaculo: ID="+resultSet.getString(1) + " Descricao=" + resultSet.getString(2) +
                        " Tipo=" + resultSet.getString(3) + " Data=" + resultSet.getString(4)
                        + " Duracao=" + resultSet.getString(5) + " Local=" + resultSet.getString(6)
                        + " Localidade=" + resultSet.getString(7) + " País=" + resultSet.getString(8)
                        + " Classificacao Etária=" + resultSet.getString(9));
            }
            statement.close();
            dbConn.close();
            return sb.toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return " ";
    }

    public static String queryDBGetLugaresPrecos(String nomeDB, MsgCliServer sqlQuery){
        Connection dbConn;
        try {
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + nomeDB);
            Statement statement = dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery.getSqlQuery());
            StringBuilder sb = new StringBuilder();
            while (resultSet.next()){
                sb.append("\nLugar: ID="+resultSet.getString(1) + " Fila=" + resultSet.getString(2) +
                        " Assento=" + resultSet.getString(3) + " Preco=" + resultSet.getString(4)
                );
            }
            statement.close();
            dbConn.close();
            return sb.toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return " ";
    }

    public static String queryDBGetMaxIDEspetaculo(String nomeDB, MsgCliServer sqlQuery){
        Connection dbConn;
        try {
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + nomeDB);
            Statement statement = dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery.getSqlQuery());
            String idMax;
            if (resultSet.next()){
                 idMax= resultSet.getString(1);
                statement.close();
                dbConn.close();
                return idMax;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "vazio";
    }

    public static String queryDBGetUserID(String nomeBD, MsgCliServer msgRec) {
        Connection dbConn;
        try {
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + nomeBD);
            Statement statement = dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(msgRec.getSqlQuery());
            String id;
            if (resultSet.next()){
                id= resultSet.getString(1);
                statement.close();
                dbConn.close();
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "vazio";
    }
    public static boolean verifyIfAdminExistsForAuthentication(String nomeDB, MsgCliServer msgRec){
        Connection dbConn;
        try{
            StringTokenizer getUsern = new StringTokenizer(msgRec.getSqlQuery(), "'");
            getUsern.nextToken();
            String username = getUsern.nextToken();
            System.out.println("Username: "+username);
            getUsern.nextToken();
            String pass = getUsern.nextToken();
            System.out.println("Pass: "+pass);
            if(!username.equals("admin") || !pass.equals("admin")){
                System.out.println("This authentication process is for administrator.");
                return false;
            }
            final String queryCheck = "SELECT * from utilizador WHERE username = '" + username + "' AND password = '" + pass+ "'";
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + nomeDB);
            final Statement statement = dbConn.createStatement();
            final ResultSet resultSet = statement.executeQuery(queryCheck);
            if (resultSet.next()){
                System.out.println("User is registered in Database.");
                dbConn.close();
                return true;
            }
            else {
                System.out.println("User isn't registered in Database.");
                dbConn.close();
                return false;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean verifyIfShowExistsAndIsNotVisible(String nomeDB, MsgCliServer msgRec){
        Connection dbConn;
        try {
            StringTokenizer getID = new StringTokenizer(msgRec.getSqlQuery(), "'");
            getID.nextToken();
            String id = getID.nextToken();
            System.out.println("id: " + id);

            final String queryCheck = "SELECT visivel from espetaculo WHERE id = '" + id + "'";
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + nomeDB);
            final Statement statement = dbConn.createStatement();
            final ResultSet resultSet = statement.executeQuery(queryCheck);
            if (resultSet.next()) {
                String resultado = resultSet.getString(1);
                if(resultado.equals("0")){
                    System.out.println("Show not visible yet.");
                    dbConn.close();
                    return true;
                }
            } else {
                System.out.println("Show already visible or show id doesn't exist in DB.");
                dbConn.close();
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static boolean verifyIfEspetaculoExistsForDeleteAndHasNoPaidReservesAssociated(String nomeDB, MsgCliServer msgRec){
        Connection dbConn;
        Connection dbConn2;
        try {
            StringTokenizer getID = new StringTokenizer(msgRec.getSqlQuery(), "'");
            getID.nextToken();
            String id = getID.nextToken();
            System.out.println("ID: " + id);

            final String queryCheck = "SELECT * from espetaculo WHERE id = '" + id + "'";
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + nomeDB);
            final Statement statement = dbConn.createStatement();
            final ResultSet resultSet = statement.executeQuery(queryCheck);
            if (resultSet.next()) {
                dbConn.close();
                //ver se tem reservas associadas pagas
                dbConn2 = DriverManager.getConnection("jdbc:sqlite:" + nomeDB);
                final String queryCheck2 = "SELECT pago from reserva WHERE id_espetaculo = '" + id + "'";
                final Statement statement2 = dbConn2.createStatement();
                final ResultSet resultSet2 = statement2.executeQuery(queryCheck2);
                while (resultSet2.next()) {
                    String resultado = resultSet2.getString(1);
                    if (resultado.equals("1")) {
                        dbConn2.close();
                        return false;
                    }
                }
                dbConn2.close();

                String sqlQuery3 = "DELETE FROM reserva where id_espetaculo='" +  id + "'";
                serverQuery(nomeDB,sqlQuery3);
                return true;
            }
            else {
                System.out.println("Not possible.");
                dbConn.close();
                return false;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void serverQuery(String nomeDB, String sqlQuery){
        Connection dbConn;
        try {
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + nomeDB);
            Statement statement = dbConn.createStatement();
            statement.executeUpdate(sqlQuery);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void queryDB(String nomeDB, MsgCliServer sqlQuery) {
        registerUser(nomeDB, sqlQuery);
    }


}



