package Sql;

import Loja.Cliente;
import Loja.Emprestimo;
import Loja.Multa;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class MultaDAO {
    private final Connection bd;

    public MultaDAO(Connection bd) {
        this.bd = bd;
    }

    public void carregarParaClientes(ArrayList<Cliente> clientes, String cnpj) {
        String sql = "SELECT Id, Valor, Data, DataPagamento, Emprestimo_Id " +
                     "FROM Multa WHERE Locadora_CNPJ = ?";
        try (PreparedStatement st = bd.prepareStatement(sql)) {
            st.setString(1, cnpj);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    int idEmprestimo = rs.getInt("Emprestimo_Id");
                    for (Cliente cliente : clientes) {
                        for (Emprestimo emp : cliente.getEmprestimos()) {
                            if (emp.getIdEmprestimo() == idEmprestimo) {
                                Multa multa = new Multa(
                                        idEmprestimo,
                                        rs.getFloat("Valor"),
                                        rs.getDate("Data").toLocalDate(),
                                        cliente.getCpf()
                                );
                                multa.setId(rs.getInt("Id"));
                                if (rs.getDate("DataPagamento") != null)
                                    multa.setDataDePagamento(rs.getDate("DataPagamento").toLocalDate());
                                cliente.addMulta(multa);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro SQL ao carregar multas!");
        }
    }

    public void inserir(Multa multa, String cnpj) {
        String sql = "INSERT INTO Multa (Valor, Data, Locadora_CNPJ, Emprestimo_Id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement st = bd.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setFloat(1, multa.getValor());
            st.setDate(2, Date.valueOf(LocalDate.now()));
            st.setString(3, cnpj);
            st.setInt(4, multa.getIdEmprestimo());
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) multa.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao inserir multa!");
        }
    }

    public void atualizarValor(int idMulta, float novoValor) {
        String sql = "UPDATE Multa SET Valor = ? WHERE Id = ?";
        try (PreparedStatement st = bd.prepareStatement(sql)) {
            st.setFloat(1, novoValor);
            st.setInt(2, idMulta);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar multa!");
        }
    }

    public void deletar(int idMulta) {
        String checkSql = "SELECT DataPagamento FROM Multa WHERE Id = ?";
        try (PreparedStatement check = bd.prepareStatement(checkSql)) {
            check.setInt(1, idMulta);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getDate("DataPagamento") == null) {
                    System.out.println("Não é possível deletar: multa ainda não paga!");
                    return;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar multa!");
            return;
        }
        String sql = "DELETE FROM Multa WHERE Id = ?";
        try (PreparedStatement st = bd.prepareStatement(sql)) {
            st.setInt(1, idMulta);
            st.executeUpdate();
            System.out.println("Multa removida com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao remover multa!");
        }
    }
}
