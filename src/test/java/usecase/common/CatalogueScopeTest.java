package usecase.common;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CatalogueScopeTest {
  @Test
  void requiresSessionAndDivision() {
    assertThrows(IllegalArgumentException.class, () -> new CatalogueScope(null, "ARTSC"));
    assertThrows(IllegalArgumentException.class, () -> new CatalogueScope("  ", "ARTSC"));
    assertThrows(IllegalArgumentException.class, () -> new CatalogueScope("20269", null));
    assertThrows(IllegalArgumentException.class, () -> new CatalogueScope("20269", "  "));
  }
}
