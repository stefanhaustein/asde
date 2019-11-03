package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;

import java.util.Map;


public class DeclarationStatement extends AbstractDeclarationStatement {

  public enum Kind {
    LET, CONST
  }

  public final Kind kind;

  public DeclarationStatement(Kind kind, String varName, Node init) {
    super(varName, init);
    this.kind = kind;
  }

  public void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    resolved = resolutionContext.resolveVariableDeclaration(varName, children[0].returnType(), kind == Kind.CONST);
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    Object value = children[0].eval(evaluationContext);
    resolved.set(evaluationContext, value);
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, kind + " " + varName + " = ", errors);
    children[0].toString(asb, errors, preferAscii);
  }
}
