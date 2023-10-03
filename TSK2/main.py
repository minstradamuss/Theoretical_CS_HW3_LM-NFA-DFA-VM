import sys
from programm import NFA, DFA

def main():
    if sys.version_info >= (3, 0):
        filename = input('Enter the name of the NFA file: ')
    elif sys.version_info >= (2, 0):
        filename = raw_input('Enter the name of the NFA file: ')
    else:
        print("Please update python to version 2.0 or newer")
        quit()

    file = open(filename, 'r')
    lines = file.readlines()
    file.close()

    nfa = NFA()
    dfa = DFA()

    nfa.construct_nfa_from_file(lines)
    dfa.convert_from_nfa(nfa)

    output_filename = input('Enter the name of the output file: ')
    with open(output_filename, 'w') as output_file:
        sys.stdout = output_file  # Redirect stdout to the output file
        dfa.print_dfa()  # This will write the output to the file
        sys.stdout = sys.__stdout__  # Reset stdout to the default value

    print("Output has been written to", output_filename)

if __name__ == "__main__":
    main()
