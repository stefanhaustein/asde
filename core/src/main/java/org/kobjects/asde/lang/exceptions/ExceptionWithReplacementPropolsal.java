package org.kobjects.asde.lang.exceptions;

public class ExceptionWithReplacementPropolsal extends RuntimeException {
  String[] replacementProposals;

  public ExceptionWithReplacementPropolsal(String message, String... replacementProposals) {
    super(message);
    this.replacementProposals = replacementProposals;
  }

}
