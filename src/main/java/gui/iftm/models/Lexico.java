package gui.iftm.models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

public class Lexico {

    private String filePath;
    private int current_int;
    private int lastOneBefore_current_int;
    private char current_char;
    PushbackReader pushbackReader;
    BufferedReader bufferedReader;
    private int colunaCount = 0;
    private int linhaCount = 1;

    public Lexico(String fileName) {
        filePath = Paths.get(fileName).toAbsolutePath().toString();

        try {
            this.bufferedReader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8));
            this.pushbackReader = new PushbackReader(bufferedReader);
            readNextChar();
        } catch (Exception e) {
            System.err.println("Não foi possível abrir o arquivo ou ler do arquivo: " + filePath);
            e.printStackTrace();
            filePath = null;
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public Token getToken() {
        try {
            checkEndOfLine();
            checkSpaces();

            if(current_int == -1) {
                return buildAndReturnToken(Classe.cEOF, null);
            } else if(Character.isLetter(current_char)) {
                return dealWithLetterCharacter();
            } else if (Character.isDigit(current_char)) {
                return dealWithDigitCharacter();
            } else {
                return dealWithSymbolCharacter();
            }
        } catch (IOException e) {
            System.err.println("Não foi possível abrir o arquivo ou ler do arquivo: " + filePath);
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //READING ----------------------------------------------------------------------
    private void readNextChar() throws IOException {
        lastOneBefore_current_int = current_int;
        colunaCount++;
        current_int = pushbackReader.read();
        current_char = (char) current_int;
    }

    private void unReadChar() throws IOException {
        System.out.println("Processo de unread");
        System.out.println("Antes " + (char) current_int);
        colunaCount--;
        pushbackReader.unread(current_int);
        current_int = lastOneBefore_current_int;
        current_char = (char) current_int;
        System.out.println("Depois " + current_char);
    }

    private void goToNextLine() throws IOException {
        if(current_int == 10) linhaCount++;
        colunaCount=0;
        readNextChar();
    }
    //END OF READING ----------------------------------------------------------------------

    //CHECK ----------------------------------------------------------------------
    private void checkEndOfLine() throws IOException {
        while(current_int == 13 || current_int == 10){
            goToNextLine();
        }
    }

    private void checkSpaces() throws IOException {
        while(current_char == ' '){
            readNextChar();
            checkEndOfLine();
        }
    }

    private boolean checkReservedWords(String word){
        List<String> reservedWords = List.of("and", "array", "begin", "case", "const", "div", "do", "downto",
                "else", "end", "file", "for", "function", "goto", "if", "in", "integer", "label", "mod", "nil", "not",
                "of", "or", "packed", "procedure", "program", "read", "real", "record", "repeat", "set", "then",
                "to", "type", "until", "var", "while", "with", "write", "writeln");
        return reservedWords.contains(word.toLowerCase());
    }
    //END OF CHECK ----------------------------------------------------------------------

    //SITUATION TREATMENT ----------------------------------------------------------------------
    private Token dealWithLetterCharacter() throws IOException {
        int firstIndex = colunaCount;
        StringBuilder lexema = new StringBuilder("");

        while (Character.isLetter(current_char) || Character.isDigit(current_char)) {
            lexema.append(current_char);
            readNextChar();
        }

        Valor valor = new Valor(lexema.toString());

        if(checkReservedWords(lexema.toString()))
            return buildAndReturnToken(Classe.cPalRes,valor,firstIndex);
        else
            return buildAndReturnToken(Classe.cId,valor,firstIndex);
    }

    private Token dealWithDigitCharacter() throws IOException, Exception {
        int firstIndex = colunaCount;
        StringBuilder lexema = new StringBuilder("");

        while (Character.isDigit(current_char) || current_char == '.') {
            lexema.append(current_char);
            readNextChar();
        }

        if(lexema.toString().contains(".")){
            //float / Real
            double num;
            try {
                num = Float.parseFloat(lexema.toString());
            } catch (NumberFormatException numErr) {
                throw new Exception("Não foi possível converter o número " + lexema.toString() + " para Real.");
            }
            return buildAndReturnToken(Classe.cReal,new Valor(num),firstIndex);
        } else {
            //int
            int num;
            try {
                num = Integer.parseInt(lexema.toString());
            } catch (NumberFormatException numErr) {
                throw new Exception("Não foi possível converter o número " + lexema.toString() + "para int.");
            }
            return buildAndReturnToken(Classe.cInt,new Valor(num),firstIndex);
        }
    }

    private Token dealWithSymbolCharacter() throws IOException, Exception {
        Token simpleSymbol = searchForSimpleSymbol();
        if(simpleSymbol != null){
            readNextChar();
            return simpleSymbol;
        } else {
            int firstIndex = colunaCount;

            if(current_char == ':') {
                readNextChar();
                if(current_char == '='){ // :=
                    readNextChar();
                    return buildAndReturnToken(Classe.cAtribuicao,null, firstIndex);
                } else { //  :
                    return buildAndReturnToken(Classe.cDoisPontos, null, firstIndex);
                }
            } else if(current_char == '<') {
                readNextChar();
                if(current_char == '='){ // <=
                    readNextChar();
                    return buildAndReturnToken(Classe.cMenorIgual, null, firstIndex);
                }else if(current_char == '>'){ // <>
                    readNextChar();
                    return buildAndReturnToken(Classe.cDiferente, null, firstIndex);
                } else { // <
                    return buildAndReturnToken(Classe.cMenor, null, firstIndex);
                }
            } else if(current_char == '>') {
                readNextChar();
                if(current_char == '='){ // >=
                    readNextChar();
                    return buildAndReturnToken(Classe.cMaiorIgual, null, firstIndex);
                } else { // >
                    return buildAndReturnToken(Classe.cMaior, null, firstIndex);
                }
            } else if(current_char == '\'') { // string
                StringBuilder lexema = new StringBuilder("");
                do{
                    lexema.append(current_char);
                    readNextChar();
                    if(current_int == 13 || current_int == 10 || current_int == -1)
                        throw new Exception("Uma string foi iniciada com o indicador ' porém não foi finalizada.");
                } while (current_char != '\'');
                readNextChar();
                return buildAndReturnToken(Classe.cString, new Valor(lexema.toString()), firstIndex);
            } else {
                throw new Exception("Situação não tratada encontrada: " + current_char);
            }
        }
    }

    private Token searchForSimpleSymbol() {
        switch (current_char){
            case '+': return buildAndReturnToken(Classe.cMais, null);
            case '-': return buildAndReturnToken(Classe.cMenos, null);
            case '/': return buildAndReturnToken(Classe.cDivisao, null);
            case '*': return buildAndReturnToken(Classe.cMultiplicacao, null);
            case '=': return buildAndReturnToken(Classe.cIgual, null);
            case ',': return buildAndReturnToken(Classe.cVirgula, null);
            case ';': return buildAndReturnToken(Classe.cPontoVirgula, null);
            case '.': return buildAndReturnToken(Classe.cPonto, null);
            case '(': return buildAndReturnToken(Classe.cParEsq, null);
            case ')': return buildAndReturnToken(Classe.cParDir, null);
        }
        return null;
    }
    //END OF SITUATION TREATMENT ----------------------------------------------------------------------

    //RETURN CONSTRUCTOR ----------------------------------------------------------------------
    private Token buildAndReturnToken(Classe classe, Valor valor) { // SE NÃO PASSAR UM VALOR PARA COLUNA, USA O VALOR ATUAL (colunaCount)
        return buildAndReturnToken(classe, valor, colunaCount);
    }
    private Token buildAndReturnToken(Classe classe, Valor valor, int coluna) { // OBRIGATÓRIO INFORMAR A COLUNA
        Token token = new Token(classe,valor,linhaCount,coluna);
        return token;
    }
    //END OF RETURN CONSTRUCTOR ----------------------------------------------------------------------
}