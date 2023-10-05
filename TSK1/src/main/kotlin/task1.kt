import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class Task1 {
    fun testString (args : Array<String>) : Boolean {
        val path : String = args[0]
        val file = File(path)
        val inputString = args[1]
        val n : Int
        val m : Int
        val start: Set<Int>
        val finish: Set<Int>
        val go : Array<Array<MutableSet<Int>>>
        file.bufferedReader().use { reader ->
            n = reader.readLine().toInt()
            m = reader.readLine().toInt()
            start = reader.readLine().split(" ").map { it.toInt() }.toSet()
            finish = reader.readLine().split(" ").map { it.toInt() }.toSet()

            go = Array(n) {
                Array(m) {
                    mutableSetOf<Int>()
                }
            }

            while (true) {
                try {
                    val (from, symbol, to) = reader.readLine().split(" ").map { it.toInt() }
                    go[from][symbol].add(to)
                } catch (e: Exception) {
                    break
                }
            }
        }

        var states: Set<Int> = start

        inputString.forEach { symbol ->
            val realSymbol: Int = symbol - '0'
            states = states.fold(emptySet()) { acc, state ->
                acc.plus(go[state][realSymbol])
            }
        }

        return states.intersect(finish).isNotEmpty()
    }
}

class Task1Test {

    private val sample = Task1()

    private val nfaFilePath = "src/test/kotlin/test1.txt"
    private val dfaFilePath = "src/test/kotlin/test2.txt"

    @Test
    fun test_NFA_1() {
        val expected = true
        val result = sample.testString(arrayOf(nfaFilePath, "00111111100"))
        assertEquals(expected, result)
    }

    @Test
    fun test_NFA_2() {
        val expected = true
        val result = sample.testString(arrayOf(nfaFilePath, "111111111100"))
        assertEquals(expected, result)
    }

    @Test
    fun test_NFA_3() {
        val expected = false
        val result = sample.testString(arrayOf(nfaFilePath, "111111111"))
        assertEquals(expected, result)
    }

    @Test
    fun test_DFA_1() {
        val expected = true
        val result = sample.testString(arrayOf(dfaFilePath, "022220"))
        assertEquals(expected, result)
    }

    @Test
    fun test_DFA_2() {
        val expected = true
        val result = sample.testString(arrayOf(dfaFilePath, "2111111220"))
        assertEquals(expected, result)
    }

    @Test
    fun test_DFA_3() {
        val expected = false
        val result = sample.testString(arrayOf(dfaFilePath, "000101010102"))
        assertEquals(expected, result)
    }
}
