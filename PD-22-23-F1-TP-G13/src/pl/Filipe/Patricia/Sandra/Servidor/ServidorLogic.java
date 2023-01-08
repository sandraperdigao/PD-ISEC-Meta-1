package pl.Filipe.Patricia.Sandra.Servidor;

import java.io.IOException;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;

public class ServidorLogic {
    public static final int MULTICAST_PORT = 4004;
    public final static String MULTICAST_IP = "239.39.39.39";
    private MulticastSocket ms;
    private ServerSocket ss;
    private SocketAddress sa;
    private NetworkInterface ni;
    private ArrayList<HeardBeat> listaServidores;
    private Connection dbConn;
    private String nomeBD;
    private final String DATABASE_URL = "jdbc:sqlite:";


    public ServidorLogic(String url){
        listaServidores = new ArrayList<>();
        try{
            this.ms = new MulticastSocket(MULTICAST_PORT);
            InetAddress ipGroup = InetAddress.getByName(MULTICAST_IP);
            sa = new InetSocketAddress(ipGroup, MULTICAST_PORT);
            ni = NetworkInterface.getByName("en0");
            //ni = NetworkInterface.getByName("wlan3");
            ms.joinGroup(sa, ni);
            ss = new ServerSocket(0);
            String db_url = DATABASE_URL + url;
            nomeBD = url;
            dbConn = DriverManager.getConnection(db_url);
        }catch(IOException | SQLException e){
            e.printStackTrace();
        }
    }
    public ServidorLogic(){
        listaServidores = new ArrayList<>();
        try{
            this.ms = new MulticastSocket(MULTICAST_PORT);
            InetAddress ipGroup = InetAddress.getByName(MULTICAST_IP);
            sa = new InetSocketAddress(ipGroup, MULTICAST_PORT);
            ni = NetworkInterface.getByName("en0");
            //ni = NetworkInterface.getByName("wlan3");
            ms.joinGroup(sa, ni);
            ss = new ServerSocket(0);
        }catch(IOException e){
            e.printStackTrace();
        }
    }



    public int getDBVersion(){
        Statement statement;
        int id = -1;
        if(dbConn != null)
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

        return id;
    }


    public MulticastSocket getMs() {
        return ms;
    }

    public void leaveGroup(){
        try {
            ms.leaveGroup(sa, ni);
            ms.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getAutomaticPort(){
        return ss.getLocalPort();
    }

    public void setDbConn(String url) {
        try {
            this.dbConn = DriverManager.getConnection( DATABASE_URL + url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ServerSocket getSs(){
        return ss;
    }

    public ArrayList<HeardBeat> getListaServidores() {
        return listaServidores;
    }

    public String getNomeBD() {
        return nomeBD;
    }
}
