public class ErrorHandler
{
    private int errores = 0;

    public void mostrarErrores(int lin, int col, String lex)
    {
        errores++;
        System.out.println("[Error Léxico] Línea " + lin + ", Columna " + col + ": Lexema no reconocido '" + lex + "'");
    }

    public int getErrores()
    {
        return this.errores;
    }
}
