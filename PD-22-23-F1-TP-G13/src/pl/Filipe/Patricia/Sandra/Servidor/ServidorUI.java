package pl.Filipe.Patricia.Sandra.Servidor;

import pl.Filipe.Patricia.Sandra.Servidor.Thread.*;

import java.util.Objects;

public class ServidorUI {

    public void run(String[] args) {
        ServidorLogic servidorLogic;
        ServerTCPSendDB serverTCPSendDB;
        if(args.length > 1){
            servidorLogic = new ServidorLogic(args[1]);
            serverTCPSendDB = new ServerTCPSendDB(servidorLogic.getSs(), args[1]);
            serverTCPSendDB.setDaemon(true);
            serverTCPSendDB.start();
        }else{
            servidorLogic = new ServidorLogic();

        }

        System.out.println("versao " + servidorLogic.getDBVersion());

       ServerStarterThread serverStarterThread;

       if(args.length > 1)
           serverStarterThread = new ServerStarterThread(servidorLogic.getMs(), servidorLogic.getListaServidores(), servidorLogic.getDBVersion() , args[1]);
       else
           serverStarterThread = new ServerStarterThread(servidorLogic.getMs(), servidorLogic.getListaServidores(), servidorLogic.getDBVersion());

        serverStarterThread.start();

       try {
            serverStarterThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

       if(!Objects.equals(serverStarterThread.getNewBdName(), ServerStarterThread.NOME_DB_VAZIO)){
           servidorLogic.setDbConn(serverStarterThread.getNewBdName());
       }

        ServerGetClientUDP serverGetClientUDP = new ServerGetClientUDP(servidorLogic.getListaServidores(), Integer.parseInt(args[0]));
        serverGetClientUDP.setDaemon(true);
        serverGetClientUDP.start();
        ThreadTCPClient threadTCPClient;
        if(args.length > 1) {
             threadTCPClient = new ThreadTCPClient(servidorLogic.getMs(), args[1]);

        }else{
            threadTCPClient = new ThreadTCPClient(servidorLogic.getMs(), serverStarterThread.getNewBdName());
        }
        threadTCPClient.start();

        ServerThreadSendBeat serverThreadSendBeat;
        if(args.length > 1) {
            serverThreadSendBeat  = new ServerThreadSendBeat(servidorLogic.getMs(), servidorLogic.getAutomaticPort(), servidorLogic.getDBVersion(), args[1] ,threadTCPClient);
        }else{
             serverThreadSendBeat = new ServerThreadSendBeat(servidorLogic.getMs(), servidorLogic.getAutomaticPort(), servidorLogic.getDBVersion(), serverStarterThread.getNewBdName(), threadTCPClient);
        }
        serverThreadSendBeat.start();
        ServerThreadReceiveBeat serverThreadReceiveBeat;
        if(args.length > 1){
            serverThreadReceiveBeat = new ServerThreadReceiveBeat(servidorLogic.getMs(), servidorLogic.getListaServidores(), servidorLogic.getDBVersion(), servidorLogic.getAutomaticPort(),  args[1], threadTCPClient);
        }else{
            serverThreadReceiveBeat = new ServerThreadReceiveBeat(servidorLogic.getMs(), servidorLogic.getListaServidores(), servidorLogic.getDBVersion(), servidorLogic.getAutomaticPort(), serverStarterThread.getNewBdName(), threadTCPClient);

        }
        serverThreadReceiveBeat.start();
        threadTCPClient.setServerThreadReceiveBeat(serverThreadReceiveBeat);



        while(!PDInput.readString("Digite o comando", true).equals("fim"))
            ;

            System.out.println("Srvidor BD" + servidorLogic.getDBVersion());

        servidorLogic.leaveGroup();
         serverThreadSendBeat.setKeepGoing(false);
        serverThreadReceiveBeat.setKeepGoing(false);
        serverGetClientUDP.setKeepGoing(false);
        try{
            serverThreadSendBeat.join();
            serverThreadReceiveBeat.join();

        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }



}
