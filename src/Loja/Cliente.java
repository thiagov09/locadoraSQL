package Loja;

import Sql.BD;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;


public class Cliente extends Conta {
    private ArrayList<Emprestimo> emprestimos = new ArrayList<>();
    private ArrayList<Multa> multas = new ArrayList<>();
    Scanner sc = new Scanner(System.in);

    public Cliente(String nome, String cpf, String senha, LocalDate dataDeNascimento) {
        super(nome, cpf, senha, dataDeNascimento);
    }

    public Cliente(String nome, String cpf, int hashsenha, LocalDate dataDeNascimento) {
        super(nome, cpf, hashsenha, dataDeNascimento);
    }

    public void addEmprestimo(Emprestimo emprestimo) {
        emprestimos.add(emprestimo);
    }

    public void addMulta(Multa multa) {
        multas.add(multa);
    }

    public ArrayList<Emprestimo> getEmprestimos() {
        return emprestimos;
    }

    public ArrayList<Multa> getMultas() {
        return multas;
    }

    public void pagaMulta(int id, Connection bd) {
        for (int i = 0; i < multas.size(); i++) {
            if (multas.get(i).getId() == id && multas.get(i).getDataDePagamento() == null) {
                String sql = "UPDATE Multa SET DataPagamento = ? WHERE Id = ?";
                try (PreparedStatement st = bd.prepareStatement(sql)) {
                    st.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
                    st.setInt(2, multas.get(i).getId());
                    st.executeUpdate();
                } catch (SQLException e) {
                    System.out.println("Erro ao pagar multa!");
                    e.printStackTrace();
                    return;
                }
                multas.get(i).setDataDePagamento(LocalDate.now());
                System.out.println("✅ Multa paga!");
                return;
            }
        }
        System.out.println("Multa não encontrada!");
    }

    public void alugarFilme(Locadora locadoraAtual, Connection bd) {
        for (Multa multa : multas) {
            if (multa.getDataDePagamento() == null) {
                System.out.println("⚠️  Você possui multas pendentes. Quite-as antes de alugar.");
                return;
            }
        }

        System.out.println("\n======== FILMES DISPONÍVEIS ========");
        boolean temFilme = false;
        for (Filme filme : locadoraAtual.getFilmes()) {
            if (filme.getDisponivel() > 0) {
                filme.mostra();
                temFilme = true;
            }
        }
        if (!temFilme) {
            System.out.println("Nenhum filme disponível no momento.");
            return;
        }
        System.out.println("====================================\n");

        System.out.print("Digite o título do filme que deseja alugar: ");
        String filmePesquisa = sc.nextLine();

        for (Filme filme : locadoraAtual.getFilmes()) {
            if (filme.getTitulo().equalsIgnoreCase(filmePesquisa)) {
                if (filme.getDisponivel() == 0) {
                    System.out.println("Filme indisponível no momento!");
                    return;
                }

                Emprestimo emprestimoPlaceholder = new Emprestimo(locadoraAtual, filme.getTitulo(), this.getCpf());

                String sql = "INSERT INTO Emprestimo (Data, Devolucao, Cliente_CPF, Locadora_CNPJ, Filme_Id, NomeFilme) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement st = bd.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    st.setDate(1, java.sql.Date.valueOf(emprestimoPlaceholder.getData()));
                    st.setDate(2, java.sql.Date.valueOf(emprestimoPlaceholder.getDevolucao()));
                    st.setString(3, this.getCpf());
                    st.setString(4, locadoraAtual.getCNPJ());
                    st.setInt(5, filme.getIdFilme());
                    st.setString(6, filme.getTitulo());
                    st.executeUpdate();

                    try (ResultSet keys = st.getGeneratedKeys()) {
                        if (keys.next()) emprestimoPlaceholder.setIdEmprestimo(keys.getInt(1));
                    }
                } catch (SQLException e) {
                    System.out.println("Erro ao registrar aluguel!");
                    e.printStackTrace();
                    return;
                }

                this.addEmprestimo(emprestimoPlaceholder);
                System.out.println("✅ Filme alugado com sucesso! Devolva até: " + emprestimoPlaceholder.getDevolucao());
                return;
            }
        }
        System.out.println("Filme não encontrado!");
    }

    public void devolverFilme(Locadora locadoraAtual, Connection bd) {
        if (this.getEmprestimos().isEmpty()) {
            System.out.println("Você não possui filmes alugados.");
            return;
        }

        System.out.println("\n======== SEUS ALUGUÉIS ATIVOS ========");
        boolean temAtivo = false;
        for (Emprestimo emp : emprestimos) {
            if (emp.getDevolvido() == null) {
                emp.mostra();
                temAtivo = true;
            }
        }
        if (!temAtivo) {
            System.out.println("Nenhum filme pendente de devolução.");
            return;
        }
        System.out.println("======================================\n");

        System.out.print("Informe o ID do empréstimo para devolver: ");
        int verificaFilme;
        try {
            verificaFilme = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Insira apenas números no ID!");
            return;
        }

        for (Emprestimo emp : emprestimos) {
            if (emp.getIdEmprestimo() == verificaFilme) {
                String sql = "UPDATE Emprestimo SET Devolvido = ? WHERE Id = ?";
                try (PreparedStatement st = bd.prepareStatement(sql)) {
                    st.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
                    st.setInt(2, emp.getIdEmprestimo());
                    st.executeUpdate();
                } catch (SQLException e) {
                    System.out.println("Erro ao devolver filme!");
                    e.printStackTrace();
                    return;
                }

                for (Filme filme : locadoraAtual.getFilmes()) {
                    if (filme.getTitulo().equals(emp.getNomeFilme())) {
                        sql = "UPDATE Filme SET Disponivel = ? WHERE Id = ?";
                        try (PreparedStatement st = bd.prepareStatement(sql)) {
                            st.setInt(1, filme.getDisponivel() + 1);
                            st.setInt(2, filme.getIdFilme());
                            st.executeUpdate();
                        } catch (SQLException e) {
                            System.out.println("Erro ao atualizar disponibilidade!");
                            e.printStackTrace();
                            return;
                        }
                        filme.setDisponivel(filme.getDisponivel() + 1);
                        break;
                    }
                }

                emp.setDevolvido(LocalDate.now());
                System.out.println("✅ Filme devolvido com sucesso!");
                return;
            }
        }
        System.out.println("Empréstimo não encontrado.");
    }

    public void conferirMulta(Locadora locadoraAtual) {
        boolean possuiMulta = false;
        for (Multa multa : multas) {
            if (multa.getDataDePagamento() == null) {
                possuiMulta = true;
                break;
            }
        }
        if (possuiMulta) {
            System.out.println("\n======== SUAS MULTAS PENDENTES ========");
            for (Multa multa : multas) {
                if (multa.getDataDePagamento() == null) multa.mostra();
            }
            System.out.println("======================================\n");
            System.out.print("Informe o ID da multa que deseja quitar: ");
            int id;
            try {
                id = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Insira apenas números no ID!");
                return;
            }
            pagaMulta(id, BD.getConexao());
        } else {
            System.out.println("✅ Sem multas pendentes!");
        }
    }
}
