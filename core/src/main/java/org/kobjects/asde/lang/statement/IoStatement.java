package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.node.AssignableNode;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.FunctionValidationContext;

import java.util.Map;

public class IoStatement extends Statement {

  public enum Kind {
    INPUT, PRINT
  }

  public final Kind kind;
  private String[] delimiter;

  public IoStatement(Kind kind, String[] delimiter, Node... children) {
    super(children);
    this.kind = kind;
    this.delimiter = delimiter;
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line) {
    if (kind == Kind.INPUT) {
      for (Node child : children) {
        if (child instanceof AssignableNode) {
          AssignableNode assignable = (AssignableNode) child;
          if (assignable.isAssignable()) {
            assignable.resolveForAssignment(resolutionContext, parent, child.returnType(), line);
          }
        }
      }
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    switch (kind) {
      case PRINT:
        print(evaluationContext);
        break;
      case INPUT:
        input(evaluationContext);
        break;
    }
    return null;
  }

  void input(EvaluationContext evaluationContext) {
    Program program = evaluationContext.control.program;
    for (int i = 0; i < children.length; i++) {
      Node child = children[i];
      if (child instanceof AssignableNode && ((AssignableNode) child).isAssignable()) {
        if (i <= 0 || i > delimiter.length || !delimiter[i-1].equals(", ")) {
          program.print("? ");
        }
        Object value;
        while(true) {
          value = program.console.input(child.returnType());
          if (child.returnType() == Types.STR) {
            break;
          }
          try {
            value = Double.parseDouble((String) value);
            break;
          } catch (NumberFormatException e) {
            program.print("Not a number. Please enter a number: ");
          }
        }
        ((AssignableNode) child).set(evaluationContext, value);
      } else {
        program.print(Program.toString(child.eval(evaluationContext)));
      }
    }
  }

  void print(EvaluationContext evaluationContext) {
    Program program = evaluationContext.control.program;
    for (int i = 0; i < children.length; i++) {
      Object val = children[i].eval(evaluationContext);
      if (val instanceof Double) {
        double d = (Double) val;
        program.print((d < 0 ? "" : " ") + Program.toString(d) + " ");
      } else {
        program.print(Program.toString(val));
      }
      if (i < delimiter.length && delimiter[i].equals(", ")) {
        program.print(
            "                    ".substring(0, 14 - (program.tabPos % 14)));
      }
    }
    if (children.length == 0 && delimiter.length == 0 ||
        delimiter.length < children.length && !children[children.length - 1].toString().startsWith("TAB")) {
      program.print("\n");
    }
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, kind.name(), errors);
    if (children.length > 0) {
      appendLinked(asb, " ", errors);
      children[0].toString(asb, errors, preferAscii);
      for (int i = 1; i < children.length; i++) {
        asb.append((delimiter == null || i > delimiter.length) ? ", " : delimiter[i - 1]);
        children[i].toString(asb, errors, preferAscii);
      }
      if (delimiter != null && delimiter.length == children.length) {
        asb.append(delimiter[delimiter.length - 1]);
      }
    }
  }
}
