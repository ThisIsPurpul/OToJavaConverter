// Обработка ошибок
class OException : RuntimeException()

fun lexError(msg: String) {
    error("$msg", pos)
}

fun rtError(msg: String) {
    print("\n$msg")
}

fun error(msg: String, p: Int) {
    while (ch != chEOL && ch != chEOT) {
        nextCh()
    }
    if (ch == chEOT) {
        println()
    }
    repeat(p - 1) {
        print(' ')
    }
    println("^\n$msg")
    throw OException()
}

fun ASSERT(b: Boolean, msg: String) {
    if (!b) {
        throw AssertionError(msg)
    }
}

fun expect(msg: String) {
    error("Ожидается $msg", lexPos)
}

fun ctxError(msg: String) {
    error(msg, lexPos)
}
