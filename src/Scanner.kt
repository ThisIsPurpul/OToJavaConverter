// Сканер (лексический анализатор)
import Lex.*

enum class Lex {
    NONE, NAME, NUM, MODULE, IMPORT, CONST, VAR, BEGIN, END, IF, THEN,
    ELSE, ELSIF, WHILE, DO, ASSIGN, COMMA, DOT, COLON, SEMI,
    PLUS, MINUS, MULT, DIV, MOD, EQ, NE, GT, GE, LT, LE, L_PAR, R_PAR,
    EOT;

    override fun toString(): String {
        return when (this) {
            NONE -> "зарезервированное слово"
            NAME -> "имя"
            NUM -> "число"
            PLUS -> "\"+\""
            MINUS -> "\"-\""
            EQ -> "\"=\""
            NE -> "\"#\""
            LT -> "\"<\""
            LE -> "\"<=\""
            GT -> "\">\""
            GE -> "\">=\""
            DOT -> "\".\""
            COMMA -> "\",\""
            COLON -> "\":\""
            SEMI -> "\";\""
            ASSIGN -> "\":=\""
            L_PAR -> "\"(\""
            R_PAR -> "\")\""
            EOT -> "конец текста"
            else -> super.toString()
        }

    }
}

var lex: Lex = NONE
var name: String = ""
var num: Int = 0
var lexPos = 0

val kw = mapOf(
    "MODULE" to MODULE,
    "IMPORT" to IMPORT,
    "CONST" to CONST,
    "VAR" to VAR,
    "BEGIN" to BEGIN,
    "END" to END,
    "IF" to IF,
    "THEN" to THEN,
    "ELSE" to ELSE,
    "ELSIF" to ELSIF,
    "WHILE" to WHILE,
    "DO" to DO,
    "DIV" to DIV,
    "MOD" to MOD,
    "ARRAY" to Lex.NONE,
    "RECORD" to Lex.NONE,
    "POINTER" to Lex.NONE,
    "FOR" to Lex.NONE,
    "TO" to Lex.NONE,
    "REPEAT" to Lex.NONE,
    "BY" to Lex.NONE,
    "CASE" to Lex.NONE,
    "EXIT" to Lex.NONE,
    "LOOP" to Lex.NONE,
    "UNTIL" to Lex.NONE,
    "IN" to Lex.NONE,
    "IS" to Lex.NONE,
    "NIL" to Lex.NONE,
    "OF" to Lex.NONE,
    "OR" to Lex.NONE,
    "PROCEDURE" to Lex.NONE,
    "RETURN" to Lex.NONE,
    "TYPE" to Lex.NONE,
    "WITH" to Lex.NONE

)

fun nextLex() {
    while (ch in setOf(chSPACE, chTAB, chEOL)) {
        nextCh()
    }
    lexPos = pos
    when (ch) {
        in 'A'..'Z', in 'a'..'z' -> lex = identOrKeyWord()
        in '0'..'9' -> lex = number()
        ';' -> {
            lex = SEMI
            nextCh()
        }
        '.' -> {
            lex = DOT
            nextCh()
        }
        ',' -> {
            lex = COMMA
            nextCh()
        }
        ':' -> {
            nextCh()
            if (ch == '=') {
                lex = ASSIGN
                nextCh()
            } else {
                lex = COLON
            }
        }
        '+' -> {
            lex = PLUS
            nextCh()
        }
        '-' -> {
            lex = MINUS
            nextCh()
        }
        '*' -> {
            lex = MULT
            nextCh()
        }
        '=' -> {
            lex = EQ
            nextCh()
        }
        '#' -> {
            lex = NE
            nextCh()
        }
        '>' -> {
            nextCh()
            if (ch == '=') {
                lex = GE
                nextCh()
            } else {
                lex = GT
            }
        }
        '<' -> {
            nextCh()
            if (ch == '=') {
                lex = LE
                nextCh()
            } else {
                lex = LT
            }
        }
        '(' -> {
            nextCh()
            if (ch == '*') {
                comment()
                nextLex()
            } else {
                lex = L_PAR
            }
        }
        ')' -> {
            lex = R_PAR
            nextCh()
        }
        chEOT -> {
            lex = EOT
        }
        else -> {
            lexError("Недопустимый символ")
        }

    }
    //   println("[$lex]")
}

private fun comment() {
    nextCh()
    do {
        while (ch != '*' && ch != chEOT)
            if (ch == '(') {
                nextCh()
                if (ch == '*')
                    comment()
            } else
                nextCh()
        if (ch == '*')
            nextCh()
    } while (ch != ')' && ch != chEOT)
    if (ch == ')')
        nextCh()
    else {
        lexError("Не закончен комментарий")
    }
}

private fun number(): Lex {
    ASSERT(ch in '0'..'9', "ch in '0'..'9'")
    num = 0
    do {
        val d = ch.toInt() - '0'.toInt();
        if (num <= (Int.MAX_VALUE - d) / 10) {
            num = num * 10 + d
        } else
            lexError("Слишком большое число")
        nextCh()
    } while (ch in '0'..'9')
    return NUM
}

private fun identOrKeyWord(): Lex {
    ASSERT(
        ch in 'a'..'z' || ch in 'A'..'Z',
        "ch in 'a'..'z' || ch in 'A'..'Z'"
    )
    // Использовать StringBuilder
    name = ""
    do {
        name = name + ch
        nextCh()
    } while (ch in 'A'..'Z' || ch in 'a'..'z' || ch in '0'..'9')

    return kw[name] ?: NAME //if (kw[name] != null) kw[name]!! else NAME
}

