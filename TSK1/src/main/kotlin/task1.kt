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
        file.bufferedReader().use {reader ->
            n = reader.readLine().toInt()
            m = reader.readLine().toInt()

            start = reader.readLine().split(" ").map {it.toInt()}.toSet()
            finish = reader.readLine().split(" ").map {it.toInt()}.toSet()

            go = Array(n) {
                Array(m) {
                    emptySet<Int>().toMutableSet()
                }
            }

            while(true) {
                try {
                    val splitted: List<Int> = reader.readLine().split(" ").map {it.toInt()}
                    val from : Int = splitted[0]
                    val symbol : Int = splitted[1]
                    val to : Int = splitted[2]
                    go[from][symbol].add(to)
                } catch (e : Exception) {
                    break
                }
            }
        }

        var states : Set<Int> = start
        for (symbol in inputString) {
            val realSymbol : Int = symbol - '0'

            val newStates = emptySet<Int>().toMutableSet()
            states.forEach {
                newStates.addAll(go[it][realSymbol])
            }
            states = newStates
        }

        return states.intersect(finish.toSet()).isNotEmpty()
    }
}

fun main() { /.../ }