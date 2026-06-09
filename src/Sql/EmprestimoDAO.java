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
}
