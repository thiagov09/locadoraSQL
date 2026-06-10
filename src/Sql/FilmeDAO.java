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
        ArrayList<Filme> filmes = new ArrayList<>();
        String sql = "SELECT Id, Titulo, Ano, Diretor, Genero, Classificacao, Quantidade, Disponivel " +
                     "FROM Filme WHERE Locadora_CNPJ = ?";
        try (PreparedStatement st = bd.prepareStatement(sql)) {
            st.setString(1, cnpj);
            try (ResultSet rs = st.executeQuery()) {
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
        String sql = "INSERT INTO Filme (Titulo, Ano, Diretor, Genero, Classificacao, Quantidade, Disponivel, Locadora_CNPJ) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement st = bd.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, filme.getTitulo());
            st.setInt(2, filme.getAnoLancamento());
            st.setString(3, filme.getDiretor());
            st.setString(4, genero);
            st.setString(5, filme.getClassificacao());
            st.setInt(6, filme.getQuantidade());
            st.setInt(7, filme.getDisponivel());
            st.setString(8, cnpj);
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) filme.setIdFilme(keys.getInt(1));
            }
            System.out.println("Filme inserido com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao inserir filme!");
        }
    }

    public void atualizar(Filme filme, String genero) {
        String sql = "UPDATE Filme SET Titulo = ?, Ano = ?, Diretor = ?, Genero = ?, Classificacao = ?, Quantidade = ?, Disponivel = ? WHERE Id = ?";
        try (PreparedStatement st = bd.prepareStatement(sql)) {
            st.setString(1, filme.getTitulo());
            st.setInt(2, filme.getAnoLancamento());
            st.setString(3, filme.getDiretor());
            st.setString(4, genero);
            st.setString(5, filme.getClassificacao());
            st.setInt(6, filme.getQuantidade());
            st.setInt(7, filme.getDisponivel());
            st.setInt(8, filme.getIdFilme());
            st.executeUpdate();
            System.out.println("Filme atualizado com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar filme!");
        }
    }

    public void deletar(int idFilme) {
        String sql = "DELETE FROM Filme WHERE Id = ?";
        try (PreparedStatement st = bd.prepareStatement(sql)) {
            st.setInt(1, idFilme);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao remover filme do banco!");
        }
    }
}
