package Sql;

import java.sql.*;

public class LocadoraDAO {
    private final Connection bd;

    public LocadoraDAO(Connection bd) {
        this.bd = bd;
    }

    public String[] buscarDados(String cnpj) {
        String sql = "SELECT CNPJ, Nome, Cidade FROM Locadora WHERE CNPJ = ?";
        try (PreparedStatement st = bd.prepareStatement(sql)) {
            st.setString(1, cnpj);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        rs.getString("CNPJ"),
                        rs.getString("Nome"),
                        rs.getString("Cidade")
                    };
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro SQL ao carregar locadora!");
        }
        return null;
    }
}
