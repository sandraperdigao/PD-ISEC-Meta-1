package pl.Filipe.Patricia.Sandra.Servidor.Thread;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class ServerTCPConnection extends Thread{
    private static final int MAX_DATA = 4000;
    private int port;
    private InetAddress ip;
    private Socket cliSocket;
    private  String nomeDB;

    public ServerTCPConnection(int port, InetAddress ip) {
        this.port = port;
        this.ip = ip;
        nomeDB = port + "TP.db";
    }

    public ServerTCPConnection(int portTCP, InetAddress ip, String newBdName) {
        this.port = portTCP;
        this.ip = ip;
        this.nomeDB = newBdName;
    }

    public String getNomeDB() {
        return nomeDB;
    }

    @Override
    public void run() {
        try {
            String dir ="./";
            cliSocket = new Socket(ip, port);
            cliSocket.setSoTimeout(4000);
            FileOutputStream fs = new FileOutputStream(dir + nomeDB);
            System.out.println("Connected to " + cliSocket.getInetAddress().getHostAddress() + ":" + cliSocket.getPort());

            InputStream is = cliSocket.getInputStream();
            OutputStream os = cliSocket.getOutputStream();


            os.write("dataBase".getBytes());

            byte[] msgBuffer = new byte[MAX_DATA];
            int nBytes;
            while((nBytes = is.read(msgBuffer)) != -1){
                fs.write(msgBuffer, 0, nBytes);
                System.out.println("Numero de bytes recebidos : "+ nBytes);
                System.out.println("Ficheiro recebido");
            }

            System.out.println("Ligação encerrada");

            cliSocket.close();



        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
