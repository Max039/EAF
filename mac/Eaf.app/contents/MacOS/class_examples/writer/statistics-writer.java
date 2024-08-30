package de.eaf.statistics;

import de.evoal.languages.model.base.Instance;
import de.evoal.optimisation.api.statistics.IterationResult;
import de.evoal.optimisation.api.statistics.writer.StatisticsWriter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import java.io.PrintWriter;

/**
 * A class that extends the shell version to execute extra code. In this case its use to process each generation.
 */
@Slf4j
@Dependent
@Named(#package#.#name#)
public class #name# implements StatisticsWriter  {

    @Override
    public StatisticsWriter init(final Instance config) {
        try {
            StatisticsWriter.super.init(config);
            #variables#
			
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }
    @Override
    public void add(IterationResult iterationResult){
     
    };

    @Override
    public void write() {

    };


}