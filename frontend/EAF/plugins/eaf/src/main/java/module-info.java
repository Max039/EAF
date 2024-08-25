module de.eaf {
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

// open the package to CDI allowing CDI to create instances using reflection.
    // additionally, we have to open the folder to allow EvoAl to access the .dl file
    exports de.eaf.statistics;
    opens de.eaf.statistics;



}
