import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.io.File

class DFA(val statesCount: Int, val alphabetSize: Int) {
    val nodes = Array(statesCount) { DFANode(it, alphabetSize) }
    var startState: Int? = null
    var acceptingStates = mutableListOf<Int>()

    fun addTransition(from: Int, to: Int, inputSymbol: Int) {
        nodes[from].addTransition(nodes[to], inputSymbol)
    }
}

class DFANode(val id: Int, private val alphabetSize: Int) {
    val transitions = Array<DFANode?>(alphabetSize) { null }

    fun addTransition(to: DFANode, inputSymbol: Int) {
        transitions[inputSymbol] = to
    }
}

class NFA(val statesCount: Int, val alphabetSize: Int) {
    val nodes = Array(statesCount) { NFANode(it, alphabetSize) }
    var startStates = emptyList<Int>()
    var acceptingStates = mutableListOf<Int>()

    fun addTransition(from: Int, to: Int, inputSymbol: Int) {
        nodes[from].addTransition(nodes[to], inputSymbol)
    }

    fun transformToDFA(): DFA {
        val dfa = DFA(1 shl statesCount, alphabetSize)
        for (mask in 0 until (1 shl statesCount)) {
            for (symbol in 0 until alphabetSize) {
                var toMask = 0
                for (i in 0 until statesCount) {
                    if ((mask shr i) and 1 == 1) {
                        nodes[i].getTransitions(symbol).forEach { toMask = toMask or (1 shl it.id) }
                    }
                }
                dfa.addTransition(mask, toMask, symbol)
            }
            for (i in 0 until statesCount) {
                if ((mask shr i) and 1 == 1 && i in acceptingStates) {
                    dfa.acceptingStates.add(mask)
                }
            }
        }
        dfa.startState = startStates.fold(0) { startMask, s -> startMask or (1 shl s) }
        return dfa
    }
}

class NFANode(val id: Int, private val alphabetSize: Int) {
    private val transitions = Array(alphabetSize) { mutableListOf<NFANode>() }

    fun addTransition(to: NFANode, inputSymbol: Int) {
        transitions[inputSymbol].add(to)
    }

    fun getTransitions(inputSymbol: Int) = transitions[inputSymbol]
}

fun readNFAFromFile(fileName: String): NFA {
    val text = File(fileName).readLines()
    val nfa = NFA(text[0].toInt(), text[1].toInt())
    nfa.startStates = text[2].split(" ").mapNotNull { it.toInt() }
    nfa.acceptingStates.addAll(text[3].split(" ").mapNotNull { it.toInt() })
    for (i in 4 until text.size) {
        val row = text[i].split(" ").mapNotNull { it.toInt() }
        nfa.addTransition(row[0], row[2], row[1])
    }
    return nfa
}

fun writeDFAToFile(fileName: String, dfa: DFA) {
    val file = File(fileName)
    file.writeText("${dfa.statesCount}\n${dfa.alphabetSize}\n${dfa.startState}\n${dfa.acceptingStates.joinToString(" ")}\n")
    for (node in dfa.nodes) {
        for (i in node.transitions.indices) {
            if (node.transitions[i] != null) {
                file.appendText("${node.id} $i ${node.transitions[i]?.id}\n")
            }
        }
    }
}

class NfaToDfaConverterTest {

    @Test
    fun testNfaToDfaConversion1() {
        val nfaFilePath = "src/test/kotlin/test1.txt"
        val dfaFilePath = "src/test/kotlin/output1.txt"
        val expectedDfaFilePath = "src/test/kotlin/expected_output1.txt"

        val nfa = readNFAFromFile(nfaFilePath)
        val dfa = nfa.transformToDFA()
        writeDFAToFile(dfaFilePath, dfa)

        val expectedLines = File(expectedDfaFilePath).readLines().map { it.trimEnd() }
        val actualLines = File(dfaFilePath).readLines().map { it.trimEnd() }
        for (i in expectedLines.indices) {
            assertEquals(expectedLines[i], actualLines[i])
        }
    }

    @Test
    fun testNfaToDfaConversion2() {
        val nfaFilePath2 = "src/test/kotlin/test2.txt"
        val dfaFilePath2 = "src/test/kotlin/output2.txt"
        val expectedDfaFilePath2 = "src/test/kotlin/expected_output2.txt"

        val nfa = readNFAFromFile(nfaFilePath2)
        val dfa = nfa.transformToDFA()
        writeDFAToFile(dfaFilePath2, dfa)

        val expectedLines = File(expectedDfaFilePath2).readLines().map { it.trimEnd() }
        val actualLines = File(dfaFilePath2).readLines().map { it.trimEnd() }
        for (i in expectedLines.indices) {
            assertEquals(expectedLines[i], actualLines[i])
        }
    }
}

fun main() { }
