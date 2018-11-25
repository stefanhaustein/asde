package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Array;
import org.kobjects.asde.lang.Function;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

// Not static for access to the variables.
public class Apply extends AssignableNode {

  private final boolean parentesis;

  public Apply(boolean parentesis, Node... children) {
    super(children);
    this.parentesis = parentesis;
  }

  public void set(Interpreter interpreter, Object value) {
    Object base = children[0].evalRaw(interpreter);
    if (!(base instanceof Array)) {
      throw new EvaluationException(this, "Can't set indexed value to non-array: " + value);
    }
    Array array = (Array) base;
    int[] indices = new int[array.getType().dimensionality];
    for (int i = 1; i < children.length; i++) {
      indices[i - 1] = evalChildToInt(interpreter, i);
    }
    array.setValueAt(value, indices);
  }

    @Override
    protected void onResolve(ResolutionContext resolutionContext) {
        // TODO: Implement!
    }

    public Object eval(Interpreter interpreter) {
    Object base = children[0].evalRaw(interpreter);
    if (!(base instanceof Function)) {
      throw new EvaluationException(this, "Can't apply parameters to " + base);
    }
    Function function = (Function) base;
    for (int i = 1; i < children.length; i++) {
      interpreter.localStack.push(children[i].eval(interpreter));
    }
    try {
        return function.call(interpreter, children.length - 1);
    } catch (Exception e) {
        throw new RuntimeException(e.getMessage() + " in " + children[0]);
    } finally {
        interpreter.localStack.drop(children.length - 1);
    }
  }

  public Type returnType() {
    return children[0].toString().endsWith("$") ? Types.STRING : Types.NUMBER;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
     children[0].toString(asb, errors);
     asb.append(parentesis ? '(' : ' ');
     for (int i = 1; i < children.length; i++) {
          if (i > 1) {
              asb.append(", ");
          }
          children[i].toString(asb, errors);
      }
      if (parentesis) {
          asb.append(')');
      }
  }
}
