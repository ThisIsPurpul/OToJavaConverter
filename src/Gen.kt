import java.lang.Math.abs
import Lex.*

//Генератор кода
var PC = 0

fun Gen(cmd: Int) {
    M[PC++] = cmd
}

fun GenConst(c: Int) {
    Gen(abs(c));
    if (c < 0)
        Gen(cmNEG)
}

fun GenAddr(v: Var) {
    Gen(v.lastAddr)
    v.lastAddr = PC+1
}

fun GenComp(op: Lex) {
    Gen(0)
    when (op) {
        EQ -> Gen(cmIFNE)
        NE -> Gen(cmIFEQ)
        GE -> Gen(cmIFLT)
        GT -> Gen(cmIFLE)
        LE -> Gen(cmIFGT)
        LT -> Gen(cmIFGE)
    }
}

// Адресная привязка
fun fixup(a: Int) {
    var A = a
    while( A != 0 ){
        var temp = M[A-2]
        M[A-2] = PC
        A = temp
    }
}
