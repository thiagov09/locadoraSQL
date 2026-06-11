package Sql;

import Loja.Filme;
import Loja.Generos.*;

import java.sql.*;
import java.util.ArrayList;

public class FilmeDAO {
    private final Connection bd;

    public FilmeDAO(Connection bd) {
        this.bd = bd;
    }

    public ArrayList<Filme> listarPorLocadora(String cnpj) {
        BD.conectar();
        ArrayList<Filme> filmes = new ArrayList<>();
        String sql = "SELECT Id, Titulo, Ano, Diretor, Genero, Classificacao, Quantidade, Disponivel " +
                     "FROM Filme WHERE Locadora_CNPJ = ?";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setString(1, cnpj);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Filme filme = criarFilmePorGenero(rs);
                    if (filme != null) filmes.add(filme);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro SQL ao carregar filmes!");
        }
        return filmes;
    }

    private Filme criarFilmePorGenero(ResultSet rs) throws SQLException {
        BD.conectar();
        String titulo        = rs.getString("Titulo");
        String classificacao = rs.getString("Classificacao");
        String diretor       = rs.getString("Diretor");
        int    ano           = rs.getInt("Ano");
        int    quantidade    = rs.getInt("Quantidade");
        int    disponivel    = rs.getInt("Disponivel");

        Filme filme = switch (rs.getString("Genero").toLowerCase()) {
            case "acao"     -> new FilmeAcao(titulo, classificacao, diretor, ano, quantidade, disponivel);
            case "comedia"  -> new FilmeComedia(titulo, classificacao, diretor, ano, quantidade, disponivel);
            case "suspense" -> new FilmeSuspense(titulo, classificacao, diretor, ano, quantidade, disponivel);
            case "romance"  -> new FilmeRomance(titulo, classificacao, diretor, ano, quantidade, disponivel);
            case "terror"   -> new FilmeTerror(titulo, classificacao, diretor, ano, quantidade, disponivel);
            default -> {
                System.out.println("Gênero desconhecido: " + rs.getString("Genero"));
                yield null;
            }
        };

        if (filme != null) filme.setIdFilme(rs.getInt("Id"));
        return filme;
    }

    public void inserir(Filme filme, String genero, String cnpj) {
        BD.conectar();
        String sql = "INSERT INTO Filme (Titulo, Ano, Diretor, Genero, Classificacao, Quantidade, Disponivel, Locadora_CNPJ) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = bd.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, filme.getTitulo());
            pst.setInt(2, filme.getAnoLancamento());
            pst.setString(3, filme.getDiretor());
            pst.setString(4, genero);
            pst.setString(5, filme.getClassificacao());
            pst.setInt(6, filme.getQuantidade());
            pst.setInt(7, filme.getDisponivel());
            pst.setString(8, cnpj);
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) filme.setIdFilme(keys.getInt(1));
            }
            System.out.println("Filme inserido com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao inserir filme!");
        }
    }

    public void atualizar(Filme filme, String genero) {
        BD.conectar();
        String sql = "UPDATE Filme SET Titulo = ?, Ano = ?, Diretor = ?, Genero = ?, Classificacao = ?, Quantidade = ?, Disponivel = ? WHERE Id = ?";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setString(1, filme.getTitulo());
            pst.setInt(2, filme.getAnoLancamento());
            pst.setString(3, filme.getDiretor());
            pst.setString(4, genero);
            pst.setString(5, filme.getClassificacao());
            pst.setInt(6, filme.getQuantidade());
            pst.setInt(7, filme.getDisponivel());
            pst.setInt(8, filme.getIdFilme());
            pst.executeUpdate();
            System.out.println("Filme atualizado com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar filme!");
        }
    }

    public void deletar(int idFilme) {
        BD.conectar();
        String sql = "DELETE FROM Filme WHERE Id = ?";
        try (PreparedStatement pst = bd.prepareStatement(sql)) {
            pst.setInt(1, idFilme);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao remover filme do banco!");
        }
    }
}
