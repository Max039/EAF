import "definitions" from de.evoal.generator.generator;
import "definitions" from de.evoal.generator.optimisation;
import "definitions" from de.evoal.optimisation.core;
import "definitions" from de.evoal.optimisation.ea.mdo;
import "definitions" from de.evoal.optimisation.ea.optimisation;
import "definitions" from de.evoal.routenoptimierung.comparators.generationPrintComparator;
import "definitions" from de.evoal.routenoptimierung.initials.emptyStringInitial;
import "definitions" from de.evoal.core.math

import "data" from 'config';

module 'config' {
  specify problem 'evolutionary-algorithm' {
    'initialisation' := 'empty-string-initial' {};
    'offspring-fraction' := 0.6;
    'comparator' := 'generation-print-comparator' {
      'child-comparator' := 'numeric-comparator' {};
    };
    'size-of-population' := 0;
    'maximum-age' := 0;
    'handlers' := [
      'constraint-handler' {
        'calculation' := 'normal-calculation' {};
        'constraint-handling' := 'kill-at-birth' {
          'repair-strategy' := 'repair-with-random' {};
        };
        'category' := Enter string here!;
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
              data 'eaf'
            ];
            'writes' := [
              data 'eaf'
            ];
          }
        ];
      };
      'target' := [
        'variable' {
          'val' := Enter literal here!;
          'name' := data 'eaf';
        }
      ];
    };
    'alterers' := 'alterers' {
      'crossover' := [
        'correlation-line-crossover' {
          'probability' := 0;
          'position' := 0;
        }
      ];
      'mutator' := [
        'array-reorder-mutator' {
          'probability' := 0;
        }
      ];
    };
    'number-of-generations' := 0;
    'selectors' := 'selectors' {
      'offspring' := 'linear-rank-selector' {
        'nminus' := 0;
      };
      'survivor' := 'boltzmann-selector' {
        'beta' := 0;
      };
    };
    'genotype' := 'vector-genotype' {
      'chromosomes' := [
        'bit-chromosome' {
          'genes' := [
            'gene' {
              'content' := data 'eaf';
            }
          ];
          'scale' := 0;
        }
      ];
    };
  }


  configure problem 'ackley' for 'evolutionary-algorithm' {    'a' := 20.0;
    'b' := 0.2;
    'c' := 2*PI;
}}