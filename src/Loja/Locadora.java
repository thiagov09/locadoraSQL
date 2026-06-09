package Loja;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;
import java.util.ArrayList;
import Sql.BD;
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
    public Connection bd;

    public Locadora() {
        bd = BD.getConexao();
        updateFromSQL();
        System.out.println("========================================");
        System.out.println("  Locadora : " + nome);
        System.out.println("  Cidade   : " + cidade);
        System.out.println("  Clientes : " + clientes.size());
        System.out.println("========================================\n");
    }

    public void updateFromSQL(){
        String sql = "SELECT CNPJ, Nome, Cidade FROM Locadora WHERE CNPJ='12.345.678/0001-95'";
        try(PreparedStatement st = bd.prepareStatement(sql);
            ResultSet rs = st.executeQuery()){
            if(rs.next()){
                this.CNPJ = rs.getString("CNPJ");
                this.nome = rs.getString("Nome");
                this.cidade = rs.getString("Cidade");
            }
        }catch(SQLException e){
            System.out.println("Erro SQL");
        }

        sql = "SELECT CPF, Nome, Data_de_nascimento, Salario, AdminStatus, Senha from Vendedor WHERE Locadora_CNPJ='12.345.678/0001-95'";
        try(PreparedStatement st = bd.prepareStatement(sql);
            ResultSet rs = st.executeQuery()){
            while(rs.next()){
                Vendedor novoVendedor = new Vendedor(rs.getString("Nome"), rs.getString("CPF"), rs.getInt("Senha"), rs.getDate("Data_de_nascimento").toLocalDate(), rs.getFloat("Salario"), rs.getBoolean("AdminStatus"));
                vendedores.add(novoVendedor);
            }
        }catch(SQLException e){
            System.out.println("Erro SQL");
        }

        sql = "SELECT Cliente.CPF, Cliente.Nome, Cliente.Data_de_nascimento, Cliente.Senha " +
                "FROM Cliente " +
                "JOIN Cliente_da_Locadora ON Cliente.CPF = Cliente_da_Locadora.Cliente_CPF " +
                "WHERE Cliente_da_Locadora.Locadora_CNPJ = '12.345.678/0001-95'";
        try (PreparedStatement st = bd.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                Cliente novoCliente = new Cliente(
                        rs.getString("Nome"),
                        rs.getString("CPF"),
                        rs.getInt("Senha"),
                        rs.getDate("Data_de_nascimento").toLocalDate());
                clientes.add(novoCliente);
            }
        } catch (SQLException e) {
            System.out.println("Erro SQL");
        }

        sql = "SELECT Id,Titulo,Ano,Diretor,Genero,Classificacao,Quantidade,Disponivel,Locadora_CNPJ FROM Filme WHERE Locadora_CNPJ='12.345.678/0001-95'";
        try (PreparedStatement st = bd.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                Filme filme;
                switch (rs.getString("Genero").toLowerCase()) {
                    case "acao":
                        filme = new FilmeAcao(rs.getString("Titulo"), rs.getString("Classificacao"), rs.getString("Diretor"), rs.getInt("Ano"), rs.getInt("Quantidade"), rs.getInt("Disponivel"));
                        break;
                    case "comedia":
                        filme = new FilmeComedia(rs.getString("Titulo"), rs.getString("Classificacao"), rs.getString("Diretor"), rs.getInt("Ano"), rs.getInt("Quantidade"), rs.getInt("Disponivel"));
                        break;
                    case "suspense":
                        filme = new FilmeSuspense(rs.getString("Titulo"), rs.getString("Classificacao"), rs.getString("Diretor"), rs.getInt("Ano"), rs.getInt("Quantidade"), rs.getInt("Disponivel"));
                        break;
                    case "romance":
                        filme = new FilmeRomance(rs.getString("Titulo"), rs.getString("Classificacao"), rs.getString("Diretor"), rs.getInt("Ano"), rs.getInt("Quantidade"), rs.getInt("Disponivel"));
                        break;
                    case "terror":
                        filme = new FilmeTerror(rs.getString("Titulo"), rs.getString("Classificacao"), rs.getString("Diretor"), rs.getInt("Ano"), rs.getInt("Quantidade"), rs.getInt("Disponivel"));
                        break;
                    default:
                        System.out.println("Gênero desconhecido: " + rs.getString("Genero"));
                        continue;
                }
                filme.setIdFilme(rs.getInt("Id"));
                filmes.add(filme);
            }
        } catch (SQLException e) {
            System.out.println("Erro SQL ao carregar filmes!");
        }

        sql = "SELECT Id, Data, Devolvido, Devolucao, Cliente_CPF, Filme_Id, NomeFilme " +
                "FROM Emprestimo WHERE Locadora_CNPJ = '12.345.678/0001-95'";
        try (PreparedStatement st = bd.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                String cpfCliente = rs.getString("Cliente_CPF");
                for (Cliente cliente : clientes) {
                    if (cliente.getCpf().equals(cpfCliente)) {
                        LocalDate devolvido = null;
                        if (rs.getDate("Devolvido") != null) {
                            devolvido = rs.getDate("Devolvido").toLocalDate();
                        }
                        Emprestimo emp = new Emprestimo(
                                rs.getInt("Id"),
                                rs.getDate("Data").toLocalDate(),
                                rs.getDate("Devolucao").toLocalDate(),
                                devolvido,
                                cpfCliente,
                                rs.getInt("Filme_Id")
                        );
                        cliente.addEmprestimo(emp);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro SQL ao carregar empréstimos!");
        }

        sql = "SELECT Id, Valor, Data, DataPagamento, Emprestimo_Id " +
                "FROM Multa WHERE Locadora_CNPJ = '12.345.678/0001-95'";
        try (PreparedStatement st = bd.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                int idEmprestimo = rs.getInt("Emprestimo_Id");
                for (Cliente cliente : clientes) {
                    for (Emprestimo emp : cliente.getEmprestimos()) {
                        if (emp.getIdEmprestimo() == idEmprestimo) {
                            Multa multa = new Multa(idEmprestimo, rs.getFloat("Valor"), rs.getDate("Data").toLocalDate(), cliente.getCpf());
                            multa.setId(rs.getInt("Id"));
                            if (rs.getDate("DataPagamento") != null) {
                                multa.setDataDePagamento(rs.getDate("DataPagamento").toLocalDate());
                            }
                            cliente.addMulta(multa);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro SQL ao carregar multas!");
        }
    }

    public boolean login(int tipo){
        while(tipo == 1){
            Menu.reset();
            Menu.addOption("Cadastro");
            Menu.addOption("Login");
            Menu.verificarOption();

            if(Menu.getOption() == 1){
                System.out.print("Digite o nome: ");
                String nomeNovo = sc.nextLine();
                while (nomeNovo.isBlank()) {
                    System.out.print("Nome inválido! Insira novamente: ");
                    nomeNovo = sc.nextLine();
                }

                String cpfNovo = Menu.scanCPF();
                while(buscarConta(clientes, cpfNovo) != null){
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

        while(true) {
            try {
                if (tipo == 1) contaAtual = buscarConta(clientes, cpfLogin);
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
            }

            tentativas++;
            if (tentativas == 3) {
                System.out.println("Você errou 3 vezes consecutivas.");
                return false;
            }
        }
    }

    private Conta buscarConta(ArrayList<? extends Conta> contas, String cpf){
        for(Conta c : contas){
            if(c.getCpf().equals(cpf)) return c;
        }
        return null;
    }

    public void addCliente(Cliente clienteNovo){
        clientes.add(clienteNovo);

        String sql = "INSERT INTO Cliente (CPF, Nome, Data_de_nascimento, Senha) VALUES (?, ?, ?, ?)";
        try(PreparedStatement st = bd.prepareStatement(sql)) {
            st.setString(1, clienteNovo.getCpf());
            st.setString(2, clienteNovo.getNome());
            st.setDate(3, java.sql.Date.valueOf(clienteNovo.getDataDeNascimento()));
            st.setInt(4, clienteNovo.getHashSenha());
            st.executeUpdate();

            System.out.println("Cliente inserido com sucesso!");

            String sqlVinculo = "INSERT INTO Cliente_da_Locadora (Locadora_CNPJ, Cliente_CPF) VALUES (?, ?)";
            try (PreparedStatement stVinculo = bd.prepareStatement(sqlVinculo)) {
                stVinculo.setString(1, CNPJ);
                stVinculo.setString(2, clienteNovo.getCpf());
                stVinculo.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Erro ao vincular cliente à locadora!");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Cliente já cadastrado!");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) System.out.println("Cliente já cadastrado!");
            else System.out.println("Erro ao inserir cliente!");
        }
    }

    public void addVendedor(Vendedor vendedorNovo){
        vendedores.add(vendedorNovo);

        String sql = "INSERT INTO Vendedor (CPF, Nome, Salario, Data_de_nascimento, Senha, Locadora_CNPJ, AdminStatus) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(PreparedStatement st = bd.prepareStatement(sql)){
            st.setString(1, vendedorNovo.getCpf());
            st.setString(2, vendedorNovo.getNome());
            st.setFloat(3, vendedorNovo.getSalario());
            st.setDate(4, java.sql.Date.valueOf(vendedorNovo.getDataDeNascimento()));
            st.setInt(5, vendedorNovo.getHashSenha());
            st.setString(6, CNPJ);
            st.setBoolean(7, vendedorNovo.isAdmin());
            st.executeUpdate();
            System.out.println("Vendedor inserido com sucesso!");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Vendedor já cadastrado!");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) System.out.println("Vendedor já cadastrado!");
            else System.out.println("Erro ao inserir vendedor!");
        }
    }

    public void addVendedor() {
        System.out.print("Nome do vendedor: ");
        String nomeNovo = sc.nextLine();
        while (nomeNovo.isBlank()) {
            System.out.print("Nome inválido! Insira novamente: ");
            nomeNovo = sc.nextLine();
        }

        System.out.print("CPF do vendedor: ");
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

        System.out.print("Data de nascimento do vendedor: ");
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
        vendedores.add(vendedorNovo);
        addVendedor(vendedorNovo);
        System.out.println("Vendedor inserido com sucesso!\n");
    }

    public void promoverVendedor(Vendedor contaVendedor){
        Vendedor vendedorAtual = null;
        String busca;
        while(true){
            busca = Menu.scanCPF();
            vendedorAtual = (Vendedor) buscarConta(vendedores, busca);
            if(vendedorAtual == null) System.out.println("CPF não encontrado!");
            else break;
        }
        Menu.reset(false);

        if (vendedorAtual.isAdmin() && contaVendedor.getCpf().equals(busca)){
            System.out.println("Você não pode retirar seu próprio cargo.\n");
            return;
        } else if(vendedorAtual.isAdmin()){
            System.out.println("O vendedor já é admin, deseja remover o admin?");
            Menu.addOption("Sim");
            Menu.addOption("Não");
        } else {
            System.out.println("O vendedor não é admin, deseja promovê-lo a admin?");
            Menu.addOption("Sim");
            Menu.addOption("Não");
        }

        Menu.verificarOption();
        if(Menu.getOption() == 1) {
            vendedorAtual.setAdmin(!vendedorAtual.isAdmin());
            String sql = "UPDATE Vendedor SET AdminStatus = ? WHERE CPF = ?";
            try (PreparedStatement st = bd.prepareStatement(sql)) {
                st.setBoolean(1, vendedorAtual.isAdmin());
                st.setString(2, vendedorAtual.getCpf());
                st.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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
        while (!genero.equals("acao") && !genero.equals("comedia") && !genero.equals("suspense") && !genero.equals("terror") && !genero.equals("romance")) {
            System.out.print("Gênero inválido! Insira novamente: ");
            genero = sc.nextLine().toLowerCase();
        }

        int anoAtual = LocalDate.now().getYear();
        int ano = -1;
        while (ano < 1897 || ano > anoAtual) {
            System.out.print("Ano de lançamento (1897 a " + anoAtual + "): ");
            try {
                ano = Integer.parseInt(sc.nextLine().trim());
                if (ano > anoAtual)
                    System.out.println("Seu futuro ainda não foi escrito!");
                else if (ano < 1897)
                    System.out.println("Ainda não pousamos na lua!");
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

        Filme filme;
        switch (genero) {
            case "acao":     filme = new FilmeAcao(titulo, classificacao, diretor, ano, quantidade, quantidade);     break;
            case "comedia":  filme = new FilmeComedia(titulo, classificacao, diretor, ano, quantidade, quantidade);  break;
            case "suspense": filme = new FilmeSuspense(titulo, classificacao, diretor, ano, quantidade, quantidade); break;
            case "romance":  filme = new FilmeRomance(titulo, classificacao, diretor, ano, quantidade, quantidade);  break;
            case "terror":   filme = new FilmeTerror(titulo, classificacao, diretor, ano, quantidade, quantidade);   break;
            default: System.out.println("Gênero inválido!"); return;
        }

        String sql = "INSERT INTO Filme (Titulo, Ano, Diretor, Genero, Classificacao, Quantidade, Disponivel, Locadora_CNPJ) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement st = bd.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, filme.getTitulo());
            st.setInt(2, filme.getAnoLancamento());
            st.setString(3, filme.getDiretor());
            st.setString(4, genero);
            st.setString(5, filme.getClassificacao());
            st.setInt(6, filme.getQuantidade());
            st.setInt(7, filme.getDisponivel());
            st.setString(8, CNPJ);
            st.executeUpdate();

            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) filme.setIdFilme(keys.getInt(1));
            }
            filmes.add(filme);
            System.out.println("Filme inserido com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao inserir filme!");
        }
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
                try {
                    String sql = "DELETE FROM Filme WHERE Id = ?";
                    try (PreparedStatement st = bd.prepareStatement(sql)) {
                        st.setInt(1, filme.getIdFilme());
                        st.executeUpdate();
                    }
                    filmes.remove(i);
                    System.out.println("Filme removido com sucesso!\n");
                } catch (SQLException e) {
                    System.out.println("Erro ao remover filme do banco!\n");
                }
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

        System.out.print("Informe o CPF do vendedor: ");
        String busca = Menu.scanCPF();

        if (vendedorAtual.getCpf().equals(busca)) {
            System.out.println("Apenas seus superiores podem excluir sua conta.\n");
            return;
        }

        for (int i = 0; i < vendedores.size(); i++) {
            if (vendedores.get(i).getCpf().equals(busca)) {
                String sql = "DELETE FROM Vendedor WHERE CPF = ?";
                try (PreparedStatement st = bd.prepareStatement(sql)) {
                    st.setString(1, vendedores.get(i).getCpf());
                    st.executeUpdate();
                } catch (SQLException e) {
                    System.out.println("Erro ao remover vendedor do banco!");
                    return;
                }
                vendedores.remove(i);
                System.out.println("Vendedor removido!\n");
                return;
            }
        }
        System.out.println("Vendedor não encontrado!\n");
    }

    public ArrayList<Filme> getFilmes() {
        return filmes;
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
                        String sql = "UPDATE Multa SET Valor = ? WHERE Id = ?";
                        try (PreparedStatement st = bd.prepareStatement(sql)) {
                            st.setFloat(1, valor);
                            st.setInt(2, multaExistente.getId());
                            st.executeUpdate();
                        } catch (SQLException e) {
                            System.out.println("Erro ao atualizar multa!");
                        }
                    } else if (multaExistente == null) {
                        Multa novaMulta = new Multa(emp.getIdEmprestimo(), valor, LocalDate.now(), cliente.getCpf());
                        String sql = "INSERT INTO Multa (Valor, Data, Locadora_CNPJ, Emprestimo_Id) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement st = bd.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                            st.setFloat(1, valor);
                            st.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                            st.setString(3, CNPJ);
                            st.setInt(4, emp.getIdEmprestimo());
                            st.executeUpdate();
                            try (ResultSet keys = st.getGeneratedKeys()) {
                                if (keys.next()) novaMulta.setId(keys.getInt(1));
                            }
                        } catch (SQLException e) {
                            System.out.println("Erro ao inserir multa!");
                        }
                        cliente.addMulta(novaMulta);
                    }
                }
            }
        }
    }

    public String getNome() {
        return nome;
    }

    public String getCNPJ() {
        return CNPJ;
    }
}
