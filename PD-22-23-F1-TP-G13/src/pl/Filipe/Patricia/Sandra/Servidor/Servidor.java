package pl.Filipe.Patricia.Sandra.Servidor;

public class Servidor {
    public static void main(String[] args) {
        if(args.length < 1){
            System.out.println("Número de argumentos da linha de comandos errado.");
            return;
        }
        ServidorUI servidorUI = new ServidorUI();
        servidorUI.run(args);
    }

}
