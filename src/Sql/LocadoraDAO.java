package Sql;

import java.sql.*;

public class LocadoraDAO {
    private final Connection bd;

    public LocadoraDAO(Connection bd) {
        this.bd = bd;
    }

    static public void inserir(String cnpj, String nome, String cidade) {
        BD.conectar();
        Connection bd = BD.getConexao();
        String sql = "INSERT INTO Locadora (CNPJ, Nome, Cidade) VALUES (?, ?, ?)";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setString(1, cnpj);
            pst.setString(2, nome);
            pst.setString(3, cidade);
            pst.executeUpdate();
            System.out.println("Locadora inserida com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro SQL ao inserir locadora!");
        }
    }

    static public void remover(String cnpj) {
        BD.conectar();
        Connection bd = BD.getConexao();
        String sql = "DELETE FROM Locadora WHERE CNPJ = ?";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setString(1, cnpj);
            int linhasAfetadas = pst.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("Locadora removida com sucesso!");
            } else {
                System.out.println("Nenhuma locadora encontrada com esse CNPJ.");
            }
        } catch (SQLException e) {
            System.out.println("Erro SQL ao remover locadora!");
            e.printStackTrace();

        }
    }


    static public void listarTodasLocadoras() {
        BD.conectar();
        Connection bd = BD.getConexao();
        String sql = "SELECT * FROM Locadora";
        try (PreparedStatement pst = bd.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                System.out.println("CNPJ: "   + rs.getString("CNPJ"));
                System.out.println("Nome: "   + rs.getString("Nome"));
                System.out.println("Cidade: " + rs.getString("Cidade"));
                System.out.println("---");
            }
        } catch (SQLException e) {
            System.out.println("Erro SQL ao listar locadoras!");
        }
    }
    static public boolean locadoraExiste(String cnpj) {
        BD.conectar();
        Connection bd = BD.getConexao();
        String sql = "SELECT 1 FROM Locadora WHERE CNPJ = ?";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setString(1, cnpj);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Erro SQL ao verificar locadora!");
            return false;
        }
    }

    public String[] buscarDados(String cnpj) {
        BD.conectar();
        String sql = "SELECT CNPJ, Nome, Cidade FROM Locadora WHERE CNPJ = ?";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setString(1, cnpj);
            try (ResultSet rs = pst.executeQuery()) {
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

    public void atualizar(String cnpj, String novoNome, String novaCidade) {
        BD.conectar();
        String sql = "UPDATE Locadora SET Nome = ?, Cidade = ? WHERE CNPJ = ?";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setString(1, novoNome);
            pst.setString(2, novaCidade);
            pst.setString(3, cnpj);
            pst.executeUpdate();
            System.out.println("Locadora atualizada com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar locadora!");
        }
    }

    public void mostrarInformacoesLocadora(String cnpj) {
        BD.conectar();
        String sql = """
        SELECT l.Nome AS NomeLocadora, l.Cidade,
               v.CPF, v.Nome AS NomeVendedor,
               v.Salario, v.Data_de_nascimento, v.AdminStatus
        FROM Locadora l
        JOIN Vendedor v ON l.CNPJ = v.Locadora_CNPJ
        WHERE l.CNPJ = ?
        """;

        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setString(1, cnpj);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    System.out.println("Locadora : "         + rs.getString("NomeLocadora"));
                    System.out.println("Cidade : "           + rs.getString("Cidade"));
                    System.out.println("CPF : "              + rs.getString("CPF"));
                    System.out.println("Nome do Vendedor : " + rs.getString("NomeVendedor"));
                    System.out.println("Salário : R$ "       + rs.getFloat("Salario"));
                    System.out.println("Nascimento : "       + rs.getDate("Data_de_nascimento"));
                    System.out.println("Admin? : "           + rs.getBoolean("AdminStatus"));
                    System.out.println("---");
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro SQL ao carregar informações da locadora!");
        }
    }
}