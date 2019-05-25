// Таблица имен
import Types.*

enum class Types {
    UNIT, INTEGER, BOOLEAN
}

open class Item(var name: String) {
}

class Type(name: String, val type: Types) : Item(name) {
}

class Const(name: String, val value: Int) : Item(name) {
    val type = INTEGER
}

class Module(name: String) : Item(name) {
}

class StProc(name: String, val type: Types) : Item(name) {
}

class Var(name: String, val type: Types) : Item(name) {
    var lastAddr = 0
}

class NameTable {
    private var scopeStack: MutableList<MutableList<Item>> = mutableListOf()

    fun openScope() {
        scopeStack.add(mutableListOf())
    }

    fun enterItem(item: Item) {
        lastScope().add(item)
    }

    fun closeScope() {
        scopeStack.remove(lastScope())
    }

    fun find(name: String): Item? {
        for (sc in scopeStack.asReversed()) {
            val res = sc.find { name == it.name }
            if (res != null)
                return res
        }
        ctxError("Неизвестное имя")
        return null
    }

    fun newItem(item: Item) {
        if (lastScope().find { it.name == item.name } == null)
            enterItem(item)
        else {
            ctxError("Повторное объявление имени")
        }
    }

    fun lastScope() = scopeStack.last()
}

var table = NameTable()



