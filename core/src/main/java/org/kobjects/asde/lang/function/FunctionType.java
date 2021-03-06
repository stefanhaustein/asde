package org.kobjects.asde.lang.function;

import org.kobjects.asde.lang.type.AwaitableType;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

public class FunctionType implements Type {

  public static FunctionType createFromTypes(Type returnType, Type... parameterTypes) {
    Parameter[] parameters = new Parameter[parameterTypes.length];
    for (int i = 0; i < parameters.length; i++) {
      parameters[i] = Parameter.create(String.valueOf(((char) ('a' + i))), parameterTypes[i]);
    }
    return new FunctionType(returnType, parameters);
  }

  private final Type returnType;
  private Parameter[] parameters;

  public FunctionType(Type returnType, Parameter... parameters) {
    this.returnType = returnType;
    this.parameters = parameters;
  }


  public Type getReturnType() {
    return returnType;
  }

  public Type getParameterType(int index) {
    return parameters[index].getExplicitType();
  }

  public int getParameterCount() {
    return parameters.length;
  }

  public String toString() {
    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
    toString(asb, null);
    return asb.toString();
  }

  public void toString(AnnotatedStringBuilder asb, String functionName) {
    Type returnType = getReturnType();
    if (returnType instanceof AwaitableType) {
      asb.append("async ");
      returnType = ((AwaitableType) returnType).getWrapped();
    }
    if (functionName != null && !functionName.isEmpty()) {
      asb.append(functionName);
      asb.append(' ');
    }
    asb.append('(');
    for (int i = 0; i < parameters.length; i++) {
      if (i > 0) {
        asb.append(", ");
      }
      asb.append(parameters[i].toString());
    }
    asb.append(')');
    if (returnType != Types.VOID) {
      asb.append(" -> ");
      asb.append(returnType.toString());
    }
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }

  @Override
  public boolean hasDefaultValue() {
    return false;
  }

  @Override
  public Object getDefaultValue() {
    throw new UnsupportedOperationException();
  }


  @Override
  public boolean equals(Object o) {
    if (!(o instanceof FunctionType)) {
      System.out.println(this + " does not match " + o+ ": not a function type");
      return false;
    }
    FunctionType other = (FunctionType) o;
    return equals(other, false);
  }


  public boolean equals(FunctionType other, boolean skipSelfType) {
    if (!returnType.equals(other.getReturnType())) {
      System.out.println(this + " does not match " + other+ ": return type mismatch");
      return false;
    }
    if (getParameterCount() != other.getParameterCount()) {
      System.out.println(this + " does not match " + other+ ": parameter count mismatch");
      return false;
    }
    for (int i = skipSelfType ? 1 : 0; i < getParameterCount(); i++) {
      if (!getParameterType(i).equals(other.getParameterType(i))) {
        return false;
      }
    }

    return true;
  }

  public Parameter getParameter(int i) {
    return parameters[i];
  }

  public int getParameterIndex(String name) {
    for (int i = 0; i < parameters.length; i++) {
      if (name.equals(parameters[i].getName())) {
        return i;
      }
    }
    return -1;
  }

}
