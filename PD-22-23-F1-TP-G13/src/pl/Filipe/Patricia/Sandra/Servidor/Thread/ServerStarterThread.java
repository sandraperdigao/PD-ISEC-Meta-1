package pl.Filipe.Patricia.Sandra.Servidor.Thread;

import pl.Filipe.Patricia.Sandra.Servidor.CriaBaseDeDados;
import pl.Filipe.Patricia.Sandra.Servidor.HeardBeat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServerStarterThread extends Thread{

    private static final int TIMEOUT = 30000;
    public static final String NOME_DB_VAZIO = "BD_VAZIO";
    private MulticastSocket ms;
    private HeardBeat heardBeat;
    private int version_bd;
    private ArrayList<HeardBeat> listaServidores;
    private Map<Integer, InetAddress> servidoresLigacao;
    private String newBdName;

    public ServerStarterThread(MulticastSocket ms, ArrayList<HeardBeat> listaServidores, int version_bd, String arg) {
        this.ms = ms;
        this.listaServidores = listaServidores;
        this.version_bd = version_bd;
        this.servidoresLigacao = new HashMap<>();
        newBdName = arg;
    }

    public ServerStarterThread(MulticastSocket ms, ArrayList<HeardBeat> listaServidores, int dbVersion) {
        this.ms = ms;
        this.listaServidores = listaServidores;
        this.version_bd = dbVersion;
        this.servidoresLigacao = new HashMap<>();
        newBdName = NOME_DB_VAZIO;

    }

    private int verificaVersao(){
        int index = -1;
        for (int i = 0; i < listaServidores.size(); i++) {
            if(version_bd < listaServidores.get(i).getVersaoBD()){
                index = i;
            }
        }
      return index;
    }


    public String getNewBdName() {
        return newBdName;
    }

    private int verificaNumligacoes(int index) {
        int i;
        for (i = 0; i < listaServidores.size(); i++) {
            if (listaServidores.get(index).getNumLigacoesTCP() > listaServidores.get(i).getNumLigacoesTCP()
                    && listaServidores.get(index).getVersaoBD() == listaServidores.get(i).getVersaoBD()) {
                index = i;
            }
        }
        return index;
    }

    public void setNewBdName(String newBdName) {
        this.newBdName = newBdName;
    }

    public int getVersion_bd() {
        return version_bd;
    }

    public void setVersion_bd(int version_bd) {
        this.version_bd = version_bd;
    }


    @Override
    public void run() {
        long start = System.currentTimeMillis();
        long end = start + TIMEOUT; // 60 seconds * 1000 ms/sec mudar 30
        while (System.currentTimeMillis() < end)
        {
            DatagramPacket dp = new DatagramPacket(new byte[5000], 5000);
            try {
                ms.setSoTimeout(TIMEOUT); // 30 para timeout
                ms.receive(dp);

            ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
            ObjectInputStream ois;
                ois = new ObjectInputStream(bais);
                heardBeat = (HeardBeat) ois.readObject();
                if(heardBeat.getAlteracao().equalsIgnoreCase("Sem alteração")){
                    servidoresLigacao.put(heardBeat.getPortTCP(), dp.getAddress());

                    System.out.println(heardBeat);
                    if(!listaServidores.contains(heardBeat)){
                        listaServidores.add(heardBeat);
                    }else{
                        for (int i = 0; i < listaServidores.size(); i++) {
                            if(listaServidores.get(i).getPortTCP() == heardBeat.getPortTCP()
                                    && listaServidores.get(i).getVersaoBD() > heardBeat.getVersaoBD()){
                                listaServidores.remove(heardBeat);
                                listaServidores.add(heardBeat);
                            }

                        }
                    }
                }


            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        System.out.println(servidoresLigacao);
        if(listaServidores.isEmpty() && version_bd == -1){
            CriaBaseDeDados criaBaseDeDados = new CriaBaseDeDados();
            setNewBdName(criaBaseDeDados.criar());
        }else{
            int versaoMax;
            versaoMax = verificaVersao();
            int index = -1;
            if(versaoMax != -1){
                index = verificaNumligacoes(versaoMax);
            }

            if(index!=-1) {
                //thread download Db
                ServerTCPConnection serverTCPConnection;
                if (newBdName.equalsIgnoreCase(NOME_DB_VAZIO)) {
                    serverTCPConnection = new ServerTCPConnection(
                            listaServidores.get(index).getPortTCP(),
                            servidoresLigacao.get(listaServidores.get(index).getPortTCP()));
                } else {
                    serverTCPConnection = new ServerTCPConnection(
                            listaServidores.get(index).getPortTCP(),
                            servidoresLigacao.get(listaServidores.get(index).getPortTCP()), newBdName);
                }

                serverTCPConnection.start();
                try {
                    serverTCPConnection.join();
                    setNewBdName(serverTCPConnection.getNomeDB());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
