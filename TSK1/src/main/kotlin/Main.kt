import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.io.File

class DFA(val n: Int, val m: Int) {
    val nodes = Array(n) { DFANode(it, m) }
    var startNode: Int? = null
    var accNodes = mutableListOf<Int>()
    fun addLink(from: Int, to: Int, x: Int) {
        nodes[from].addLink(nodes[to], x)
    }
}

class DFANode(val i: Int, private val alphabetSize: Int) {
    val link = Array<DFANode?>(alphabetSize) { null }
    fun addLink(to: DFANode, x: Int) {
        link[x] = to
    }
}

class NFA(val n: Int, val m: Int) {
    val nodes = Array(n) { NFANode(it, m) }
    var startNodes = emptyList<Int>()
    var accNodes = mutableListOf<Int>()
    fun addLink(from: Int, to: Int, x: Int) {
        nodes[from].addLink(nodes[to], x)
    }

    fun transformToDFA(): DFA {
        val dfa = DFA(1 shl n, m)
        for (mask in 0 until (1 shl n)) {
            for (c in 0 until m) {
                var toMask = 0
                for (i in 0 until n) {
                    if ((mask shr i) and 1 == 1) {
                        nodes[i].getNexts(c).forEach { toMask = toMask or (1 shl it.i) }
                    }
                }
                dfa.addLink(mask, toMask, c)
            }
            for (i in 0 until n) {
                if ((mask shr i) and 1 == 1 && i in accNodes) {
                    dfa.accNodes.add(mask)
                }
            }
        }
        dfa.startNode = startNodes.fold(0) { startMask, s -> startMask or (1 shl s) }
        return dfa
    }
}

class NFANode(val i: Int, private val alphabetSize: Int) {
    private val link = Array(alphabetSize) { mutableListOf<NFANode>() }
    fun addLink(to: NFANode, x: Int) {
        link[x].add(to)
    }
    fun getNexts(x: Int) = link[x]
}

fun readNFA(fileName: String): NFA {
    val text = File(fileName).readLines()
    val nfa = NFA(text[0].toInt(), text[1].toInt())
    nfa.startNodes = text[2].split(" ").mapNotNull { it.toInt() }
    nfa.accNodes.addAll(text[3].split(" ").mapNotNull { it.toInt() })
    for (i in 4 until text.size) {
        val row = text[i].split(" ").mapNotNull { it.toInt() }
        nfa.addLink(row[0], row[2], row[1])
    }
    return nfa
}

fun writeDFA(fileName: String, dfa: DFA) {
    val file = File(fileName)
    file.writeText("${dfa.n}\n${dfa.m}\n${dfa.startNode}\n${dfa.accNodes.joinToString(" ")}\n")
    for (node in dfa.nodes) {
        for (i in node.link.indices) {
            if (node.link[i] != null) {
                file.appendText("${node.i} $i ${node.link[i]?.i}\n")
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

        val nfa = readNFA(nfaFilePath)
        val dfa = nfa.transformToDFA()
        writeDFA(dfaFilePath, dfa)

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

        val nfa = readNFA(nfaFilePath2)
        val dfa = nfa.transformToDFA()
        writeDFA(dfaFilePath2, dfa)

        val expectedLines = File(expectedDfaFilePath2).readLines().map { it.trimEnd() }
        val actualLines = File(dfaFilePath2).readLines().map { it.trimEnd() }
        for (i in expectedLines.indices) {
            assertEquals(expectedLines[i], actualLines[i])
        }
    }
}

fun main() { }