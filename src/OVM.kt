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
    var stackPointer = MEM_SIZE

    loop@ while (true) {
        val cmd = M[PC++]
        if (cmd >= 0)
            M[--stackPointer] = cmd
        else {
            when (cmd) {
                cmADD -> {
                    stackPointer++
                    M[stackPointer] += M[stackPointer - 1]
                }
                cmSUB -> {
                    stackPointer++
                    M[stackPointer] -= M[stackPointer - 1]
                }
                cmMULT -> {
                    stackPointer++
                    M[stackPointer] *= M[stackPointer - 1]
                }
                cmDIV -> {
                    stackPointer++
                    M[stackPointer] /= M[stackPointer - 1]
                }
                cmMOD -> {
                    stackPointer++
                    M[stackPointer] %= M[stackPointer - 1]
                }
                cmNEG -> M[stackPointer] = -M[stackPointer]
                cmLOAD -> M[stackPointer] = M[M[stackPointer]]
                cmSAVE -> {
                    M[M[stackPointer + 1]] = M[stackPointer]
                    stackPointer += 2
                }
                cmDUP -> {
                    stackPointer--
                    M[stackPointer] = M[stackPointer + 1]
                }
                cmDROP -> stackPointer++
                cmSWAP -> {
                    val buf = M[stackPointer]
                    M[stackPointer] = M[stackPointer + 1]
                    M[stackPointer + 1] = buf
                }
                cmOVER -> {
                    stackPointer--
                    M[stackPointer] = M[stackPointer + 2]
                }
                cmGOTO -> PC = M[stackPointer++]
                cmIFEQ -> {
                    if (M[stackPointer + 2] == M[stackPointer + 1])
                        PC = M[stackPointer]
                    stackPointer += 3
                }
                cmIFNE -> {
                    if (M[stackPointer + 2] != M[stackPointer + 1])
                        PC = M[stackPointer]
                    stackPointer += 3
                }
                cmIFLE -> {
                    if (M[stackPointer + 2] <= M[stackPointer + 1])
                        PC = M[stackPointer]
                    stackPointer += 3
                }
                cmIFLT -> {
                    if (M[stackPointer + 2] < M[stackPointer + 1])
                        PC = M[stackPointer]
                    stackPointer += 3
                }
                cmIFGE -> {
                    if (M[stackPointer + 2] >= M[stackPointer + 1])
                        PC = M[stackPointer]
                    stackPointer += 3
                }
                cmIFGT -> {
                    if (M[stackPointer + 2] > M[stackPointer + 1])
                        PC = M[stackPointer]
                    stackPointer += 3
                }
                cmIN -> {
                    print('?')
                    M[--stackPointer] = readInt()
                }
                cmOUT -> {
                    val w = (M[stackPointer] - (M[stackPointer + 1]).toString().length)
                    for (i in 1..w)
                        print(" ")
                    print(M[stackPointer + 1])
                    //print("%${M[SP]}d".format(M[SP + 1]))
                    stackPointer += 2
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
    if (stackPointer < MEM_SIZE)
        println("Код возврата ${M[stackPointer]}")
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
    for (i in 0 until programCounter) {
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
