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

    public MiniLexer(BufferedReader lector)
    {
        this.reader = lector;
    }

    private int inspeccionar(int n) throws IOException
    {
        reader.mark(n+1);
        int c = -1;
        for(int i = 0; i < n; i++)
        {
            c = reader.read();
            if(c == -1) break;
        }
        reader.reset();
        return c;
    }

    private int inspeccionar() throws IOException
    {
        return inspeccionar(1);
    }

    private int avan() throws IOException
    {
        int c = reader.read();
        if(c == '\n') { lin++; col = 1; }
        else if(c == '\t') { col += 4; }
        else if(c == '\r') { col += 1; }
        else if(c != -1) { col++; }
        return c;
    }

    private static boolean esLetra(int c)
    {
        return (c >= 'a' && c <= 'z')  || (c >= 'A' && c <= 'Z');
    }

    private static boolean esDigito(int c)
    {
        return (c >= '0' && c <= '9');
    }

    private void omitirEspaciosComentarios() throws IOException
    {
        while(true)
        {
            int c = inspeccionar();
            if(c == ' ' || c == '\t' || c == '\r' || c == '\n')
            {
                avan();
            }
            else if (c == '/' && inspeccionar(2) == '/')
            {
                int x;
                while((x = inspeccionar()) != '\n' && x != -1) avan();
            } else if (c == '/' && inspeccionar(2) == '*')
            {
                int l = lin, co = col;
                avan(); avan();
                boolean cerrado = false;
                while(inspeccionar() != -1)
                {
                    if(inspeccionar() == '*' && inspeccionar(2) == '/')
                    {
                        avan(); avan();
                        cerrado = true;
                        break;
                    }
                    avan();
                }
                if(!cerrado) errores.mostrarErrores(l, co, "/*");
            }
            else break;
        }
    }

    public Token sigToken() throws IOException
    {
        while(true)
        {
            notasDeTabla = "";
            omitirEspaciosComentarios();

            int l = lin, co = col;
            int ch = inspeccionar();
            Token t;

            //if(ch == -1) return new Token(Token.TipoToken.TOKEN_EOF, "", l, co);
            // else if (esLetra(ch))

            
        }
    }

    public static void main(String[] args)
    {
        //alexis estuvo aqui a las 4:10 pm
        //System.out.println("hola luis");

    }
}
