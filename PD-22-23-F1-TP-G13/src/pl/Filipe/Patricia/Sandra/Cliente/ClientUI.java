package pl.Filipe.Patricia.Sandra.Cliente;

import pl.Filipe.Patricia.Sandra.Servidor.PDInput;

import java.util.ArrayList;
import java.util.Scanner;

public class ClientUI {

    private boolean isAutenticado;
    private boolean isAdmnistrator;
    private String username;
    private String password;
    private String nome;
    private int alteracao;
    private String localidade;
    private String pais;
    private int classificacao_etaria;
    private int espetaculo;
    private int reservaEliminar;
    private int idMax;
    private int espetaculoEliminar;
    private int espetaculoVisivel;
    private String lugaresSelecionados;
    private ArrayList<Integer> lugares;

    private boolean podeApagar;

    private int userID;
    public ClientUI() {
        isAutenticado = false;
        isAdmnistrator = false;
        nome = "sem nome";
        localidade = "sem nome";
        pais = "sem nome";
        classificacao_etaria = -1;
        espetaculo = -1;
        reservaEliminar = -1;
        idMax = 1;
        lugares = new ArrayList<>();
        lugaresSelecionados = "";
        userID = 1;
    }

    protected String getMenu(){
        StringBuilder sb = new StringBuilder();

        if(!isAutenticado){
            sb.append("1- Registo de novos utilizadores\n");
            sb.append("2- Autenticação de utilizador\n");
            sb.append("3- Autenticação de um administrador\n");
        }

        if(isAutenticado){
            sb.append("4- Edição dos dados\n");
            sb.append("5- Consulta de reservas que aguardam confirmação de pagamento\n");
            sb.append("6- Consulta de reservas pagas\n");
            sb.append("7- Consulta e pesquisa de espetáculos\n");
            sb.append("8- Seleção de um espetáculo\n");
            sb.append("9- Visualização dos lugares disponíveis e respetivos preços\n");
            sb.append("10- Submissão/validação de um pedido de reserva\n");
            sb.append("11- Eliminação de uma reserva ainda não paga\n");
            if(isAdmnistrator){
                sb.append("12- Inserção de espetáculo\n");
                sb.append("13- Eliminação de espetáculo\n");
                sb.append("14- Tornar espetáculo visível\n");
            }
            sb.append("15- Selecionar os lugares pretendidos\n");
            sb.append("16- Logout\n");
        }

        sb.append("-1 - Sair\n");
        return sb.toString();
    }

    public int runUI(){
        int res;
        alteracao = 0;
        localidade = "sem nome";
        pais = "sem nome";
        classificacao_etaria = -1;
        System.out.println(getMenu());
        System.out.println("->");
        Scanner sc = new Scanner(System.in);
        res = sc.nextInt();

        if(res == -1){
            return res;
        }
        if(!isAutenticado) {
            if (res == 1) {
                nome = PDInput.readString("Digite o nome: ", false);
                username = PDInput.readString("Digite o username: ", true);
                password = PDInput.readString("Digite o password: ", true);
            }
            if (res == 2 || res == 3) {
                username = PDInput.readString("Digite o username: ", true);
                password = PDInput.readString("Digite o password: ", true);
            }
        }

        if(isAutenticado){
            if (res == 4) {
                switch (alteracao = PDInput.readInt("Qual elemento pretende alterar:\n1- Nome\n2- username\n3- password\n")) {
                    case 1 -> nome = PDInput.readString("Digite o nome: ", false);
                    case 2 -> username = PDInput.readString("Digite o username: ", true);
                    case 3 -> password = PDInput.readString("Digite a password: ", true);
                }
            }

            if(res == 7){
                switch (PDInput.chooseOption("Opcoes: ", "1 -> Sem filtros" , "2-> Todos os filtros"
                        , "3-> Apenas localidade", "4-> Apenas faixa etária", "5-> Apenas país")){
                    case 2 ->{
                        localidade = PDInput.readString("Digite a localidade:", true);
                        pais = PDInput.readString("Digite o país:", true);
                        classificacao_etaria = PDInput.readInt("Digite a idade:");
                    }
                    case 3 -> localidade = PDInput.readString("Digite a localidade:", true);
                    case 4 -> classificacao_etaria = PDInput.readInt("Digite a idade:");
                    case 5 -> pais = PDInput.readString("Digite o país:", true);
                }
            }

            if(res == 8){
                espetaculo = PDInput.readInt("Digite o ID do espetáculo que irá decorrer pelo menos 24 horas depois da data atual:");
            }

            if(res == 11){
                reservaEliminar = PDInput.readInt("Insira o ID da reserva não paga a eliminar:");
            }

            if(res == 13){
                espetaculoEliminar = PDInput.readInt("Insira o ID do espetáculo a eliminar:");
            }

            if(res == 14){
                espetaculoVisivel = PDInput.readInt("Insira o ID do espetáculo a tornar visível:");
            }

            if(res == 15){
                if (espetaculo!=-1){
                    lugaresSelecionados = PDInput.readString("Insira os ID dos lugares pretendidos no espetáculo selecionado:",false);
                }
            }
        }

        return res;
    }

    public boolean isAutenticado() {
        return isAutenticado;
    }

    public void setAutenticado(boolean autenticado) {
        isAutenticado = autenticado;
    }

    public boolean isAdmnistrator() {
        return isAdmnistrator;
    }

    public void setAdmnistrator(boolean admnistrator) {
        isAdmnistrator = admnistrator;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username1){username = username1;}

    public void setPassword(String password1){password = password1;}

    public String getPassword() {
        return password;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String name){nome = name;}

    public int getAlteracao() {
        return alteracao;
    }


    public String getLocalidade() {
        return localidade;
    }

    public String getPais() {
        return pais;
    }

    public int getClassificacao_etaria() {
        return classificacao_etaria;
    }

    public int getEspetaculo() {
        return espetaculo;
    }

    public void setEspetaculo(int espetaculo) {
        this.espetaculo = espetaculo;
    }

    public int getReservaEliminar() {
        return reservaEliminar;
    }

    public void setReservaEliminar(int reservaEliminar) {
        this.reservaEliminar = reservaEliminar;
    }

    public int getIdMax() {
        return idMax;
    }

    public void setIdMax(int idMax) {
        this.idMax = idMax;
    }

    public int getEspetaculoEliminar() {
        return espetaculoEliminar;
    }

    public void setEspetaculoEliminar(int espetaculoEliminar) {
        this.espetaculoEliminar = espetaculoEliminar;
    }

    public int getEspetaculoVisivel() {
        return espetaculoVisivel;
    }

    public void setEspetaculoVisivel(int espetaculoVisivel) {
        this.espetaculoVisivel = espetaculoVisivel;
    }

    public ArrayList<Integer> getLugares() {
        return lugares;
    }

    public void setLugares(ArrayList<Integer> lugares) {
        this.lugares = lugares;
    }

    public String getLugaresSelecionados() {
        return lugaresSelecionados;
    }

    public void setLugaresSelecionados(String lugaresSelecionados) {
        this.lugaresSelecionados = lugaresSelecionados;
    }

    public boolean getPodeApagar() {
        return podeApagar;
    }

    public void setPodeApagar(boolean podeApagar) {
        this.podeApagar = podeApagar;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
