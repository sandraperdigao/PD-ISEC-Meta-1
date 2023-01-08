package pl.Filipe.Patricia.Sandra.Cliente;

public class ClienteStarter {
    public ClienteStarter() {

    }

    protected void run(String[] args){
        ClientLogic cl = new ClientLogic();
        System.out.println("Bem Vindo ao Programa!");
        System.out.println("A tentar ligar ao servidor...");
        switch (cl.verificaLigacao(args)) {
            case 0 -> System.out.println("Criação do socket bem sucedida");
            case 1 -> {
                System.out.println("[ERRO]Host não econtrado");
                return;
            } case 2 -> {
                System.out.println("[ERRO] Ligação ao servidor.");
                return;
            }
        }
      switch (cl.enviaUDP()){
            case 0 -> System.out.println("Packet enviado e lista recebida com sucesso");
            case 1 -> System.out.println("[ERRO]na ligação UPD Cliente - Servidor");
      }

      cl.ligacaoTCP();
    }

    

}
