from helper_functions import inputValidation, printPath

def DFA(file, word):
    with open(file) as automata:
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
                delta[state1] = {transition:state2}
            else:
                delta[state1][transition] = state2
            line = automata.readline()

        # processing the word
        state = initialState
        path = [initialState]
        for letter in word:
            state = delta[state][letter]
            path.append(state)

        # check if the word is accepted
        if  path[len(path)-1] in finalStates:
            print(f'The word {word} is accepted!')
            print('Path: ', end = "")
            print(printPath(path))
        else:
            print("Rejected!")

word = input("Word = ")
DFA("dfa2.txt", word)
