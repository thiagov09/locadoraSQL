package Loja.Generos;

import Loja.Filme;

public class FilmeSuspense extends Filme {

    public FilmeSuspense(String titulo, String classificacao, String diretor, int anoLancamento, int quantidade, int disponivel) {
        super(titulo, classificacao, diretor, anoLancamento, quantidade, disponivel);
    }

    @Override
    public void descricaoGenero() {
        System.out.println("A ascensão do gênero thriller coincide com a ascensão dos romances policiais e de espionagem das décadas de 1960 e 1970. Tem sido um dos melhores veículos cinematográficos para explorar verdades, por vezes perturbadoras e pouco representadas, sobre nossos governos e a sociedade em geral. Graças a cineastas famosos como Alfred Hitchcock e a personagens favoritos como James Bond, o thriller se tornou uma parte popular e importante da tradição cinematográfica.\n");
    }
}
