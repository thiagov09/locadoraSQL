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
        BD.conectar();
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
        BD.conectar();
        String sql = "INSERT INTO Multa (Valor, Data, Locadora_CNPJ, Emprestimo_Id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = bd.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setFloat(1, multa.getValor());
            pst.setDate(2, Date.valueOf(LocalDate.now()));
            pst.setString(3, cnpj);
            pst.setInt(4, multa.getIdEmprestimo());
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) multa.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao inserir multa!");
        }
    }

    public void atualizarValor(int idMulta, float novoValor) {
        BD.conectar();
        String sql = "UPDATE Multa SET Valor = ? WHERE Id = ?";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setFloat(1, novoValor);
            pst.setInt(2, idMulta);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar multa!");
        }
    }

    public void deletar(int idMulta) {
        BD.conectar();
        String checkSql = "SELECT DataPagamento FROM Multa WHERE Id = ?";
        try (PreparedStatement pst = bd.prepareStatement(checkSql)) {
            pst.setInt(1, idMulta);
            try (ResultSet rs = pst.executeQuery()) {
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
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setInt(1, idMulta);
            pst.executeUpdate();
            System.out.println("Multa removida com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao remover multa!");
        }
    }
}
