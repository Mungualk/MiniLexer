import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class MiniLexer
{
    private static final Set <String> Reserv = Set.of("if", "then", "else", "while");
    private final BufferedReader reader;
    private int lin = 1, col = 1;

    private final Map<String, Simbolo> tablitaDeSimbolos = new HashMap<>();
    private final ErrorHandler errores = new ErrorHandler();
    private String notasDeTabla = ""; // Pa saber que tipo de referencia se está haciendo

    public MiniLexer(BufferedReader reader)
    {
        this.reader = reader;
    }

    public static void main(String[] args)
    {
        //System.out.println("hola luis");

    }
}
