package at.ac.uibk.timeguess.flipflapp.term;

public class TermNotFoundException extends RuntimeException {

  public TermNotFoundException(Long termId) {
    super("The term with id %d was not found.".formatted(termId));
  }

}