package Loja;

public abstract class Filme implements Programa.Exibir {
    protected int idFilme;
    protected String titulo;
    protected String classificacao;
    protected String diretor;
    protected int anoLancamento;
    protected int quantidade;
    protected int disponivel;

    public Filme(String titulo, String classificacao, String diretor, int anoLancamento, int quantidade, int disponivel) {
        this.idFilme = -1;
        this.titulo = titulo;
        this.classificacao = classificacao;
        this.diretor = diretor;
        this.anoLancamento = anoLancamento;
        this.quantidade = quantidade;
        this.disponivel = disponivel;
    }

    public int getIdFilme() {
        return idFilme;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getClassificacao() {
        return classificacao;
    }

    public String getDiretor() {
        return diretor;
    }

    public int getAnoLancamento() {
        return anoLancamento;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public int getDisponivel() {
        return disponivel;
    }

    public void setDisponivel(int disponivel) {
        this.disponivel = disponivel;
    }

    public void setIdFilme(int idFilme) {
        this.idFilme = idFilme;
    }

    public abstract void descricaoGenero();

    @Override
    public void mostra() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.printf( "  %-36s%n", titulo);
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println("  ID            : " + idFilme);
        System.out.println("  Diretor       : " + diretor);
        System.out.println("  Ano           : " + anoLancamento);
        System.out.println("  Classificação : " + classificacao);
        System.out.println("  Disponíveis   : " + disponivel + " / " + quantidade);
        descricaoGenero();
    }
}
