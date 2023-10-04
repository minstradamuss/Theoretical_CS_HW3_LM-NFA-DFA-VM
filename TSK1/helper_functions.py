def inputValidation(alphabet, inputWord):
    for letter in inputWord:
        if letter not in alphabet:
            return False
    return True

def printPath(pathArr):
    fpath = ""
    for state in pathArr:
        fpath += str(state) + "->"
    return fpath[:-2]
