package pl.Filipe.Patricia.Sandra.Cliente;

import pl.Filipe.Patricia.Sandra.Servidor.DataListaServers;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

public class ClientLogic {
    private InetAddress ipServer;
    private int portServer;
    private DatagramSocket socket;
    private ArrayList<DataListaServers> listaServidores;

    public ClientLogic() {
        this.listaServidores = new ArrayList<>();
    }

    public int verificaLigacao(String[] args){
        try{
            ipServer= InetAddress.getByName(args[0]);
            portServer = Integer.parseInt(args[1]);
            socket = new DatagramSocket();
        } catch (UnknownHostException e){
            e.printStackTrace();
            return 1;
        } catch (IOException e){
            e.printStackTrace();
            return 2;
        }
        return 0;
    }

    public int enviaUDP(){
        try {
            socket.setSoTimeout(3000);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject("ligacao");

            byte[] msgBytes = baos.toByteArray();

            DatagramPacket dpSend = new DatagramPacket(msgBytes, msgBytes.length, ipServer, portServer);

            socket.send(dpSend);
            System.out.println("enviou");

            DatagramPacket dpRec = new DatagramPacket(new byte[5000], 5000);
            socket.receive(dpRec);
            System.out.println("Recebeu");

            ByteArrayInputStream bais = new ByteArrayInputStream(dpRec.getData()); // 1 Passo
            ObjectInputStream ois = new ObjectInputStream(bais);// 2 passo
            listaServidores= (ArrayList<DataListaServers>) ois.readObject();


            System.out.println("Ip servidor: " + dpRec.getAddress().getHostAddress() + "  \nPorto: " + dpRec.getPort()
                    + "\nLista de Servidores\n" + listaServidores);

            bais.close();
            ois.close();
            baos.close();
            oos.close();
            socket.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    public void ligacaoTCP(){
        //ciclo e reiniiiar o i do ciclo for e atualizar a lista de servidores
        ClientUI clientUI = new ClientUI();

        for (int i = 0; i < listaServidores.size(); i++) {
            Socket cliSocket;
            ThreadGetServers threadGetServers = null;
            try {
                cliSocket = new Socket(listaServidores.get(i).getIp(), listaServidores.get(i).getPort());
                int res = 0;
                ObjectOutputStream oos = new ObjectOutputStream(cliSocket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(cliSocket.getInputStream());

                threadGetServers = new ThreadGetServers(listaServidores, ois, oos, clientUI);
                threadGetServers.start();

                //set ID max espetaculo
                String sqlQuery2 = "SELECT id FROM espetaculo WHERE id = (SELECT max(id) FROM espetaculo)";
                MsgCliServer ms2 = new MsgCliServer(sqlQuery2,0);
                oos.writeObject(ms2);

                while ((res = clientUI.runUI()) != -1){

                    if(!clientUI.isAutenticado()) {

                        if (res == 1) {
                            String sqlQuery = "INSERT INTO utilizador VALUES (NULL,'" + clientUI.getUsername() + "','" + clientUI.getNome() +
                                    "','" + clientUI.getPassword() + "','" + "0" + "','" + "0" + "')";
                            MsgCliServer msg = new MsgCliServer(sqlQuery, 1);
                            oos.writeObject(msg);
                        }
                        if (res == 2) {
                            String sqlQuery = "UPDATE utilizador SET autenticado=1" + " WHERE username='" + clientUI.getUsername() + "'"
                                    + "AND password='" + clientUI.getPassword() + "'";
                            MsgCliServer msg = new MsgCliServer(sqlQuery, 2);
                            oos.writeObject(msg);
                        }
                        if (res == 3) {
                            String sqlQuery = "UPDATE utilizador SET autenticado=1" + " WHERE username='" + clientUI.getUsername() + "'"
                                    + "AND password='" + clientUI.getPassword() + "'";
                            MsgCliServer msg = new MsgCliServer(sqlQuery, 3);
                            oos.writeObject(msg);
                        }
                    }
                    if (clientUI.isAutenticado()) {

                        if(res == 4){
                            String sqlQuery = " ";
                            switch (clientUI.getAlteracao()){
                                case 1 -> sqlQuery = "UPDATE utilizador SET nome='" + clientUI.getNome() + "'" +  " WHERE username='" +  clientUI.getUsername() + "'"
                                        + "AND password='" + clientUI.getPassword() + "'";
                                case 2 -> sqlQuery = "UPDATE utilizador SET username='" + clientUI.getUsername() + "'" +  " WHERE nome='" +  clientUI.getNome() + "'"
                                        + "AND password='" + clientUI.getPassword() + "'";
                                case 3 -> sqlQuery = "UPDATE utilizador SET password='" + clientUI.getPassword() + "'" +  " WHERE username='" +  clientUI.getUsername() + "'"
                                        + "AND nome='" + clientUI.getNome() + "'";
                            }

                            MsgCliServer msg = new MsgCliServer(sqlQuery, 4);
                            oos.writeObject(msg);
                        }
                        if(res == 5){
                            String sqlQuery = "SELECT * from RESERVA where pago= " + 0;

                            MsgCliServer msg = new MsgCliServer(sqlQuery, 5);
                            oos.writeObject(msg);
                        }
                        if(res == 6){
                            String sqlQuery = "SELECT * from RESERVA where pago= " + 1;

                            MsgCliServer msg = new MsgCliServer(sqlQuery, 6);
                            oos.writeObject(msg);
                        }
                        if(res == 7){
                            String sqlQuery = " ";
                            if(clientUI.isAdmnistrator()){
                                if(clientUI.getLocalidade().equalsIgnoreCase("sem nome") && clientUI.getClassificacao_etaria() == -1
                                        && clientUI.getPais().equalsIgnoreCase("sem nome")){
                                    sqlQuery=  "SELECT * FROM espetaculo";
                                }else if(clientUI.getLocalidade().equalsIgnoreCase("sem nome") && clientUI.getClassificacao_etaria() != -1
                                        && clientUI.getPais().equalsIgnoreCase("sem nome")){
                                    sqlQuery=  "SELECT * FROM espetaculo WHERE classificacao_etaria='" + clientUI.getClassificacao_etaria() + "'";
                                }else if(clientUI.getLocalidade().equalsIgnoreCase("sem nome") && clientUI.getClassificacao_etaria() == -1
                                        && !clientUI.getPais().equalsIgnoreCase("sem nome")){
                                    sqlQuery=  "SELECT * FROM espetaculo WHERE pais='" + clientUI.getPais() + "'";

                                }
                                else if(!clientUI.getLocalidade().equalsIgnoreCase("sem nome") && clientUI.getClassificacao_etaria() == -1
                                        && clientUI.getPais().equalsIgnoreCase("sem nome")){
                                    sqlQuery=  "SELECT * FROM espetaculo WHERE localidade='" + clientUI.getLocalidade() + "'";
                                }
                                else if(!clientUI.getLocalidade().equalsIgnoreCase("sem nome") && clientUI.getClassificacao_etaria() != -1
                                        && !clientUI.getPais().equalsIgnoreCase("sem nome")){
                                    sqlQuery=  "SELECT * FROM espetaculo WHERE localidade='" + clientUI.getLocalidade() + "' AND pais='" + clientUI.getPais() + "' AND classificacao_etaria = '" + clientUI.getClassificacao_etaria() + "'";
                                }
                            }
                            else {
                                if(clientUI.getLocalidade().equalsIgnoreCase("sem nome") && clientUI.getClassificacao_etaria() == -1
                                        && clientUI.getPais().equalsIgnoreCase("sem nome")){
                                    sqlQuery=  "SELECT * FROM espetaculo WHERE visivel=1";
                                }else if(clientUI.getLocalidade().equalsIgnoreCase("sem nome") && clientUI.getClassificacao_etaria() != -1
                                        && clientUI.getPais().equalsIgnoreCase("sem nome")){
                                    sqlQuery=  "SELECT * FROM espetaculo WHERE classificacao_etaria='" + clientUI.getClassificacao_etaria() + "' AND visivel=1";
                                }else if(clientUI.getLocalidade().equalsIgnoreCase("sem nome") && clientUI.getClassificacao_etaria() == -1
                                        && !clientUI.getPais().equalsIgnoreCase("sem nome")){
                                    sqlQuery=  "SELECT * FROM espetaculo WHERE pais='" + clientUI.getPais() + "' AND visivel=1";

                                }
                                else if(!clientUI.getLocalidade().equalsIgnoreCase("sem nome") && clientUI.getClassificacao_etaria() == -1
                                        && clientUI.getPais().equalsIgnoreCase("sem nome")){
                                    sqlQuery=  "SELECT * FROM espetaculo WHERE localidade='" + clientUI.getLocalidade() + "' AND visivel=1";
                                }
                                else if(!clientUI.getLocalidade().equalsIgnoreCase("sem nome") && clientUI.getClassificacao_etaria() != -1
                                        && !clientUI.getPais().equalsIgnoreCase("sem nome")){
                                    sqlQuery=  "SELECT * FROM espetaculo WHERE localidade='" + clientUI.getLocalidade() + "' AND pais='" + clientUI.getPais() + "' AND classificacao_etaria = '" + clientUI.getClassificacao_etaria() + "' AND visivel=1";
                                }
                            }

                            MsgCliServer msg = new MsgCliServer(sqlQuery, 7);
                            oos.writeObject(msg);
                        }
                        if(res == 8){
                            if(clientUI.isAdmnistrator()){
                                String sqlQuery = "SELECT data_hora from ESPETACULO where id='" + clientUI.getEspetaculo()+ "'";
                                MsgCliServer msg = new MsgCliServer(sqlQuery, 8);
                                oos.writeObject(msg);
                            }
                            else{
                                String sqlQuery = "SELECT data_hora from ESPETACULO where id='" + clientUI.getEspetaculo()+ "' AND visivel=1";
                                MsgCliServer msg = new MsgCliServer(sqlQuery, 8);
                                oos.writeObject(msg);
                            }
                        }
                        if(res == 9){
                            if(clientUI.getEspetaculo()!=-1){
                                String sqlQuery = "SELECT * from LUGAR where espetaculo_id='" + clientUI.getEspetaculo()+ "'";
                                MsgCliServer msg = new MsgCliServer(sqlQuery, 9);
                                oos.writeObject(msg);
                            }
                            else {
                                System.out.println("Pf primeiro escolha 8 - Seleção de um espetáculo");
                                continue;
                            }
                        }

                        if(res == 10){
                            if(clientUI.getEspetaculo()!=-1 && clientUI.getLugares().size()>0){
                                for(int k=0; k<clientUI.getLugares().size(); k++){

                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                                    Date date = new Date();
                                    String strdate = simpleDateFormat.format(date);
                                    System.out.println(strdate);

                                    String sqlQuery4 = "SELECT id from utilizador where username='" + clientUI.getUsername() + "'";
                                    MsgCliServer msg2 = new MsgCliServer(sqlQuery4, 10);
                                    oos.writeObject(msg2);

                                    String sqlQuery = "INSERT INTO reserva VALUES (NULL,'" + strdate + "','" + 0 +
                                            "','" + clientUI.getUserID() + "','" + clientUI.getEspetaculo()+ "')";
                                    MsgCliServer msg = new MsgCliServer(sqlQuery, 10);
                                    oos.writeObject(msg);

                                }
                            }
                        }

                        if(res == 11){
                            String sqlQuery = "DELETE FROM reserva where pago='" + "0" + "' AND id='" +clientUI.getReservaEliminar() + "'";
                            MsgCliServer msg = new MsgCliServer(sqlQuery, 11);
                            oos.writeObject(msg);
                        }

                        if (clientUI.isAdmnistrator()) {
                            if(res == 12){
                                FileInputStream fstream = new FileInputStream("teste2.txt");
                                DataInputStream dstream = new DataInputStream(fstream);
                                BufferedReader bf = new BufferedReader(new InputStreamReader(dstream));

                                String firstLine = bf.readLine();
                                String descricao = firstLine.substring(firstLine.indexOf(";") + 1);
                                descricao.trim();
                                descricao = descricao.substring(1, descricao.length() - 1);

                                String secondLine = bf.readLine();
                                String tipo = secondLine.substring(secondLine.indexOf(":") + 1);
                                tipo.trim();
                                tipo = tipo.substring(1, tipo.length() - 1);

                                String thirdLine = bf.readLine();
                                String data = thirdLine.substring(thirdLine.indexOf(";") + 1);
                                data.trim();
                                data = data.replace("\"","").replace("”","").replace(";","-");
                                String fourLine = bf.readLine();
                                String hora = fourLine.substring(fourLine.indexOf(";") + 1);
                                hora.trim();
                                hora = hora.replace("\"","").replace("”","").replace(";",":");
                                data = data + " " + hora;

                                String fiveLine = bf.readLine();
                                String duracaoAux = fiveLine.substring(fiveLine.indexOf(";") + 1);
                                duracaoAux.trim();
                                duracaoAux = duracaoAux.substring(1, duracaoAux.length() - 1);
                                int duracao = Integer.parseInt(duracaoAux);

                                String sixLine = bf.readLine();
                                String local = sixLine.substring(sixLine.indexOf(";") + 1);
                                local.trim();
                                local = local.substring(1, local.length() - 1);

                                String sevenLine = bf.readLine();
                                String localidade = sevenLine.substring(sevenLine.indexOf(";") + 1);
                                localidade.trim();
                                localidade = localidade.substring(1, localidade.length() - 1);

                                String eightLine = bf.readLine();
                                String pais = eightLine.substring(eightLine.indexOf(";") + 1);
                                pais.trim();
                                pais = pais.substring(1, pais.length() - 1);

                                String nineLine = bf.readLine();
                                String classEtaria = nineLine.substring(nineLine.indexOf(";") + 1);
                                classEtaria.trim();
                                classEtaria = classEtaria.substring(1, classEtaria.length() - 1);

                                String sqlQuery = "INSERT INTO espetaculo VALUES (NULL,'"+descricao+"','"+tipo+"','"+data+"', '"+duracao+"','"+local+"','"+localidade+"','"+pais+"','"+classEtaria+"',0)";
                                MsgCliServer msg = new MsgCliServer(sqlQuery, 12);
                                oos.writeObject(msg);
                                String tenLine = bf.readLine();

                                String line;
                                while ((line = bf.readLine())!=null){
                                    StringTokenizer st = new StringTokenizer(line,";");
                                    String filaAux = st.nextToken();
                                    String fila = filaAux.substring(1, filaAux.length() - 1);
                                    while (st.hasMoreTokens()){
                                        String aux = st.nextToken();
                                        StringTokenizer stAux = new StringTokenizer(aux,":");
                                        String lugarAux = stAux.nextToken();
                                        String lugar;
                                        if(lugarAux.length() == 2)
                                            lugar = lugarAux.substring(1);
                                        else
                                            lugar = lugarAux.substring(2);

                                        String precoAux = stAux.nextToken();
                                        String preco = precoAux.substring(0, precoAux.length() - 1);
                                        Double preco1 = Double.parseDouble(preco);

                                        String sqlQuery1 = "INSERT INTO lugar VALUES(NULL,'"+fila+"','"+lugar+"','"+preco1+"','"+clientUI.getIdMax()+"')";
                                        MsgCliServer msg1 = new MsgCliServer(sqlQuery1, 12);
                                        oos.writeObject(msg1);

                                    }
                                }

                                System.out.println("All data are inserted in the database table");

                                //set ID max espetaculo
                                String sqlQuery3 = "SELECT id FROM espetaculo WHERE id = (SELECT max(id) FROM espetaculo)";
                                MsgCliServer ms3= new MsgCliServer(sqlQuery3,0);
                                oos.writeObject(ms3);

                                bf.close();
                            }

                            if(res == 13){

                                String sqlQuery = "DELETE FROM espetaculo where id='" +  clientUI.getEspetaculoEliminar() + "'";
                                MsgCliServer msg = new MsgCliServer(sqlQuery, 13);
                                oos.writeObject(msg);

                            }

                            if(res == 14){
                                String sqlQuery = "UPDATE espetaculo SET visivel=1" + " WHERE id='" +  clientUI.getEspetaculoVisivel() + "'";
                                MsgCliServer msg = new MsgCliServer(sqlQuery, 14);
                                oos.writeObject(msg);
                            }
                        }

                        if(res == 15){
                            if(clientUI.getEspetaculo() == -1){
                                System.out.println("Pf selecione primeiro o espetáculo 8-Seleção de um espetáculo");
                            }
                            else {
                                String[] strArray = clientUI.getLugaresSelecionados().split(" ");
                                ArrayList<Integer> intArrayList = clientUI.getLugares();
                                for (String string: strArray){
                                    if(!string.equals("")){
                                        intArrayList.add(Integer.parseInt(string));
                                    }
                                }
                                System.out.println("Lugares selecionados:");
                                System.out.println(clientUI.getLugares());
                            }
                        }

                        if(res == 16){
                            String sqlQuery = "UPDATE utilizador SET autenticado=0" + " WHERE username='" +  clientUI.getUsername() + "'"
                                    + "AND password='" + clientUI.getPassword() + "'";
                            MsgCliServer msg = new MsgCliServer(sqlQuery, 16);
                            oos.writeObject(msg);
                        }

                    }
                }

                cliSocket.close();
                return;

            }
            catch (IOException e) {
                e.printStackTrace();
                if(threadGetServers != null){
                    listaServidores = threadGetServers.getListaServers();
                }
                System.out.println("trocou de Server ");

            }
        }

        System.out.println("Fechou servidor : " + listaServidores);

    }


}
