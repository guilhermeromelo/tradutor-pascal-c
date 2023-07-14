package gui.iftm.models;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Sintatico {
    private Lexico lexico;
    private Token token;
    private TabelaSimbolos tabela;
    private BufferedWriter bufferedWriter;
    private String rotulo = "";
    private int endereco;
    private List<Registro> variaveisDeclaradas = new ArrayList<>();


    public Sintatico(String filePath) {
        this.lexico = new Lexico(filePath);
    }

    public void readNextToken() {
        token = lexico.getToken();
        System.out.println(token);
    }

    public void ExecutarAnalise() {
        readNextToken();

        this.endereco = 0;
        String caminhoArquivoSaida = Paths.get("outputFile.c").toAbsolutePath().toString();

        bufferedWriter = null;
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(caminhoArquivoSaida, Charset.forName("UTF-8"));
            bufferedWriter = new BufferedWriter(fileWriter);
            programa();
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Simbolos encontrados:");
        System.out.println(this.tabela);
    }

    //ACOES DO TRADUTOR ------------------------------------------------------------------------------------------------
    public void AcaoCriarCabeçalhoCodigoC(){
        tabela=new TabelaSimbolos();

        tabela.setTabelaPai(null);

        Registro registro=new Registro();
        registro.setNome(token.getValor().getValorIdentificador());
        registro.setCategoria(Categoria.PROGRAMAPRINCIPAL);

        registro.setNivel(0);
        registro.setOffset(0);
        registro.setTabelaSimbolos(tabela);
        registro.setRotulo("main");
        tabela.inserirRegistro(registro);
        String codigo = "#include <stdio.h>\n" +
                "\nint main(){\n";

        gerarCodigo(codigo);
    }

    public void AcaoCriarRodapeCodigoC(){
        Registro registro=new Registro();
        registro.setNome(null);
        registro.setCategoria(Categoria.PROGRAMAPRINCIPAL);
        registro.setNivel(0);
        registro.setOffset(0);
        registro.setTabelaSimbolos(tabela);
        registro.setRotulo("finalCode");
        tabela.inserirRegistro(registro);
        String codigo = "\n}\n";
        gerarCodigo(codigo);
    }

    private void AcaoDeclararVariavelNumerica(String type) {
        String codigo = '\t'+type;
        for(int i = 0; i<this.variaveisDeclaradas.size(); i++)
        {
            codigo = codigo + ' ' + this.variaveisDeclaradas.get(i).getNome();
            if(i == this.variaveisDeclaradas.size()-1)
            {
                codigo = codigo + ';';
            }
            else{
                codigo = codigo + ',';
            }
        }
        gerarCodigo(codigo);
    }

    public void AcaoDeclararVariaveisChar()
    {
        Registro registro=new Registro();
        registro.setNome(token.getValor().getValorIdentificador());
        registro.setCategoria(Categoria.VARIAVEL);
        registro.setNivel(0);
        registro.setOffset(0);
        registro.setTabelaSimbolos(tabela);
        this.endereco++;
        registro.setRotulo("variavel"+this.endereco);
        variaveisDeclaradas.add(registro);
        this.tabela.inserirRegistro(registro);
    }
    //FIM ACOES DO TRADUTOR --------------------------------------------------------------------------------------------

    private void gerarCodigo(String instrucoes) {
        try {
            if (rotulo.isEmpty()) {
                bufferedWriter.write(instrucoes + "\n");
            } else {
                bufferedWriter.write(rotulo + ": " +  instrucoes + "\n");
                rotulo = "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void programa() {
        if ((token.getClasse() == Classe.cPalRes)
                && (token.getValor().getValorIdentificador().equalsIgnoreCase("program"))) {
            readNextToken();
            if (token.getClasse() == Classe.cId) {
                readNextToken();
                AcaoCriarCabeçalhoCodigoC();
                corpo();
                if (token.getClasse() == Classe.cPonto) {
                    readNextToken();
                } else showErrorMessage(ErrorConstants.pontoFinal.getDescription());
                AcaoCriarRodapeCodigoC();
            } else showErrorMessage(ErrorConstants.nomePrograma.getDescription());
        } else showErrorMessage(ErrorConstants.declaracaoPrograma.getDescription());
    }

    public void corpo() {
        declara();
        if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("begin"))) {
            readNextToken();
            sentencas();
            if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("end"))) {
                readNextToken();
            } else showErrorMessage(ErrorConstants.declaracaoFim.getDescription() + ErrorConstants.rotinaPrograma.getDescription());
        } else showErrorMessage(ErrorConstants.declaracaoInicio.getDescription() + ErrorConstants.rotinaPrograma.getDescription());
    }

    public void declara() {
        if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("var"))) {
            readNextToken();
            dvar();
            mais_dc();
        }
    }

    public void mais_dc() {
        if (token.getClasse() == Classe.cPontoVirgula) {
            readNextToken();
            cont_dc();
        } else showErrorMessage(ErrorConstants.pontoVirgula.getDescription());
    }

    public void cont_dc() {
        if (token.getClasse() == Classe.cId) {
            dvar();
            mais_dc();
        }
    }

    public void dvar() {
        variaveis();
        if (token.getClasse() == Classe.cDoisPontos) {
            readNextToken();
            tipo_var();
        } else showErrorMessage(ErrorConstants.doisPontos.getDescription());
    }

    public void tipo_var() {
        if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("integer"))) {
            AcaoDeclararVariavelNumerica("int");
            readNextToken();
        } else if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("real"))) {
            AcaoDeclararVariavelNumerica("float");
            readNextToken();
        } else showErrorMessage(ErrorConstants.declaracaoTipoVariavel.getDescription());
    }

    public void variaveis() {
        if (token.getClasse() == Classe.cId) {
            AcaoDeclararVariaveisChar();
            readNextToken();
            mais_var();
        } else showErrorMessage(ErrorConstants.declaracaoIdentificadorVariavel.getDescription());
    }

    public void mais_var() {
        if (token.getClasse() == Classe.cVirgula) {
            readNextToken();
            variaveis();
        }
    }

    public void sentencas() {
        comando();
        mais_sentencas();
    }

    public void mais_sentencas() {
        if (token.getClasse() == Classe.cPontoVirgula) {
            readNextToken();
            cont_sentencas();
        } else showErrorMessage(ErrorConstants.pontoVirgula.getDescription());
    }

    public void cont_sentencas() {
        if(token.getClasse() == Classe.cPalRes) {
            String valIdentificador = token.getValor().getValorIdentificador();

            if((valIdentificador.equalsIgnoreCase("read")) ||
                    (valIdentificador.equalsIgnoreCase("write")) ||
                    (valIdentificador.equalsIgnoreCase("for")) ||
                    (valIdentificador.equalsIgnoreCase("repeat")) ||
                    (valIdentificador.equalsIgnoreCase("while")) ||
                    (valIdentificador.equalsIgnoreCase("if"))){
                sentencas();
            }
        } else if (token.getClasse() == Classe.cId){
            sentencas();
        }
    }

    public List<Token> var_read(List<Token> arrayTokens) {
        if (token.getClasse() == Classe.cId) {
            arrayTokens.add(token);
            readNextToken();
            arrayTokens = mais_var_read(arrayTokens);
        } else showErrorMessage(ErrorConstants.declaracaoIdentificadorVariavel.getDescription());
        return arrayTokens;
    }

    public List<Token> mais_var_read(List<Token> arrayTokens) {
        if (token.getClasse() == Classe.cVirgula) {
            readNextToken();
            arrayTokens = var_read(arrayTokens);
        }
        return arrayTokens;
    }

    public String var_write(String codigo) {
        if (token.getClasse() == Classe.cId) {
            codigo = codigo+token.getValor().getValorIdentificador();
            readNextToken();
            codigo = mais_var_write(codigo);
        } else showErrorMessage(ErrorConstants.declaracaoIdentificadorVariavel.getDescription());
        return codigo;
    }

    public String mais_var_write(String codigo) {
        if (token.getClasse() == Classe.cVirgula) {
            codigo = codigo+ ',';
            readNextToken();
            codigo = var_write(codigo);
        }
        return codigo;
    }

    public void comando() {
        if(token.getClasse() == Classe.cPalRes){
            String valIdentificador = token.getValor().getValorIdentificador();

            if (valIdentificador.equalsIgnoreCase("read")){
                comandoRead();
            } else if (valIdentificador.equalsIgnoreCase("write")){
                comandoWrite();
            } else if (valIdentificador.equalsIgnoreCase("for")){
                comandoFor();
            } else if (valIdentificador.equalsIgnoreCase("repeat")){
                comandoRepeat();
            } else if (valIdentificador.equalsIgnoreCase("while")){
                comandoWhile();
            } else if (valIdentificador.equalsIgnoreCase("if")){
                comandoIf();
            }
        } else if (token.getClasse() == Classe.cId){
            String codigo="\n\t";
            codigo=codigo+token.getValor().getValorIdentificador();
            readNextToken();
            if (token.getClasse() == Classe.cAtribuicao){
                codigo=codigo+"=";
                readNextToken();
                codigo=codigo+expressao()+";";
                gerarCodigo(codigo);
            } else showErrorMessage(ErrorConstants.operadorAtribuicao.getDescription());
        }
    }

    public void comandoRead() {
        String codigo = "\tscanf";
        readNextToken();
        if (token.getClasse() == Classe.cParEsq) {
            codigo = codigo + "(\"";
            readNextToken();
            List<Token> arrayToken = new ArrayList<Token>();
            arrayToken=var_read(arrayToken);
            for(Token i: arrayToken){
                codigo = codigo + "%d ";
            }
            codigo = codigo + "\", ";
            for(Token i: arrayToken){
                if(i == arrayToken.get(arrayToken.size()-1)){
                    codigo = codigo + "&"+i.getValor().getValorIdentificador();
                }else{
                    codigo = codigo + "&"+i.getValor().getValorIdentificador()+", ";
                }
            }
            if (token.getClasse() == Classe.cParDir) {
                codigo = codigo + ");";
                gerarCodigo(codigo);
                readNextToken();
            } else showErrorMessage(ErrorConstants.parenteseFechamento.getDescription());
        } else showErrorMessage(ErrorConstants.parenteseAbertura.getDescription());
    }

    public void comandoWrite() {
        String referencias="\tprintf";
        String codigo = "";
        readNextToken();
        if (token.getClasse() == Classe.cParEsq) {
            referencias = referencias + "(\"";
            readNextToken();
            codigo = codigo + var_write("");

            if (codigo.length() >  0) {
                referencias = referencias + "%d ".repeat(codigo.split(",").length);
                referencias = referencias + "\", ";
            } else {
                referencias = referencias + "\"";
            }

            if (token.getClasse() == Classe.cParDir) {
                codigo = codigo+");";
                gerarCodigo(referencias + codigo);
                readNextToken();
            } else showErrorMessage(ErrorConstants.parenteseFechamento.getDescription());
        } else showErrorMessage(ErrorConstants.parenteseAbertura.getDescription());
    }

    public void comandoFor() {
        String rotinaDescription = ErrorConstants.rotinaFor.getDescription();
        String codigo = "\n\tfor(";

        readNextToken();
        if (token.getClasse() == Classe.cId) {
            String identificador = token.getValor().getValorIdentificador();
            codigo = codigo+identificador;
            readNextToken();
            if (token.getClasse() == Classe.cAtribuicao){
                codigo = codigo+"=";
                readNextToken();
                codigo = codigo + expressao();
                if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("to"))){
                    codigo = codigo + ";";
                    readNextToken();
                    codigo = codigo + identificador;
                    codigo = codigo + "<="+expressao() +";";
                    codigo = codigo + identificador + "++)";
                    if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("do"))){
                        readNextToken();
                        if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("begin"))){
                            codigo = codigo +"{";
                            gerarCodigo(codigo);
                            readNextToken();
                            sentencas();
                            if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("end"))){
                                gerarCodigo("\t}");
                                readNextToken();
                            } else showErrorMessage(ErrorConstants.declaracaoFim.getDescription() + rotinaDescription);
                        } else showErrorMessage(ErrorConstants.declaracaoInicio.getDescription() + rotinaDescription);
                    } else showErrorMessage(ErrorConstants.declaracaoDo.getDescription() + rotinaDescription);
                } else showErrorMessage(ErrorConstants.declaracaoTo.getDescription() + rotinaDescription);
            } else showErrorMessage(ErrorConstants.operadorAtribuicao.getDescription() + rotinaDescription);
        } else showErrorMessage(ErrorConstants.declaracaoIdentificadorVariavel.getDescription() + rotinaDescription);
    }

    public void comandoRepeat() {
        String rotinaDescription = ErrorConstants.rotinaRepeat.getDescription();

        readNextToken();
        gerarCodigo("\n\tdo {\n\t");
        sentencas();
        if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("until"))){
            readNextToken();
            if (token.getClasse() == Classe.cParEsq){
                String codigoFinal="\n\t}while";
                codigoFinal=codigoFinal+"(";
                readNextToken();
                codigoFinal= codigoFinal + condicao();
                if (token.getClasse() == Classe.cParDir){
                    codigoFinal=codigoFinal+");";
                    gerarCodigo(codigoFinal);
                    readNextToken();
                } else showErrorMessage(ErrorConstants.parenteseFechamento.getDescription() + rotinaDescription);
            } else showErrorMessage(ErrorConstants.parenteseAbertura.getDescription() + rotinaDescription);
        } else showErrorMessage(ErrorConstants.declaracaoUntil.getDescription() + rotinaDescription);
    }

    public void comandoWhile() {
        String rotinaDescription = ErrorConstants.rotinaWhile.getDescription();
        String codigo = "\n\twhile";

        readNextToken();
        if (token.getClasse() == Classe.cParEsq){
            codigo = codigo + "(";
            readNextToken();
            codigo = codigo + condicao();
            if (token.getClasse() == Classe.cParDir){
                codigo = codigo + ")";
                readNextToken();
                if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("do"))){
                    readNextToken();
                    if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("begin"))){
                        codigo = codigo+"{\n";
                        gerarCodigo(codigo);
                        readNextToken();
                        sentencas();
                        if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("end"))){
                            codigo = "\t}\n";
                            gerarCodigo(codigo);
                            readNextToken();
                        } else showErrorMessage(ErrorConstants.declaracaoFim.getDescription() + rotinaDescription);
                    } else showErrorMessage(ErrorConstants.declaracaoInicio.getDescription() + rotinaDescription);
                } else showErrorMessage(ErrorConstants.declaracaoDo.getDescription() + rotinaDescription);
            } else showErrorMessage(ErrorConstants.parenteseFechamento.getDescription() + rotinaDescription);
        } else showErrorMessage(ErrorConstants.parenteseAbertura.getDescription() + rotinaDescription);
    }

    public void comandoIf() {
        String rotinaDescription = ErrorConstants.rotinaIf.getDescription();
        String codigo = "";

        readNextToken();
        if (token.getClasse() == Classe.cParEsq){
            codigo = codigo + "\n\tif(";
            readNextToken();
            codigo = codigo+condicao();
            if (token.getClasse() == Classe.cParDir){
                codigo = codigo+")";
                readNextToken();
                if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("then"))){
                    readNextToken();
                    if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("begin"))){
                        codigo = codigo +" {";
                        gerarCodigo(codigo);
                        readNextToken();
                        sentencas();
                        if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("end"))){
                            readNextToken();
                            String codigoFinal = "";
                            codigoFinal = codigoFinal + "\t}";
                            gerarCodigo(codigoFinal);
                            pfalsa();
                        } else showErrorMessage(ErrorConstants.declaracaoFim.getDescription() + rotinaDescription);
                    } else showErrorMessage(ErrorConstants.declaracaoInicio.getDescription() + rotinaDescription);
                } else showErrorMessage(ErrorConstants.declaracaoDo.getDescription() + rotinaDescription);
            } else showErrorMessage(ErrorConstants.parenteseFechamento.getDescription() + rotinaDescription);
        } else showErrorMessage(ErrorConstants.parenteseAbertura.getDescription() + rotinaDescription);
    }

    public String condicao() {
        String expressao1 = expressao();
        String relacao = relacao();
        String expressao2 = expressao();
        return expressao1 + relacao + expressao2;
    }


    public void pfalsa() {
        String codigo = "";
        if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("else"))){
            codigo = codigo + "\telse";
            readNextToken();
            if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("begin"))){
                codigo = codigo + "{";
                gerarCodigo(codigo);
                readNextToken();
                sentencas();
                if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("end"))){
                    String codigoFinal = "\n\t}";
                    gerarCodigo(codigoFinal);
                    readNextToken();
                } else showErrorMessage(ErrorConstants.declaracaoFim.getDescription());
            } else showErrorMessage(ErrorConstants.declaracaoInicio.getDescription());
        }
    }

    public String relacao() {
        String operador="";
        if (token.getClasse() == Classe.cIgual) {
            operador="=";
            readNextToken();
        } else if (token.getClasse() == Classe.cMaior) {
            operador=">";
            readNextToken();
        } else if (token.getClasse() == Classe.cMenor) {
            operador="<";
            readNextToken();
        } else if (token.getClasse() == Classe.cMaiorIgual) {
            operador = ">=";
            readNextToken();
        } else if (token.getClasse() == Classe.cMenorIgual) {
            operador = "<=";
            readNextToken();
        } else if (token.getClasse() == Classe.cDiferente) {
            operador = "!=";
            readNextToken();
        } else showErrorMessage(ErrorConstants.operadorComparacao.getDescription());
        return operador;
    }

    public String expressao() {
        String termo = termo();
        String outrosTermos = outros_termos();
        return termo + outrosTermos;
    }

    public String outros_termos() {
        String op = "";
        String termo= "";
        String outrosTermos = "";

        if (token.getClasse() == Classe.cMais || token.getClasse() == Classe.cMenos) {
            op = op_ad();
            termo = termo();
            outrosTermos = outros_termos();
        }
        return op + termo + outrosTermos;
    }

    public String op_ad() {
        String op = "";
        if (token.getClasse() == Classe.cMais) {
            op = "+";
            readNextToken();
        } else if (token.getClasse() == Classe.cMenos) {
            op = "-";
            readNextToken();
        } else showErrorMessage(ErrorConstants.operadorSoma.getDescription());
        return op;
    }


    public String termo() {
        String fator = fator();
        String maisFatores = mais_fatores();
        return fator + maisFatores;
    }

    public String mais_fatores() {
        if (token.getClasse() == Classe.cMultiplicacao || token.getClasse() == Classe.cDivisao) {
            String op = op_mul();
            String fator = fator();
            String outrosFatores = mais_fatores();
            return op + fator + outrosFatores;
        }
        return "";
    }

    public String op_mul() {
        String op = "";
        if (token.getClasse() == Classe.cMultiplicacao) {
            op = "*";
            readNextToken();
        } else if (token.getClasse() == Classe.cDivisao) {
            op = "/";
            readNextToken();
        } else showErrorMessage(ErrorConstants.operadorMultiplicacao.getDescription());
        return op;
    }

    public String fator() {
        String returnFator = "";
        if (token.getClasse() == Classe.cId) {
            returnFator = token.getValor().getValorIdentificador();
            readNextToken();
        } else if (token.getClasse() == Classe.cInt) {
            returnFator = String.valueOf(token.getValor().getValorInteiro());
            readNextToken();
        } else if (token.getClasse() == Classe.cReal) {
            returnFator = String.valueOf(token.getValor().getValorDecimal());
            readNextToken();
        } else if (token.getClasse() == Classe.cParEsq){
            returnFator="(";
            readNextToken();
            returnFator = returnFator + expressao();
            if (token.getClasse() == Classe.cParDir){
                returnFator = returnFator + ")";
                readNextToken();
            } else  showErrorMessage(ErrorConstants.parenteseFechamento.getDescription());
        } else showErrorMessage(ErrorConstants.declaracaoExpoente.getDescription());
        return returnFator;
    }

    public void showErrorMessage(String error) {
        System.err.println("Problema sintático encontrado -> " + error + " (Linha: "+ token.getLinha() + ", Coluna: " + token.getColuna() + ")");
    }
}
