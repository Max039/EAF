package de.eaf.statistics;

import de.evoal.languages.model.base.Instance;
import de.evoal.optimisation.api.statistics.IterationResult;
import de.evoal.optimisation.api.statistics.writer.StatisticsWriter;

import javax.enterprise.context.Dependent;
import javax.inject.Named;


/**
 * A class that extends the shell version to execute extra code. In this case its use to process each generation.
 */
@Dependent
@Named("eaf-hook")
public class hook implements StatisticsWriter  {



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
