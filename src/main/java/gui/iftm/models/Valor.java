package gui.iftm.models;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Valor {
    private int valorInteiro;
    private double valorDecimal;
    private String valorIdentificador;

    public Valor() {}
    public Valor(double valorDecimal) { this.valorDecimal = valorDecimal; }
    public Valor(int valorInteiro) { this.valorInteiro = valorInteiro; }
    public Valor(String valorIdentificador) { this.valorIdentificador = valorIdentificador; }

    public int getValorInteiro() { return valorInteiro; }
    public void setValorInteiro(int valorInteiro) { this.valorInteiro = valorInteiro; }

    public double getValorDecimal() { return valorDecimal; }
    public void setValorDecimal(double valorDecimal) { this.valorDecimal = valorDecimal; }

    public String getValorIdentificador() { return valorIdentificador; }
    public void setValorIdentificador(String valorIdentificador) { this.valorIdentificador = valorIdentificador; }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.######", new DecimalFormatSymbols(Locale.ENGLISH));
        String valorDecimalString = valorDecimal != 0 ? df.format(valorDecimal) : String.valueOf(valorDecimal);
        return "Valor {" +
                "valorInteiro: " + valorInteiro +
                ", valorDecimal: " + valorDecimalString +
                ", valorIdentificador: '" + valorIdentificador + '\'' +
                '}';
    }
}
