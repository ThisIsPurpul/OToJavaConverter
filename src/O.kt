import java.io.FileNotFoundException

fun main(args: Array<String>) {
    println("Компилятор языка \"O\"")

    try {
        resetText(args)
        compile()
        showCode()
        closeText()
        run()
    } catch (e: FileNotFoundException) {
        println("Файл не найден")
    } catch (e: AssertionError) {
        println("\nASSERT: ${e.localizedMessage}")
    } catch (e: OException) {
        // Уже обработанная ошибка
    } catch (e: Exception) {
        println("Необработанное исключение")
    }
}

