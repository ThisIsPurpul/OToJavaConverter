import java.io.FileInputStream

//Драйвер исходного текста
const val chEOT = 0.toChar()
const val chEOL = '\n'
const val chSPACE = ' '
const val chTAB = '\t'

var ch = chEOT
var f = System.`in`
var pos = 0

fun resetText(args: Array<String>) {
    if (args.isNotEmpty()) {
        println("Файл: ${args[0]}")
        f = FileInputStream(args[0])
    } else {
        println(">")
    }
    nextCh();
}

fun closeText() {
    f.close()
}

fun nextCh() {
    val n = f.read()
    if (n == -1)
        ch = chEOT
    else {
        val c = n.toChar()
        if (c == '\n') {
            ch = chEOL
            println()
            pos = 0
        } else if (c == '\r')
            nextCh()
        else {
            ch = c
            System.out.write(ch.toInt())
            pos++
        }
    }
}