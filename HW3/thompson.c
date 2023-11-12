// Copyright 2007-2009 Russ Cox.  All Rights Reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

#include "regexp.h"

typedef struct Thread Thread;
struct Thread
{
	Inst *pc;
};

typedef struct ThreadList ThreadList;
struct ThreadList
{
	int n;
	Thread t[1];
};

static Thread
thread(Inst *pc)
{
	Thread t = {pc};
	return t;
}

static ThreadList*
threadlist(int n)
{
	return mal(sizeof(ThreadList)+n*sizeof(Thread));
}

static void
addthread(ThreadList *l, Thread t)
{
	if(t.pc->gen == gen)
		return;	// already on list

	t.pc->gen = gen;
	l->t[l->n] = t;
	l->n++;
	
	switch(t.pc->opcode) {
	case Jmp:
		addthread(l, thread(t.pc->x));
		break;
	case Split:
		addthread(l, thread(t.pc->x));
		addthread(l, thread(t.pc->y));
		break;
	case Save:
		addthread(l, thread(t.pc+1));
		break;
	}
}

int
is_match_thompson(Prog *prog, char *input, char **subp, int nsubp)
{
	int i, len, matched;
	ThreadList *clist, *nlist, *tmp;
	Inst *pc;
	char *sp;
	
	for(i=0; i<nsubp; i++)
		subp[i] = nil;

	len = prog->len;
	clist = threadlist(len);
	nlist = threadlist(len);
	
	if(nsubp >= 1)
		subp[0] = input;
	gen++;
	addthread(clist, thread(prog->start));
	matched = 0;
	for(sp=input;; sp++) {
		if(clist->n == 0)
			break;
		// printf("%d(%02x).", (int)(sp - input), *sp & 0xFF);
		gen++;
		for(i=0; i<clist->n; i++) {
			pc = clist->t[i].pc;
			// printf(" %d", (int)(pc - prog->start));
			switch(pc->opcode) {
			case Char:
				if(*sp != pc->c)
					break;
			case Any:
				if(*sp == 0)
					break;
				addthread(nlist, thread(pc+1));
				break;
			case Match:
				if(nsubp >= 2)
					subp[1] = sp;
				matched = 1;
				goto BreakFor;
			// Jmp, Split, Save handled in addthread, so that
			// machine execution matches what a backtracker would do.
			// This is discussed (but not shown as code) in
			// Regular Expression Matching: the Virtual Machine Approach.
			}
		}
	BreakFor:
		// printf("\n");
		tmp = clist;
		clist = nlist;
		nlist = tmp;
		nlist->n = 0;
		if(*sp == '\0')
			break;
	}
	return matched;
}




static void test_dot(void)
{
  struct Regexp *re = parse("a.+");
  struct Prog *prog = compile(re);
  char *sub[MAXSUB];
  assert(!is_match_thompson(prog, "a", sub, nelem(sub)));
  char *input = "aa";
  assert(is_match_thompson(prog, input, sub, nelem(sub)));
  assert(0 == (int)(sub[0] - input));
  assert(2 == (int)(sub[1] - input));
  assert(is_match_thompson(prog, "ab", sub, nelem(sub)));
  free(prog);
}

static void test_alt(void)
{
  struct Regexp *re = parse("a|b");
  struct Prog *prog = compile(re);
  char *sub[MAXSUB];
  assert(is_match_thompson(prog, "a", sub, nelem(sub)));
  assert(is_match_thompson(prog, "b", sub, nelem(sub)));
  assert(!is_match_thompson(prog, "c", sub, nelem(sub)));
  free(prog);
}

static void test_quest(void)
{
  struct Regexp *re = parse("ba?");
  struct Prog *prog = compile(re);
  char *sub[MAXSUB];
  assert(1 == is_match_thompson(prog, "b", sub, nelem(sub)));
  assert(1 == is_match_thompson(prog, "ba", sub, nelem(sub)));
  assert(1 == is_match_thompson(prog, "baa", sub, nelem(sub)));
  assert(1 == is_match_thompson(prog, "baaaaa", sub, nelem(sub)));
  assert(0 == is_match_thompson(prog, "aaaaa", sub, nelem(sub)));
  free(prog);
}

static void test_is_match_thompson_concat(void)
{
  struct Regexp *re = parse("ab");
  struct Prog *prog = compile(re);
  char *sub[MAXSUB];
  assert(is_match_thompson(prog, "ab", sub, nelem(sub)));
  assert(!is_match_thompson(prog, "bc", sub, nelem(sub)));
  free(prog);
}

static void test_is_match_thompson_plus(void)
{
  char input[] = "a+b+";
  struct Regexp *re = parse(input);
  struct Prog *prog = compile(re);
  char *sub[MAXSUB];
  assert(!is_match_thompson(prog, "a", sub, nelem(sub)));
  assert(is_match_thompson(prog, "aab", sub, nelem(sub)));
  assert(is_match_thompson(prog, "aaabb", sub, nelem(sub)));
  assert(!is_match_thompson(prog, "b", sub, nelem(sub)));
  free(prog);
}

static void test_is_match_thompson_star(void)
{
  char input[] = "a*b";
  struct Regexp *re = parse(input);
  struct Prog *prog = compile(re);
  char *sub[MAXSUB];
  assert(is_match_thompson(prog, "b", sub, nelem(sub)));
  assert(is_match_thompson(prog, "aaab", sub, nelem(sub)));
  assert(!is_match_thompson(prog, "aa", sub, nelem(sub)));
  free(prog);
}

static void test_is_match_thompson_paren(void)
{
  struct Regexp *re = parse("P(ython|erl)");
  struct Prog *prog = compile(re);
  char *sub[MAXSUB];
  assert(is_match_thompson(prog, "Python", sub, nelem(sub)));
  assert(is_match_thompson(prog, "Perl", sub, nelem(sub)));
  assert(!is_match_thompson(prog, "Ruby", sub, nelem(sub)));
  free(prog);
}


static void test_is_match_thompson_one(void)
{
  struct Regexp *re = parse("(a)|(b*)|c");
  struct Prog *prog = compile(re);
  char *sub[MAXSUB];
  assert(is_match_thompson(prog, "a", sub, nelem(sub)));
  assert(is_match_thompson(prog, "b", sub, nelem(sub)));
  assert(is_match_thompson(prog, "c", sub, nelem(sub)));
  assert(is_match_thompson(prog, "aaaa", sub, nelem(sub)));
  assert(is_match_thompson(prog, "bbbb", sub, nelem(sub)));
  assert(is_match_thompson(prog, "cccc", sub, nelem(sub)));
  assert(is_match_thompson(prog, "qwerty", sub, nelem(sub)));
  assert(is_match_thompson(prog, "opl", sub, nelem(sub)));
  assert(is_match_thompson(prog, "ghj", sub, nelem(sub)));
  free(prog);
}

static void test_is_match_thompson_two(void)
{
  struct Regexp *re = parse("(a|bc)+");
  struct Prog *prog = compile(re);
  char *sub[MAXSUB];
  assert(is_match_thompson(prog, "a", sub, nelem(sub)));
  assert(is_match_thompson(prog, "bc", sub, nelem(sub)));
  assert(is_match_thompson(prog, "abc", sub, nelem(sub)));
  assert(is_match_thompson(prog, "aaaabcbc", sub, nelem(sub)));
  assert(!is_match_thompson(prog, "bbbb", sub, nelem(sub)));
  assert(is_match_thompson(prog, "bcbcbc", sub, nelem(sub)));
  assert(is_match_thompson(prog, "bcbcabcaa", sub, nelem(sub)));
  assert(!is_match_thompson(prog, "opl", sub, nelem(sub)));
  assert(!is_match_thompson(prog, "bdbg", sub, nelem(sub)));
  free(prog);
}


static void test_is_match_thompson_three(void)
{
  struct Regexp *re = parse("((a+b)+c+)(d+)");
  struct Prog *prog = compile(re);
  char *sub[MAXSUB];
  assert(!is_match_thompson(prog, "a", sub, nelem(sub)));
  assert(is_match_thompson(prog, "abcd", sub, nelem(sub)));
  assert(is_match_thompson(prog, "aaabcccddd", sub, nelem(sub)));
  assert(is_match_thompson(prog, "aababaaabcd", sub, nelem(sub)));
  assert(!is_match_thompson(prog, "bbbb", sub, nelem(sub)));
  assert(!is_match_thompson(prog, "abcc", sub, nelem(sub)));
  assert(!is_match_thompson(prog, "qwerty", sub, nelem(sub)));
  assert(!is_match_thompson(prog, "opl", sub, nelem(sub)));
  assert(!is_match_thompson(prog, "ghj", sub, nelem(sub)));
  free(prog);
}

void test_thompson(void)
{
  test_dot();
  test_alt();
  test_quest();
  test_is_match_thompson_concat();
  test_is_match_thompson_plus();
  test_is_match_thompson_star();
  test_is_match_thompson_paren();
  test_is_match_thompson_one();
  test_is_match_thompson_two();
  test_is_match_thompson_three();
}
