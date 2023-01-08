package pl.Filipe.Patricia.Sandra.Cliente;


public class Cliente {

    public static void main(String[] args) {
        if(args.length != 2){
            System.out.println("Número de argumentos da linha de comandos errado.");
            return;
        }
        ClienteStarter cliente = new ClienteStarter();
        cliente.run(args);
    }
}
