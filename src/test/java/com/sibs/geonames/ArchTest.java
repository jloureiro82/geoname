package com.sibs.geonames;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ArchTest {

    @Test
    void servicesAndRepositoriesShouldNotDependOnWebLayer() {
        JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.sibs.geonames");

        noClasses()
            .that()
            .resideInAnyPackage("com.sibs.geonames.service..")
            .or()
            .resideInAnyPackage("com.sibs.geonames.repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..com.sibs.geonames.web..")
            .because("Services and repositories should not depend on web layer")
            .check(importedClasses);
    }
}
