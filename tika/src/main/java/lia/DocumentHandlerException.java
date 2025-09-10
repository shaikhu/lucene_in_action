package lia;

public class DocumentHandlerException extends Exception {
  public DocumentHandlerException() {
    super();
  }

  public DocumentHandlerException(String message) {
    super(message);
  }

  public DocumentHandlerException(Throwable cause) {
    super(cause);
  }

  public DocumentHandlerException(String message, Throwable cause) {
    super(message, cause);
  }
}
