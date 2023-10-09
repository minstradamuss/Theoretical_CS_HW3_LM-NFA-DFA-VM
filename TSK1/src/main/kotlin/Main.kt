import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.io.File

class DFA(val n: Int, val m: Int) {
    val nodes = Array(n) { i -> DFANode(i, m) }
    var startNode: Int? = null
    var accNodes: MutableList<Int> = mutableListOf()

    fun addLink(from: Int, to: Int, x: Int) {
        nodes[from].addLink(nodes[to], x)
    }

}

class DFANode(val i: Int, alphabetSize: Int) {
    val link: Array<DFANode?> = Array(alphabetSize) {_ -> null}
    fun addLink(to: DFANode, x: Int) {
        link[x] = to
    }
}


fun readDFA(fileName: String): DFA {
        val text = File(fileName).readLines()

        val dfa = DFA(text[0].toInt(), text[1].toInt())

        dfa.startNode = text[2].split(" ").map { it.toInt() }[0]
        dfa.accNodes = text[3].split(" ").map { it.toInt() }.toMutableList()

        for (i in 4..text.size-1) {
            val row = text[i].split(" ").map { it.toInt() }
            dfa.addLink(row[0], row[2], row[1])
        }

        return dfa
    }

fun writeDFA(fileName: String, dfa: DFA) {
        File(fileName).appendText("${dfa.n}\n")
        File(fileName).appendText("${dfa.m}\n")
        File(fileName).appendText("${dfa.startNode}\n")
        for (accNode in dfa.accNodes) {
            File(fileName).appendText("$accNode ")
        }
        File(fileName).appendText("\n")
        for (node in dfa.nodes) {
            for (i in 0 until node.link.size) {
                if (node.link[i] != null)
                    File(fileName).appendText("${node.i} $i ${node.link[i]?.i}\n")
            }
        }
    }


class DFATest {

    @Test
    fun test1() {
        val dfaFilePath1 = "src/test/kotlin/test1.txt"
        val minDfaFilePath2 = "src/test/kotlin/output1.txt"
        val expectedDfaFilePath2 = "src/test/kotlin/expected_output1.txt"

        val dfa = readDFA(dfaFilePath1)
        val minDFA = MooresAlgorithm(dfa)
        writeDFA  (minDfaFilePath2, minDFA)

        val expectedLines = File(expectedDfaFilePath2).readLines().map { it.trimEnd() }
        val actualLines = File(minDfaFilePath2).readLines().map { it.trimEnd() }
        for (i in expectedLines.indices) {
            assertEquals(expectedLines[i], actualLines[i])
        }
    }

    @Test
    fun test2() {
        val dfaFilePath1 = "src/test/kotlin/test2.txt"
        val minDfaFilePath2 = "src/test/kotlin/output2.txt"
        val expectedDfaFilePath2 = "src/test/kotlin/expected_output2.txt"

        val dfa = readDFA(dfaFilePath1)
        val minDFA = MooresAlgorithm(dfa)
        writeDFA  (minDfaFilePath2, minDFA)

        val expectedLines = File(expectedDfaFilePath2).readLines().map { it.trimEnd() }
        val actualLines = File(minDfaFilePath2).readLines().map { it.trimEnd() }
        for (i in expectedLines.indices) {
            assertEquals(expectedLines[i], actualLines[i])
        }
    }
}
fun main() {  }