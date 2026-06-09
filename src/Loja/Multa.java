package Loja;

import Programa.Exibir;
import java.time.LocalDate;

public class Multa implements Exibir {
    private int id;
    private int idEmprestimo;
    private float valor;
    private LocalDate dataDeInicio;
    private LocalDate dataDePagamento;
    private String cpfUsuario;

    public Multa(int idEmprestimo, float valor, LocalDate data, String cpfUsuario) {
        this.id = -1;
        this.idEmprestimo = idEmprestimo;
        this.valor = valor;
        this.dataDeInicio = data;
        this.cpfUsuario = cpfUsuario;
    }

    public int getId() {
        return id;
    }

    public int getIdEmprestimo() {
        return idEmprestimo;
    }

    public LocalDate getDataDePagamento() {
        return dataDePagamento;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }

    public void setDataDePagamento(LocalDate dataDePagamento) {
        this.dataDePagamento = dataDePagamento;
    }

    public float getValor() {
        return valor;
    }

    @Override
    public void mostra() {
        System.out.println("  ----------------------------------------");
        System.out.println("  Multa #" + id + "  (Empréstimo #" + idEmprestimo + ")");
        System.out.printf( "  Valor         : R$ %.2f (R$1,00 por dia)%n", valor);
        System.out.println("  Desde         : " + dataDeInicio);
        System.out.println("  CPF           : " + cpfUsuario);
        if (dataDePagamento != null) System.out.println("  Pago em       : " + dataDePagamento);
        System.out.println("  ----------------------------------------");
    }
}
