
#include "regexp.h"

// вывод
void print_prog(struct Prog *p)
{
  struct Inst *pc, *end;
  end = p->start + p->len;
  for (pc = p->start; pc < end; pc++)
  {
    switch (pc->opcode)
    {
    default:
      assert(0);
    case Char:
    {
      int addr = (int)(pc - p->start);
      char buf[80], *p;
      p = buf;
      while (pc->opcode == Char)
        *p++ = pc++->c;
      pc--;
      *p = '\0';
      printf("%2d. <%s>\n", addr, buf);
      break;
    }
    case Match:
      printf("%2d. match\n", (int)(pc - p->start));
      break;
    case Split:
      printf("%2d. split %d, %d\n",
             (int)(pc - p->start), (int)(pc->x - p->start), (int)(pc->y - p->start));
      break;
    case Jmp:
      printf("%2d. jmp %d\n",
             (int)(pc - p->start), (int)(pc->x - p->start));
      break;
    case Save:
      printf("%2d. save [%d]\n",
             (int)(pc - p->start), (int)(pc->n));
      break;
    case Any:
      printf("%2d. any\n", (int)(pc - p->start));
      break;
    }
  }
}

// вывод инструкций через строки
static void prog_to_str(char *str, struct Prog *p)
{
  struct Inst *pc, *end;
  end = p->start + p->len;
  for (pc = p->start; pc < end; pc++)
  {
    switch (pc->opcode)
    {
    default:
      assert(0);
    case Char:
      sprintf(str, "%d. char %c\n", (int)(pc - p->start), pc->c);
      str = str + strlen(str);
      break;
    case Match:
      sprintf(str, "%d. match\n", (int)(pc - p->start));
      str = str + strlen(str);
      break;
    case Split:
      sprintf(str, "%d. split %d, %d\n",
              (int)(pc - p->start), (int)(pc->x - p->start), (int)(pc->y - p->start));
      str = str + strlen(str);
      break;
    case Jmp:
      sprintf(str, "%d. jmp %d\n",
              (int)(pc - p->start), (int)(pc->x - p->start));
      str = str + strlen(str);
      break;
    case Save:
      sprintf(str, "%d. save [%d]\n",
              (int)(pc - p->start), (int)(pc->n));
      str = str + strlen(str);
      break;
    case Any:
      sprintf(str, "%d. any\n", (int)(pc - p->start));
      str = str + strlen(str);
      break;
    }
  }
}

void reg_to_str(char *str, struct Regexp *re)
{
  switch (re->type)
  {
  default:
    assert(0);
  case Lit:
    sprintf(str, "Lit(%c)", re->ch);
    break;
  case Cat:
  {
    char lbuf[280], rbuf[280];
    reg_to_str(lbuf, re->left);
    reg_to_str(rbuf, re->right);
    sprintf(str, "Cat(%s, %s)", lbuf, rbuf);
    break;
  }
  case Alt:
  {
    char lbuf[280], rbuf[280];
    reg_to_str(lbuf, re->left);
    reg_to_str(rbuf, re->right);
    sprintf(str, "Alt(%s, %s)", lbuf, rbuf);
    break;
  }
  case Plus:
  {
    char buf[280];
    reg_to_str(buf, re->left);
    sprintf(str, "Plus(%s)", buf);
    break;
  }
  case Star:
  {
    char buf[280];
    reg_to_str(buf, re->left);
    sprintf(str, "Star(%s)", buf);
    break;
  }
  case Paren:
  {
    char buf[280];
    reg_to_str(buf, re->left);
    sprintf(str, "Paren(%d, %s)", re->n, buf);
    break;
  }
  case Quest:
  {
    char buf[280];
    reg_to_str(buf, re->left);
    sprintf(str, "Quest(%s)", buf);
    break;
  }
  case Dot:
  {
    strcpy(str, "Dot");
    break;
  }
  }
}

// тесты
void test(void);

void test_reg(void)
{
  struct Regexp *lit1 = reg(Lit, NULL, NULL);
  lit1->ch = 'a';
  struct Regexp *lit2 = reg(Lit, NULL, NULL);
  lit2->ch = 'b';

  char str[80];
  reg_to_str(str, lit1);
  assert(!strcmp("Lit(a)", str));

  struct Regexp *cat = reg(Cat, lit1, lit2);

  reg_to_str(str, cat);
  assert(!strcmp("Cat(Lit(a), Lit(b))", str));

  free(lit1);
  free(lit2);
  free(cat);
}

void test_parse_concat(void)
{
  struct Regexp *re1 = parse("a");
  char str[80];
  reg_to_str(str, re1);
  //printf("%s\n", str);
  assert(!strcmp("Cat(Star(Dot), Paren(0, Lit(a)))", str));
  struct Regexp *re2 = parse("ab");
  reg_to_str(str, re2);
  //printf("%s\n", str);
  assert(!strcmp("Cat(Star(Dot), Paren(0, Cat(Lit(a), Lit(b))))", str));
}

void test_parse_plus(void)
{
  struct Regexp *re = parse("a+");
  char str[80];
  reg_to_str(str, re);
  //printf("%s\n", str);
  assert(!strcmp("Cat(Star(Dot), Paren(0, Plus(Lit(a))))", str));
}

void test_parse_star(void)
{
  struct Regexp *re = parse("a*");
  char str[80];
  reg_to_str(str, re);
  // printf("%s\n", str);
  assert(!strcmp("Cat(Star(Dot), Paren(0, Star(Lit(a))))", str));
}

void test_parse_alt(void)
{
  struct Regexp *re = parse("a|b");
  char str[80];
  reg_to_str(str, re);
  // printf("%s\n", str);
  assert(!strcmp("Cat(Star(Dot), Paren(0, Alt(Lit(a), Lit(b))))", str));
}

void test_parse_paren(void)
{
  struct Regexp *re = parse("(a)");
  char str[80];
  reg_to_str(str, re);
  // printf("%s\n", str);
  assert(!strcmp("Cat(Star(Dot), Paren(0, Paren(1, Lit(a))))", str));
}

void test_parse_quest(void)
{
  struct Regexp *re = parse("a?");
  char str[80];
  reg_to_str(str, re);
  // printf("%s\n", str);
  assert(!strcmp("Cat(Star(Dot), Paren(0, Quest(Lit(a))))", str));
}

void test_compile_dot(void)
{
  struct Regexp *re = parse(".+");
  char str[180];
  reg_to_str(str, re);
  assert(!strcmp("Cat(Star(Dot), Paren(0, Plus(Dot)))", str));
  struct Prog *prog = compile(re);
  prog_to_str(str, prog);
  char expect[] =
      "0. split 3, 1\n"
      "1. any\n"
      "2. jmp 0\n"
      "3. save [0]\n"
      "4. any\n"
      "5. split 4, 6\n"
      "6. save [1]\n"
      "7. match\n";
  assert(!strcmp(expect, str));
  free(prog);
}

void test_compile_concat(void)
{
  struct Regexp *re = parse("ab");
  struct Prog *prog = compile(re);
  char str[180];
  prog_to_str(str, prog);
  char expect[] =
    "0. split 3, 1\n"
    "1. any\n"
    "2. jmp 0\n"
    "3. save [0]\n"
    "4. char a\n"
    "5. char b\n"
    "6. save [1]\n"
    "7. match\n";
  assert(!strcmp(expect, str));
  free(prog);
}

void test_compile_plus(void)
{
  struct Regexp *re1 = parse("a+");
  struct Prog *prog = compile(re1);
  char str[180];
  prog_to_str(str, prog);
  char expect[] =
      "0. split 3, 1\n"
      "1. any\n"
      "2. jmp 0\n"
      "3. save [0]\n"
      "4. char a\n"
      "5. split 4, 6\n"
      "6. save [1]\n"
      "7. match\n";
  assert(!strcmp(expect, str));
  free(prog);
}

void test_compile_star(void)
{
  struct Regexp *re1 = parse("a*");
  struct Prog *prog = compile(re1);
  char str[180];
  prog_to_str(str, prog);
  char expect[] =
      "0. split 3, 1\n"
      "1. any\n"
      "2. jmp 0\n"
      "3. save [0]\n"
      "4. split 5, 7\n"
      "5. char a\n"
      "6. jmp 4\n"
      "7. save [1]\n"
      "8. match\n";
  assert(!strcmp(expect, str));
  free(prog);
}

void test_compile_alt(void)
{
  struct Regexp *re1 = parse("a|b");
  struct Prog *prog = compile(re1);
  char str[180];
  prog_to_str(str, prog);
  char expect[] =
      "0. split 3, 1\n"
      "1. any\n"
      "2. jmp 0\n"
      "3. save [0]\n"
      "4. split 5, 7\n"
      "5. char a\n"
      "6. jmp 8\n"
      "7. char b\n"
      "8. save [1]\n"
      "9. match\n";
  assert(!strcmp(expect, str));
  free(prog);
}

void test_compile_paren(void)
{
  struct Regexp *re1 = parse("(a)");
  struct Prog *prog = compile(re1);
  char str[180];
  prog_to_str(str, prog);
  char expect[] =
      "0. split 3, 1\n"
      "1. any\n"
      "2. jmp 0\n"
      "3. save [0]\n"
      "4. save [2]\n"
      "5. char a\n"
      "6. save [3]\n"
      "7. save [1]\n"
      "8. match\n";

  assert(!strcmp(expect, str));
  free(prog);
}

void test_compile_quest(void)
{
  struct Regexp *re1 = parse("a?");
  struct Prog *prog = compile(re1);
  char str[180];
  prog_to_str(str, prog);
  char expect[] =
      "0. split 3, 1\n"
      "1. any\n"
      "2. jmp 0\n"
      "3. save [0]\n"
      "4. split 5, 6\n"
      "5. char a\n"
      "6. save [1]\n"
      "7. match\n";
  assert(!strcmp(expect, str));
  free(prog);
}


void test_thompson();
void test_sub();

void test(void)
{
  test_reg();
  test_parse_concat();
  test_parse_plus();
  test_parse_star();
  test_parse_alt();
  test_parse_paren();
  test_parse_quest();
  test_compile_dot();
  test_compile_concat();
  test_compile_plus();
  test_compile_star();
  test_compile_alt();
  test_compile_paren();
  test_compile_quest();
  test_thompson();  
}
