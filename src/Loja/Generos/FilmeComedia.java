package Loja.Generos;

import Loja.Filme;

public class FilmeComedia extends Filme {

    public FilmeComedia(String titulo, String classificacao, String diretor, int anoLancamento, int quantidade, int disponivel) {
        super(titulo, classificacao, diretor, anoLancamento, quantidade, disponivel);
    }

    @Override
    public void descricaoGenero() {
        System.out.println("O Um gênero cinematográfico favorito do público jovem e adulto desde os primórdios do cinema, a comédia sempre foi um gênero divertido, sofisticado e inovador que encanta os espectadores. Alguns dos maiores nomes da história do cinema incluem pioneiros da comédia — como Buster Keaton ,  Charlie Chaplin e Lucille Ball — que construíram carreiras de sucesso encontrando maneiras novas e originais de fazer o público rir.\n");
    }
}
