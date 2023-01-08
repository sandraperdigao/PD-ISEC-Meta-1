package pl.Filipe.Patricia.Sandra.Servidor.Thread;

import pl.Filipe.Patricia.Sandra.Servidor.DataListaServers;
import pl.Filipe.Patricia.Sandra.Servidor.HeardBeat;
import pl.Filipe.Patricia.Sandra.Servidor.HeardBeatComparator;
import pl.Filipe.Patricia.Sandra.Servidor.ServidorLogic;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;


public class ServerThreadReceiveBeat extends Thread{
    private static final int MAX_DATA = 4000;
    private MulticastSocket ms;
    private boolean keepGoing;
    private HeardBeat heardBeat;
    private ArrayList<HeardBeat> listaServidores;
    private int myBDVersion;
    private int port;
    private String nomeDB;
    private ThreadTCPClient threadTCPClient;
    private int novaVersao;

    public ServerThreadReceiveBeat(MulticastSocket ms, ArrayList<HeardBeat> listaServidores, int bdVersion, int port, String nomeBD, ThreadTCPClient threadTCPClient) {
        this.ms = ms;
        this.keepGoing = true;
        this.listaServidores = listaServidores;
        myBDVersion = bdVersion;
        this.port = port;
        this.nomeDB = nomeBD;
        this.threadTCPClient = threadTCPClient;
        novaVersao = -1;
    }

    @Override
    public void run() {
        while(keepGoing){

            DatagramPacket dp = new DatagramPacket(new byte[5000], 5000);
            try {
                ms.receive(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
            ObjectInputStream ois;
            try {
                ois = new ObjectInputStream(bais);
                heardBeat = (HeardBeat) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            if(heardBeat.getAlteracao().equalsIgnoreCase("Sem alteração")) {
                synchronized (listaServidores){
                    if (!listaServidores.contains(heardBeat)) {
                        listaServidores.add(heardBeat);
                        enviaListaServidores();
                    } else {
                        listaServidores.remove(heardBeat);
                        listaServidores.add(heardBeat);
                    }
                    listaServidores.removeIf(i -> !i.isDisponivel());
                    removeTimePassed();
                    listaServidores.sort(new HeardBeatComparator());
                    verificaVersaoBD();
                }

            }
            System.out.println("\nLista: " + listaServidores);
            System.out.println("msg recebida\n" + heardBeat);


            if(heardBeat.getAlteracao().equalsIgnoreCase("numTCP")){
                enviaListaServidores();
            }


            if (heardBeat.getAlteracao().equalsIgnoreCase("Versão Maior")) {
                ArrayList<Socket>listaClientes;
                System.out.println("FECHAR CLIENTES");
                ArrayList<DataListaServers> listaServersCli = new ArrayList<>();
                ArrayList<HeardBeat> listaServidores = threadTCPClient.getListaServers();
                listaServidores.sort(new HeardBeatComparator());
                listaServidores.remove(heardBeat);
                for (HeardBeat b : listaServidores) {
                    listaServersCli.add(new DataListaServers(b.getPortTCPClient(), "127.0.0.1"));
                }

                listaClientes = threadTCPClient.getListaClientes();
                ArrayList<ObjectOutputStream> objectOutput = threadTCPClient.getOutputStreamArrayList();

                for (int i = 0; i < listaClientes.size(); i++) {
                    try {
                        objectOutput.get(i).writeObject(listaServersCli);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        listaClientes.get(i).close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            if(heardBeat.getAlteracao().equalsIgnoreCase("Abort")){
                ThreadTrataClient.ignore = false;
                novaVersao = -1;
            }


            if(heardBeat.getAlteracao().equalsIgnoreCase("prepare") && heardBeat.getPortTCP() != ThreadTrataClient.PORT){
                try {
                    ThreadTrataClient.ignore = true;
                    MulticastSocket ds = new MulticastSocket(heardBeat.getPortTCP());
                    byte[] msgbytes = new byte[0];
                    dp = new DatagramPacket(msgbytes, 0, InetAddress.getByName("localhost"), heardBeat.getPortTCP());
                    ds.send(dp);
                    novaVersao = heardBeat.getVersaoBD();
                    System.out.println("NOVA VERSAO PREPARARE : " + novaVersao);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            if(heardBeat.getAlteracao().equalsIgnoreCase("commit") && heardBeat.getPortTCP() != ThreadTrataClient.PORT){
                DBHelper.serverQuery(nomeDB, heardBeat.getAlteracaoDB());
                if(novaVersao != -1){
                    System.out.println("NOVA VERSAO Commit : " + novaVersao);
                    DBHelper.serverQuery(nomeDB, "UPDATE versao SET versao_db=" + novaVersao);
                    myBDVersion = novaVersao;
                }
                ThreadTrataClient.ignore = true;
            }


        }
    }

    public  void verificaVersaoBD(){
        for (HeardBeat b : listaServidores) {
            if(b.getVersaoBD() > myBDVersion){
                System.out.println("versao maior");
                //enviar msg Multicast
                threadTCPClient.setNumLigacoes(0);
                HeardBeat heardBeattemp = new HeardBeat(port, false, myBDVersion,threadTCPClient.getNumLigacoes());
                heardBeattemp.setAlteracao("Versão Maior");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos;
                try {
                    oos = new ObjectOutputStream(baos);
                    oos.writeObject(heardBeattemp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte[] msgBytes = baos.toByteArray();
                DatagramPacket dp;
                try {
                    dp = new DatagramPacket(msgBytes, msgBytes.length, InetAddress.getByName(ServidorLogic.MULTICAST_IP),ServidorLogic.MULTICAST_PORT);
                    ms.send(dp);
                    myBDVersion = 10;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //fazer ligação para nova BD com o mesmo nome
                try {
                    Socket cliSocket = new Socket("localhost", b.getPortTCP());
                    OutputStream os = cliSocket.getOutputStream();
                    InputStream is = cliSocket.getInputStream();
                    cliSocket.setSoTimeout(4000);
                    FileOutputStream fs = new FileOutputStream(nomeDB);

                    os.write("dataBase".getBytes());

                    byte[] msgBuffer = new byte[MAX_DATA];
                    int nBytes;
                    while((nBytes = is.read(msgBuffer)) != -1){
                        fs.write(msgBuffer, 0, nBytes);
                        System.out.println("Numero de bytes recebidos : "+ nBytes);
                        System.out.println("Ficheiro recebido");
                    }

                    myBDVersion = DBHelper.getDBVersion(nomeDB);
                    cliSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public  void removeTimePassed(){
        Calendar aux = Calendar.getInstance();
        int time = aux.get(Calendar.HOUR_OF_DAY)*3600+aux.get(Calendar.MINUTE)*60+aux.get(Calendar.SECOND);

            for (HeardBeat i:listaServidores) {
                if(i.getCalendar() != null){
                    int t= i.getCalendar().get(Calendar.HOUR_OF_DAY)*3600+ i.getCalendar().get(Calendar.MINUTE)*60+i.getCalendar().get(Calendar.SECOND);
                    if(time - t > 35){
                        listaServidores.remove(i);
                    }
                }

            }
    }


    private void enviaListaServidores(){
        ArrayList<HeardBeat> listaServidores = threadTCPClient.getListaServers();
        listaServidores.sort(new HeardBeatComparator());
        ArrayList<DataListaServers> listaServersCli = new ArrayList<>();
        ArrayList<ObjectOutputStream> objectOutput = threadTCPClient.getOutputStreamArrayList();

        for (HeardBeat b : listaServidores) {
            listaServersCli.add(new DataListaServers(b.getPortTCPClient(), "127.0.0.1"));
        }

        ArrayList<Socket>listaClientes = threadTCPClient.getListaClientes();
        for (int i = 0; i < listaClientes.size(); i++) {
            try {
                objectOutput.get(i).writeObject(listaServersCli);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void setKeepGoing(boolean keepGoing) {
        this.keepGoing = keepGoing;
    }

    public ArrayList<HeardBeat> getListaServidores() {
        return listaServidores;
    }

    public void setMyBDVersion(int myBDVersion) {
        this.myBDVersion = myBDVersion;
    }
}
