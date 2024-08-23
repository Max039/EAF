import "definitions" from de.evoal.optimisation.core;
import "definitions" from de.evoal.optimisation.ea.'genetic-programming';
import "definitions" from de.evoal.optimisation.ea.optimisation;
import "definitions" from de.evoal.core.math;

import "data" from 'config';

module 'config' {
  specify problem 'problem' {
    'search-space' := [
      data 'regression-function'
    ];
    'optimisation-function' := 'unknown-function' {};
    'description' := "Genetic Programming Example";
    'optimisation-space' := [
      data 'ℝ'
    ];
    'maximise' := false;
  }


  configure 'evolutionary-algorithm' for 'problem' {
    'initialisation' := 'random-tree-population' {};
    'offspring-fraction' := 0.6;
    'comparator' := 'numeric-comparator' {};
    'size-of-population' := 100;
    'maximum-age' := 1000;
    'handlers' := [];
    'optimisation-function' := 'regression-fitness' {
      'calculations' := [
        'squared-error' {
          'output' := [
            data 'y0'
          ];
          'reference' := "gp-example-function-data.csv";
          'input' := [
            data 'x0'
          ];
          'function' := data 'regression-function';
        }
      ];
    };
    'alterers' := 'alterers' {
      'crossover' := [
        'single-node-crossover' {
          'probability' := 0.3;
        }
      ];
      'mutator' := [
        'probability-mutator' {
          'probability' := 0.2;
        },
        'mathematical-expression-rewriter' {
          'probability' := 0.4;
        }
      ];
    };
    'number-of-generations' := 1000;
    'selectors' := 'selectors' {
      'offspring' := 'roulette-wheel-selector' {};
      'survivor' := 'elite-selector' {
        'size-factor' := 0.3;
        'non-elite-selector' := 'tournament-selector' {
          'size-factor' := 0.1;
        };
      };
    };
    'genotype' := 'program-genotype' {
      'chromosomes' := [
        'program-chromosome' {
          'variables' := [
            data 'x0'
          ];
          'operations' := [
            'plus' {},
            'multiply' {},
            'minus' {},
            'divide' {},
            'sqrt' {},
            'pow' {}
          ];
          'validators' := [
            'must-use-variable' {
              'count' := 1;
            },
            'program-size' {
              'max-size' := 64;
            }
          ];
          'ephemeral-constants' := [
            'ephemeral-constant' {
              'lower' := -50;
              'upper' := 50;
              'count' := 2;
            }
          ];
          'constants' := [
            'constant' {
              'name' := "PI";
              'value' := 'π';
            }
          ];
          'initial-depth' := 5;
          'content' := data 'regression-function';
        }
      ];
    };
  }}