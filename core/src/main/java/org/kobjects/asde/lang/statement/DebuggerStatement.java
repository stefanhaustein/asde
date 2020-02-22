package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.PropertyValidationContext;
import org.kobjects.asde.lang.node.Node;

import java.util.Map;

public class DebuggerStatement extends Statement {
  @Override
  protected void onResolve(PropertyValidationContext resolutionContext, int line) {
    // nothing to do here...
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    evaluationContext.control.pause();
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "DEBUGGER", errors);
  }
}
