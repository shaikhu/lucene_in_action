package lia;

import java.io.PrintStream;
import java.io.PrintWriter;

public class DocumentHandlerException extends Exception {
  private Throwable cause;

  public DocumentHandlerException() {
    super();
  }

  public DocumentHandlerException(String message) {
    super(message);
  }

  public DocumentHandlerException(Throwable cause) {
    super(cause.toString());
    this.cause = cause;
  }

  public DocumentHandlerException(String message, Throwable cause) {
    super(message, cause);
  }

  public Throwable getException() {
    return cause;
  }

  public void printStackTrace() {
    printStackTrace(System.err);
  }

  public void printStackTrace(PrintStream printStream) {
    synchronized (printStream) {
      super.printStackTrace(printStream);
      if (cause != null) {
        printStream.println("--- Nested Exception ---");
        cause.printStackTrace(printStream);
      }
    }
  }

  public void printStackTrace(PrintWriter printWriter) {
    synchronized (printWriter) {
      super.printStackTrace(printWriter);
      if (cause != null) {
        printWriter.println("--- Nested Exception ---");
        cause.printStackTrace(printWriter);
      }
    }
  }
}
