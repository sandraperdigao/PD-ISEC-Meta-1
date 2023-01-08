package pl.Filipe.Patricia.Sandra.Servidor.Thread;

import pl.Filipe.Patricia.Sandra.Servidor.DataListaServers;
import pl.Filipe.Patricia.Sandra.Servidor.HeardBeat;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class ServerGetClientUDP extends Thread {
    private static final int MAX_DATA = 4000;

    private ArrayList<HeardBeat> listaServidores;
    private ArrayList<DataListaServers> listaServersCli;
    private boolean keepGoing;
    private DatagramSocket ds;
    public ServerGetClientUDP(ArrayList<HeardBeat> listaServidores, int port) {

        this.listaServersCli = new ArrayList<>();
        this.listaServidores = listaServidores;
        this.keepGoing = true;
        try {
            ds = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void setKeepGoing(boolean keepGoing) {
        this.keepGoing = keepGoing;
    }

    private void preencheListaCli(){
        listaServersCli = new ArrayList<>();
        for (HeardBeat b: listaServidores) {
            listaServersCli.add(new DataListaServers(b.getPortTCPClient(), "127.0.0.1"));
        }
    }

    @Override
    public void run() {

        while(keepGoing) {
            try {
                DatagramPacket dpRec = new DatagramPacket(new byte[MAX_DATA], MAX_DATA);
                ds.receive(dpRec);
                System.out.println("recebeu");
                ByteArrayInputStream bais = new ByteArrayInputStream(dpRec.getData()); // 1 Passo
                ObjectInputStream ois = new ObjectInputStream(bais);// 2 passo
                String msgRec = (String) ois.readObject();
                if (msgRec.equals("ligacao")) {
                    preencheListaCli();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(listaServersCli);
                    byte[] msgBytes = baos.toByteArray();
                    DatagramPacket dpSend = new DatagramPacket(msgBytes, msgBytes.length, dpRec.getAddress(), dpRec.getPort());
                    ds.send(dpSend);

                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
