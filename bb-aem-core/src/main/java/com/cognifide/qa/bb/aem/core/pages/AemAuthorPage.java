/*-
 * #%L
 * Bobcat
 * %%
 * Copyright (C) 2018 Cognifide Ltd.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.cognifide.qa.bb.aem.core.pages;

import java.util.List;

import javax.inject.Named;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.cognifide.qa.bb.aem.core.component.GlobalBar;
import com.cognifide.qa.bb.frame.FrameSwitcher;
import com.cognifide.qa.bb.mapper.field.PageObjectProviderHelper;
import com.cognifide.qa.bb.page.Page;
import com.cognifide.qa.bb.qualifier.PageObjectInterface;
import com.cognifide.qa.bb.utils.PageObjectInjector;
import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.internal.LinkedBindingImpl;

import io.qameta.allure.Step;

/**
 * Abstract class that marks page as being from AEM
 */
public class AemAuthorPage<T extends AemAuthorPage> extends Page {

  private static final String CONTENT_FRAME = "ContentFrame";

  @Inject
  private GlobalBar globalBar;

  @Inject
  private FrameSwitcher frameSwitcher;

  @Inject
  private PageObjectInjector pageObjectInjector;

  @Inject
  private WebDriver driver;

  @Inject
  @Named("author.url")
  protected String authorUrl;

  public <T> T getContent(Class<T> component, int order) {
    globalBar.switchToPreviewMode();
    frameSwitcher.switchTo(CONTENT_FRAME);
    By selector = getSelectorFromComponent(component);
    List<WebElement> scope = driver.findElements(selector);
    frameSwitcher.switchBack();
    return scope == null
        ? pageObjectInjector.inject(component, CONTENT_FRAME)
        : pageObjectInjector.inject(component, scope.get(order), CONTENT_FRAME);
  }

  /**
   * open the page in browser
   */
  @Step("Open page")
  @Override
  public T open() {
    webDriver.get(authorUrl + getFullUrl());
    return (T) this;
  }

  @Step("Open page in editor")
  public T openInEditor() {
    webDriver.get(authorUrl + "/editor.html" + getFullUrl());
    return (T) this;
  }

  private By getSelectorFromComponent(Class component) {
    By selector = null;
    if (component.isAnnotationPresent(
        PageObjectInterface.class)) {
      Binding<?> binding = pageObjectInjector.getOriginalInjector().getBinding(component);
      if (binding instanceof LinkedBindingImpl) {
        selector = PageObjectProviderHelper
            .retrieveSelectorFromPageObjectInterface(
                ((LinkedBindingImpl) binding).getLinkedKey().getTypeLiteral().getRawType());
      }
    } else {
      selector = PageObjectProviderHelper.retrieveSelectorFromPageObjectInterface(component);
    }
    return selector;
  }
}
