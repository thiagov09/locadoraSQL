package Sql;

import java.sql.*;

public class BD{
    private static String url = "jdbc:mysql://localhost:3306/locadora";
    private static String usuario = "root";
    private static String senha = "root";      //Dependendo da senha do SQL do computador do host
    private static Connection conexao = null;

    public static boolean conectar(){
        try{
            conexao = DriverManager.getConnection(url, usuario, senha);
            System.out.println("Conexão ao banco de dados com sucesso! ");
            return true;
        }
        catch (SQLException e){
            System.out.println("Erro ao conectar ao banco de dados!");

            return false;
        }
    }

    public static Connection getConexao() {
        return conexao;
    }
}

