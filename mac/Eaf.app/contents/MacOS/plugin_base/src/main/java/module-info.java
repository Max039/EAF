module #module# {
    // declare dependencies to evoal
    requires de.evoal.core.main;
    requires de.evoal.optimisation.api;
    requires de.evoal.languages.model.ddl;

    // CDI related dependencies
    requires jakarta.enterprise.cdi.api;
    requires jakarta.inject.api;

    // logging
    requires org.slf4j;
    requires de.evoal.languages.model.generator;
    requires de.evoal.languages.model.base;
    requires lombok;

    // jenetics
    requires io.jenetics.base;

    //Csv
    requires commons.csv;

}