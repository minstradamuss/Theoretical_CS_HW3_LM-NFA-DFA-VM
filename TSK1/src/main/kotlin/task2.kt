import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

import java.io.PrintWriter

data class Quadruple(val numStates: Int, val numSymbols: Int, val startState: Int, val numFinalStates: Int, val transitions: Array<Array<MutableSet<Int>>>)

class NfaToDfaConverter {

    fun convertNfaToDfa(nfaFilePath: String, dfaFilePath: String) {
        val (numStates, numSymbols, startState, numFinalStates, transitions) = readNfaFromFile(nfaFilePath)
        val dfaTransitions = mutableMapOf<Set<Int>, MutableMap<Int, Set<Int>>>()
        val visited = mutableSetOf<Set<Int>>()
        val queue = mutableListOf<Set<Int>>()

        val initialStateSet = epsilonClosure(setOf(startState), transitions)
        queue.add(initialStateSet)
        visited.add(initialStateSet)

        while (queue.isNotEmpty()) {
            val currentStateSet = queue.removeAt(0)
            dfaTransitions[currentStateSet] = mutableMapOf()

            for (symbol in 0 until numSymbols) {
                val nextStateSet = epsilonClosure(move(currentStateSet, symbol, transitions), transitions)
                dfaTransitions[currentStateSet]?.set(symbol, nextStateSet)

                if (nextStateSet !in visited) {
                    queue.add(nextStateSet)
                    visited.add(nextStateSet)
                }
            }
        }

        writeDfaToFile(dfaFilePath, dfaTransitions, initialStateSet, numFinalStates)
    }

    private fun readNfaFromFile(filePath: String): Quadruple {
        val lines = File(filePath).readLines()
        val numStates = lines[0].trim().toInt()
        val numSymbols = lines[1].trim().toInt()
        val startState = lines[2].trim().toInt()
        val numFinalStates = lines[3].trim().toInt()

        val transitions = Array(numStates) { Array(numSymbols) { mutableSetOf<Int>() } }

        var lineIndex = 4
        for (i in 0 until numStates) {
            for (j in 0 until numSymbols) {
                val transitionStates = lines[lineIndex].trim().split(" ").map { it.toInt() }
                transitions[i][j].addAll(transitionStates)
                lineIndex++
            }
        }

        return Quadruple(numStates, numSymbols, startState, numFinalStates, transitions)
    }



    private fun epsilonClosure(states: Set<Int>, transitions: Array<Array<MutableSet<Int>>>): Set<Int> {
        val epsilonClosureSet = mutableSetOf<Int>()
        val stack = states.toMutableList()

        while (stack.isNotEmpty()) {
            val currentState = stack.removeAt(stack.size - 1)
            epsilonClosureSet.add(currentState)
            epsilonClosureSet.addAll(transitions[currentState][EPSILON])

            // Добавьте все следующие состояния в стек для обработки
            stack.addAll(transitions[currentState][EPSILON].filter { it !in epsilonClosureSet })
        }

        return epsilonClosureSet
    }


    private fun move(states: Set<Int>, symbol: Int, transitions: Array<Array<MutableSet<Int>>>): Set<Int> {
        val moveSet = mutableSetOf<Int>()
        states.forEach { state ->
            moveSet.addAll(transitions[state][symbol])
            moveSet.addAll(transitions[state][EPSILON])
        }
        return moveSet
    }



    private fun writeDfaToFile(filePath: String, transitions: Map<Set<Int>, Map<Int, Set<Int>>>, startState: Set<Int>, numFinalStates: Int) {
        val writer = PrintWriter(File(filePath))

        val numStates = transitions.size
        val numSymbols = transitions.values.first().values.first().size

        // Выводим первые 4 числа в столбик
        writer.println(numStates)
        writer.println(numSymbols)
        writer.println(startState.first())
        writer.println(numFinalStates)

        for ((currentStateSet, symbolTransitions) in transitions) {
            for (symbol in symbolTransitions.keys) {
                val nextStateSet = symbolTransitions[symbol]
                writer.print("${currentStateSet.joinToString(" ")} $symbol ${nextStateSet?.joinToString(" ")}\n")
            }
        }

        writer.close()
    }



    companion object {
        const val EPSILON = 0
    }
}

class NfaToDfaConverterTest {

    @Test
    fun testNfaToDfaConversion1() {
        // Arrange
        val nfaFilePath = "src/test/kotlin/test1.txt"
        val dfaFilePath = "src/test/kotlin/output1.txt"
        val expectedDfaFilePath = "src/test/kotlin/expected_output1.txt"

        // Act
        val converter = NfaToDfaConverter()
        converter.convertNfaToDfa(nfaFilePath, dfaFilePath)

        // Assert
        val expectedLines = File(expectedDfaFilePath).readLines()
        val actualLines = File(dfaFilePath).readLines()

        assertEquals(expectedLines.size, actualLines.size)

        for (i in expectedLines.indices) {
            assertEquals(expectedLines[i], actualLines[i])
        }
    }

    @Test
    fun testNfaToDfaConversion2() {
        val nfaFilePath2 = "src/test/kotlin/test2.txt"
        val dfaFilePath2 = "src/test/kotlin/output2.txt"
        val expectedDfaFilePath2 = "src/test/kotlin/expected_output2.txt"

        // Выполняем конвертацию
        val converter2 = NfaToDfaConverter()
        converter2.convertNfaToDfa(nfaFilePath2, dfaFilePath2)

        // Проверяем, что результат соответствует ожиданиям
        val expectedLines2 = File(expectedDfaFilePath2).readLines()
        val actualLines2 = File(dfaFilePath2).readLines()

        assertEquals(expectedLines2.size, actualLines2.size)
        for (i in expectedLines2.indices) {
            assertEquals(expectedLines2[i], actualLines2[i])
        }
    }

}