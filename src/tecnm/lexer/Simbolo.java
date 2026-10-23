package tecnm.lexer;

public class Simbolo
{
    private String lexema;
    private String tipoToken;
    private int lineaInicial;
    private int columnaInicial;

    public Simbolo(String lexema, String tipoToken, int lineaInicial, int columnaInicial)
    {
        this.lexema = lexema;
        this.tipoToken = tipoToken;
        this.lineaInicial = lineaInicial;
        this.columnaInicial = columnaInicial;
    }

    public String getLexema()
    {
        return lexema;
    }

    public String getTipoToken()
    {
        return tipoToken;
    }

    public int getLineaInicial()
    {
        return lineaInicial;
    }

    public int getColumnaInicial()
    {
        return columnaInicial;
    }

    @Override
    public String toString()
    {
        return String.format("%-10s %-18s L%d, C%d", lexema, tipoToken, lineaInicial, columnaInicial);
    }
}