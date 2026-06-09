package Loja;

import java.time.LocalDate;

public class ContaVendedor extends Conta{
    private float salario;
    private boolean adminStatus = false;

    public ContaVendedor(String nome, String cpf, String senha, LocalDate dataDeNascimento, float salario, boolean adminStatus) {
        super(nome, cpf, senha, dataDeNascimento);
        this.salario = salario;
        this.adminStatus = adminStatus;
    }

    public ContaVendedor(String nome, String cpf, int hashsenha, LocalDate dataDeNascimento, float salario, boolean adminStatus) {
        super(nome, cpf, hashsenha, dataDeNascimento);
        this.salario = salario;
        this.adminStatus = adminStatus;
    }

    public boolean isAdmin(){
        return adminStatus;
    }

    public float getSalario() {
        return salario;
    }

    public void setAdmin(boolean status){
        adminStatus = status;
    }
}