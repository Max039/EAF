import "definitions" from de.evoal.generator.generator;
import "definitions" from de.evoal.generator.optimisation;
import "definitions" from de.evoal.optimisation.core;
import "definitions" from de.evoal.optimisation.ea.mdo;
import "definitions" from de.evoal.optimisation.ea.optimisation;
import "definitions" from de.evoal.routenoptimierung.initials.emptyStringInitial;
import "definitions" from de.evoal.core.math

import "data" from 'config';

module 'config' {

  const real Test := 5;

  specify problem 'evolutionary-algorithm' {
    'initialisation' := 'empty-string-initial' {};
    'offspring-fraction' := 0.6;
    'comparator' := 'numeric-comparator' {};
    'size-of-population' := 55;
    'maximum-age' := 5;
    'handlers' := [
      'constraint-handler' {
        'calculation' := 'normal-calculation' {};
        'constraint-handling' := 'kill-at-birth' {
          'repair-strategy' := 'repair-with-random' {};
        };
        'category' := Test;
      }
    ];
    'optimisation-function' := 'fitness-distance' {
      'function' := 'benchmark-function' {
        'benchmarks' := [
          'benchmark-configuration' {
            'function' := 'ackley' {              'a' := 20.0;
              'b' := 0.2;
              'c' := 2*PI;
};
            'reads' := [
              data 'var'
            ];
            'writes' := [
              data 'var2'
            ];
          }
        ];
      };
      'target' := [
        'variable' {
          'val' := Test;
          'name' := data 'var';
        }
      ];
    };
    'alterers' := 'alterers' {
      'crossover' := [
        'correlation-line-crossover' {
          'probability' := 5;
          'position' := 6;
        }
      ];
      'mutator' := [
        'array-size-mutator' {
          'probability' := 5;
        }
      ];
    };
    'number-of-generations' := 60;
    'selectors' := 'selectors' {
      'offspring' := 'boltzmann-selector' {
        'beta' := 5;
      };
      'survivor' := 'elite-selector' {
        'size-factor' := 05;
        'non-elite-selector' := 'boltzmann-selector' {
          'beta' := 5;
        };
      };
    };
    'genotype' := 'vector-genotype' {
      'chromosomes' := [
        'bit-chromosome' {
          'genes' := [
            'gene' {
              'content' := data 'var2';
            }
          ];
          'scale' := 5;
        }
      ];
    };
  }


  configure problem 'ackley' for 'evolutionary-algorithm' {    'a' := 20.0;
    'b' := 0.2;
    'c' := 2*PI;
}}