// Распознаватель
import Lex.*
import Types.*
import com.sun.org.apache.bcel.internal.generic.IFNE

fun compile() {
    nextLex()
    with(table) {
        openScope()
        enterItem(Type("INTEGER", INTEGER))
        enterItem(StProc("ABS", INTEGER))
        enterItem(StProc("HALT", UNIT))
        enterItem(StProc("MAX", INTEGER))
        enterItem(StProc("MIN", INTEGER))
        enterItem(StProc("ODD", BOOLEAN))
        enterItem(StProc("DEC", UNIT))
        enterItem(StProc("INC", UNIT))
        enterItem(StProc("In.Open", UNIT))
        enterItem(StProc("In.Int", UNIT))
        enterItem(StProc("Out.Int", UNIT))
        enterItem(StProc("Out.Ln", UNIT))
        module()
        closeScope()
        println("\nКомпиляция завершена")
    }
}

// MODULE Имя ";"
// [Импорт]
// ПослОбъявл
// [BEGIN
//   ПослОператоров]
// END Имя "."
fun module() {
    table.openScope()
    skip(MODULE)
    check(NAME)
    table.enterItem(Module(name))
    nextLex()
    skip(SEMI)
    if (lex == IMPORT) {
        import()
    }
    declSeq()
    if (lex == BEGIN) {
        nextLex()
        statSeq()
    }
    skip(END)
    check(NAME)
    var x = table.find(name)
    if (x !is Module)
        ctxError("Это не имя модуля")
    else if (x.name != name)
        ctxError("Это имя не того модуля")
    else
        nextLex()
    skip(DOT)
    Gen(cmSTOP)
    allocateVariables()
    table.closeScope()
}

//Распределение памяти для переменных
fun allocateVariables() {
    for (item in table.lastScope()) {
        if (item is Var) {
            fixup(item.lastAddr)
            M[PC++] = 0
        }
    }
}

// Оператор {";"  Оператор }
fun statSeq() {
    statement()
    while (lex == SEMI) {
        nextLex()
        statement()
    }

}

//   [ Переменная ":=" Выраж
//   |[Имя "."] Имя ["(" [Выраж {"," Выраж}] ")"]
//   |IF Выраж THEN
//      ПослОператоров
//   {ELSIF Выраж THEN
//      ПослОператоров}
//   [ELSE
//      ПослОператоров]
//    END
//   |WHILE Выраж DO
//      ПослОператоров
//    END
// ]
fun statement() {
    if (lex == NAME) {
        var x = table.find(name)
        if (x is Var) {
            assStatement()
        } else if (x is StProc && x.type == UNIT || x is Module) {
            callStatement()
        } else
            expect("имя переменной или процедуры")
    } else if (lex == IF) {
        ifStatement()
    } else if (lex == WHILE) {
        whileStatement()
    } else if (lex == CASE) {
        caseStatement()
    } // Пустой оператор
}

fun callStatement() {
    check(NAME)
    var x = table.find(name)
    var procName = ""
    if (x is Module) {
        nextLex()
        procName = name
        skip(DOT)
        procName = procName + "."
        check(NAME)
        procName = procName + name
        nextLex()
    } else if (x is StProc && x.type == UNIT) {
        procName = name
        nextLex()
    } else
        expect("обозначение процедуры")
    x = table.find(procName)
    stProc((x as StProc).name) // Здесь уточнение
}

fun stProc(pname: String) {
    when (pname) {
        "Out.Ln" -> {
            if (lex == L_PAR) {
                nextLex()
                skip(R_PAR)

            }
            Gen(cmOUTLN)
        }
        "In.Open" -> {
            if (lex == L_PAR) {
                nextLex()
                skip(R_PAR)
            }
        }
        "Out.Int" -> {
            skip(L_PAR)
            intExpr()
            skip(COMMA)
            intExpr()
            skip(R_PAR)
            Gen(cmOUT)
        }
        "In.Int" -> {
            skip(L_PAR)
            variable()
            skip(R_PAR)
            Gen(cmIN)
            Gen(cmSAVE)
        }
        "HALT" -> {
            skip(L_PAR)
            var c = constExpr()
            skip(R_PAR)
            GenConst(c)
            Gen(cmSTOP)
        }
        "DEC" -> {
            skip(L_PAR)
            variable()
            Gen(cmDUP)
            Gen(cmLOAD)
            if (lex == COMMA) {
                nextLex();
                intExpr()
            } else {
                Gen(1)
            }
            Gen(cmSUB)
            Gen(cmSAVE)
            skip(R_PAR)
        }
        "INC" -> {
            skip(L_PAR)
            variable()
            Gen(cmDUP)
            Gen(cmLOAD)
            if (lex == COMMA) {
                nextLex();
                intExpr()
            } else {
                Gen(1)
            }
            Gen(cmADD)
            Gen(cmSAVE)
            skip(R_PAR)
        }

    }
}

fun intExpr() {
    val T = Expression()
    TestInt(T)
}

fun whileStatement() {
    val WhilePC = PC
    skip(WHILE)
    boolExpr()
    val CondPC = PC
    skip(DO)
    statSeq()
    skip(END)
    Gen(WhilePC)
    Gen(cmGOTO)
    fixup(CondPC)
}

// IF Выраж THEN
// ПослОператоров
// {ELSIF Выраж THEN
//    ПослОператоров}
// [ELSE
//    ПослОператоров]
// END
fun ifStatement() {  //todo: IF here
    var LastGOTO = 0
    skip(IF)
    boolExpr()
    var CondPC = PC
    skip(THEN)
    statSeq()
    while (lex == ELSIF) {
        Gen(LastGOTO)
        Gen(cmGOTO)
        LastGOTO = PC
        nextLex()
        fixup(CondPC)
        boolExpr()
        CondPC = PC
        skip(THEN)
        statSeq()
    }
    if (lex == ELSE) {
        Gen(LastGOTO)
        Gen(cmGOTO)
        LastGOTO = PC
        nextLex()
        fixup(CondPC)
        statSeq()
    } else
        fixup(CondPC)

    skip(END)

    fixup(LastGOTO)

}

//-> сохранить вычисления выражения в стек
//-> сравнивать с ним каждую из меток варианта
//-> проверку типа вариантов также необходимо произвести (скорее еще при компиляции)
//-> метки могут быть представлены диапазонами (н-р.   1..5)
//-> пересечения диапазонов недопустимы т.е. каждая метка встречается единожды, иначе - ошибка
//-> встретив нужную нам метку, мы должны позаботиться о ее удалении из стека
//-> при сравнении же с выражением мы должЫны дублировать ее во избежание потери данной
//-> если значение метки совпало с ожидаемым, то выполнять последовательность операторов
//-> выход из кейса
//TODO: caseStatement
fun caseStatement() {
    skip(CASE)
    var LastGOTO = 0   //предыдущего перехода нет
    intExpr()
    Gen(cmDUP)
    var CondPC = PC    //Запомн. положение усл. перехода
    check(OF)
    do {
        nextLex()
        intExpr()
        if (lex == COMMA) {
            nextLex()

            skip(COLON)
        } else if (lex == DOT) {
            nextLex()
            skip(DOT)

            skip(COLON)
        } else {
            skip(COLON)
            Gen(LastGOTO)
            Gen(cmGOTO)
            LastGOTO = PC
            Gen(cmIFNE)
            Gen(cmDROP)
            statSeq()
//            fixup(CondPC)
            Gen(cmGOTO)
        }
    }while (lex == V_BAR)


/*    Gen(LastGOTO)
    Gen(cmGOTO)
    LastGOTO = PC
    intExpr()
    CondPC = PC
    skip(COLON)
    statSeq()
    do {
        Gen(LastGOTO)
        Gen(cmGOTO)
        LastGOTO = PC
        nextLex()
        fixup(CondPC)
        intExpr()
        CondPC = PC
        skip(COLON)
        statSeq()
    } while (lex == V_BAR)

    if (lex == ELSE) {
        Gen(LastGOTO)
        Gen(cmGOTO)
        LastGOTO = PC
        nextLex()
        fixup(CondPC)
        statSeq()
    } else
        fixup(CondPC)
    skip(END)
    fixup(LastGOTO)*/
}

fun comparisonWithMark() {
    intExpr()
}


// ПростоеВыраж [("=" | "#" | "<" | "<=" | ">" | ">=") ПростоеВыраж]
fun Expression(): Types {
    var T = SimpleExpr()
    if (lex in setOf(EQ, NE, GT, GE, LT, LE)) {
        val Op = lex
        TestInt(T)
        nextLex()
        T = SimpleExpr()
        TestInt(T)
        T = BOOLEAN
        GenComp(Op)
    }
    return T
}

// ["+"|"-"] Слагаемое {("+" | "-") Слагаемое}
fun SimpleExpr(): Types {
    var T: Types
    if (lex in setOf(PLUS, MINUS)) {
        var Op = lex
        nextLex()
        T = Term()
        TestInt(T)
        if (Op == MINUS)
            Gen(cmNEG)
    } else
        T = Term()
    while (lex in setOf(PLUS, MINUS)) {
        val Op = lex
        TestInt(T)
        nextLex()
        T = Term()
        TestInt(T)
        when (Op) {
            PLUS -> Gen(cmADD)
            MINUS -> Gen(cmSUB)
        }
    }
    return T
}

fun TestInt(T: Types) {
    if (T != INTEGER)
        expect("выражение целого типа слева")
}

// Множитель {("*" | DIV | MOD)} Множитель}
fun Term(): Types {
    var T = Factor()
    while (lex in setOf(MULT, DIV, MOD)) {
        var Op = lex
        TestInt(T)
        nextLex()
        T = Factor()
        TestInt(T)
        when (Op) {
            MULT -> Gen(cmMULT)
            DIV -> Gen(cmDIV)
            MOD -> Gen(cmMOD)
        }
    }
    return T
}

//    Имя ["("Выраж")"]
//   | Число
//   | "(" Выраж ")"
fun Factor(): Types {
    var T: Types = UNIT
    if (lex == NAME) {
        var x = table.find(name)
        if (x is Const) {
            GenConst(x.value)
            T = x.type
            nextLex();
        } else if (x is Var) {
            GenAddr(x)
            Gen(cmLOAD)
            T = x.type
            nextLex()
        } else if (x is StProc && x.type != UNIT) {
            nextLex()
            skip(L_PAR)
            stFunc(x.name)
            skip(R_PAR)
            T = x.type
        } else
            expect("имя константы переменной или процедуры-функции")
    } else if (lex == NUM) {
        GenConst(num)
        nextLex()
        T = INTEGER
    } else if (lex == L_PAR) {
        nextLex()
        T = Expression()
        skip(R_PAR)
    } else {
        expect("имя, число или выражение в скобках")
    }
    return T
}

fun stFunc(name: String) {
    when (name) {
        "ABS" -> {
            intExpr()
            Gen(cmDUP); // x, x
            Gen(0)
            Gen(PC + 3)
            Gen(cmIFGE)
            Gen(cmNEG)
        }
        "MAX" -> {
            var T = type()
            TestInt(T)
            Gen(Int.MAX_VALUE)
        }
        "MIN" -> {
            var T = type()
            TestInt(T)
            Gen(Int.MAX_VALUE)
            Gen(cmNEG)
            Gen(1)
            Gen(cmSUB)
        }
        "ODD" -> {
            intExpr()
            Gen(2)
            Gen(cmMOD)
            Gen(1)
            Gen(0) // Фиктивный (резервный) адрес перехода вперед
            Gen(cmIFNE)
        }
    }
}

fun boolExpr() {
    var T = Expression()
    if (T != BOOLEAN)
        expect("логическое выражение")
}

fun assStatement() {
    variable()
    skip(ASSIGN)
    val T = Expression()
    TestInt(T)
    Gen(cmSAVE)
}

fun variable() {
    check(NAME)
    var x = table.find(name)
    if (x !is Var)
        expect("переменная")
    else
        GenAddr(x)
    nextLex()
}

// {CONST {ОбъявлКонст ";"}
// |VAR {ОбъявлПерем ";"} }.
fun declSeq() {
    while (lex in setOf(CONST, VAR)) {
        if (lex == CONST) {
            nextLex()
            while (lex == NAME) {
                constDecl()
                skip(SEMI)
            }
        } else {
            nextLex()
            while (lex == NAME) {
                varDecl()
                skip(SEMI)
            }
        }
    }
}

// Имя {"," Имя} ":" Тип
fun varDecl() {
    check(NAME)
    table.newItem(Var(name, Types.INTEGER))
    nextLex()
    while (lex == COMMA) {
        nextLex()
        check(NAME)
        table.newItem(Var(name, Types.INTEGER))
        nextLex()
    }
    skip(COLON)
    type()
}

fun type(): Types {
    check(NAME)
    var x = table.find(name)
    if (x !is Type)
        expect("имя типа")
    nextLex()
    return (x as Type).type
}

// Имя "=" КонстВыраж. //todo: const declaration
fun constDecl() {
    check(NAME)
    val cname = name
    nextLex()
    skip(EQ)
    val c = constExpr()
    table.newItem(Const(cname, c))
}

// ["+" | "-"] (Число | Имя)
fun constExpr(): Int {
    var c: Int = 1
    if (lex == PLUS)
        nextLex()
    else if (lex == MINUS) {
        c = -1
        nextLex()
    }
    if (lex == NUM) {
        c = c * num
        nextLex()
    } else if (lex == NAME) {
        val x = table.find(name)
        if (x is Const) {
            c = c * x.value
        } else
            expect("имя констаты")
        nextLex()
    } else {
        expect("число или имя")
    }
    return c
}

fun importName() {
    check(NAME)
    if (name == "In" || name == "Out")
        table.newItem(Module(name))
    else
        ctxError("Неизвестный модуль")
    nextLex()
}

// IMPORT Имя {"," Имя} ";".
fun import() {
    skip(IMPORT)
    importName()
    while (lex == COMMA) {
        nextLex()
        importName()
    }
    skip(SEMI)
}

fun skip(L: Lex) {
    if (lex == L)
        nextLex()
    else
        expect("$L")
}

fun check(L: Lex) {
    if (lex != L)
        expect("$L")
}
