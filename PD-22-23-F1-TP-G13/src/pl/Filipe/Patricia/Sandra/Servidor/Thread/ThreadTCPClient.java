package pl.Filipe.Patricia.Sandra.Servidor.Thread;

import java.io.IOException;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.io.ByteArrayOutputStream;
import pl.Filipe.Patricia.Sandra.Servidor.HeardBeat;
import pl.Filipe.Patricia.Sandra.Servidor.ServidorLogic;


public class ThreadTCPClient extends Thread{
    private int numLigacoes;
    private ServerSocket ss;
    private ArrayList<Socket> listaClientes;
    private MulticastSocket ms;
    private String nomeBD;
    private ServerThreadReceiveBeat serverThreadReceiveBeat;

    private ArrayList<ObjectOutputStream> outputStreamArrayList;
    public ThreadTCPClient( MulticastSocket ms, String nomeBD) {
        numLigacoes = 0;
        listaClientes = new ArrayList<>();
        this.ms = ms;
        this.nomeBD = nomeBD;
        outputStreamArrayList = new ArrayList<>();
    }

    public void setServerThreadReceiveBeat(ServerThreadReceiveBeat serverThreadReceiveBeat) {
        this.serverThreadReceiveBeat = serverThreadReceiveBeat;
    }

    @Override
    public void run() {

        try {
            ss = new ServerSocket(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while(true) {
            try {
                Socket cliSocket = ss.accept();
                listaClientes.add(cliSocket);
                numLigacoes++;

                ObjectOutputStream objectOutput = new ObjectOutputStream(cliSocket.getOutputStream());
                ObjectInputStream objectInput = new ObjectInputStream(cliSocket.getInputStream());
                outputStreamArrayList.add(objectOutput);
                ThreadTrataClient threadTrataClient = new ThreadTrataClient(cliSocket, ms, nomeBD, serverThreadReceiveBeat, this, objectInput, objectOutput);
                threadTrataClient.start();

                MulticastSocket socket = new MulticastSocket();
                HeardBeat heardBeattemp = new HeardBeat(socket.getLocalPort(), true, DBHelper.getDBVersion(nomeBD) , 0);
                heardBeattemp.setAlteracao("numTCP");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos;
                oos = new ObjectOutputStream(baos);
                oos.writeObject(heardBeattemp);
                byte[] msgBytes = baos.toByteArray();
                DatagramPacket dp = new DatagramPacket(msgBytes, msgBytes.length, InetAddress.getByName(ServidorLogic.MULTICAST_IP),ServidorLogic.MULTICAST_PORT);
                ms.send(dp);
                socket.close();


            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    public int getPortSS(){
        return ss.getLocalPort();
    }

    public void setNumLigacoes(int numLigacoes) {
        this.numLigacoes = numLigacoes;
    }

    public int getNumLigacoes() {
        return numLigacoes;
    }

    public ArrayList<Socket> getListaClientes() {
        return listaClientes;
    }
    public ArrayList<HeardBeat> getListaServers(){
        return serverThreadReceiveBeat.getListaServidores();
    }



    public ArrayList<ObjectOutputStream> getOutputStreamArrayList() {
        return outputStreamArrayList;
    }
}
