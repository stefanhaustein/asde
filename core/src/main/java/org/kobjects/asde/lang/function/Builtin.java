package org.kobjects.asde.lang.function;

import org.kobjects.markdown.AnnotatedString;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.expression.Literal;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

/**
 * Note: functions needing
 */
public enum Builtin implements Callable {
  ABS("Calculates the absolute value of the input.\n\nExamples:\n\n * abs(3.4) = 3.4\n * abs(-4) = 4\n * abs(0) = 0",
      Types.FLOAT, Types.FLOAT),
  BIN("Convert an number to a binary integer string prefixed with '0b'.", Types.STR, Types.FLOAT),
  ATAN2("Converts the given cartesian coordinates into the angle of the corresponding polar coordinates",
      Types.FLOAT, Types.FLOAT, Types.FLOAT),
  CEIL("Rounds up to the next higher integer",
      Types.FLOAT, Types.FLOAT),
  CHR("Returns a  string representing the given unicode code point.\n\nExample:\n\n * chr(65) = \"A\"",
      Types.STR, Types.FLOAT),
  COS("Calculates the cosine of the parameter value.",
      Types.FLOAT, Types.FLOAT),
  EXP("Returns e raised to the power of the parameter value.",
      Types.FLOAT, Types.FLOAT),
  FLOAT("Parses the argument as a floating point number. If this fails, the return value is 0.",
      Types.FLOAT, Types.STR),
  FLOOR("Rounds down to the next lower integer", Types.FLOAT, Types.FLOAT),
  HEX("Convert a number to a lowercase hexadecimal integer prefixed with 0x.", Types.STR, Types.FLOAT),
  INT("Rounds down to the next lower integer",
    Types.FLOAT, Types.FLOAT),
  LEN("Returns the length of the given string.\n\nExample:\n\n * len(\"ABC\") = 3",
        Types.FLOAT, Types.STR),
  LOG("Calculates the logarithm to the base e.",
        Types.FLOAT, Types.FLOAT),
  OCT("Convert the given number to an octal integer string prfixed with '0o'.", Types.STR, Types.FLOAT),
  ORD("Returns the code point value of the first character of the string\n\nExample:\n\n * ord(\"A\") = 65.",
      Types.FLOAT, Types.STR),
  RANGE(
      "Returns a sequence of integers from the first parameter (inclusive) to the second parameter (exclusive)",
      new ListType(Types.FLOAT),
      Parameter.create("start", Types.FLOAT),
      Parameter.create("end", new Literal(Double.NaN)),
      Parameter.create("step", new Literal(1.0))),
  RANDOM("Returns a (pseudo-)random number in the range from 0 (inclusive) to 1 (exclusive)",
        Types.FLOAT, Parameter.EMPTY_ARRAY),
  STR("Converts the given number to a string (similar to print).",
        Types.STR, Types.FLOAT),
  SQRT("Calculates the square root of the argument\n\nExample:\n\n * sqr(9) = 3",
        Types.FLOAT, Types.FLOAT),
  SIN("Calculates the sine of the parameter value.", Types.FLOAT, Types.FLOAT),
  TAN("Calculates the tangent of the argument", Types.FLOAT, Types.FLOAT);

  private static Parameter[] typesToParams(Type[] parameterTypes) {
    Parameter[] parameters = new Parameter[parameterTypes.length];
    for (int i = 0; i < parameters.length; i++) {
      parameters[i] = Parameter.create("" + ((char) (i + 'a')), parameterTypes[i]);
    }
    return parameters;
  }
  private static int len(String s) {
    int pos = 0;
    int count = 0;
    int len = s.length();
    while (pos < len) {
      int cp = Character.codePointAt(s, pos);
      pos += Character.charCount(cp);
      count++;
    }
    return count;
  }

  private static String mid(String s, int start) {
    int pos = 0;
    int len = s.length();
    for (int i = 1; i < start && pos < len; i++) {
      int cp = Character.codePointAt(s, pos);
      pos += Character.charCount(cp);
    }
    return pos >= len ? "" : s.substring(pos);
  }

  public FunctionType signature;
  private final AnnotatedString documentation;

    Builtin(String documentation, Type returnType, Parameter... parameters) {
      this.documentation = AnnotatedString.of(documentation);
      this.signature = new FunctionType(returnType, parameters);
    }

  Builtin(String documentation, Type returnType, Type... parameterTypes) {
    this(documentation, returnType, typesToParams(parameterTypes));
  }


  @Override
  public FunctionType getType() {
    return signature;
  }

  public Object call(EvaluationContext evaluationContext, int paramCount) {
    switch (this) {
      case ABS:
        return Math.abs((Double) evaluationContext.getParameter(0));
      case ATAN2:
        return Math.atan2((Double) evaluationContext.getParameter(0), (Double) evaluationContext.getParameter(1));
      case BIN:
        return "0b" + Long.toBinaryString(((Double) evaluationContext.getParameter(0)).longValue());
      case CEIL:
        return Math.ceil((Double) evaluationContext.getParameter(0));
      case CHR:
        return String.valueOf(Character.toChars(((Double) (evaluationContext.getParameter(0))).intValue()));
      case COS:
        return Math.cos((Double) evaluationContext.getParameter(0));
      case EXP:
        return Math.exp((Double) evaluationContext.getParameter(0));
      case FLOAT:
        return Double.parseDouble((String) evaluationContext.getParameter(0));
      case FLOOR:
        return Math.floor((Double) evaluationContext.getParameter(0));
      case HEX:
        return "0x" + Long.toHexString(((Double) evaluationContext.getParameter(0)).longValue());
      case INT:
        return Double.valueOf(((Double) evaluationContext.getParameter(0)).longValue());
      case LEN:
        return Double.valueOf(len((String) evaluationContext.getParameter(0)));
      case LOG:
        return Math.log((Double) evaluationContext.getParameter(0));
      case OCT:
        return "0o" + Long.toOctalString(((Double) evaluationContext.getParameter(0)).longValue());
      case ORD: {
        String s = (String) evaluationContext.getParameter(0);
        return s.length() == 0 ? 0.0 : (double) Character.codePointAt(s, 0);
      }
      case RANGE: {
        double start = (Double) evaluationContext.getParameter(0);
        double end = (Double) evaluationContext.getParameter(1);
        double step = (Double) evaluationContext.getParameter(2);
        if (Double.isNaN(end)) {
          end = start;
          start = 0;
        }
        Object[] values = new Object[(int) ((end - start) / step)];
        for (int i = 0; i < values.length; i++) {
          values[i] = Double.valueOf(start + i * step);
        }
        return new ListImpl(Types.FLOAT, values);
      }
      case RANDOM:
        return Math.random();
      case SIN:
        return Math.sin((Double) evaluationContext.getParameter(0));
      case SQRT:
        return Math.sqrt((Double) evaluationContext.getParameter(0));
      case STR:
        return Program.toString(evaluationContext.getParameter(0));
      case TAN:
        return Math.tan((Double) evaluationContext.getParameter(0));
      default:
        throw new IllegalArgumentException("NYI: " + name());
    }
  }

  @Override
  public AnnotatedString getDocumentation() {
    return documentation;
  }

  @Override
  public Property getDeclaringSymbol() {
    return null;
  }
}
