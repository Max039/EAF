import "definitions" from de.evoal.optimisation.core;
import "definitions" from de.evoal.optimisation.ea.mdo;
import "definitions" from de.evoal.optimisation.ea.optimisation;
import "definitions" from de.evoal.routenoptimierung.initials.emptyStringInitial;
import "definitions" from de.evoal.surrogate.optimisation;
import "definitions" from de.evoal.surrogate.smile.ml;

import "data" from 'config';

module 'config' {
  specify problem '{
    'initialisation' := 'empty-string-initial' {};
    'comparator' := 'hierarchical-comparator' {
      'order' := [
        data 'Num',
        data 'Num2',
        data ''
      ];
    };
    'size-of-population' := 20;
    'maximum-age' := 5;
    'handlers' := [
      'constraint-handler' {
        'calculation' := 'normal-calculation' {};
        'constraint-handling' := 'malus-for-fitness' {
          'smoothing' := 50;
        };
        'category' := Test string;
      }
    ];
    'optimisation-function' := 'gaussian-svr' {
      'σ' := 5;
      'soft-margin' := 6;
      'ε' := 7;
      'tolerance' := 8;
    };
    'alterers' := 'alterers' {
      'crossover' := [
        'correlation-line-crossover' {
          'probability' := 5;
          'position' := 8;
        },
        'correlation-single-point-crossover' {
          'probability' := 5;
        }
      ];
      'mutator' := [
        'array-reorder-mutator' {
          'probability' := 3;
        },
        'bit-flip-mutator' {
          'probability' := 2;
        }
      ];
    };
    'number-of-generations' := 5;
    'selectors' := 'selectors' {
      'offspring' := 'boltzmann-selector' {
        'beta' := 3;
      };
      'survivor' := 'elite-selector' {
        'size-factor' := 4;
        'non-elite-selector' := 'linear-rank-selector' {
          'nminus' := 5;
        };
      };
    };
    'genotype' := 'vector-genotype' {
      'chromosomes' := [
        'bit-chromosome' {
          'genes' := [
            'gene' {
              'content' := data 'Num2';
            }
          ];
          'scale' := 5;
        }
      ];
    };
  }' evolutionary-algorithm
  configure problem '{
    'crossover' := [
      'correlation-line-crossover' {
        'probability' := 0;
        'position' := 0;
      },
      'correlation-mean-alterer' {
        'probability' := 0;
      }
    ];
    'mutator' := [];
  }' for '{
    'initialisation' := 'empty-string-initial' {};
    'comparator' := 'hierarchical-comparator' {
      'order' := [
        data 'Num',
        data 'Num2',
        data ''
      ];
    };
    'size-of-population' := 20;
    'maximum-age' := 5;
    'handlers' := [
      'constraint-handler' {
        'calculation' := 'normal-calculation' {};
        'constraint-handling' := 'malus-for-fitness' {
          'smoothing' := 50;
        };
        'category' := Test string;
      }
    ];
    'optimisation-function' := 'gaussian-svr' {
      'σ' := 5;
      'soft-margin' := 6;
      'ε' := 7;
      'tolerance' := 8;
    };
    'alterers' := 'alterers' {
      'crossover' := [
        'correlation-line-crossover' {
          'probability' := 5;
          'position' := 8;
        },
        'correlation-single-point-crossover' {
          'probability' := 5;
        }
      ];
      'mutator' := [
        'array-reorder-mutator' {
          'probability' := 3;
        },
        'bit-flip-mutator' {
          'probability' := 2;
        }
      ];
    };
    'number-of-generations' := 5;
    'selectors' := 'selectors' {
      'offspring' := 'boltzmann-selector' {
        'beta' := 3;
      };
      'survivor' := 'elite-selector' {
        'size-factor' := 4;
        'non-elite-selector' := 'linear-rank-selector' {
          'nminus' := 5;
        };
      };
    };
    'genotype' := 'vector-genotype' {
      'chromosomes' := [
        'bit-chromosome' {
          'genes' := [
            'gene' {
              'content' := data 'Num2';
            }
          ];
          'scale' := 5;
        }
      ];
    };
  }' alterers}