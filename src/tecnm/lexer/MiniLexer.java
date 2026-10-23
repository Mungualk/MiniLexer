package tecnm.lexer;

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

            if(ch == -1) return new Token(Token.TipoToken.TOKEN_EOF, "", l, co);
            else if (esLetra(ch)) t = esReservado(lin, col);
            else if (esDigito(ch)) t = leerNum(lin, col);
            else if (ch == '"') t = leerCadena(lin, col);
            else t = leerSimbolo(ch, lin, col);

            if (t != null) return t;         // null = error ya reportado; seguimos
        }

    }

    private Token esReservado(int l, int c) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        while(esLetra(inspeccionar()) || esDigito(inspeccionar()))
        {
            sb.append((char)avan());
        }
        String lexema = sb.toString();
        if(Reserv.contains(lexema))
        {
            return new Token(Token.TipoToken.TOKEN_RESERVED, lexema, l, c);
        }
        if(!tablitaDeSimbolos.containsKey(lexema))
        {
            notasDeTabla = " -> [Registrado en Tabla de Símbolos]";
        }
        else
        {
            notasDeTabla = " -> [Referenciado de la Tabla de Símbolos]";
        }
        return new Token(Token.TipoToken.TOKEN_IDENTIFIER, lexema, l, c);
    }

    private Token leerNum(int l, int c) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        while (esDigito(inspeccionar()))
        {
            sb.append((char)avan());
        }
        if(inspeccionar() != '.')
        {
            return new Token(Token.TipoToken.TOKEN_INT_LITERAL, sb.toString(), l, c);
        }

        int pu = 0;
        int ch;
        while (esDigito(ch = inspeccionar()) || ch == '.')
        {
            if(ch == '.') pu++;
            sb.append((char)avan());
        }

        String lexema = sb.toString();
        boolean terminaEnPunto = lexema.endsWith(".");
        if(pu > 1 || terminaEnPunto)
        {
            errores.mostrarErrores(l,c, lexema);
            return (null);
        }
        return new Token(Token.TipoToken.TOKEN_FLOAT_LITERAL, lexema, l, c);
    }

    private Token leerCadena(int l, int c) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append((char) avan());
        int ch;
        while ((ch = inspeccionar()) != '"' && ch != '\n' && ch != '\r' && ch != -1) {
            sb.append((char) avan());
        }
        if (ch == '"') {
            sb.append((char) avan());
            return new Token(Token.TipoToken.TOKEN_STRING_LITERAL, sb.toString(), l, c);
        }
        errores.mostrarErrores(l, c, sb.toString());
        return null;
    }

    // ---- Operadores, delimitadores y caracteres ilegales ----
    private Token leerSimbolo(int ch, int l, int c) throws IOException {
        avan();                                                   // consume el primer carácter
        char a = (char) ch;

        switch (a) {
            case '+': case '-': case '*': case '/':
                return new Token(Token.TipoToken.TOKEN_ARITH_OP, String.valueOf(a), l, c);

            case '(': case ')': case '{': case '}': case ';': case ',':
                return new Token(Token.TipoToken.TOKEN_SPECIAL_SYMBOL, String.valueOf(a), l, c);

            case '!':
                return new Token(Token.TipoToken.TOKEN_LOGIC_OP, "!", l, c);

            case '=':
                if (inspeccionar() == '=') { avan(); return new Token(Token.TipoToken.TOKEN_REL_OP, "==", l, c); }
                return new Token(Token.TipoToken.TOKEN_ASSIGN_OP, "=", l, c);

            case '>': case '<':
                if (avan() == '=') { avan(); return new Token(Token.TipoToken.TOKEN_REL_OP, a + "=", l, c); }
                return new Token(Token.TipoToken.TOKEN_REL_OP, String.valueOf(a), l, c);

            case '&': case '|':
                if (avan() == a) { avan(); return new Token(Token.TipoToken.TOKEN_LOGIC_OP, "" + a + a, l, c); }
                modoPanico(l, c, String.valueOf(a));                 // '&' o '|' solitarios
                return null;

            default:                                                 // fuera del alfabeto: ~ @ # ...
                modoPanico(l, c, String.valueOf(a));
                return null;
        }
    }

    private static boolean esSincronizacion(int c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == ';';
    }

    /** Descarta caracteres hasta un delimitador de sincronización y reporta el error. */
    private void modoPanico(int l, int c, String yaConsumido) throws IOException {
        StringBuilder sb = new StringBuilder(yaConsumido);
        int ch;
        while ((ch = inspeccionar()) != -1 && !esSincronizacion(ch)) {
            sb.append((char) avan());
        }
        errores.mostrarErrores(l, c, sb.toString());
        // El delimitador NO se consume: ';' es un token válido y el espacio lo salta S0.
    }

    public String getNotaTabla()                  { return notasDeTabla; }
    public Map<String, Simbolo> getTablaSimbolos(){ return tablitaDeSimbolos; }
    public ErrorHandler getErrores()              { return errores; }

    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Uso: java -jar MiniLexer.jar <archivo.mc>");
            return;
        }
        File archivo = new File(args[0]);

        try (BufferedReader br = new BufferedReader(
                new FileReader(archivo, StandardCharsets.UTF_8))) {

            MiniLexer lexer = new MiniLexer(br);
            System.out.println(">>> INICIANDO ANÁLISIS LÉXICO: " + archivo.getName() + " <<<");

            Token t;
            while ((t = lexer.sigToken()).getTipo() != Token.TipoToken.TOKEN_EOF)
            {
                System.out.println(t + lexer.getNotaTabla());
            }

            System.out.println(">>> ANÁLISIS LÉXICO FINALIZADO CON " + lexer.getErrores().getTotErrores() + " ERROR(ES) <<<");

        }
        catch

        (IOException e)
        {
            System.err.println("No se pudo leer el archivo: " + e.getMessage());
        }
    }
}
