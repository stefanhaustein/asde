package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Builtin;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class Operator extends Node {
  public final String name;

  public Operator(String name, Node... children) {
    super(children);
    this.name = name;
  }

  public Object eval(Interpreter interpreter) {
    Object lVal = children[0].eval(interpreter);

    if (children.length == 1) {
      switch (name.charAt(0)) {
        case '-': return -(Double) lVal;
        case 'N':
        case 'n': return Double.valueOf(~(Builtin.asInt(lVal)));
        default:
          throw new RuntimeException("Unsupported unary operator: " + name);
      }
    }

    Object rVal = children[1].eval(interpreter);
    boolean numbers = (lVal instanceof Double) && (rVal instanceof Double);
    if (!numbers) {
      lVal = String.valueOf(lVal);
      rVal = String.valueOf(rVal);
    }
    if ("<=>".indexOf(name.charAt(0)) != -1) {
      int cmp = (((Comparable) lVal).compareTo(rVal));
      return (cmp == 0 ? name.contains("=") : cmp < 0 ? name.contains("<") : name.contains(">"))
          ? -1.0 : 0.0;
    }
    if (!numbers) {
      if (!name.equals("+")) {
        throw new IllegalArgumentException("Numbers arguments expected for operator " + name);
      }
      return "" + lVal + rVal;
    }
    double l = (Double) lVal;
    double r = (Double) rVal;
    switch (name.charAt(0)) {
      case 'a':
        return Double.valueOf(((int) l) & ((int) r));
      case 'o':
        return Double.valueOf(((int) l) | ((int) r));
      case '^':
        return Math.pow(l, r);
      case '+':
        return l + r;
      case '-':
        return l - r;
      case '/':
        return l / r;
      case '*':
        return l * r;
      default:
        throw new RuntimeException("Unsupported binary operator " + name);
    }
  }

  @Override
  public Type returnType() {
    return (name.equals("+") && (children[0].returnType() == Types.STRING
        || children[1].returnType() == Types.STRING)) ? Types.STRING : Types.NUMBER;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    if (children.length == 1) {
      appendLinked(asb, name.equals("-") ? "-" : (name + " "), errors);
      children[0].toString(asb, errors);
    } else {
      children[0].toString(asb, errors);
      appendLinked(asb, ' ' + name + ' ', errors);
      children[1].toString(asb, errors);
    }
  }
}
