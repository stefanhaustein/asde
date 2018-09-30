package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.CodeLine;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.StackEntry;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.Apply;
import org.kobjects.asde.lang.node.AssignableNode;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Operator;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;


public class LegacyStatement extends Node {

  public enum Kind {
    DATA, DEF, DUMP,
    END,
    GOTO, GOSUB,
    ON,
    PAUSE, // TODO: Turn into builtin
    READ, RESTORE,
    STOP,
  }

  final Program program;
  public final Kind kind;
  final String[] delimiter;

  public LegacyStatement(Program program, Kind kind, String[] delimiter, Node... children) {
    super(children);
    this.program = program;
    this.kind = kind;
    this.delimiter = delimiter;
  }

  public LegacyStatement(Program program, Kind kind, Node... children) {
    this(program, kind, null, children);
  }

  public Object eval(Interpreter interpreter) {
    if (kind == null) {
      return null;
    }

    if (program.trace) {
      program.print(interpreter.currentLine + ":" + interpreter.currentIndex + ": " + this);
    }

    switch (kind) {
      case DEF: {
        Node assignment = children[0];
        if (!(assignment instanceof Operator)
                || !((Operator) assignment).name.equals("=")
                || !(assignment.children[0] instanceof Apply)
                || !(((Apply) assignment.children[0]).children[0] instanceof Identifier)) {
          throw new RuntimeException("Assignment to function declaration expected.");
        }
        Apply target = (Apply) assignment.children[0];
        String name = ((Identifier) target.children[0]).name;
        Type[] parameterTypes = new Type[target.children.length - 1];
        String[] parameterNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
          Identifier parameterNode = (Identifier) target.children[i + 1];
          parameterNames[i] = parameterNode.name;
          parameterTypes[i] = parameterNode.name.endsWith("$") ? Types.STRING : Types.NUMBER;
        }
        CallableUnit fn = new CallableUnit(program, new FunctionType(name.endsWith("$") ? Types.STRING : Types.NUMBER, parameterTypes), parameterNames);
        fn.setLine(10, new CodeLine(Collections.singletonList(new ReturnStatement(assignment.children[1]))));
        program.setValue(interpreter.getSymbolScope(), name, fn);
        break;
      }
      case DATA:
        break;

      case END:
        interpreter.currentLine = Integer.MAX_VALUE;
        interpreter.currentIndex = 0;
        break;

      case GOSUB: {
        StackEntry entry = new StackEntry();
        entry.lineNumber = interpreter.currentLine;
        entry.statementIndex = interpreter.currentIndex;
        interpreter.stack.add(entry);
      }  // Fallthrough intended
      case GOTO:
        interpreter.currentLine = (int) evalDouble(interpreter,0);
        interpreter.currentIndex = 0;
        break;

      case PAUSE:
        try {
          Thread.sleep(evalInt(interpreter, 0));
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        break;

      case ON: {
        int index = (int) Math.round(evalDouble(interpreter,0));
        if (index < children.length && index > 0) {
          if (delimiter[0].equals(" GOSUB ")) {
            StackEntry entry = new StackEntry();
            entry.lineNumber = interpreter.currentLine;
            entry.statementIndex = interpreter.currentIndex;
            interpreter.stack.add(entry);
          }
          interpreter.currentLine = (int) evalDouble(interpreter, index);
          interpreter.currentIndex = 0;
        }
        break;
      }
      case READ:
        for (int i = 0; i < children.length; i++) {
          while (interpreter.dataStatement == null
              || interpreter.dataPosition[2] >= interpreter.dataStatement.children.length) {
            interpreter.dataPosition[2] = 0;
            if (interpreter.dataStatement != null) {
              interpreter.dataPosition[1]++;
            }
            interpreter.dataStatement = (LegacyStatement) program.main.find((Node statement)->(statement instanceof LegacyStatement && ((LegacyStatement) statement).kind == Kind.DATA), interpreter.dataPosition);
            if (interpreter.dataStatement == null) {
              throw new RuntimeException("Out of data.");
            }
          }
          ((AssignableNode) children[i]).set(interpreter, interpreter.dataStatement.children[interpreter.dataPosition[2]++].eval(interpreter));
        }
        break;

      case RESTORE:
        interpreter.dataStatement = null;
        Arrays.fill(interpreter.dataPosition, 0);
        if (children.length > 0) {
          interpreter.dataPosition[0] = (int) evalDouble(interpreter, 0);
        }
        break;

      case STOP:
        program.stopped = new int[]{interpreter.currentLine, interpreter.currentIndex};
        program.println("\nSTOPPED in " + interpreter.currentLine + ":" + interpreter.currentIndex);
        interpreter.currentLine = Integer.MAX_VALUE;
        interpreter.currentIndex = 0;
        break;

      default:
        throw new RuntimeException("Unimplemented statement: " + kind);
    }
    if (program.trace) {
      program.println();
    }
    return null;
  }




  @Override
  public Type returnType() {
    return Types.VOID;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    if (kind == null) {
      return;
    }
    appendLinked(asb, kind.name(), errors);
    if (children.length > 0) {
      appendLinked(asb, " ", errors);
      children[0].toString(asb, errors);
      for (int i = 1; i < children.length; i++) {
        asb.append((delimiter == null || i > delimiter.length) ? ", " : delimiter[i - 1]);
        children[i].toString(asb, errors);
      }
      if (delimiter != null && delimiter.length == children.length) {
        asb.append(delimiter[delimiter.length - 1]);
      }
    }
  }
}