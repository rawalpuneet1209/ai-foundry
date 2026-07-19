package com.aifoundry.ai.gateway;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ArchitectureRulesTest {
  private final com.tngtech.archunit.core.domain.JavaClasses classes =
      new ClassFileImporter().importPackages("com.aifoundry");

  @Test
  void domainIsFrameworkFree() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("org.springframework..", "jakarta.servlet..")
        .check(classes);
  }

  @Test
  void spiDoesNotDependOnSpringAi() {
    noClasses()
        .that()
        .resideInAPackage("..provider.spi..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("org.springframework.ai..")
        .check(classes);
  }

  @Test
  void applicationDoesNotDependOnGateway() {
    noClasses()
        .that()
        .resideInAPackage("..application..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..gateway..")
        .check(classes);
  }

  @Test
  void controllersDoNotAccessAdapter() {
    noClasses()
        .that()
        .resideInAPackage("..gateway.api..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..provider.springai..")
        .check(classes);
  }
}
