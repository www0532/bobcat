package com.cognifide.qa.bb.mapper.field;

import static com.cognifide.qa.bb.mapper.field.PageObjectProviderHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import com.cognifide.qa.bb.qualifier.FindPageObject;
import com.cognifide.qa.bb.qualifier.PageObject;
import com.cognifide.qa.bb.qualifier.PageObjectInterface;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

class PageObjectProviderHelperTest {

  @Nested
  @DisplayName("Given a class with PageObject annotation...")
  class GivenClass {

    @Test
    @DisplayName("can retrieve XPath locator when specified")
    void xpathLocatorFromPageObjectAnnotation() {
      Optional<By> selector = getSelectorFromPageObjectClass(PageObjectXpath.class);

      assertThat(selector).hasValue(By.xpath("//div"));
    }

    @Test
    @DisplayName("can retrieve CSS locator when specified")
    void cssLocatorFromPageObjectAnnotation() {
      Optional<By> selector = getSelectorFromPageObjectClass(PageObjectCss.class);

      assertThat(selector).hasValue(By.cssSelector("div"));
    }

    @Test
    @DisplayName("throws exception when both locators are specified")
    void throwsExceptionWhenBothSelectorsAreSpecifiedInAnnotation() {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> getSelectorFromPageObjectClass(PageObjectInvalid.class));
    }

    @Test
    @DisplayName("returns an empty Optional when no locator is specified")
    void emptyOptionalWhenNoLocatorIsSpecified() {
      Optional<By> selector = getSelectorFromPageObjectClass(NoLocator.class);

      assertThat(selector).isEmpty();
    }

  }


  @Nested
  @DisplayName("Given a field which type has PageObject annotation...")
  class GivenField {
    Field field = ExamplePO.class.getDeclaredField("xpath");

    GivenField() throws NoSuchFieldException {
    }

    @Test
    @DisplayName("can retrieve selector when specified in annotation")
    void selectorFromField() {
      assertThat(getSelectorFromPageObject(field)).hasValue(By.xpath("//div"));
    }

    @Test
    @DisplayName("can retrieve selector when using getSelectorFromPageObjectField()")
    void canRetrieveSelectorFromFieldUsingGetSelectorFromPageObjectField() {
      assertThat(getSelectorFromPageObjectField(field, null)).hasValue(By.xpath("//div"));
    }
  }


  @Nested
  @DisplayName("Given a parameterized field which parameter's type has PageObject annotation...")
  class GivenParametrizedField {
    @Test
    @DisplayName("can retrieve selector from annotation on parameter's type")
    void canRetrieveSelectorFromParameterizedField() throws NoSuchFieldException {
      Field field = ExamplePO.class.getDeclaredField("cssList");

      assertThat(getSelectorFromGenericPageObject(field, null)).hasValue(By.cssSelector("div"));
    }
  }


  @Nested
  @DisplayName("Given a type implements PageObjectInterface...")
  class GivenPoInterface {
    @Test
    @DisplayName("can retrieve selector from a field having an implementation bound in provided injector")
    void canRetrieveSelectorFromFieldImplementingPoInterface()
        throws NoSuchFieldException {
      Field css = ExamplePO.class.getDeclaredField("cssInterface");
      Injector injector = Guice.createInjector(new PoModule());

      assertThat(getSelectorFromPageObjectField(css, injector)).hasValue(By.cssSelector("span"));
    }

    @Test
    @DisplayName("can retrieve selector from a parameterized field having an implementation bound in provided injector")
    void canRetrieveSelectorFromParameterizedFieldImplementingPoInterface()
        throws NoSuchFieldException {
      Field css = ExamplePO.class.getDeclaredField("cssInterfaceList");
      Injector injector = Guice.createInjector(new PoModule());

      assertThat(getSelectorFromGenericPageObject(css, injector)).hasValue(By.cssSelector("span"));
    }
  }


  @PageObject(xpath = "//div")
  private static class PageObjectXpath {
  }


  @PageObject(css = "div")
  private static class PageObjectCss {
  }


  @PageObject(css = "div", xpath = "//div")
  private static class PageObjectInvalid {
  }


  @PageObject
  private static class NoLocator {
  }


  @PageObject
  private static class ExamplePO {
    @FindPageObject
    PageObjectXpath xpath;

    @FindPageObject
    List<PageObjectCss> cssList;

    @FindPageObject
    PoInterface cssInterface;

    @FindPageObject
    List<PoInterface> cssInterfaceList;
  }


  @PageObjectInterface
  private interface PoInterface {

  }


  @PageObject(css = "span")
  private static class PoInterfaceImpl implements PoInterface {

  }


  private class PoModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(PoInterface.class).to(PoInterfaceImpl.class);
    }
  }

}
