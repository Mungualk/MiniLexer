package tecnm.lexer;

import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Analizador léxico (escáner) del lenguaje simplificado .mc.
 *
 * Cada llamada a sigToken() devuelve un token. Cada método de lectura (esReservado, leerNum, leerCadena, leerSimbolo) es un AFD escrito como código:
 * un while que permanece en el estado de aceptación mientras el siguiente carácter pertenezca al patrón, y termina cuando ya no sirve, es nuestra implementacion del maximal munch.
 */
public class MiniLexer
{

    private static final Set <String> Reserv = Set.of("if", "then", "else", "while");
    private final BufferedReader reader;
    private int lin = 1, col = 1;   // coordenadas del PRÓXIMO carácter por leer (base 1)

    // Tabla de Símbolos: la búsqueda por hash (O(1) en promedio) permite deduplicar identificadores
    private final Map<String, Simbolo> tablitaDeSimbolos = new HashMap<>();
    private final ErrorHandler errores = new ErrorHandler();
    private String notasDeTabla = ""; // anotación que main imprime junto al token (Registrado / Referenciado)

    public MiniLexer(BufferedReader lector)
    {
        this.reader = lector;
    }

    /**
     * devuelve el n-ésimo carácter por delante SIN consumirlo (-1 si es EOF).
     * Usa mark()/reset() del BufferedReader. Se necesitan 2 caracteres de vista previa para distinguir la división de los comentarios de línea y de bloque.
     */
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

    /**
     * Consume un carácter y actualiza las coordenadas. Es el ÚNICO método que lo hace,
     * por eso los comentarios multilínea y los errores nunca desfasan línea/columna.
     */
    private int avan() throws IOException
    {
        int c = reader.read();
        if(c == '\n') { lin++; col = 1; }          // nueva línea: reinicia la columna
        else if(c == '\t') { col += 4; }           // el tabulador cuenta como 4 columnas
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

    /** Descarta el ruido (espacios y comentarios) hasta encontrar un carácter significativo. */
    private void omitirEspaciosComentarios() throws IOException
    {
        while(true)
        {
            int c = inspeccionar();
            if(c == ' ' || c == '\t' || c == '\r' || c == '\n')
            {
                avan();
            }
            // Comentario de línea: se detiene ANTES del '\n', que consume la rama de espacios
            else if (c == '/' && inspeccionar(2) == '/')
            {
                int x;
                while((x = inspeccionar()) != '\n' && x != -1) avan();
            }
            // Comentario de bloque: avan() suma línea cada vez que pasa por un '\n' interno
            else if (c == '/' && inspeccionar(2) == '*')
            {
                int l = lin, co = col;     // posición donde INICIA el comentario
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
                // Si llegó a EOF sin cerrar, el error se reporta donde empezó el comentario
                if(!cerrado) errores.mostrarErrores(l, co, "/*");
            }
            else break;
        }
    }

    /**
     * Estado inicial S0: salta el ruido, observa el primer carácter y decide qué AFD aplicar.
     * Si un AFD devuelve null significa "error ya reportado": el ciclo vuelve a S0 y sigue
     * con el resto del archivo, que es la recuperación en modo pánico.
     */
    public Token sigToken() throws IOException
    {
        while(true)
        {
            notasDeTabla = "";
            omitirEspaciosComentarios();

            int l = lin, co = col;      // coordenadas de INICIO del lexema
            int ch = inspeccionar();
            Token t;

            if(ch == -1) return new Token(Token.TipoToken.TOKEN_EOF, "", l, co);
            else if (esLetra(ch)) t = esReservado(l, co);
            else if (esDigito(ch)) t = leerNum(l, co);
            else if (ch == '"') t = leerCadena(l, co);
            else t = leerSimbolo(ch, l, co);

            if (t != null) return t;
        }

    }

    /**
     * AFD de identificadores: letra (letra | dígito)*.
     * Al terminar se decide la categoría: palabra reservada, o identificador,
     * que pasa por la Tabla de Símbolos para deduplicarse.
     */
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

        // Deduplicación: solo la primera aparición crea un Simbolo; las demás lo reutilizan
        if(!tablitaDeSimbolos.containsKey(lexema))
        {
            tablitaDeSimbolos.put(lexema, new Simbolo(lexema, Token.TipoToken.TOKEN_IDENTIFIER.toString(), l, c));
            notasDeTabla = " -> [Registrado en Tabla de Símbolos]";
        }
        else
        {
            notasDeTabla = " -> [Referenciado en Tabla de Símbolos]";
        }
        return new Token(Token.TipoToken.TOKEN_IDENTIFIER, lexema, l, c);
    }

    /**
     * AFD de números: dígito+ ( '.' ... )?
     *  - Solo dígitos: entero válido (estado de aceptación).
     *  - Con un punto seguido de dígitos: flotante válido.
     *  - Con punto final ("1.") o más de un punto ("1.5.6"): número mal formado.
     * Maximal munch: se detiene en el primer carácter ajeno al número, por eso "2x"
     * produce el entero 2 y deja la x para el siguiente token.
     */
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

        // Tras el primer punto también se aceptan puntos para consumir "1.5.6" completo
        // y reportarlo como UN solo lexema, tal como pide la práctica.
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

    /**
     * AFD de cadenas: '"' (cualquier carácter salvo '"' y salto de línea)* '"'.
     * Si se llega a un salto de línea o a EOF sin la comilla de cierre, la cadena
     * está incompleta: se reporta todo lo leído como un solo lexema. El salto de
     * línea no se consume, para que avan() lo procese y la línea siguiente conserve su numeración.
     */
    private Token leerCadena(int l, int c) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append((char) avan());       // comilla de apertura
        int ch;
        while ((ch = inspeccionar()) != '"' && ch != '\n' && ch != '\r' && ch != -1) {
            sb.append((char) avan());
        }
        if (ch == '"') {
            sb.append((char) avan());   // comilla de cierre
            return new Token(Token.TipoToken.TOKEN_STRING_LITERAL, sb.toString(), l, c);
        }
        errores.mostrarErrores(l, c, sb.toString());
        return null;
    }

    /**
     * Operadores, delimitadores y caracteres ilegales.
     * Para los operadores de dos caracteres (>=, <=, ==, &&, ||) se usa inspeccionar()
     * y NO avan(): el segundo carácter solo debe consumirse si realmente completa el operador.
     * Con avan() se "comería" el carácter siguiente aunque no perteneciera al operador
     * (por ejemplo la 'b' en "a>b").
     */
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
                if (inspeccionar() == '=') { avan(); return new Token(Token.TipoToken.TOKEN_REL_OP, a + "=", l, c); }
                return new Token(Token.TipoToken.TOKEN_REL_OP, String.valueOf(a), l, c);

            case '&': case '|':
                if (inspeccionar() == a) { avan(); return new Token(Token.TipoToken.TOKEN_LOGIC_OP, "" + a + a, l, c); }
                modoPanico(l, c, String.valueOf(a));                 // '&' o '|' solitarios
                return null;

            default:                                                 // fuera del alfabeto: ~ @ # ...
                modoPanico(l, c, String.valueOf(a));
                return null;
        }
    }

    /** Caracteres de sincronización: puntos donde el modo pánico deja de descartar. */
    private static boolean esSincronizacion(int c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == ';';
    }

    /**
     * Modo pánico: descarta caracteres hasta un delimitador de sincronización y reporta
     * todo lo descartado como un solo lexema. Después regresa a S0 (vía sigToken).
     * El delimitador NO se consume: ';' es un token válido y el espacio lo salta S0.
     */
    private void modoPanico(int l, int c, String yaConsumido) throws IOException {
        StringBuilder sb = new StringBuilder(yaConsumido);
        int ch;
        while ((ch = inspeccionar()) != -1 && !esSincronizacion(ch)) {
            sb.append((char) avan());
        }
        errores.mostrarErrores(l, c, sb.toString());
    }

    public String getNotaTabla()                  { return notasDeTabla; }
    public Map<String, Simbolo> getTablaSimbolos(){ return tablitaDeSimbolos; }
    public ErrorHandler getErrores()              { return errores; }

    /** Recibe la ruta del archivo .mc / .txt e imprime la traza de tokens y el total de errores. */
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Uso: java -jar MiniLexer.jar <archivo.mc>");
            return;
        }
        File archivo = new File(args[0]);

        // try-with-resources: cierra el archivo automáticamente al terminar o fallar
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
        catch (IOException e)
        {
            System.err.println("No se pudo leer el archivo: " + e.getMessage());
        }
    }
}