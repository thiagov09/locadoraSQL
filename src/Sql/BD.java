package Sql;

import java.sql.*;

public abstract class BD{
    private static String url = "jdbc:mysql://localhost:3306/locadora";
    private static String usuario = "root";
    private static String senha = "roof";      //Dependendo da senha do SQL do computador do host
    private static Connection conexao = null;

    public static boolean conectar(){
        try{
            conexao = DriverManager.getConnection(url, usuario, senha);
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

