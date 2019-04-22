package org.kobjects.asde.demo;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.WrappedExecutionException;
import org.kobjects.asde.lang.io.Console;
import org.kobjects.asde.lang.io.ProgramReference;
import org.kobjects.asde.lang.io.Shell;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.expressionparser.ExpressionParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;

public class AsdeShell  {

  public static void main(String[] args) throws IOException {
    final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    final StdioConsole console = new StdioConsole(reader);
    final Program program = new Program(console);
    final Shell shell = new Shell(program);

    System.out.println("  **** EXPRESSION PARSER BASIC DEMO V1 ****\n");
    System.out.println("  " + (Runtime.getRuntime().totalMemory() / 1024) + "K SYSTEM  "
        + Runtime.getRuntime().freeMemory() + " BASIC BYTES FREE\n");

    boolean prompt = true;

    while (true) {
      if (prompt) {
        System.out.println("\nREADY.");
      }
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      prompt = true;
      try {
          shell.enter(line, program.mainSymbol, result -> {
              console.print(result == null ? "Ok" : String.valueOf(result));
          });
      } catch (ExpressionParser.ParsingException e) {
        char[] fill = new char[e.start + 1];
        Arrays.fill(fill, ' ');
        System.out.println(new String(fill) + '^');
        System.out.println("?SYNTAX ERROR: " + e.getMessage());
        program.lastException = e;
      } catch (WrappedExecutionException e) {
          System.out.println("\nERROR in " + e.lineNumber +  ": " + e.getMessage());
          System.out.println("\nREADY.");
          program.lastException = e;
      } catch (Exception e) {
        System.out.println("\nERROR in : " + e.getMessage());
        System.out.println("\nREADY.");
        program.lastException = e;
      }
    }
  }

  static class StdioConsole implements Console {

      private final BufferedReader reader;

      StdioConsole(BufferedReader reader) {
          this.reader = reader;
      }

      @Override
      public void print(CharSequence s) {
          System.out.print(s);
      }

      @Override
      public String input() {
          try {
              return reader.readLine();
          } catch (IOException e) {
              throw new RuntimeException(e);
          }
      }

      @Override
      public void clearOutput() {

      }

      @Override
      public void clearCanvas() {

      }


      @Override
      public void highlight(FunctionImplementation function, int lineNumber) {
          // TBD
      }

      @Override
      public InputStream openInputStream(String url) {
          throw new UnsupportedOperationException();
      }

      @Override
      public OutputStream openOutputStream(String url) {
          throw new UnsupportedOperationException();
      }


      @Override
      public ProgramReference nameToReference(String name) {
          return new ProgramReference(name, name, true);
      }

      @Override
      public void startProgress(String title) {

      }

      @Override
      public void updateProgress(String update) {

      }

      @Override
      public void endProgress() {

      }

      @Override
      public void delete(int line) {
          throw new UnsupportedOperationException();
      }

      @Override
      public void edit(int i) {
          throw new UnsupportedOperationException();
      }

      @Override
      public void showError(String message, Exception e) {
          print(message + e);
      }

    }
}