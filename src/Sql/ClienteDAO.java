package Sql;

import Loja.Cliente;
import Loja.Emprestimo;
import Loja.Multa;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class ClienteDAO {
    private final Connection bd;

    public ClienteDAO(Connection bd) {
        this.bd = bd;
    }

    public ArrayList<Cliente> listarPorLocadora(String cnpj) {
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
        String sql = "INSERT INTO Cliente (CPF, Nome, Data_de_nascimento, Senha) VALUES (?, ?, ?, ?)";
        try (PreparedStatement st = bd.prepareStatement(sql)) {
            st.setString(1, cliente.getCpf());
            st.setString(2, cliente.getNome());
            st.setDate(3, Date.valueOf(cliente.getDataDeNascimento()));
            st.setInt(4, cliente.getHashSenha());
            st.executeUpdate();
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
        try (PreparedStatement st = bd.prepareStatement(sql)) {
            st.setString(1, cnpj);
            st.setString(2, cpfCliente);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao vincular cliente à locadora!");
        }
    }
}
