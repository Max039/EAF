package de.eaf.statistics;

import de.evoal.languages.model.base.Instance;
import de.evoal.optimisation.api.statistics.IterationResult;
import de.evoal.optimisation.api.statistics.writer.StatisticsWriter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.Dependent;
import javax.inject.Named;


/**
 * A class that extends the shell version to execute extra code. In this case its use to process each generation.
 */
@Slf4j
@Dependent
@Named("de.eaf.statistics.hook.eaf-hook")
public class Hook implements StatisticsWriter  {

    @Override
    public StatisticsWriter init(final Instance config) {
        try {
            StatisticsWriter.super.init(config);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    };

    @Override
    public void add(IterationResult iterationResult){
        System.out.println(iterationResult);
    };

    /**
     * Writes the output
     */
    @Override
    public void write() {

    };


}
