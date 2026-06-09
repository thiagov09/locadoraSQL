import Loja.Locadora;
import Programa.Menu;
import Sql.BD;


void main() {
    if(BD.conectar()) {
        Locadora locadoraAtual = new Locadora();

        /*
        BD.novaLocadoraSQL("12.345.678/0001-95", "Inafilmes", "Santa Rita do Sapucaí");
        locadoraAtual.addCliente(new Cliente("Cliente1", "123.123.123-12", "senha", LocalDate.parse("2000-03-02")));
        locadoraAtual.addVendedor(new Vendedor("Vendedor1", "321.321.321-32", "senha", LocalDate.parse("2001-04-04"), 1900.00f, true));
        locadoraAtual.addVendedor(new Vendedor("Vendedor2", "444.444.444-44", "senha", LocalDate.parse("2001-04-04"), 1900.00f, false));
        */

        Menu.start(locadoraAtual);
    }
}
