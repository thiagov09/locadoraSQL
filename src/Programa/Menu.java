package Programa;

import Loja.*;
import Sql.BD;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Menu {
    private static int optionQuant = 0;
    private static int option;
    private static Scanner sc = new Scanner(System.in);
    private static Locadora locadoraAtual;
    private static boolean enableOptionZero = true;

    public static void start(Locadora locadoraNova){
        locadoraAtual = locadoraNova;
        System.out.println("--- Bem vindo a " + locadoraAtual.getNome() + " ---\n");
        locadoraAtual.verificaMultas();
        while(true){
            System.out.println("--Escolha um tipo de conta--");

            reset();
            addOption("Cliente");
            addOption("Vendedor");
            verificarOption();

            if(option == 0){
                break;
            }

            boolean loginConcluido = locadoraAtual.login(option);

            if(!loginConcluido){
                System.out.println("Reiniciando processo...\n");
            }
        }
    }

    public static void reset(){
        reset(true);
    }

    public static void reset(boolean optionZero){
        optionQuant = 0;
        enableOptionZero = optionZero;
        if(optionZero) System.out.println("[0] - Sair do programa");
    }

    public static void addOption(String option) {
        System.out.printf("[%d] - %s\n", ++optionQuant, option);
    }

    public static void verificarOption(){
        while(true) {
            try {
                System.out.print("Escolha uma opção: ");
                option = sc.nextInt();
                sc.nextLine();

                if (option > 0 && option <= optionQuant) {
                    System.out.println();
                    break;
                }
                else if (option == 0 && enableOptionZero) {
                    sc.close();
                    System.exit(0);
                }

                System.out.println("Escolha uma opção válida!");

            } catch (InputMismatchException e) {
                System.out.println("Espirito de porco detectado. Insira um valor inteiro (pelo menos)!");
                sc.nextLine();
            }
        }
    }

    public static String scanCPF() {
        while (true) {
            System.out.print("Digite o CPF no formato (xxx.xxx.xxx-yy): ");
            String scannedCPF = sc.nextLine();

            if (scannedCPF.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}")) { // Regex obrigando a sequência de 3 algarismos e ponto literal e hífen
                return scannedCPF;
            }

            System.out.println("CPF inválido.");
        }
    }

    public static LocalDate scanData() {
        while (true) {
            try {
                System.out.print("Digite a data no formato AAAA-MM-DD: ");
                return LocalDate.parse(sc.nextLine());
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida.");
            }

        }
    }

    public static void menuCliente(Cliente clienteAtual){
        TempoSessao sessao = new TempoSessao();
        Thread s = new Thread(sessao);
        s.start();

        while(clienteAtual.isLogado()) {
            reset();
            addOption("Deslogar da conta");
            addOption("Alugar filme");
            addOption("Devolver filme");
            addOption("Pagar / conferir multa");
            addOption("Histórico de Multas");
            verificarOption();

            switch (option) {
                case 1:
                    clienteAtual.deslogar();
                    break ;
                case 2:
                    clienteAtual.alugarFilme(locadoraAtual, BD.getConexao());
                    break;
                case 3:
                    locadoraAtual.verificaMultas();
                    clienteAtual.devolverFilme(locadoraAtual, BD.getConexao());
                    break;
                case 4:
                    locadoraAtual.verificaMultas();
                    clienteAtual.conferirMulta(locadoraAtual);
                    break;
                case 5:
                    clienteAtual.mostrarInformacoesMultas(locadoraAtual.getClienteDAO());
                    break;
            }
        }
        sessao.encerrar();
        System.out.println("Tempo usando a conta de [" + clienteAtual.getNome() + "]: " + sessao.getSegundos() + " segundos.");
        System.out.println("Retornando ao menu principal\n");
    }

    public static void menuVendedor(Vendedor vendedorAtual) throws SQLException {
        TempoSessao sessao = new TempoSessao();
        Thread s = new Thread(sessao);
        s.start();

        while(vendedorAtual.isLogado()) {
            reset();
            addOption("Deslogar da conta");
            addOption("Adicionar filme");
            addOption("Remover filme");
            addOption("Editar filme");
            addOption("Editar cliente");
            addOption("Deletar cliente");
            addOption("Deletar empréstimo");
            addOption("Deletar multa");
            addOption("Ver todas as tabelas");
            addOption("Consultar locadora e vendedores");
            if (vendedorAtual.isAdmin()) {
                addOption("Adicionar vendedor");
                addOption("Remover vendedor");
                addOption("Tornar vendedor admin / remover admin");
                addOption("Editar locadora");
            }
            verificarOption();

            switch (option) {
                case 1  -> vendedorAtual.deslogar();
                case 2  -> locadoraAtual.addFilme();
                case 3  -> locadoraAtual.RemoverFilme();
                case 4  -> locadoraAtual.editarFilme();
                case 5  -> locadoraAtual.editarCliente();
                case 6  -> locadoraAtual.deletarCliente();
                case 7  -> locadoraAtual.deletarEmprestimo();
                case 8  -> locadoraAtual.deletarMulta();
                case 9  -> locadoraAtual.verTodasTabelas();
                case 10 -> locadoraAtual.mostrarInformacoesDaLocadora();
                case 11 -> locadoraAtual.addVendedor();
                case 12 -> locadoraAtual.RemoverVendedor(vendedorAtual);
                case 13 -> locadoraAtual.promoverVendedor(vendedorAtual);
                case 14 -> locadoraAtual.editarLocadora();
            }
        }
        sessao.encerrar();
        System.out.println("Tempo usando a conta de [" + vendedorAtual.getNome() + "]: " + sessao.getSegundos() + " segundos.");
        System.out.println("Retornando ao menu principal\n");
    }

    public static int getOption() {
        return option;
    }
}
