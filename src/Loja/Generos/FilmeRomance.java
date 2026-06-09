package Loja.Generos;

import Loja.Filme;

public class FilmeRomance extends Filme {

    public FilmeRomance(String titulo, String classificacao, String diretor, int anoLancamento, int quantidade, int disponivel) {
        super(titulo, classificacao, diretor, anoLancamento, quantidade, disponivel);
    }

    @Override
    public void descricaoGenero() {
        System.out.println("Ah, o que seria da grandiosa tradição do cinema sem as inúmeras histórias de amor e namoro? Desde o surgimento das salas de cinema, o cinema tem sido um passatempo predileto para casais que buscam escapar para um mundo de romance.\n");
    }
}