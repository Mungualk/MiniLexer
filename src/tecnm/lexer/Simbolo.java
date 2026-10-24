package tecnm.lexer;

/**
 * Entrada de la Tabla de Símbolos. Solo se crea en la PRIMERA aparición de un identificador; las apariciones
 * posteriores reutilizan este mismo objeto, por eso guarda la posición inicial.
 */
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

    /** Fila de tabla con columnas alineadas, útil para imprimir mas facil la tabla completa. */
    @Override
    public String toString()
    {
        return String.format("%-10s %-18s L%d, C%d", lexema, tipoToken, lineaInicial, columnaInicial);
    }
}