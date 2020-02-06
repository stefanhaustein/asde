package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;


public class DeclarationStatement extends AbstractDeclarationStatement {

  public enum Kind {
    VAR, CONST
  }

  public final Kind kind;

  public DeclarationStatement(Kind kind, String varName, Node init) {
    super(varName, init);
    this.kind = kind;
  }

  public void onResolve(FunctionValidationContext resolutionContext, int line) {
    resolved = resolutionContext.resolveVariableDeclaration(varName, children[0].returnType(), kind == Kind.CONST);
  }

  @Override
  public Object evalValue(EvaluationContext evaluationContext) {
    return children[0].eval(evaluationContext);
  }

  @Override
  public Type getValueType() {
    return children[0].returnType();
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    asb.append(kind.name().toLowerCase(), SyntaxColor.KEYWORD);
    appendLinked(asb, " " + varName + " = ", errors);
    children[0].toString(asb, errors, preferAscii);
  }
}
