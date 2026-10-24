package tecnm.lexer;

/**
 * Componente léxico: lo que el analizador entrega por cada lexema reconocido.
 * Guarda la categoría (tipo), el texto exacto (lexema) y la posición donde inició.
 */
public class Token
{
    /**
     * Categorías léxicas del lenguaje .mc o .txt.
     * Se usa un enum y no un String para que el compilador detecte errores de escritura.
     */
    public enum  TipoToken
    {
        TOKEN_RESERVED,
        TOKEN_IDENTIFIER,
        TOKEN_INT_LITERAL,
        TOKEN_FLOAT_LITERAL,
        TOKEN_STRING_LITERAL,
        TOKEN_ARITH_OP,
        TOKEN_REL_OP,
        TOKEN_ASSIGN_OP,
        TOKEN_LOGIC_OP,
        TOKEN_SPECIAL_SYMBOL,
        TOKEN_EOF   // token de cierre para indicar que no quedan más caracteres por leer
    }

    private final TipoToken tipo;
    private final String lexema;
    private final int linea;     // posición del PRIMER carácter del lexema
    private final int columna;

    public Token(TipoToken tipo, String lexema, int linea, int columna)
    {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
    }

    public TipoToken getTipo()
    {
        return this.tipo;
    }

    public String getLexema()
    {
        return this.lexema;
    }

    public int getLinea()
    {
        return this.linea;
    }

    public int getColumna()
    {
        return this.columna;
    }

    /** Formato de la traza de salida: [Línea X, Columna Y] TIPO: 'lexema' */
    @Override
    public String toString()
    {
        return "[Línea " + getLinea() + ", Columna " + getColumna() + "] " + getTipo() + ": '" + getLexema() + "'";
    }
}
