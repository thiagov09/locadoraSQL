package Sql;

import Loja.Vendedor;

import java.sql.*;
import java.util.ArrayList;

public class VendedorDAO {
    private final Connection bd;

    public VendedorDAO(Connection bd) {
        this.bd = bd;
    }

    public ArrayList<Vendedor> listarPorLocadora(String cnpj) {
        BD.conectar();
        ArrayList<Vendedor> vendedores = new ArrayList<>();
        String sql = "SELECT CPF, Nome, Data_de_nascimento, Salario, AdminStatus, Senha " +
                     "FROM Vendedor WHERE Locadora_CNPJ = ?";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setString(1, cnpj);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    vendedores.add(new Vendedor(
                            rs.getString("Nome"),
                            rs.getString("CPF"),
                            rs.getInt("Senha"),
                            rs.getDate("Data_de_nascimento").toLocalDate(),
                            rs.getFloat("Salario"),
                            rs.getBoolean("AdminStatus")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro SQL ao carregar vendedores!");
        }
        return vendedores;
    }

    public void inserir(Vendedor vendedor, String cnpj) {
        BD.conectar();
        String sql = "INSERT INTO Vendedor (CPF, Nome, Salario, Data_de_nascimento, Senha, Locadora_CNPJ, AdminStatus) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setString(1, vendedor.getCpf());
            pst.setString(2, vendedor.getNome());
            pst.setFloat(3, vendedor.getSalario());
            pst.setDate(4, Date.valueOf(vendedor.getDataDeNascimento()));
            pst.setInt(5, vendedor.getHashSenha());
            pst.setString(6, cnpj);
            pst.setBoolean(7, vendedor.isAdmin());
            pst.executeUpdate();
            System.out.println("Vendedor inserido com sucesso!");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Vendedor já cadastrado!");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) System.out.println("Vendedor já cadastrado!");
            else System.out.println("Erro ao inserir vendedor!");
        }
    }

    public void atualizarAdmin(String cpf, boolean status) {
        BD.conectar();
        String sql = "UPDATE Vendedor SET AdminStatus = ? WHERE CPF = ?";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setBoolean(1, status);
            pst.setString(2, cpf);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deletar(String cpf) {
        BD.conectar();
        String sql = "DELETE FROM Vendedor WHERE CPF = ?";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setString(1, cpf);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao remover vendedor do banco!");
        }
    }

    public void deletarCliente(String cpf) {
        BD.conectar();
        String sql = "DELETE FROM Cliente WHERE CPF = ?";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setString(1, cpf);
            pst.executeUpdate();
            System.out.println("Cliente removido com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao remover cliente!");
        }
    }

    public void selectTudo(String cnpj) {
        BD.conectar();
        String[] queries = {
            "SELECT * FROM Locadora WHERE CNPJ = ?",
            "SELECT v.* FROM Vendedor v WHERE v.Locadora_CNPJ = ?",
            "SELECT c.* FROM Cliente c JOIN Cliente_da_Locadora cl ON c.CPF = cl.Cliente_CPF WHERE cl.Locadora_CNPJ = ?",
            "SELECT f.* FROM Filme f WHERE f.Locadora_CNPJ = ?",
            "SELECT e.* FROM Emprestimo e WHERE e.Locadora_CNPJ = ?",
            "SELECT m.* FROM Multa m WHERE m.Locadora_CNPJ = ?"
        };
        String[] nomes = { "LOCADORA", "VENDEDORES", "CLIENTES", "FILMES", "EMPRÉSTIMOS", "MULTAS" };

        for (int i = 0; i < queries.length; i++) {
            System.out.println("\n===== " + nomes[i] + " =====");
            try (PreparedStatement pst = bd.prepareStatement(queries[i])) {
                pst.setString(1, cnpj);
                try (ResultSet rs = pst.executeQuery()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int cols = meta.getColumnCount();
                    while (rs.next()) {
                        for (int c = 1; c <= cols; c++)
                            System.out.print(meta.getColumnName(c) + ": " + rs.getString(c) + "  ");
                        System.out.println();
                    }
                }
            } catch (SQLException e) {
                System.out.println("Erro ao exibir " + nomes[i] + "!");
            }
        }
    }
}
