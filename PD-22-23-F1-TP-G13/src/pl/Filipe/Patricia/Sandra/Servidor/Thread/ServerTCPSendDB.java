package pl.Filipe.Patricia.Sandra.Servidor.Thread;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTCPSendDB extends Thread{
    private static final int MAX_DATA = 4000;
    private ServerSocket ss;
    private String dirFile;

    public ServerTCPSendDB(ServerSocket ss, String dirFile) {
        this.ss = ss;
        this.dirFile = dirFile;
    }

    @Override
    public void run() {
        boolean keepGoing = true;
        try {
            while(keepGoing){
                Socket cliSocket = ss.accept();
                OutputStream os = cliSocket.getOutputStream();
                InputStream is = cliSocket.getInputStream();

                byte[] curBytes = new byte[MAX_DATA];
                byte[] msgBuffer = new byte[MAX_DATA];

                int nBytes = is.read(msgBuffer);
                String msgRec = new String(msgBuffer, 0, nBytes);
                if(!msgRec.equals("Cliente")){
                    File fich = new File(dirFile);
                    FileInputStream fs = new FileInputStream(fich);

                    int valor;
                    while((valor = fs.read(curBytes)) != -1){
                        os.write(curBytes, 0 , valor);
                    }
                }
                cliSocket.close();
            }
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
