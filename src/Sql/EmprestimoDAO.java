package Sql;

import Loja.Cliente;
import Loja.Emprestimo;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class EmprestimoDAO {
    private final Connection bd;

    public EmprestimoDAO(Connection bd) {
        this.bd = bd;
    }

    public void carregarParaClientes(ArrayList<Cliente> clientes, String cnpj) {
        String sql = "SELECT Id, Data, Devolvido, Devolucao, Cliente_CPF, Filme_Id " +
                     "FROM Emprestimo WHERE Locadora_CNPJ = ?";
        try (PreparedStatement st = bd.prepareStatement(sql)) {
            st.setString(1, cnpj);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String cpfCliente = rs.getString("Cliente_CPF");
                    for (Cliente cliente : clientes) {
                        if (cliente.getCpf().equals(cpfCliente)) {
                            LocalDate devolvido = rs.getDate("Devolvido") != null
                                    ? rs.getDate("Devolvido").toLocalDate() : null;
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
            }
        } catch (SQLException e) {
            System.out.println("Erro SQL ao carregar empréstimos!");
        }
    }

    public void deletar(int idEmprestimo) {
        String checkSql = "SELECT Devolvido FROM Emprestimo WHERE Id = ?";
        try (PreparedStatement check = bd.prepareStatement(checkSql)) {
            check.setInt(1, idEmprestimo);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getDate("Devolvido") == null) {
                    System.out.println("Não é possível deletar: empréstimo ainda não devolvido!");
                    return;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar empréstimo!");
            return;
        }
        String sql = "DELETE FROM Emprestimo WHERE Id = ?";
        try (PreparedStatement st = bd.prepareStatement(sql)) {
            st.setInt(1, idEmprestimo);
            st.executeUpdate();
            System.out.println("Empréstimo removido com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao remover empréstimo!");
        }
    }
}
