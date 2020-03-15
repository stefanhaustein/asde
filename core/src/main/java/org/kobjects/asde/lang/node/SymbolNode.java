package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.classifier.Property;

/**
 * Used for signature changes. Not needed for rename!
 */
public abstract class SymbolNode extends AssignableNode {

  SymbolNode(Node... children) {
    super(children);
  }

  /**
   * Null if N/A
   */
  public abstract Property getResolvedProperty();

}
