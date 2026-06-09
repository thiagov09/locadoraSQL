package Programa;

public class TempoSessao implements Runnable {
    private boolean ativo = true;
    private int segundos = 0;

    public void encerrar() {
        ativo = false;
    }

    public int getSegundos() {
        return segundos;
    }

    @Override
    public void run() {

        while (ativo) {

            try {
                Thread.sleep(1000);
                segundos++;

            } catch (InterruptedException e) {
                System.out.println("Thread interrompida.");
                return;
            }
        }
    }
}