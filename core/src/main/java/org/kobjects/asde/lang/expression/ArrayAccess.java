package org.kobjects.asde.lang.expression;

import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.expressionparser.Tokenizer;
import org.kobjects.markdown.AnnotatedStringBuilder;

import java.util.Map;

/**
 * Note that slicing is handled separately, as it can be identified at compile time.
 */
public class ArrayAccess extends AssignableWasmNode {

  enum Kind {
    UNRESOLVED, ARRAY_ACCESS, QUALIFIED_TYPE, LIST_CONSTRUCTOR, STRING_ACCESS, ERROR
  }

  Kind kind = Kind.UNRESOLVED;
  Type resolvedElementType;

  public ArrayAccess(ExpressionNode... children) {
    super(children);
  }

  @Override
  public Type resolveForAssignment(WasmExpressionBuilder wasm, ValidationContext validationContext, int line) {
    return resolveWasmImpl(wasm, validationContext, line, /* forSet= */ true);
  }

  private Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line, boolean forSet) {
    kind = Kind.ERROR;

    if (children[0].toString().equals("List")) {
      if (forSet) {
        throw new RuntimeException("Assignment to list types not supported");
      }
      // The reason for parsing the type is the ambiguity of float (conversion method vs. type).
      kind = Kind.QUALIFIED_TYPE;
      Tokenizer tokenizer = resolutionContext.program.parser.createTokenizer(toString());
      tokenizer.nextToken();
      resolvedElementType = resolutionContext.program.parser.parseType(tokenizer);
      wasm.objConst(resolvedElementType);
      return new MetaType(resolvedElementType);
    }

    Type type0 = children[0].resolveWasm(new WasmExpressionBuilder(), resolutionContext, line);

    if (type0 instanceof ListType || type0 == Types.STR) {
      // For real...
      children[0].resolveWasm(wasm, resolutionContext, line);
      kind = type0 == Types.STR ? Kind.STRING_ACCESS : Kind.ARRAY_ACCESS;
      if (children.length != 2) {
        throw new RuntimeException("Exactly one array index argument expected");
      }
      Type type1 = children[1].resolveWasm(wasm, resolutionContext, line);
      if (type1 != Types.FLOAT) {
        throw new RuntimeException("Number argument expected for array access; got: " + type1);
      }

      if (type0 == Types.STR) {
        if (forSet) {
          throw new RuntimeException("Assignment to substrings not supported");
        }
        kind = Kind.STRING_ACCESS;
        wasm.callWithContext(context -> {
          int index = context.dataStack.popI32();
          String s = (String) context.dataStack.popObject();
          if (index < 0) {
            index = s.codePointCount(0, s.length()) - index;
          }
          int pos = 0;
          for (int i = 0; i < index; i++) {
            pos += Character.charCount(s.codePointAt(pos));
          }
          context.dataStack.pushObject(s.substring(pos, pos + Character.charCount(pos)));
        });
        return Types.STR;
      }
      kind = Kind.ARRAY_ACCESS;
      if (forSet) {
        wasm.callWithContext(context -> {
          int index = context.dataStack.popI32();
          ListImpl array = (ListImpl) context.dataStack.popObject();
          array.setValueAt(context.dataStack.popObject(), index < 0 ? array.length() - index : index);
        });
      } else {
        wasm.callWithContext(context -> {
          int index = context.dataStack.popI32();
          ListImpl array = (ListImpl) context.dataStack.popObject();
          context.dataStack.pushObject(array.get(index < 0 ? array.length() - index : index));
        });
      }
      return ((ListType) type0).elementType;
    }

    if (type0 instanceof MetaType) {
      if (forSet) {
        throw new RuntimeException("Assignment to list constructors not supported");
      }
      kind = Kind.LIST_CONSTRUCTOR;
      Type inner = ((MetaType) type0).getWrapped();
      if (!(inner instanceof ListType)) {
        throw new RuntimeException("List type expected for list constructor");
      }
      resolvedElementType = ((ListType) inner).elementType;
      final int count = children.length - 1;
      for (int i = 1; i < count; i++) {
        Type actualType = children[i + 1].resolveWasm(wasm, resolutionContext, line);
        TraitCast.autoCastWasm(wasm, actualType, resolvedElementType, resolutionContext);
      }
      wasm.callWithContext(context -> {
        Object[] array = new Object[count];
        for (int i = count - 1; i >= 0; i--) {
          array[i] = context.dataStack.popObject();
        }
        context.dataStack.pushObject(new ListImpl(resolvedElementType, array));
      });
      return new ListType(resolvedElementType);
    }

    throw new RuntimeException("Not a list: " + children[0] + " (" + type0 + ") -- this: " + this);
  }

  @Override
  protected Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
    return resolveWasmImpl(wasm, resolutionContext, line, /* forSet= */ false);
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    int start = asb.length();
    children[0].toString(asb, errors, preferAscii);
    asb.append('[');
    for (int i = 1; i < children.length; i++) {
      if (i > 1) {
        asb.append(", ");
      }
      children[i].toString(asb, errors, preferAscii);
    }
    asb.append(']');
    asb.annotate(start, asb.length(), errors.get(this));
  }
}
