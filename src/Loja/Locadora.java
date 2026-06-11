package Loja;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;
import java.util.ArrayList;
import Sql.BD;
import Sql.ClienteDAO;
import Sql.VendedorDAO;
import Sql.FilmeDAO;
import Sql.EmprestimoDAO;
import Sql.MultaDAO;
import Sql.LocadoraDAO;
import Loja.Generos.*;
import Programa.Menu;

public class Locadora {
    private String nome;
    private String CNPJ;
    private String cidade;
    private ArrayList<Filme> filmes = new ArrayList<>();
    private ArrayList<Cliente> clientes = new ArrayList<>();
    private ArrayList<Vendedor> vendedores = new ArrayList<>();
    private static Scanner sc = new Scanner(System.in);

    private final Connection bd;
    private final ClienteDAO clienteDAO;
    private final VendedorDAO vendedorDAO;
    private final FilmeDAO filmeDAO;
    private final EmprestimoDAO emprestimoDAO;
    private final MultaDAO multaDAO;
    private final LocadoraDAO locadoraDAO;

    public Locadora(String cnpjNovo) {
        this.CNPJ = cnpjNovo;
        BD.conectar();
        this.bd       = BD.getConexao();
        clienteDAO    = new ClienteDAO(bd);
        vendedorDAO   = new VendedorDAO(bd);
        filmeDAO      = new FilmeDAO(bd);
        emprestimoDAO = new EmprestimoDAO(bd);
        multaDAO      = new MultaDAO(bd);
        locadoraDAO   = new LocadoraDAO(bd);
        updateFromSQL();

        System.out.println("========================================");
        System.out.println("  Locadora : " + nome);
        System.out.println("  Cidade   : " + cidade);
        System.out.println("  Clientes : " + clientes.size());
        System.out.println("========================================\n");
    }

    public void updateFromSQL() {
        String[] dados = locadoraDAO.buscarDados(CNPJ);
        if (dados != null) {
            this.CNPJ   = dados[0];
            this.nome   = dados[1];
            this.cidade = dados[2];
        }

        vendedores = vendedorDAO.listarPorLocadora(CNPJ);
        if (vendedores.isEmpty()) {
            System.out.println("Nenhum vendedor! Cadastre pelo menos um!");
            addVendedor();
            vendedores = vendedorDAO.listarPorLocadora(CNPJ);
            if (!vendedores.isEmpty()) {
                vendedores.get(0).setAdmin(true);
                vendedorDAO.atualizarAdmin(vendedores.get(0).getCpf(), true);
            }
        }

        clientes = clienteDAO.listarPorLocadora(CNPJ);
        filmes   = filmeDAO.listarPorLocadora(CNPJ);
        emprestimoDAO.carregarParaClientes(clientes, CNPJ);
        multaDAO.carregarParaClientes(clientes, CNPJ);
    }

    public boolean login(int tipo) {
        while (tipo == 1) {
            Menu.reset();
            Menu.addOption("Cadastro");
            Menu.addOption("Login");
            Menu.verificarOption();

            if (Menu.getOption() == 1) {
                System.out.print("Digite o nome: ");
                String nomeNovo = sc.nextLine();
                while (nomeNovo.isBlank()) {
                    System.out.print("Nome inválido! Insira novamente: ");
                    nomeNovo = sc.nextLine();
                }

                String cpfNovo = Menu.scanCPF();
                while (buscarConta(clientes, cpfNovo) != null) {
                    System.out.println("CPF já cadastrado!");
                    cpfNovo = Menu.scanCPF();
                }

                System.out.print("Digite a senha: ");
                String senhaNova = sc.nextLine();
                while (senhaNova.isBlank()) {
                    System.out.print("Senha inválida! Insira novamente: ");
                    senhaNova = sc.nextLine();
                }

                LocalDate dataNova = Menu.scanData();
                while (dataNova.isAfter(LocalDate.now()) || dataNova.isBefore(LocalDate.now().minusYears(130))) {
                    System.out.println("Data inválida! A idade deve ser entre 0 e 130 anos.");
                    dataNova = Menu.scanData();
                }

                Cliente clienteNovo = new Cliente(nomeNovo, cpfNovo, senhaNova, dataNova);
                addCliente(clienteNovo);
            } else {
                break;
            }
            System.out.println();
        }

        System.out.println("--Insira os dados da conta--");
        String cpfLogin = Menu.scanCPF();
        Conta contaAtual = null;
        int tentativas = 0;

        while (true) {
            try {
                if (tipo == 1)      contaAtual = buscarConta(clientes, cpfLogin);
                else if (tipo == 2) contaAtual = buscarConta(vendedores, cpfLogin);

                System.out.print("Digite a senha: ");
                String senhaLogin = sc.nextLine();
                contaAtual.logar(cpfLogin, senhaLogin);

                if (tipo == 1 && contaAtual.isLogado()) {
                    Menu.menuCliente((Cliente) contaAtual);
                    return true;
                } else if (tipo == 2 && contaAtual.isLogado()) {
                    Menu.menuVendedor((Vendedor) contaAtual);
                    return true;
                }
            } catch (NullPointerException e) {
                System.out.println("Conta não encontrada!");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            tentativas++;
            if (tentativas == 3) {
                System.out.println("Você errou 3 vezes consecutivas.");
                return false;
            }
        }
    }

    private Conta buscarConta(ArrayList<? extends Conta> contas, String cpf) {
        for (Conta c : contas) {
            if (c.getCpf().equals(cpf)) return c;
        }
        return null;
    }

    public void addCliente(Cliente clienteNovo) {
        clientes.add(clienteNovo);
        clienteDAO.inserir(clienteNovo, CNPJ);
    }

    public void addVendedor(Vendedor vendedorNovo) {
        vendedores.add(vendedorNovo);
        vendedorDAO.inserir(vendedorNovo, CNPJ);
    }

    public void addVendedor() {
        System.out.print("Nome do vendedor: ");
        String nomeNovo = sc.nextLine();
        while (nomeNovo.isBlank()) {
            System.out.print("Nome inválido! Insira novamente: ");
            nomeNovo = sc.nextLine();
        }

        String cpfNovo = Menu.scanCPF();
        while (buscarConta(vendedores, cpfNovo) != null) {
            System.out.println("CPF já cadastrado!");
            cpfNovo = Menu.scanCPF();
        }

        System.out.print("Senha do vendedor: ");
        String senhaNova = sc.nextLine();
        while (senhaNova.isBlank()) {
            System.out.print("Senha inválida! Insira novamente: ");
            senhaNova = sc.nextLine();
        }

        LocalDate dataNova = Menu.scanData();
        while (dataNova.isAfter(LocalDate.now()) || dataNova.isBefore(LocalDate.now().minusYears(130))) {
            System.out.println("Data inválida! A idade deve ser entre 0 e 130 anos.");
            dataNova = Menu.scanData();
        }

        float salarioNovo = -1;
        while (salarioNovo < 0) {
            System.out.print("Salário do vendedor: ");
            try {
                salarioNovo = Float.parseFloat(sc.nextLine().trim().replace(",", "."));
                if (salarioNovo < 0) System.out.println("Salário inválido! Insira um valor positivo.");
            } catch (NumberFormatException e) {
                System.out.println("Insira apenas números no salário!");
            }
        }

        System.out.println("O vendedor tem acesso de administrador?");
        Menu.reset(false);
        Menu.addOption("Sim");
        Menu.addOption("Não");
        Menu.verificarOption();
        boolean statusNovo = Menu.getOption() == 1;

        Vendedor vendedorNovo = new Vendedor(nomeNovo, cpfNovo, senhaNova, dataNova, salarioNovo, statusNovo);
        addVendedor(vendedorNovo);
        System.out.println("Vendedor inserido com sucesso!\n");
    }

    public void promoverVendedor(Vendedor contaVendedor) {
        Vendedor vendedorAtual = null;
        String busca;
        while (true) {
            busca = Menu.scanCPF();
            vendedorAtual = (Vendedor) buscarConta(vendedores, busca);
            if (vendedorAtual == null) System.out.println("CPF não encontrado!");
            else break;
        }
        Menu.reset(false);

        if (vendedorAtual.isAdmin() && contaVendedor.getCpf().equals(busca)) {
            System.out.println("Você não pode retirar seu próprio cargo.\n");
            return;
        } else if (vendedorAtual.isAdmin()) {
            System.out.println("O vendedor já é admin, deseja remover o admin?");
            Menu.addOption("Sim");
            Menu.addOption("Não");
        } else {
            System.out.println("O vendedor não é admin, deseja promovê-lo a admin?");
            Menu.addOption("Sim");
            Menu.addOption("Não");
        }

        Menu.verificarOption();
        if (Menu.getOption() == 1) {
            vendedorAtual.setAdmin(!vendedorAtual.isAdmin());
            vendedorDAO.atualizarAdmin(vendedorAtual.getCpf(), vendedorAtual.isAdmin());
            System.out.println("Status de admin atualizado: " + (vendedorAtual.isAdmin() ? "Admin" : "Vendedor comum") + "\n");
        }
    }

    public void addFilme() {
        System.out.print("Título do filme: ");
        String titulo = sc.nextLine();
        while (titulo.isBlank()) {
            System.out.print("Título inválido! Insira novamente: ");
            titulo = sc.nextLine();
        }

        System.out.print("Classificação do filme: ");
        String classificacao = sc.nextLine();
        while (classificacao.isBlank()) {
            System.out.print("Classificação inválida! Insira novamente: ");
            classificacao = sc.nextLine();
        }

        System.out.print("Diretor do filme: ");
        String diretor = sc.nextLine();
        while (diretor.isBlank()) {
            System.out.print("Diretor inválido! Insira novamente: ");
            diretor = sc.nextLine();
        }

        System.out.print("Gênero (acao, comedia, suspense, terror, romance): ");
        String genero = sc.nextLine().toLowerCase();
        while (!genero.equals("acao") && !genero.equals("comedia") && !genero.equals("suspense")
                && !genero.equals("terror") && !genero.equals("romance")) {
            System.out.print("Gênero inválido! Insira novamente: ");
            genero = sc.nextLine().toLowerCase();
        }

        int anoAtual = LocalDate.now().getYear();
        int ano = -1;
        while (ano < 1897 || ano > anoAtual) {
            System.out.print("Ano de lançamento (1897 a " + anoAtual + "): ");
            try {
                ano = Integer.parseInt(sc.nextLine().trim());
                if (ano > anoAtual) System.out.println("Seu futuro ainda não foi escrito!");
                else if (ano < 1897) System.out.println("Ainda não pousamos na lua!");
            } catch (NumberFormatException e) {
                System.out.println("Insira apenas números no ano!");
            }
        }

        int quantidade = -1;
        while (quantidade < 1) {
            System.out.print("Quantidade de cópias (mínimo 1): ");
            try {
                quantidade = Integer.parseInt(sc.nextLine().trim());
                if (quantidade < 1) System.out.println("Quantidade inválida! Insira pelo menos 1.");
            } catch (NumberFormatException e) {
                System.out.println("Insira apenas números na quantidade!");
            }
        }

        Filme filme = switch (genero) {
            case "acao"     -> new FilmeAcao(titulo, classificacao, diretor, ano, quantidade, quantidade);
            case "comedia"  -> new FilmeComedia(titulo, classificacao, diretor, ano, quantidade, quantidade);
            case "suspense" -> new FilmeSuspense(titulo, classificacao, diretor, ano, quantidade, quantidade);
            case "romance"  -> new FilmeRomance(titulo, classificacao, diretor, ano, quantidade, quantidade);
            case "terror"   -> new FilmeTerror(titulo, classificacao, diretor, ano, quantidade, quantidade);
            default -> null;
        };

        if (filme == null) { System.out.println("Gênero inválido!"); return; }

        filmeDAO.inserir(filme, genero, CNPJ);
        filmes.add(filme);
    }

    public void RemoverFilme() {
        System.out.println("\n======== FILMES CADASTRADOS ========");
        for (Filme filme : filmes) filme.mostra();
        System.out.println("====================================\n");

        System.out.print("Informe o ID do filme a remover: ");
        int busca;
        try {
            busca = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Insira apenas números no ID!\n");
            return;
        }

        for (int i = 0; i < filmes.size(); i++) {
            Filme filme = filmes.get(i);
            if (filme.getIdFilme() == busca) {
                if (filme.getDisponivel() < filme.getQuantidade()) {
                    System.out.println("Não é possível remover: há cópias deste filme emprestadas no momento!\n");
                    return;
                }
                filmeDAO.deletar(filme.getIdFilme());
                filmes.remove(i);
                System.out.println("Filme removido com sucesso!\n");
                return;
            }
        }
        System.out.println("Filme não encontrado!\n");
    }

    public void RemoverVendedor(Vendedor vendedorAtual) {
        System.out.println("\n======== VENDEDORES CADASTRADOS ========");
        for (Vendedor v : vendedores) {
            System.out.println("  Nome   : " + v.getNome());
            System.out.println("  CPF    : " + v.getCpf());
            System.out.println("  Salário: R$ " + v.getSalario());
            System.out.println("  Admin  : " + (v.isAdmin() ? "Sim" : "Não") + "\n");
        }
        System.out.println("======================================\n");

        String busca = Menu.scanCPF();

        if (vendedorAtual.getCpf().equals(busca)) {
            System.out.println("Apenas seus superiores podem excluir sua conta.\n");
            return;
        }

        for (int i = 0; i < vendedores.size(); i++) {
            if (vendedores.get(i).getCpf().equals(busca)) {
                vendedorDAO.deletar(vendedores.get(i).getCpf());
                vendedores.remove(i);
                System.out.println("Vendedor removido!\n");
                return;
            }
        }
        System.out.println("Vendedor não encontrado!\n");
    }

    public void verificaMultas() {
        for (Cliente cliente : clientes) {
            for (Emprestimo emp : cliente.getEmprestimos()) {
                if (emp.getDevolvido() == null && LocalDate.now().isAfter(emp.getDevolucao())) {
                    float valor = ChronoUnit.DAYS.between(emp.getDevolucao(), LocalDate.now());
                    Multa multaExistente = null;

                    for (Multa mul : cliente.getMultas()) {
                        if (mul.getIdEmprestimo() == emp.getIdEmprestimo()) {
                            multaExistente = mul;
                            break;
                        }
                    }

                    if (multaExistente != null && multaExistente.getDataDePagamento() == null) {
                        multaExistente.setValor(valor);
                        multaDAO.atualizarValor(multaExistente.getId(), valor);
                    } else if (multaExistente == null) {
                        Multa novaMulta = new Multa(emp.getIdEmprestimo(), valor, LocalDate.now(), cliente.getCpf());
                        multaDAO.inserir(novaMulta, CNPJ);
                        cliente.addMulta(novaMulta);
                    }
                }
            }
        }
    }

    public void mostrarInformacoesDaLocadora() {
        locadoraDAO.mostrarInformacoesLocadora(this.CNPJ);
    }

    public void verTodasTabelas() {
        vendedorDAO.selectTudo(CNPJ);
    }

    public void editarCliente() {
        String cpf = Menu.scanCPF();
        System.out.print("Novo nome: ");
        String nome = sc.nextLine();
        while (nome.isBlank()) { System.out.print("Nome inválido! Insira novamente: "); nome = sc.nextLine(); }
        System.out.print("Nova senha: ");
        String senha = sc.nextLine();
        while (senha.isBlank()) { System.out.print("Senha inválida! Insira novamente: "); senha = sc.nextLine(); }
        clienteDAO.atualizarConta(cpf, nome, senha);
    }

    public void editarFilme() {
        System.out.println("\n======== FILMES CADASTRADOS ========");
        for (Filme f : filmes) f.mostra();
        System.out.println("====================================\n");
        System.out.print("ID do filme a editar: ");
        int id;
        try { id = Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("ID inválido!"); return; }

        Filme alvo = null;
        for (Filme f : filmes) { if (f.getIdFilme() == id) { alvo = f; break; } }
        if (alvo == null) { System.out.println("Filme não encontrado!"); return; }

        System.out.print("Novo título (atual: " + alvo.getTitulo() + "): ");
        String titulo = sc.nextLine();
        if (titulo.isBlank()) titulo = alvo.getTitulo();

        System.out.print("Novo diretor (atual: " + alvo.getDiretor() + "): ");
        String diretor = sc.nextLine();
        if (diretor.isBlank()) diretor = alvo.getDiretor();

        System.out.print("Novo gênero (acao/comedia/suspense/terror/romance, atual mantido se vazio): ");
        String genero = sc.nextLine().toLowerCase();
        if (genero.isBlank()) genero = alvo.getClass().getSimpleName().replace("Filme", "").toLowerCase();

        System.out.print("Nova classificação (atual: " + alvo.getClassificacao() + "): ");
        String classif = sc.nextLine();
        if (classif.isBlank()) classif = alvo.getClassificacao();

        int ano = alvo.getAnoLancamento();
        System.out.print("Novo ano (atual: " + ano + ", enter para manter): ");
        String anoStr = sc.nextLine().trim();
        if (!anoStr.isBlank()) {
            try { ano = Integer.parseInt(anoStr); }
            catch (NumberFormatException e) { System.out.println("Ano inválido, mantido o anterior."); }
        }

        Filme atualizado = switch (genero) {
            case "acao"     -> new FilmeAcao(titulo, classif, diretor, ano, alvo.getQuantidade(), alvo.getDisponivel());
            case "comedia"  -> new FilmeComedia(titulo, classif, diretor, ano, alvo.getQuantidade(), alvo.getDisponivel());
            case "suspense" -> new FilmeSuspense(titulo, classif, diretor, ano, alvo.getQuantidade(), alvo.getDisponivel());
            case "romance"  -> new FilmeRomance(titulo, classif, diretor, ano, alvo.getQuantidade(), alvo.getDisponivel());
            case "terror"   -> new FilmeTerror(titulo, classif, diretor, ano, alvo.getQuantidade(), alvo.getDisponivel());
            default -> { System.out.println("Gênero inválido, edição cancelada."); yield null; }
        };
        if (atualizado == null) return;
        atualizado.setIdFilme(id);
        filmeDAO.atualizar(atualizado, genero);
        filmes.set(filmes.indexOf(alvo), atualizado);
    }

    public void editarLocadora() {
        System.out.print("Novo nome da locadora (atual: " + nome + "): ");
        String novoNome = sc.nextLine();
        if (novoNome.isBlank()) novoNome = nome;
        System.out.print("Nova cidade (atual: " + cidade + "): ");
        String novaCidade = sc.nextLine();
        if (novaCidade.isBlank()) novaCidade = cidade;
        locadoraDAO.atualizar(CNPJ, novoNome, novaCidade);
        nome   = novoNome;
        cidade = novaCidade;
    }

    public void deletarCliente() {
        System.out.println("\n======== CLIENTES CADASTRADOS ========");
        for (Cliente c : clientes) System.out.println("  " + c.getNome() + " - " + c.getCpf());
        System.out.println("======================================\n");
        String cpf = Menu.scanCPF();
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getCpf().equals(cpf)) {
                vendedorDAO.deletar(cpf); // corrigido: era vendedorDAO.deletarCliente()
                clientes.remove(i);
                System.out.println("Cliente removido!\n");
                return;
            }
        }
        System.out.println("Cliente não encontrado!");
    }

    public void deletarEmprestimo() {
        System.out.print("ID do empréstimo a deletar: ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            emprestimoDAO.deletar(id);
        } catch (NumberFormatException e) {
            System.out.println("ID inválido!");
        }
    }

    public void deletarMulta() {
        System.out.print("ID da multa a deletar: ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            multaDAO.deletar(id);
        } catch (NumberFormatException e) {
            System.out.println("ID inválido!");
        }
    }

    public ArrayList<Filme> getFilmes() { return filmes; }
    public String getNome()             { return nome; }
    public String getCNPJ()             { return CNPJ; }
    public ClienteDAO getClienteDAO()   { return clienteDAO; }
}