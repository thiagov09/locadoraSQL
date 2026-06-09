package Loja.Generos;

import Loja.Filme;

public class FilmeTerror extends Filme {

    public FilmeTerror(String titulo, String classificacao, String diretor, int anoLancamento, int quantidade, int disponivel) {
        super(titulo, classificacao, diretor, anoLancamento, quantidade, disponivel);
    }

    @Override
    public void descricaoGenero() {
        System.out.println("Embora o gênero de terror seja às vezes considerado um gênero cinematográfico mais recente, elementos do terror são há muito tempo um alicerce do cinema clássico, remontando a alguns dos primórdios — e dos dias mais assustadores — da produção cinematográfica.\n");
    }
}
