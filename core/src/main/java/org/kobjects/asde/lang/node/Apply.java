package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.function.Function;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.util.Map;

// Not static for access to the variables.
public class Apply extends AssignableNode {

  private final boolean parenthesis;

  enum Kind {
    ARRAY_ACCESS, FUNCTION_EVALUATION, ERROR
  }

  private Kind resolvedKind = Kind.ERROR;

  public Apply(boolean parenthesis, Node... children) {
    super(children);
    this.parenthesis = parenthesis;
  }

  @Override
  public void changeSignature(StaticSymbol symbol, int[] newOrder) {
    Node base = children[0];
    if (!(base instanceof SymbolNode) || !((SymbolNode) base).matches(symbol, symbol.getName())) {
      return;
    }
    Node[] oldChildren = children;
    children = new Node[newOrder.length + 1];
    children[0] = base;
    for (int i = 0; i < newOrder.length; i++) {
      if (newOrder[i] != -1) {
        children[i + 1] = oldChildren[newOrder[i] + 1];
      } else {
        children[i + 1] = new Identifier("placeholder" + i);
      }
    }
  }


  // Resolves "base" node last, which allows identifiers to access parameter types
  public void resolve(FunctionValidationContext resolutionContext, Node parent, int line) {
    for (int i = 1; i < children.length; i++) {
      children[i].resolve(resolutionContext, this, line);
    }
    children[0].resolve(resolutionContext, this, line);
    try {
      onResolve(resolutionContext, parent, line);
    } catch (Exception e) {
      resolutionContext.addError(this, e);
    }
  }


  @Override
  public void resolveForAssignment(FunctionValidationContext resolutionContext, Node parent, Type type, int line) {
    resolve(resolutionContext, parent, line);

    if (!(children[0].returnType() instanceof ListType)) {
      throw new RuntimeException("Array expected");
    }

    if (!type.isAssignableFrom(returnType())) {
      throw new RuntimeException("Expected type for assignment: " + type + " actual type: " + returnType());
    }
  }


  public void set(EvaluationContext evaluationContext, Object value) {
    Object base = children[0].eval(evaluationContext);
    ListImpl array = (ListImpl) base;
    int[] indices = new int[children.length - 1];
    for (int i = 1; i < children.length; i++) {
      indices[i - 1] = children[i].evalInt(evaluationContext);
    }
    array.setValueAt(value, indices);
  }

  @Override
  public boolean isConstant() {
    return false;
  }

  @Override
  public boolean isAssignable() {
    return children[0].returnType() instanceof ListType;
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line) {
    if (children[0].returnType() instanceof ListType) {
      resolvedKind = Kind.ARRAY_ACCESS;
      for (int i = 1; i < children.length; i++) {
        if (children[i].returnType() != Types.FLOAT) {
          throw new RuntimeException("Number expected for paramter " + i + "; got: " + children[i].returnType());
        }
      }
    } else if (children[0].returnType() instanceof FunctionType) {
      resolvedKind = Kind.FUNCTION_EVALUATION;
      FunctionType resolved = (FunctionType) children[0].returnType();
      // TODO: b/c optional params, add minParameterCount
      if (children.length - 1 > resolved.getParameterCount() || children.length - 1 < resolved.getMinParameterCount()) {
        throw new RuntimeException("Expected parameter count is "
            + resolved.getMinParameterCount() + ".."
            + resolved.getParameterCount() + " but got " + (children.length - 1) + " for " + this);
      }
      for (int i = 0; i < children.length - 1; i++) {
        if (!resolved.getParameterType(i).isAssignableFrom(children[i+1].returnType())) {
          throw new RuntimeException("Type mismatch for parameter " + i + ": expected: "
              + resolved.getParameterType(i) + " actual: " + children[i+1].returnType() + " base type: " + resolved);
        }
      }
    } else {
      resolvedKind = Kind.ERROR;
      throw new RuntimeException("Can't apply parameters to " + children[0].returnType());
    }
  }

  public Object eval(EvaluationContext evaluationContext) {
    switch (resolvedKind) {
      case ARRAY_ACCESS:
        ListImpl array = (ListImpl) children[0].eval(evaluationContext);
        for (int i = 1; i < children.length - 1; i++) {
          array = (ListImpl) array.get(children[i].evalInt(evaluationContext));
        }
        return array.get(children[children.length-1].evalInt(evaluationContext));

      case FUNCTION_EVALUATION:
        Object base = children[0].eval(evaluationContext);
        if (!(base instanceof Function)) {
          throw new EvaluationException(this, "Can't apply parameters to " + base + " / " + children[0]);
        }
        Function function = (Function) base;
        evaluationContext.ensureExtraStackSpace(function.getLocalVariableCount());
        if (children.length - 1 > function.getLocalVariableCount()) {
          throw new RuntimeException("Too many params for " + function);
        }
        // Push is important here, as parameter evaluation might also run apply().
        for (int i = 1; i < children.length; i++) {
          evaluationContext.push(children[i].eval(evaluationContext));
        }
        evaluationContext.popN(children.length - 1);
        try {
          return function.call(evaluationContext, children.length - 1);
        } catch (Exception e) {
          throw new RuntimeException(e.getMessage() + " in " + children[0], e);
        }

      default:
        throw new IllegalStateException("Unresolved call: " + this);
    }
  }

  // Shouldn't throw, as it's used outside validation!
  public Type returnType() {
    switch (resolvedKind) {
      case ARRAY_ACCESS:
        return ((ListType) children[0].returnType()).getElementType(children.length - 2);
      case FUNCTION_EVALUATION:
        return ((FunctionType) children[0].returnType()).getReturnType();
      default:
        return null;

    }
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    int start = asb.length();
    children[0].toString(asb, errors, preferAscii);
    asb.append(parenthesis ? resolvedKind == Kind.ARRAY_ACCESS ? '[' : '(' : ' ');
    for (int i = 1; i < children.length; i++) {
      if (i > 1) {
        asb.append(", ");
      }
      children[i].toString(asb, errors, preferAscii);
    }
    if (parenthesis) {
      asb.append(resolvedKind == Kind.ARRAY_ACCESS ? ']' : ')');
    }
    asb.annotate(start, asb.length(), errors.get(this));
  }
}
