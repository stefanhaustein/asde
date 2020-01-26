package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.FunctionValidationContext;

import java.util.Map;

public class FunctionReturnStatement extends Statement {

  public FunctionReturnStatement(Node... children) {
    super(children);
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line) {
    if (resolutionContext.functionImplementation.getType().getReturnType() == Types.VOID) {
      if (children.length != 0) {
        throw new RuntimeException("Unexpected return value for subroutine.");
      }
    } else {
      if (children.length != 1) {
        throw new RuntimeException("Return value expected for function.");
      }
      if (!children[0].returnType().equals(resolutionContext.functionImplementation.getType().getReturnType())) {
        throw new RuntimeException("Expected return type: " + resolutionContext.functionImplementation.getType().getReturnType() + "; actual: " + children[0].returnType());
      }
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    if (children.length > 0) {
      evaluationContext.returnValue = children[0].eval(evaluationContext);
    }
    evaluationContext.currentLine = Integer.MAX_VALUE;
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "RETURN", errors);
    if (children.length > 0) {
      asb.append(' ');
      children[0].toString(asb, errors, preferAscii);
    }
  }
}
