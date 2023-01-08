package pl.Filipe.Patricia.Sandra.Cliente;

import pl.Filipe.Patricia.Sandra.Servidor.DataListaServers;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

public class ThreadGetServers extends Thread{
    private ArrayList<DataListaServers> listaServidores;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private ClientUI clientUI;
    public ThreadGetServers(ArrayList<DataListaServers> listaServers, ObjectInputStream is, ObjectOutputStream os, ClientUI clientUI)  {
        this.listaServidores = listaServers;
        this.ois = is;
        this.oos = os;
        this.clientUI = clientUI;
    }

    @Override
    public void run() {
        try {
            while (true){
                Object msgRec = ois.readObject();
                if(msgRec instanceof ArrayList){
                    listaServidores = (ArrayList<DataListaServers>) msgRec;
                    System.out.println("NOVA LISTA   :" +   listaServidores);
                }else if(msgRec instanceof String){
                    String msgRecebida = String.valueOf(msgRec);
                    System.out.println("mensagem do Serv:" + msgRecebida);

                    if(((String) msgRec).contains("User successfully authenticated.")){
                        clientUI.setAutenticado(true);
                        clientUI.setAdmnistrator(false);
                        StringTokenizer st = new StringTokenizer(msgRecebida, ":");
                        st.nextToken();
                        String obterNome = st.nextToken();
                        clientUI.setNome(obterNome);

                        System.out.println(clientUI.getMenu());

                    }
                    if(msgRec.equals("Admin successfully authenticated.")){
                        clientUI.setAutenticado(true);
                        clientUI.setAdmnistrator(true);
                        clientUI.setNome("admin");

                        System.out.println(clientUI.getMenu());

                    }
                    if(msgRec.equals("Show selection not possible.")){
                        clientUI.setEspetaculo(-1);
                    }
                    if(msgRec.equals("Delete successfully of unpaid reserve.")){
                        clientUI.setReservaEliminar(-1);
                    }
                    if(((String) msgRec).contains("ID max espetaculo na BD:")){
                        StringTokenizer st = new StringTokenizer(msgRecebida, ":");
                        st.nextToken();
                        String obterID= st.nextToken();
                        if(obterID.equals("vazio")){
                            clientUI.setIdMax(1);
                        }
                        else{
                            int id = Integer.parseInt(obterID);
                            clientUI.setIdMax(++id);
                        }
                    }

                    if(((String) msgRec).contains("ID do user:")){
                        StringTokenizer st = new StringTokenizer(msgRecebida, ":");
                        st.nextToken();
                        String obterID= st.nextToken();
                        clientUI.setUserID(Integer.valueOf(obterID));
                    }

                    if(msgRec.equals("Logout successfully.")){
                        clientUI.setAutenticado(false);
                        clientUI.setAdmnistrator(false);
                        clientUI.setNome("sem nome");
                        clientUI.setUsername("sem nome");
                        clientUI.setPassword("sem nome");
                        clientUI.setEspetaculo(-1);
                        clientUI.setReservaEliminar(-1);

                        System.out.println(clientUI.getMenu());

                    }
                    System.out.println("->");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Servidor Fechado");
        }
    }

    public ArrayList<DataListaServers> getListaServers() {
        return listaServidores;
    }

}
