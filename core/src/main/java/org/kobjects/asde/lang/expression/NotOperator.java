package org.kobjects.asde.lang.expression;

import org.kobjects.asde.lang.wasm.Wasm;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class NotOperator extends ExpressionNode {

  public NotOperator(ExpressionNode child) {
    super(child);
  }

  @Override
  protected Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
    children[0].resolveWasm(wasm, resolutionContext, line, Types.BOOL);
    wasm.opCode(Wasm.I32_EQZ);
    return Types.BOOL;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb,"not ", errors);
    children[0].toString(asb, errors, preferAscii);
  }
}
