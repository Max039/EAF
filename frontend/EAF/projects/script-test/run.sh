#!/bin/sh
export DISPLAY=localhost:0.0
export EVOAL_HOME=$( cd -- "$(dirname $0)/../../builds/20240831-083433/evoal" >/dev/null 2>&1 ; pwd -P )
# Print the current working directory for debugging
echo "Current working directory in second script: $(pwd)"
echo "EVOAL_HOME in second script: $EVOAL_HOME"
export EVOAL_JVM_ARGUMENTS="--add-opens=de.evoal.optimisation.ea/de.evoal.optimisation.ea.main.codec.vector.chromosome=de.test --add-exports=de.evoal.optimisation.ea/de.evoal.optimisation.ea.main.codec.vector.chromosome=de.test --add-opens=de.evoal.generator.main/de.evoal.generator.api=de.test --add-exports=de.evoal.generator.main/de.evoal.generator.api=de.test --add-opens=de.evoal.optimisation.api/de.evoal.optimisation.api.model=de.test --add-exports=de.evoal.optimisation.api/de.evoal.optimisation.api.model=de.test --add-opens=de.evoal.optimisation.api/de.evoal.optimisation.api.statistics=de.test --add-exports=de.evoal.optimisation.api/de.evoal.optimisation.api.statistics=de.test --add-opens=de.evoal.optimisation.ea/de.evoal.optimisation.ea.main.search=de.test --add-exports=de.evoal.optimisation.ea/de.evoal.optimisation.ea.main.search=de.test --add-opens=de.evoal.optimisation.ea/de.evoal.optimisation.ea.api.operators=de.test --add-exports=de.evoal.optimisation.ea/de.evoal.optimisation.ea.api.operators=de.test --add-opens=de.evoal.core.main/de.evoal.core.api.properties=de.test --add-exports=de.evoal.core.main/de.evoal.core.api.properties=de.test --add-opens=de.evoal.optimisation.ea/de.evoal.optimisation.ea.main.codec.vector.chromosome=de.eaf --add-exports=de.evoal.optimisation.ea/de.evoal.optimisation.ea.main.codec.vector.chromosome=de.eaf --add-opens=de.evoal.generator.main/de.evoal.generator.api=de.eaf --add-exports=de.evoal.generator.main/de.evoal.generator.api=de.eaf --add-opens=de.evoal.optimisation.api/de.evoal.optimisation.api.model=de.eaf --add-exports=de.evoal.optimisation.api/de.evoal.optimisation.api.model=de.eaf --add-opens=de.evoal.optimisation.api/de.evoal.optimisation.api.statistics=de.eaf --add-exports=de.evoal.optimisation.api/de.evoal.optimisation.api.statistics=de.eaf --add-opens=de.evoal.optimisation.ea/de.evoal.optimisation.ea.main.search=de.eaf --add-exports=de.evoal.optimisation.ea/de.evoal.optimisation.ea.main.search=de.eaf --add-opens=de.evoal.optimisation.ea/de.evoal.optimisation.ea.api.operators=de.eaf --add-exports=de.evoal.optimisation.ea/de.evoal.optimisation.ea.api.operators=de.eaf --add-opens=de.evoal.core.main/de.evoal.core.api.properties=de.eaf --add-exports=de.evoal.core.main/de.evoal.core.api.properties=de.eaf"
# Get the directory of the currently executed script
SCRIPT_DIR=$(dirname "$(readlink -f "$0")")
# Change the working directory to the script's directory
cd "$SCRIPT_DIR"
$SHELL $EVOAL_HOME/bin/evoal-search.sh . config.ol output
