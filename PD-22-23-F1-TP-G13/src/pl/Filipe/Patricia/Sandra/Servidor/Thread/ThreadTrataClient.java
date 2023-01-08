package pl.Filipe.Patricia.Sandra.Servidor.Thread;


import pl.Filipe.Patricia.Sandra.Cliente.MsgCliServer;
import pl.Filipe.Patricia.Sandra.Servidor.HeardBeat;
import pl.Filipe.Patricia.Sandra.Servidor.ServidorLogic;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.net.*;

public class ThreadTrataClient extends Thread{
    public static int PORT = 0;
    private Socket cliSocket;
    private MulticastSocket ms;
    private String nomeBD;
    private ArrayList<HeardBeat> listaServidores;
    private ServerThreadReceiveBeat serverThreadReceiveBeat;
    private ThreadTCPClient threadTCPClient;
    public static boolean ignore = false;
    private String nomeAux;

    private  MulticastSocket socket;

    private ObjectInputStream objectInput;

    private ObjectOutputStream objectOutput;
    public ThreadTrataClient(Socket cliSocket, MulticastSocket ms, String nomeBD, ServerThreadReceiveBeat threadReceiveBeat, ThreadTCPClient threadTCPClient, ObjectInputStream objectInput, ObjectOutputStream objectOutput) {
        this.cliSocket = cliSocket;
        this.ms = ms;
        this.nomeBD = nomeBD;
        this.serverThreadReceiveBeat = threadReceiveBeat;
        this.threadTCPClient = threadTCPClient;
        this.objectInput = objectInput;
        this.objectOutput = objectOutput;
    }

    @Override
    public void run() {
        //Receber dados

        while(true){
            try {
                MsgCliServer object = null;
                try {
                    object = (MsgCliServer) objectInput.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                MsgCliServer msgRec = object;

                if(msgRec.getOpcao()==1){
                    if(!DBHelper.verifyIfUserExistsForRegistration(nomeBD,msgRec)){
                        objectOutput.writeObject("Inserting user...");
                    }
                    else {
                        objectOutput.writeObject("Insertion not possible");
                        continue;
                    }
                }

                if(msgRec.getOpcao()==2){
                    if(DBHelper.verifyIfUserExistsForAuthentication(nomeBD,msgRec)){
                        nomeAux = DBHelper.getUserName(nomeBD,msgRec);
                        objectOutput.writeObject("Authentication in process...");
                    }
                    else {
                        objectOutput.writeObject("Authentication not possible.");
                        continue;
                    }
                }

                if(msgRec.getOpcao()==3){
                    if(DBHelper.verifyIfAdminExistsForAuthentication(nomeBD,msgRec)){
                        objectOutput.writeObject("Authentication in process...");
                    }
                    else {
                        objectOutput.writeObject("Authentication not possible.");
                        continue;
                    }
                }

                if(msgRec.getOpcao()==4){
                    objectOutput.writeObject("Updating in process...");
                }

                if(msgRec.getOpcao()==5 || msgRec.getOpcao()==6 || msgRec.getOpcao()==7 || msgRec.getOpcao()==9){
                    objectOutput.writeObject("Consultation in process...");
                }

                if(msgRec.getOpcao()==8){
                    if(DBHelper.verifyIfEspetaculoExistsAndHappens24HrLatter(nomeBD,msgRec)){
                        objectOutput.writeObject("Show selection in process...");
                    }
                    else {
                        objectOutput.writeObject("Show selection not possible.");
                        continue;
                    }
                }

                if(msgRec.getOpcao()==11){
                    if(DBHelper.verifyIfReservaExistsForDelete(nomeBD,msgRec)){
                        objectOutput.writeObject("Deleting in process...");
                    }
                    else {
                        objectOutput.writeObject("Delete not possible.");
                        continue;
                    }
                }

                if(msgRec.getOpcao()==13){
                    if(DBHelper.verifyIfEspetaculoExistsForDeleteAndHasNoPaidReservesAssociated(nomeBD,msgRec)){
                        objectOutput.writeObject("Show deleting in process...");
                    }
                    else {
                        objectOutput.writeObject("Show delete not possible.");
                        continue;
                    }
                }

                if(msgRec.getOpcao()==14){
                    if(DBHelper.verifyIfShowExistsAndIsNotVisible(nomeBD,msgRec)){
                        objectOutput.writeObject("Turning show visible in process...");
                    }
                    else {
                        objectOutput.writeObject("Operation not possible.");
                        continue;
                    }
                }

                if(!ignore){
                    if(msgRec.getSqlQuery().toLowerCase().contains("insert") || msgRec.getSqlQuery().toLowerCase().contains("update")|| msgRec.getSqlQuery().toLowerCase().contains("delete")){
                        socket = new MulticastSocket();
                        PORT = socket.getLocalPort();
                        HeardBeat heardBeattemp = new HeardBeat(socket.getLocalPort(), true, DBHelper.getDBVersion(nomeBD) , 0);


                        heardBeattemp.setAlteracao("Prepare");
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ObjectOutputStream oos;
                        oos = new ObjectOutputStream(baos);
                        oos.writeObject(heardBeattemp);
                        byte[] msgBytes = baos.toByteArray();
                        DatagramPacket dp = new DatagramPacket(msgBytes, msgBytes.length, InetAddress.getByName(ServidorLogic.MULTICAST_IP),ServidorLogic.MULTICAST_PORT);
                        ms.send(dp);

                        listaServidores = serverThreadReceiveBeat.getListaServidores();

                        socket.setSoTimeout(1000);
                        byte[] bytes = new byte[0];
                        for (int i = 0; i < listaServidores.size() - 1; i++) {
                            socket.receive(new DatagramPacket(bytes, bytes.length));
                        }

                        heardBeattemp = new HeardBeat(socket.getLocalPort(), true, DBHelper.getDBVersion(nomeBD) + 1, 0);
                        heardBeattemp.setAlteracao("Commit");
                        heardBeattemp.setAlteracaoDB(msgRec.getSqlQuery());
                        baos = new ByteArrayOutputStream();
                        oos = new ObjectOutputStream(baos);
                        oos.writeObject(heardBeattemp);
                        msgBytes = baos.toByteArray();
                        dp = new DatagramPacket(msgBytes, msgBytes.length, InetAddress.getByName(ServidorLogic.MULTICAST_IP),ServidorLogic.MULTICAST_PORT);
                        ms.send(dp);
                        int novaVersao= DBHelper.getDBVersion(nomeBD) + 1;
                        DBHelper.queryDB(nomeBD, msgRec);
                        DBHelper.serverQuery(nomeBD, "UPDATE versao SET versao_db=" + novaVersao);
                        serverThreadReceiveBeat.setMyBDVersion(novaVersao);

                        if(msgRec.getOpcao()==1){
                            objectOutput.writeObject("User successfully inserted in Database.");
                        }

                        if(msgRec.getOpcao()==2){
                            objectOutput.writeObject("User successfully authenticated."+"Name of the user:"+ nomeAux);
                        }

                        if(msgRec.getOpcao()==3){
                            objectOutput.writeObject("Admin successfully authenticated.");
                        }

                        if(msgRec.getOpcao()==4){
                            objectOutput.writeObject("User data successfully updated.");
                        }

                        if(msgRec.getOpcao()==10){
                            objectOutput.writeObject("Reservation successfully done.");
                        }

                        if(msgRec.getOpcao()==11){
                            objectOutput.writeObject("Delete successfully of unpaid reserve.");
                        }

                        if(msgRec.getOpcao()==13){
                            objectOutput.writeObject("Show delete successfully.");
                        }

                        if(msgRec.getOpcao()==14){
                            objectOutput.writeObject("Operation successfully.");
                        }

                        if(msgRec.getOpcao()==16){
                            objectOutput.writeObject("Logout successfully.");
                        }
                        socket.close();
                    }
                    else if(msgRec.getSqlQuery().toLowerCase().contains("select")){
                        //DBHelper.queryDB(nomeBD, msgRec);
                        if(msgRec.getOpcao()==5 || msgRec.getOpcao()==6){
                            String resultado = DBHelper.queryDBGetReservations(nomeBD,msgRec);
                            objectOutput.writeObject(resultado);
                        }
                        if(msgRec.getOpcao()==7){
                            String resultado = DBHelper.queryDBGetEspetaculos(nomeBD,msgRec);
                            objectOutput.writeObject(resultado);
                        }
                        if(msgRec.getOpcao()==8){
                            DBHelper.queryDB(nomeBD,msgRec);
                            objectOutput.writeObject("Show selected successfully");
                        }
                        if(msgRec.getOpcao()==9){
                            String resultado = DBHelper.queryDBGetLugaresPrecos(nomeBD,msgRec);
                            objectOutput.writeObject(resultado);
                        }

                        if(msgRec.getOpcao()==0){
                            String resultado = DBHelper.queryDBGetMaxIDEspetaculo(nomeBD,msgRec);
                            objectOutput.writeObject("ID max espetaculo na BD:"+resultado);
                        }

                        if(msgRec.getOpcao()==10){
                            String resultado = DBHelper.queryDBGetUserID(nomeBD,msgRec);
                            objectOutput.writeObject("ID do user:"+resultado);
                        }
                    }

                }else{
                    objectOutput.writeObject("Servidor Indisponivel");
                }


            } catch (SocketTimeoutException e){
                e.printStackTrace();
                sencondTime(objectOutput);
            }

            catch (IOException e) {
                e.printStackTrace();
                try {
                    threadTCPClient.getListaClientes().remove(cliSocket);
                    threadTCPClient.getOutputStreamArrayList().remove(objectOutput);
                    threadTCPClient.setNumLigacoes(threadTCPClient.getNumLigacoes() - 1);
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
                    return;
                } catch (IOException ex) {
                    return;
                }
            }

        }
    }

    public void sencondTime(ObjectOutputStream objectOutput){

        try{
            HeardBeat heardBeattemp = new HeardBeat(socket.getLocalPort(), true, DBHelper.getDBVersion(nomeBD) , 0);

            heardBeattemp.setAlteracao("Prepare");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos;
            oos = new ObjectOutputStream(baos);
            oos.writeObject(heardBeattemp);
            byte[] msgBytes = baos.toByteArray();
            DatagramPacket dp = new DatagramPacket(msgBytes, msgBytes.length, InetAddress.getByName(ServidorLogic.MULTICAST_IP),ServidorLogic.MULTICAST_PORT);
            ms.send(dp);

            listaServidores = serverThreadReceiveBeat.getListaServidores();

            socket.setSoTimeout(1000);
            byte[] bytes = new byte[0];
            for (int i = 0; i < listaServidores.size() - 1; i++) {
                socket.receive(new DatagramPacket(bytes, bytes.length));
            }

        }catch (SocketTimeoutException e){
            try{
                HeardBeat heardBeattemp = new HeardBeat(socket.getLocalPort(), true, DBHelper.getDBVersion(nomeBD) , 0);
                heardBeattemp.setAlteracao("Abort");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos;
                oos = new ObjectOutputStream(baos);
                oos.writeObject(heardBeattemp);
                byte[] msgBytes = baos.toByteArray();
                DatagramPacket dp = new DatagramPacket(msgBytes, msgBytes.length, InetAddress.getByName(ServidorLogic.MULTICAST_IP),ServidorLogic.MULTICAST_PORT);
                ms.send(dp);
                socket.close();
                objectOutput.writeObject("Impossible to sync all servers. Aborting the task.....");
            }catch (IOException ei){
                ei.printStackTrace();
            }
            e.printStackTrace();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
