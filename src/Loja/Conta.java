package Loja;

import java.time.LocalDate;

public abstract class Conta {
    protected String nome;
    protected String cpf;
    protected int hashSenha;
    protected LocalDate dataDeNascimento;
    protected boolean logado = false;

    public Conta(String nome, String cpf, String senha, LocalDate dataDeNascimento) {
        this.nome = nome;
        this.cpf = cpf;
        this.hashSenha = senha.hashCode();
        this.dataDeNascimento = dataDeNascimento;
    }

    public Conta(String nome, String cpf, int hashsenha, LocalDate dataDeNascimento) {
        this.nome = nome;
        this.cpf = cpf;
        this.hashSenha = hashsenha;
        this.dataDeNascimento = dataDeNascimento;
    }

    public void logar(String cpfLogin, String senhaLogin){
        if (this.cpf.equals(cpfLogin) && this.hashSenha == senhaLogin.hashCode()){
            logado = true;
            System.out.printf("\n--Logado com sucesso! Seja bem-vindo [%s]--\n", nome);
        }
        else System.out.println("Falha no login!");
    }

    public void deslogar(){
        if(logado){
            this.logado = false;
        }
    }

    public boolean isLogado(){
        return logado;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public int getHashSenha() {
        return hashSenha;
    }

    public LocalDate getDataDeNascimento() {
        return dataDeNascimento;
    }
}
