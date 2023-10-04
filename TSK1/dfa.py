from helper_functions import inputValidation, printPath

def DFA(file, word):
    with open(file) as automata:
        nstates = int (automata.readline())
        alphabet = automata.readline().split()
        initialState = 0
        finalStates = [int(x) for x in automata.readline().split()]
        # input validation
        if not inputValidation(alphabet, word):
                print("The word can only contain ", end="")
                print(', '.join(alphabet) +  "!!!")
                return
        # lambda word
        if word == "":
            if initialState in finalStates:
                print("True")
                return
            else:
                print("False")
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
            print("False")
# чтобы попробовать свой ввод, нужно расскомментировать эти строчки

#word = input("Word = ")
#DFA("dfa1.txt", word)

def run_tests():
    test_cases = [
        ("0101", True),
        ("001", True),
        ("", True),
        ("1010101010101010", False),
        ("11111", False)
    ]

    for idx, (word, expected_output) in enumerate(test_cases, 1):
        print(f"Running Test #{idx} with input word: '{word}'")
        try:
            DFA("dfa2.txt", word)
        except Exception as e:
            print(f"Error occurred: {e}")
            continue
        print(f"Expected Output: {expected_output}")
        print("=" * 30)

    test_cases2 = [
        ("aaaa", False),
        ("bab", False),
        ("aabbb", False),
        ("ababab", False)
    ]

    for idx, (word, expected_output) in enumerate(test_cases2, 1):
        print(f"Running Test #{idx} with input word: '{word}'")
        try:
            DFA("dfa1.txt", word)
        except Exception as e:
            print(f"Error occurred: {e}")
            continue
        print(f"Expected Output: {expected_output}")
        print("=" * 30)

run_tests()
