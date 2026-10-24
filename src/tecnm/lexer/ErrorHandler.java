package tecnm.lexer;

/**
 * Centraliza el reporte de errores léxicos.
 * Solo informa y cuenta: nunca detiene la ejecución. La recuperación en
 * modo pánico la aplica MiniLexer,porque es quien controla la lectura del archivo.
 */
public class ErrorHandler
{
    private int errores = 0;

    /** Cuenta el error e imprime el mensaje con el formato oficial de la práctica. */
    public void mostrarErrores(int lin, int col, String lex)
    {
        errores++;
        System.out.println("[Error Léxico] Línea " + lin + ", Columna " + col + ": Lexema no reconocido '" + lex + "'");
    }

    public int getTotErrores()
    {
        return this.errores;
    }
}