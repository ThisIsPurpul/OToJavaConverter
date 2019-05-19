//Виртуальная машина
const val MEM_SIZE = 8 * 1024

var M = IntArray(MEM_SIZE)

const val cmSTOP = -1
const val cmADD = -2
const val cmSUB = -3
const val cmMULT = -4
const val cmDIV = -5
const val cmMOD = -6
const val cmNEG = -7
const val cmLOAD = -8
const val cmSAVE = -9
const val cmDUP = -10
const val cmDROP = -11
const val cmSWAP = -12
const val cmOVER = -13
const val cmGOTO = -14
const val cmIFEQ = -15
const val cmIFNE = -16
const val cmIFLE = -17
const val cmIFLT = -18
const val cmIFGE = -19
const val cmIFGT = -20
const val cmIN = -21
const val cmOUT = -22
const val cmOUTLN = -23

fun run() {
    var PC = 0
    var SP = MEM_SIZE

    loop@ while (true) {
        val cmd = M[PC++]
        if (cmd >= 0)
            M[--SP] = cmd
        else {
            when (cmd) {
                cmADD -> {
                    SP++
                    M[SP] += M[SP - 1]
                }
                cmSUB -> {
                    SP++
                    M[SP] -= M[SP - 1]
                }
                cmMULT -> {
                    SP++
                    M[SP] *= M[SP - 1]
                }
                cmDIV -> {
                    SP++
                    M[SP] /= M[SP - 1]
                }
                cmMOD -> {
                    SP++
                    M[SP] %= M[SP - 1]
                }
                cmNEG -> M[SP] = -M[SP]
                cmLOAD -> M[SP] = M[M[SP]]
                cmSAVE -> {
                    M[M[SP + 1]] = M[SP]
                    SP += 2
                }
                cmDUP -> {
                    SP--
                    M[SP] = M[SP + 1]
                }
                cmDROP -> SP++
                cmSWAP -> {
                    val buf = M[SP]
                    M[SP] = M[SP + 1]
                    M[SP + 1] = buf
                }
                cmOVER -> {
                    SP--
                    M[SP] = M[SP + 2]
                }
                cmGOTO -> PC = M[SP++]
                cmIFEQ -> {
                    if (M[SP + 2] == M[SP + 1])
                        PC = M[SP]
                    SP += 3
                }
                cmIFNE -> {
                    if (M[SP + 2] != M[SP + 1])
                        PC = M[SP]
                    SP += 3
                }
                cmIFLE -> {
                    if (M[SP + 2] <= M[SP + 1])
                        PC = M[SP]
                    SP += 3
                }
                cmIFLT -> {
                    if (M[SP + 2] < M[SP + 1])
                        PC = M[SP]
                    SP += 3
                }
                cmIFGE -> {
                    if (M[SP + 2] >= M[SP + 1])
                        PC = M[SP]
                    SP += 3
                }
                cmIFGT -> {
                    if (M[SP + 2] > M[SP + 1])
                        PC = M[SP]
                    SP += 3
                }
                cmIN -> {
                    print('?')
                    M[--SP] = readInt()
                }
                cmOUT -> {
                    val w = (M[SP] - (M[SP + 1]).toString().length)
                    for (i in 1..w)
                        print(" ")
                    print(M[SP + 1])
                    //print("%${M[SP]}d".format(M[SP + 1]))
                    SP += 2
                }
                cmOUTLN -> println()
                cmSTOP -> break@loop
                else -> {
                    rtError("OVM: Недопустимый код операции")
                }
            }
        }
    }
    println()
    if (SP < MEM_SIZE)
        println("Код возврата ${M[SP]}")
}

fun readInt(): Int {
    try {
        return readLine()?.toInt() ?: 0
    } catch (e: Exception) {
        rtError("Неправильный формат числа")
    }
    return 0
}

fun showCode() {
    for (i in 0 until PC) {
        print("$i) ")
        println(
            when (M[i]) {
                cmSTOP -> "STOP"
                cmADD -> "ADD"
                cmSUB -> "SUB"
                cmMULT -> "MULT"
                cmDIV -> "DIV"
                cmMOD -> "MOD"
                cmNEG -> "NEG"
                cmLOAD -> "LOAD"
                cmSAVE -> "SAVE"
                cmDUP -> "DUP"
                cmDROP -> "DROP"
                cmSWAP -> "SWAP"
                cmOVER -> "OVER"
                cmGOTO -> "GOTO"
                cmIFEQ -> "IFEQ"
                cmIFNE -> "IFNE"
                cmIFLE -> "IFLE"
                cmIFLT -> "IFLT"
                cmIFGE -> "IFGE"
                cmIFGT -> "IFGT"
                cmIN -> "IN"
                cmOUT -> "OUT"
                cmOUTLN -> "OUTLN"
                else -> M[i]
            }
        )
    }
}
