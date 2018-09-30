package org.kobjects.asde.lang.parser;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.node.AssignStatement;
import org.kobjects.asde.lang.node.AssignableNode;
import org.kobjects.asde.lang.node.Command;
import org.kobjects.asde.lang.node.ForStatement;
import org.kobjects.asde.lang.node.LetStatement;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.node.NextStatement;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Operator;
import org.kobjects.asde.lang.node.Statement;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.expressionparser.ExpressionParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Parser {
  final ExpressionParser<Node> expressionParser;
  final Program program;

  public Parser(Program program) {
    this.program = program;

    expressionParser = new ExpressionParser<>(new ExpressionBuilder(program));
    expressionParser.addApplyBrackets(9,"(", ",", ")");
    expressionParser.addApplyBrackets(9,"[", ",", "]");  // HP
    expressionParser.addGroupBrackets("(", null, ")");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 10, ".");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 8, "^");
    expressionParser.addOperators(ExpressionParser.OperatorType.PREFIX, 7, "-");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 6, "*", "/");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 5, "+", "-");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 4, ">=", "<=", "<>", ">", "<", "=");
    expressionParser.addOperators(ExpressionParser.OperatorType.PREFIX, 3, "not", "NOT", "Not");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 2, "and", "AND", "And");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 1, "or", "OR", "Or");
  }


  public ExpressionParser.Tokenizer createTokenizer(String line) {
    return new ExpressionParser.Tokenizer(new Scanner(line), expressionParser.getSymbols(), "->");
  }


  Node parseStatement(ExpressionParser.Tokenizer tokenizer) {
    String name = tokenizer.currentValue;

    switch (name.toUpperCase()) {
      case "FOR":
        return parseFor(tokenizer);
      case "LET":
        return parseLet(tokenizer);
      case "NEXT":
        return parseNext(tokenizer);
    }
    for (Command.Kind kind : Command.Kind.values()) {
      if (name.equalsIgnoreCase(kind.name())) {
        return parseCommand(tokenizer, kind);
      }
    }

    if (tryConsume(tokenizer, "GO")) {  // GO TO, GO SUB -> GOTO, GOSUB
      name += tokenizer.currentValue;
    } else if (name.equals("?")) {
      name = "PRINT";
    }

    for (Statement.Kind kind : Statement.Kind.values()) {
      if (name.equalsIgnoreCase(kind.name())) {
        return parseStatement(tokenizer, kind);
      }
    }

    Node expression = expressionParser.parse(tokenizer);
    if ((expression instanceof Operator) && (expression.children[0] instanceof AssignableNode)
             && ((Operator) expression).name.equals("=")) {
      return new AssignStatement(expression.children[0], expression.children[1]);
    }
    return expression;
  }

  Statement parseStatement(ExpressionParser.Tokenizer tokenizer, Statement.Kind kind) {
    tokenizer.nextToken();
    switch (kind) {
      case RESTORE: // 0 or 1 param; Default is 0
      case RETURN:
        if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
            !tokenizer.currentValue.equals(":")) {
          return new Statement(program, kind, expressionParser.parse(tokenizer));
        }
        return new Statement(program, kind);

      case DEF:  // Exactly one param
      case GOTO:
      case GOSUB:
      case PAUSE:
        return new Statement(program, kind, expressionParser.parse(tokenizer));


      case DATA:  // One or more params
      case DIM:
      case READ: {
        ArrayList<Node> expressions = new ArrayList<>();
        do {
          expressions.add(expressionParser.parse(tokenizer));
        } while (tokenizer.tryConsume(","));
        return new Statement(program, kind, expressions.toArray(new Node[expressions.size()]));
      }

      case IF:
        Node condition = expressionParser.parse(tokenizer);
        if (!tryConsume(tokenizer, "THEN") && !tryConsume(tokenizer, "GOTO")) {
          throw tokenizer.exception("'THEN expected after IF-condition.'", null);
        }
        if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.NUMBER) {
          double target = (int) Double.parseDouble(tokenizer.currentValue);
          tokenizer.nextToken();
          return new Statement(program, kind, new String[]{" THEN "}, condition,
              new Literal(target));
        }
        return new Statement(program, kind, new String[]{" THEN"}, condition);

      case INPUT:
      case PRINT:
        List<Node> args = new ArrayList<>();
        List<String> delimiter = new ArrayList<>();
        while (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF
            && !tokenizer.currentValue.equals(":")) {
          if (tokenizer.currentValue.equals(",") || tokenizer.currentValue.equals(";")) {
            delimiter.add(tokenizer.currentValue + " ");
            tokenizer.nextToken();
            if (delimiter.size() > args.size()) {
              args.add(new Literal(Program.INVISIBLE_STRING));
            }
          } else {
            args.add(expressionParser.parse(tokenizer));
          }
        }
        return new Statement(program, kind, delimiter.toArray(new String[delimiter.size()]),
            args.toArray(new Node[args.size()]));

      case ON: {
        List<Node> expressions = new ArrayList<Node>();
        expressions.add(expressionParser.parse(tokenizer));
        String[] suffix = new String[1];
        if (tryConsume(tokenizer, "GOTO")) {
          suffix[0] = " GOTO ";
        } else if (tryConsume(tokenizer, "GOSUB")) {
          suffix[0] = " GOSUB ";
        } else {
          throw tokenizer.exception("GOTO or GOSUB expected.", null);
        }
        do {
          expressions.add(expressionParser.parse(tokenizer));
        } while (tokenizer.tryConsume(","));
        return new Statement(program, kind, suffix,
            expressions.toArray(new Node[expressions.size()]));
      }
      case REM: {
        StringBuilder sb = new StringBuilder();
        while (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF) {
          sb.append(tokenizer.leadingWhitespace).append(tokenizer.currentValue);
          tokenizer.nextToken();
        }
        if (sb.length() > 0 && sb.charAt(0) == ' ') {
          sb.deleteCharAt(0);
        }
        return new Statement(program, kind, new Identifier(program, sb.toString()));
      }
      default:
        return new Statement(program, kind);
    }
  }

  Command parseCommand(ExpressionParser.Tokenizer tokenizer, Command.Kind kind) {
    tokenizer.nextToken();
    switch (kind) {
      case RUN:  // 0 or 1 param; Default is 0
      case SAVE:
        if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
                !tokenizer.currentValue.equals(":")) {
          return new Command(kind, expressionParser.parse(tokenizer));
        }
        return new Command(kind);

      case LOAD: // Exactly one param
        return new Command(kind, expressionParser.parse(tokenizer));

      default:
        return new Command(kind);
    }
  }

  private Node parseFor(ExpressionParser.Tokenizer tokenizer) {
    tokenizer.nextToken();
    Node assignment = expressionParser.parse(tokenizer);
    if (!(assignment instanceof Operator) || !(assignment.children[0] instanceof Identifier)
            || assignment.children[0].children.length != 0
            || !((Operator) assignment).name.equals("=")) {
      throw new RuntimeException("LocalVariable assignment expected after FOR");
    }
    String varName = ((Identifier) assignment.children[0]).name;
    require(tokenizer, "TO");
    Node end = expressionParser.parse(tokenizer);
    if (tryConsume(tokenizer, "STEP")) {
      return new ForStatement(varName, assignment.children[1], end,
              expressionParser.parse(tokenizer));
    }
    return new ForStatement(varName, assignment.children[1], end);
  }

  private Node parseLet(ExpressionParser.Tokenizer tokenizer) {
    tokenizer.nextToken();
    Node assignment = expressionParser.parse(tokenizer);
    if (!(assignment instanceof Operator) || !(assignment.children[0] instanceof AssignableNode)
            || !((Operator) assignment).name.equals("=")) {
      throw tokenizer.exception("Unrecognized statement or illegal assignment: '"
              + assignment + "'.", null);
    }
    if (assignment.children[0] instanceof Identifier) {
      String varName = ((Identifier) assignment.children[0]).name;
      return new LetStatement(varName, assignment.children[1]);
    }
    return new AssignStatement(assignment.children[0], assignment.children[1]);
  }

  private NextStatement parseNext(ExpressionParser.Tokenizer tokenizer) {
    tokenizer.nextToken();
    ArrayList<Node> vars = new ArrayList<>();
    if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
            !tokenizer.currentValue.equals(":")) {
      do {
        vars.add(expressionParser.parse(tokenizer));
      } while (tokenizer.tryConsume(","));
    }
    return new NextStatement(vars.toArray(new Node[vars.size()]));

  }

  public List<? extends Node> parseStatementList(ExpressionParser.Tokenizer tokenizer) {
    ArrayList<Node> result = new ArrayList<>();
    Node statement;
    do {
      while (tokenizer.tryConsume(":")) {
        result.add(new Statement(program, null));
      }
      if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.EOF) {
        break;
      }
      statement = parseStatement(tokenizer);
      result.add(statement);
    } while (statement instanceof Statement && ((Statement) statement).kind == Statement.Kind.IF ? statement.children.length == 1
        : tokenizer.tryConsume(":"));
    if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF) {
      throw tokenizer.exception("Leftover input.", null);
    }
    return result;
  }

  void require(ExpressionParser.Tokenizer tokenizer, String s) {
    if (!tryConsume(tokenizer, s)) {
      throw tokenizer.exception("Expected: '" + s + "'.", null);
    }
  }

  boolean tryConsume(ExpressionParser.Tokenizer tokenizer, String s) {
    if (tokenizer.currentValue.equalsIgnoreCase(s)) {
      tokenizer.nextToken();
      return true;
    }
    return false;
  }


}
