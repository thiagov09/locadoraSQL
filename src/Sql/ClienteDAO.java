package Sql;

import Loja.Cliente;

import java.sql.*;
import java.util.ArrayList;

public class ClienteDAO{
    private final Connection bd;

    public ClienteDAO(Connection bd) {
        this.bd = bd;
    }

    public ArrayList<Cliente> listarPorLocadora(String cnpj) {
        BD.conectar();
        ArrayList<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT Cliente.CPF, Cliente.Nome, Cliente.Data_de_nascimento, Cliente.Senha " +
                     "FROM Cliente " +
                     "JOIN Cliente_da_Locadora ON Cliente.CPF = Cliente_da_Locadora.Cliente_CPF " +
                     "WHERE Cliente_da_Locadora.Locadora_CNPJ = ?";
        try (PreparedStatement st = bd.prepareStatement(sql)) {
            st.setString(1, cnpj);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    clientes.add(new Cliente(
                            rs.getString("Nome"),
                            rs.getString("CPF"),
                            rs.getInt("Senha"),
                            rs.getDate("Data_de_nascimento").toLocalDate()
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro SQL ao carregar clientes!");
        }
        return clientes;
    }

    public void inserir(Cliente cliente, String cnpj) {
        BD.conectar();
        String sql = "INSERT INTO Cliente (CPF, Nome, Data_de_nascimento, Senha) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setString(1, cliente.getCpf());
            pst.setString(2, cliente.getNome());
            pst.setDate(3, Date.valueOf(cliente.getDataDeNascimento()));
            pst.setInt(4, cliente.getHashSenha());
            pst.executeUpdate();
            vincularLocadora(cliente.getCpf(), cnpj);
            System.out.println("Cliente inserido com sucesso!");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Cliente já cadastrado!");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) System.out.println("Cliente já cadastrado!");
            else System.out.println("Erro ao inserir cliente!");
        }
    }

    private void vincularLocadora(String cpfCliente, String cnpj) {
        String sql = "INSERT INTO Cliente_da_Locadora (Locadora_CNPJ, Cliente_CPF) VALUES (?, ?)";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setString(1, cnpj);
            pst.setString(2, cpfCliente);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao vincular cliente à locadora!");
        }
    }

    public void atualizarConta(String cpf, String novoNome, String novaSenha) {
        String sql = "UPDATE Cliente SET Nome = ?, Senha = ? WHERE CPF = ?";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setString(1, novoNome);
            pst.setInt(2, novaSenha.hashCode());
            pst.setString(3, cpf);
            pst.executeUpdate();
            System.out.println("Conta atualizada com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar conta!");
        }
    }

    public void mostrarInformacoesMultas(String cpf) {
        String sql = """
        SELECT f.Titulo, e.Data, e.Devolucao, e.Devolvido,
               m.Valor, m.DataPagamento
        FROM Emprestimo e
        INNER JOIN Multa m ON e.Id = m.Emprestimo_Id
        INNER JOIN Filme f ON f.Id = e.Filme_Id
        WHERE e.Cliente_CPF = ?
        """;

        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setString(1, cpf);
            try (ResultSet rs = pst.executeQuery()) {
                boolean temRegistro = false;
                while (rs.next()) {
                    temRegistro = true;
                    boolean devolvido     = rs.getDate("Devolvido") != null;
                    boolean multaPaga     = rs.getDate("DataPagamento") != null;

                    System.out.println("======= Histórico de Multa =======");
                    System.out.println("Filme:              " + rs.getString("Titulo"));
                    System.out.println("Data de Empréstimo: " + rs.getDate("Data"));
                    System.out.println("Prazo de Devolução: " + rs.getDate("Devolucao"));
                    System.out.println("Devolvido:          " + (devolvido ? rs.getDate("Devolvido") : "Não devolvido"));
                    System.out.println("Valor da Multa:     R$ " + rs.getFloat("Valor"));
                    System.out.println("Multa paga:         " + (multaPaga ? rs.getDate("DataPagamento") : "Pendente"));
                    System.out.println("----------------------------------");
                }
                if (!temRegistro) System.out.println("Nenhuma multa encontrada.");
            }
        } catch (SQLException e) {
            System.out.println("Erro SQL ao carregar multas!");
        }
    }
}
