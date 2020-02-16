package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.function.Function;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

// Not static for access to the variables.
public class InvokeMethod extends Node {

  public String name;
  Property resolvedProperty;

  public InvokeMethod(String name, Node... children) {
    super(children);
    this.name = name;
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
  protected void onResolve(FunctionValidationContext resolutionContext, int line) {
    if (!(children[0].returnType() instanceof Classifier)) {
      throw new RuntimeException("InstanceType base expected");
    }
    resolvedProperty = ((Classifier) children[0].returnType()).getPropertyDescriptor(name);
    if (resolvedProperty == null) {
      throw new RuntimeException("Property '" + name + "' not found in " + children[0].returnType());
    }
    if (!(resolvedProperty.getType() instanceof FunctionType)) {
      throw new RuntimeException("Type of property '" + resolvedProperty + "' is not callable.");
    }

    FunctionType resolved = (FunctionType) resolvedProperty.getType() ;
    // TODO: b/c optional params, add minParameterCount
    if (children.length  > resolved.getParameterCount() || children.length < resolved.getMinParameterCount()) {
      throw new RuntimeException("Expected parameter count is "
          + resolved.getMinParameterCount() + ".."
          + resolved.getParameterCount() + " but got " + (children.length ) + " for " + this);
    }
    for (int i = 0; i < children.length; i++) {
      if (!resolved.getParameterType(i).isAssignableFrom(children[i].returnType())) {
        throw new RuntimeException("Type mismatch for parameter " + i + ": expected: "
            + resolved.getParameterType(i) + " actual: " + children[i].returnType() + " base type: " + resolved);
      }
    }
  }

  public Object eval(EvaluationContext evaluationContext) {
    Object base = children[0].eval(evaluationContext);
    Function function = (Function) resolvedProperty.get(evaluationContext, base);
    evaluationContext.ensureExtraStackSpace(function.getLocalVariableCount());
    if (children.length > function.getLocalVariableCount()) {
      throw new RuntimeException("Too many params for " + function);
     }
    // Push is important here, as parameter evaluation might also run apply().
    evaluationContext.push(base);
    for (int i = 1; i < children.length; i++) {
       evaluationContext.push(children[i].eval(evaluationContext));
     }
     evaluationContext.popN(children.length);
     try {
       return function.call(evaluationContext, children.length);
     } catch (Exception e) {
       throw new RuntimeException(e.getMessage() + " in " + children[0], e);
     }
  }

  // Shouldn't throw, as it's used outside validation!
  public Type returnType() {
    return ((FunctionType) resolvedProperty.getType()).getReturnType();
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    int start = asb.length();
    children[0].toString(asb, errors, preferAscii);
    asb.append(".");
    appendLinked(asb, name, errors);
    asb.append("(");
    for (int i = 1; i < children.length; i++) {
      if (i > 1) {
        asb.append(", ");
      }
      children[i].toString(asb, errors, preferAscii);
    }
    asb.append(")");
    asb.annotate(start, asb.length(), errors.get(this));
  }
}