package gui.iftm.models;

public enum ErrorConstants {
    //PONTUAÇÃO / FORMATAÇÃO
    pontoFinal("Esperado ponto ao final"),
    pontoVirgula("Esperado ponto e virgula (\";\") ao final da linha"),
    doisPontos("Esperado dois pontos (\":\") para declaracao do tipo de variavel"),
    nomePrograma("Esperado nome do programa"),
    parenteseFechamento("Esperado fechamento de parentese (\")\""),
    parenteseAbertura("Esperado abertura de parentese (\"(\""),
    operadorComparacao("Esperado operador matemático de comparacao (\">= > <= < = <>\""),
    operadorSoma("Esperado operador matemático de soma (\"+ -\""),
    operadorMultiplicacao("Esperado operador matemático de multiplicacao (\"+ -\""),
    operadorAtribuicao("Esperado operador de atribuicao (\":=\""),

    //DECLARAÇÕES
    declaracaoPrograma("Esperado declaracao do programa (\"program\")"),
    declaracaoTipoVariavel("Esperado declaracao valida do tipo de variavel"),
    declaracaoIdentificadorVariavel("Esperado nome/identificador da variavel"),
    declaracaoInicio("Esperado declaracao de inicio (\"begin\")"),
    declaracaoFim("Esperado declaracao de fim (\"end\")"),
    declaracaoTo("Esperado declaracao de token To"),
    declaracaoDo("Esperado declaracao de token Do"),
    declaracaoUntil("Esperado declaracao de token Until"),
    declaracaoExpoente("Esperado fator de expoente"),

    //ROTINAS
    rotinaWhile(" na rotina de repeticao while"),
    rotinaFor(" na rotina de repeticao for"),
    rotinaRepeat(" na rotina de repeticao repeat"),
    rotinaIf(" na rotina if"),
    rotinaPrograma(" no programa");

    private String description;
    ErrorConstants(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}
