package gui.iftm;

import gui.iftm.models.*;


public class TradutorPascalParaC {

    public static void main(String[] args) {
        String filePath = "src/main/java/gui/iftm/test/teste.pas";
        Helper.parseTabIntoSpace(filePath);

        Sintatico sintatico = new Sintatico(filePath);
        sintatico.ExecutarAnalise();
    }
}