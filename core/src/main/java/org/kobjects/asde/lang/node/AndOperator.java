package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.PropertyValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class AndOperator extends Node {

  boolean boolMode;

  public AndOperator(Node child1, Node child2) {
    super(child1, child2);
  }

  @Override
  protected void onResolve(PropertyValidationContext resolutionContext, int line) {
    Type t0 = children[0].returnType();
    if (t0 != Types.BOOL && t0 != Types.FLOAT) {
      throw new IllegalArgumentException("First argument must be number or boolean instead of " + t0);
    }
    Type t1 = children[1].returnType();
    if (t1 != Types.BOOL && t1 != Types.FLOAT) {
      throw new IllegalArgumentException("Second argument must be number or boolean instead of " + t1);
    }
    boolMode = children[0].returnType() == Types.BOOL || children[1].returnType() == Types.BOOL;
  }

  @Override
  public boolean evalBoolean(EvaluationContext evaluationContext) {
    return children[0].evalBoolean(evaluationContext) && children[1].evalBoolean(evaluationContext);
  }

  @Override
  public double evalDouble(EvaluationContext evaluationContext) {
    return children[0].evalInt(evaluationContext) & children[1].evalInt(evaluationContext);
  }

  @Override
  public int evalInt(EvaluationContext evaluationContext) {
    return children[0].evalInt(evaluationContext) & children[1].evalInt(evaluationContext);
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    if (boolMode) {
      return evalBoolean(evaluationContext);
    }
    return evalDouble(evaluationContext);
  }


  @Override
  public Type returnType() {
    return boolMode ? Types.BOOL : Types.FLOAT;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    children[0].toString(asb, errors, preferAscii);
    appendLinked(asb, " AND ", errors);
    children[1].toString(asb, errors, preferAscii);
  }
}
