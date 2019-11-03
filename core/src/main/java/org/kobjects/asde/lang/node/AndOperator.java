package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class AndOperator extends Node {

  boolean boolMode;

  public AndOperator(Node child1, Node child2) {
    super(child1, child2);
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    Type t0 = children[0].returnType();
    if (t0 != Types.BOOLEAN && t0 != Types.NUMBER) {
      throw new IllegalArgumentException("First argument must be number or boolean instead of " + t0);
    }
    Type t1 = children[1].returnType();
    if (t1 != Types.BOOLEAN && t1 != Types.NUMBER) {
      throw new IllegalArgumentException("Second argument must be number or boolean instead of " + t1);
    }
    boolMode = children[0].returnType() == Types.BOOLEAN || children[1].returnType() == Types.BOOLEAN;
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
    return boolMode ? Types.BOOLEAN : Types.NUMBER;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    children[0].toString(asb, errors, preferAscii);
    appendLinked(asb, " AND ", errors);
    children[1].toString(asb, errors, preferAscii);
  }
}
