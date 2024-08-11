'evolutionary-algorithm' {
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
  }