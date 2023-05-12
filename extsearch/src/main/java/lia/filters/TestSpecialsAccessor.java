package lia.filters;

import java.util.List;

public class TestSpecialsAccessor implements SpecialsAccessor {
  private final List<String> isbns;

  public TestSpecialsAccessor(List<String> isbns) {
    this.isbns = isbns;
  }

  @Override
  public List<String> getIsbns() {
    return isbns;
  }
}
