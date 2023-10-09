var used : MutableList<Boolean> = mutableListOf()

fun dfs(v: DFANode) {
    used[v.i] = true
    for (i in 0 until v.link.size) {
        if (v.link[i] != null && !used[v.link[i]!!.i])
            dfs(v.link[i]!!)
    }
}

fun MooresAlgorithm(dfa: DFA): DFA {
    used = MutableList(dfa.n) { false }
    dfs(dfa.nodes[dfa.startNode!!])
    var groups: MutableList<MutableList<Int>> = MutableList(2) { mutableListOf() }
    val groupId: MutableList<Int> = MutableList(dfa.n) { -1 }
    for (acc in dfa.accNodes) {
        if (!used[acc]) continue
        groups[1].add(acc)
        groupId[acc] = 1
        used[acc] = false
    }
    for (i in 0 until dfa.n) {
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
            for (x in 0 until dfa.m) {
                val cl = group.groupBy { v: Int -> groupId[dfa.nodes[v].link[x]!!.i] }
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
    val minDFA = DFA(groups.size, dfa.m)
    minDFA.startNode = groupId[dfa.startNode!!]
    for (acc in dfa.accNodes) {
        if (groupId[acc] != -1) {
            used[groupId[acc]] = true
        }
    }
    for (i in 0 until groups.size) {
        if (used[i]) {
            minDFA.accNodes.add(i)
        }
    }
    for (i in 0 until groups.size) {
        val v = groups[i][0]
        for (x in 0 until dfa.m) {
            minDFA.addLink(i, groupId[dfa.nodes[v].link[x]!!.i], x)
        }
    }
    return minDFA
}