public class Token
{
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
        TOKEN_EOF
    }

    private final TipoToken tipo;
    private final String lexema;
    private final int linea;
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

    @Override
    public String toString()
    {
        return "[Línea " + getLinea() + ", Columna " + getColumna() + "] " + getTipo() + ": '" + getLexema() + "'";
    }
}
