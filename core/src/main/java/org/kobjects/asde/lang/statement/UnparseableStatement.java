package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.PropertyValidationContext;
import org.kobjects.asde.lang.node.Node;

import java.util.Map;

public class UnparseableStatement extends Statement {

  private final String text;
  private final RuntimeException error;

  public UnparseableStatement(String text, Exception error) {
    this.text = text;
    this.error = error instanceof RuntimeException ? (RuntimeException) error : new RuntimeException(error);
  }

  @Override
  protected void onResolve(PropertyValidationContext resolutionContext, int line) {
    throw error;
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, text, errors);
  }
}
