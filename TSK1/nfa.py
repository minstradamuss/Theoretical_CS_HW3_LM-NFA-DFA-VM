from helper_functions import inputValidation, printPath

def NFA(file, word):
    with open(file) as automata:
        nrStates = int(automata.readline())
        alphabet = automata.readline().split()
        initialState = int(automata.readline())
        finalStates = [int(x) for x in automata.readline().split()]
        # input validation
        if not inputValidation(alphabet, word):
                print("The word can only contain ", end="")
                print(', '.join(alphabet) +  "!!!")
                return
        # lambda word
        if word == "":
            if initialState in finalStates:
                print("The word λ is accepted!")
                return
            else:
                print("The word λ is rejected!")
                return
        # delta function
        delta = {}
        line = automata.readline()
        while line:
            aux = line.split()
            state1 = int(aux[0])
            transition = aux[1]
            state2 = int(aux[2])
            if state1 not in delta:
                delta[state1] = {transition:[state2]}
            else:
                try:
                    delta[state1][transition].append(state2)
                except:
                    delta[state1][transition] = [state2]
            line = automata.readline()
        # processing the word
        sol = False
        path = [initialState]
        # i is for accessing every letter in the word
        def bkt(i,state):
            nonlocal path, sol
            letter = word[i]
            try:
                delta[state][letter]
                for nextState in delta[state][letter]:
                    state = nextState
                    path.append(nextState)
                    if i == len(word)-1:
                        if path[len(path)-1] in finalStates:
                            print(printPath(path))
                            sol = True
                            path = path[:i]
                    else:
                        # next letter
                        bkt(i+1, state)
            except KeyError:
                if i == len(word)-1:
                    if path[len(path) - 1] in finalStates and len(path) == len(word) + 1 and path[len(path)-1] not in finalStates:
                        print(printPath(path))
                        sol = True
                    else:
                        path = path[:i]
                else:
                    path = path[:i]

        bkt(0, initialState)

        if sol == False:
            print("Rejected!")

word = input("Word = ")
NFA("nfa2.txt", word)
# check for abac