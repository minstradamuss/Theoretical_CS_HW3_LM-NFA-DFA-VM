import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.io.File

var used: MutableList<Boolean> = mutableListOf()
fun dfs(v: DFANode) {
    used[v.id] = true
    for (i in v.transitions.indices) {
        if (v.transitions[i] != null && !used[v.transitions[i]!!.id]) {
            dfs(v.transitions[i]!!)
        }
    }
}
class DFA(val statesCount: Int, val alphabetSize: Int) {
    val nodes = Array(statesCount) { i -> DFANode(i, alphabetSize) }
    var startState: Int? = null
    var acceptingStates: MutableList<Int> = mutableListOf()

    fun addTransition(from: Int, to: Int, symbol: Int) {
        nodes[from].addTransition(nodes[to], symbol)
    }
}

class DFANode(val id: Int, alphabetSize: Int) {
    val transitions: Array<DFANode?> = Array(alphabetSize) { _ -> null }

    fun addTransition(to: DFANode, symbol: Int) {
        transitions[symbol] = to
    }
}

fun readDFA(fileName: String): DFA {
    val text = File(fileName).readLines()
    val dfa = DFA(text[0].toInt(), text[1].toInt())
    dfa.startState = text[2].split(" ").map { it.toInt() }[0]
    dfa.acceptingStates = text[3].split(" ").map { it.toInt() }.toMutableList()
    for (i in 4 until text.size) {
        val row = text[i].split(" ").map { it.toInt() }
        dfa.addTransition(row[0], row[2], row[1])
    }
    return dfa
}

fun writeDFA(fileName: String, dfa: DFA) {
    val fileContent = buildString {
        appendln(dfa.statesCount)
        appendln(dfa.alphabetSize)
        appendln(dfa.startState)
        appendln(dfa.acceptingStates.joinToString(" "))
        dfa.nodes.forEach { node ->
            node.transitions.forEachIndexed { i, transition ->
                if (transition != null) {
                    appendln("${node.id} $i ${transition.id}")
                }
            }
        }
    }
    File(fileName).writeText(fileContent)
}

fun MooresAlgorithm(dfa: DFA): DFA {
    used = MutableList(dfa.statesCount) { false }
    dfs(dfa.nodes[dfa.startState!!])
    var groups: MutableList<MutableList<Int>> = MutableList(2) { mutableListOf() }
    val groupId: MutableList<Int> = MutableList(dfa.statesCount) { -1 }

    for (acc in dfa.acceptingStates) {
        if (!used[acc]) continue
        groups[1].add(acc)
        groupId[acc] = 1
        used[acc] = false
    }

    for (i in 0 until dfa.statesCount) {
        if (!used[i]) continue
        groups[0].add(i)
        groupId[i] = 0
        used[i] = false
    }

    var go = true
    while (go) {
        val newGroups: MutableList<MutableList<Int>> = mutableListOf()
        go = false
        for (group in groups) {
            var split = false
            for (x in 0 until dfa.alphabetSize) {
                val cl = group.groupBy { v: Int -> groupId[dfa.nodes[v].transitions[x]!!.id] }
                if (cl.size > 1) {
                    cl.forEach { entry ->
                        for (v in entry.value) {
                            groupId[v] = newGroups.size
                        }
                        newGroups.add(entry.value.toMutableList())
                    }
                    split = true
                    go = true
                    break
                }
            }
            if (!split) {
                for (v in group) {
                    groupId[v] = newGroups.size
                }
                newGroups.add(group)
            }
        }
        groups = newGroups
    }

    val minDFA = DFA(groups.size, dfa.alphabetSize)
    minDFA.startState = groupId[dfa.startState!!]
    for (acc in dfa.acceptingStates) {
        if (groupId[acc] != -1) {
            used[groupId[acc]] = true
        }
    }
    for (i in 0 until groups.size) {
        if (used[i]) {
            minDFA.acceptingStates.add(i)
        }
    }
    for (i in 0 until groups.size) {
        val v = groups[i][0]
        for (x in 0 until dfa.alphabetSize) {
            minDFA.addTransition(i, groupId[dfa.nodes[v].transitions[x]!!.id], x)
        }
    }
    return minDFA
}

class DFATest {

    @Test
    fun test1() {
        val dfaFilePath1 = "src/test/kotlin/test1.txt"
        val minDfaFilePath2 = "src/test/kotlin/output1.txt"
        val expectedDfaFilePath2 = "src/test/kotlin/expected_output1.txt"

        val dfa = readDFA(dfaFilePath1)
        val minDFA = MooresAlgorithm(dfa)
        writeDFA(minDfaFilePath2, minDFA)

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
        writeDFA(minDfaFilePath2, minDFA)

        val expectedLines = File(expectedDfaFilePath2).readLines().map { it.trimEnd() }
        val actualLines = File(minDfaFilePath2).readLines().map { it.trimEnd() }
        for (i in expectedLines.indices) {
            assertEquals(expectedLines[i], actualLines[i])
        }
    }
}
fun main() {}
