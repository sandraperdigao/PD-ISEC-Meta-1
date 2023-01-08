package pl.Filipe.Patricia.Sandra.Servidor.Thread;

import pl.Filipe.Patricia.Sandra.Servidor.HeardBeat;
import pl.Filipe.Patricia.Sandra.Servidor.ServidorLogic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Calendar;

public class ServerThreadSendBeat extends Thread{
    private MulticastSocket ms;
    private boolean keepGoing;
    private HeardBeat heardBeat;
    private String nomeBD;
    private ThreadTCPClient threadTCPClient;

    public ServerThreadSendBeat(MulticastSocket ms, int port, int versaoDB, String nomeBD, ThreadTCPClient threadTCPClient) {
        this.ms = ms;
        this.keepGoing = true;
        heardBeat = new HeardBeat(port, true, versaoDB,threadTCPClient.getNumLigacoes());
        this.nomeBD = nomeBD;
        this.threadTCPClient = threadTCPClient;
    }


    @Override
    public void run() {
        while(keepGoing){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos;
             heardBeat.setCalendar(Calendar.getInstance());
             heardBeat.setVersaoBD(DBHelper.getDBVersion(nomeBD));
             if(threadTCPClient.getNumLigacoes() < 0)
                 threadTCPClient.setNumLigacoes(0);
             heardBeat.setNumLigacoesTCP(threadTCPClient.getNumLigacoes());
             heardBeat.setPortTCPClient(threadTCPClient.getPortSS());
            try {
                oos = new ObjectOutputStream(baos);
                oos.writeObject(heardBeat);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] msgBytes = baos.toByteArray();
            DatagramPacket dp = null;
            try {
                dp = new DatagramPacket(msgBytes, msgBytes.length, InetAddress.getByName(ServidorLogic.MULTICAST_IP),ServidorLogic.MULTICAST_PORT);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            try {
                ms.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setKeepGoing(boolean keepGoing) {
        this.keepGoing = keepGoing;
    }
}
