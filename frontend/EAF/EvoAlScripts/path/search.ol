import "definitions" from de.evoal.core.math;

import "definitions" from de.evoal.core.optimisation;
import "definitions" from de.evoal.generator.optimisation;
import "definitions" from de.evoal.generator.generator;

import "definitions" from de.evoal.core.ea.optimisation;
import "definitions" from de.evoal.routenoptimierung.functions.shortestPath;
import "definitions" from de.evoal.routenoptimierung.selectors.generationPrintSelector;
import "definitions" from de.evoal.routenoptimierung.alterers.generationPrintMutator;
import "definitions" from de.evoal.routenoptimierung.alterers.generationPrintCrossover;
import "definitions" from de.evoal.routenoptimierung.alterers.pathCrossover;
import "definitions" from de.evoal.routenoptimierung.algorithms.evolutionaryAlgorithmOptimisation;
import "definitions" from de.evoal.routenoptimierung.statistics.generationPrintStatisticsWriter;
import "definitions" from de.evoal.routenoptimierung.comparators.generationPrintComparator;
import "definitions" from de.evoal.routenoptimierung.initials.generationPrintInitial;
import "definitions" from de.evoal.routenoptimierung.chromosomes.identifiableChromosome;
import "definitions" from de.evoal.routenoptimierung.chromosomes.pathChromosome;
import "definitions" from de.evoal.routenoptimierung.definitions;
import "definitions" from de.evoal.routenoptimierung.initials.emptyStringInitial;
import "definitions" from de.evoal.routenoptimierung.initials.fixedValueInitial;
import "definitions" from de.evoal.routenoptimierung.alterers.pathMutator;

import "data" from search;

module search {

	
	
	specify problem 'example-search' {
		description := "Simple search";
		'search-space' := [data 'id'];
		'optimisation-space' := [data 'fitness'];
		'maximise' := true;
		'optimisation-function' := 'benchmark-function' {
			'benchmarks' := [
				'benchmark-configuration' { function := 'shortest-path' {
					'jenetics-chromosome' := "de.evoal.jenetics.chromosomes.PathChromosome";
					'fitness-function-class' := "de.evoal.routenoptimierung.util.Util";
					'fitness-function-name' := "finnsFitness";
					
				};    reads := [data 'id']; writes := [data 'fitness']; }
			];
		};
	}
		

		
	configure 'evolutionary-algorithm-override' for 'example-search' {
		'number-of-generations' := 1000000;
		'size-of-population' := 300;
		'maximum-age' := 75;
		'proximityRewardFactor' := 200.0;
		'lengthPenaltyFactor' := 0.003;
		'serverMode' := false;
		'printBest' := false;
		'run-instruction' := 'no-loop-control' {};
		'parent-tree-diagram' := 'no-parent-tree-diagram' {};
		'writer' := 'generation-print-statistics-writer' {
			'graph-name' := "Evolutionary Algorithm Statistics";
			'child-writer' := 'best-candidate-per-generation' {};
			'informations' := [
			/**
				information {
					'name' := "Fitness";
					'target-name' := "value";
					'refresh-rate' := 1; 
					'cast-class-type' := candidate{};
					'graphics-type' := 'graph-representation' {
						'multiplier' := 1000.0;
					};
					'reference-type' := methode{};
					'representation-type' := best{};
				},
				information {
					'name' := "Proximity";
					'target-name' := "distanzTOTarget";
					'refresh-rate' := 1; 
					'cast-class-type' := 'custom-target'{
						'cast-class-name' := "de.evoal.jenetics.chromosomes.PathChromosome";
						'property-index':= 0;
					};
					
					'graphics-type' := 'graph-representation' {
						'multiplier' := 10000.0;
					};
					'reference-type' := field{};
					'representation-type' := best{};
				},
				information {
					'name' := "Length";
					'target-name' := "length";
					'refresh-rate' := 1; 
					'cast-class-type' := 'custom-target'{
						'cast-class-name' := "de.evoal.jenetics.chromosomes.PathChromosome";
						'property-index':= 0;
					};
					
					'graphics-type' := 'graph-representation' {
						'multiplier' := 10.0;
					};
					'reference-type' := field{};
					'representation-type' := best{};
				},
				information {
					'name' := "Avg. Fitness";
					'target-name' := "value";
					'refresh-rate' := 5; 
					'cast-class-type' := candidate{};
					'graphics-type' := 'variable-representation'{
						'multiplier' := 1000.0;
					};
					'reference-type' := methode{};
					'representation-type' := average{};
				},
				information {
					'name' := "Avg. Proximity";
					'target-name' := "distanzTOTarget";
					'refresh-rate' := 5; 
					'cast-class-type' := 'custom-target'{
						'cast-class-name' := "de.evoal.jenetics.chromosomes.PathChromosome";
						'property-index':= 0;
					};
					
					'graphics-type' := 'variable-representation'{
						'multiplier' := 10000.0;
					};
					'reference-type' := field{};
					'representation-type' := average{};
				},
				information {
					'name' := "Avg. Length";
					'target-name' := "length";
					'refresh-rate' := 5; 
					'cast-class-type' := 'custom-target'{
						'cast-class-name' := "de.evoal.jenetics.chromosomes.PathChromosome";
						'property-index':= 0;
					};
					
					'graphics-type' := 'variable-representation'{
						'multiplier' := 10.0;
					};
					'reference-type' := field{};
					'representation-type' := average{};
				}
				**/
			];
		};
			
		'initialisation' := 'generation-print-initial' {
			'child-initial' := 'fixed-value-initial' {
				'values' := [
					'methode-value' {
						'class' := "java.lang.String";
						'methode-class' := "de.evoal.routenoptimierung.util.Util";
						'methode-name' := "getId";
						'input-value' := "";
					},
					'set-value' {
						'class' := "java.lang.Integer";
						'value' := "0";
					}
				];
				
			};
		};
		'comparator' := 'numeric-comparator' {};
	    genotype := 'vector-genotype' {
   			 chromosomes := [

	    		 'identifiable-chromosome' {
			        'jenetics-chromosome' := "de.evoal.jenetics.chromosomes.PathChromosome";
						genes:= [
	                            gene {content:= data 'id' ;}
	                         
	                    ];

				  }

	            
	    	];

  		};
	
	    handlers := [];
	
	    selectors := selectors {
	        offspring := 'generation-print-selector' {
	                'child-selector' := 'elite-selector' {
		                	'size-factor' := 0.15;
		                	'non-elite-selector' := 'roulette-wheel-selector' {
		                        //'size-factor' := 0.2;
		                	};
		              
		              	};
	        };
	     
        survivor := 'generation-print-selector' {
               			'child-selector' := 'elite-selector' {
		                	'size-factor' := 0.15;
		                	'non-elite-selector' := 'roulette-wheel-selector' {
		                        //'size-factor' := 0.2;
		                	};
		              
		              	};

        			};
	    };
	    
		alterers := alterers {
	        crossover := [
	   				'path-crossover' {
	                        'crossoverCrossoverChance' := 0.5;
	                        'jenetics-crossover' := "de.evoal.jenetics.crossovers.PathCrossover";
	                 }
	       		
	        ];
	        mutator := [
	                'generation-print-mutator' {
	                	'child-alterer' := 
	                		'path-mutator' {
	                        	'mutatorMutationChance' := 0.45;
								'mutatorDeletionChance' := 0.04;
								'mutatorDistanceWeightFactor' := 3.0;
								'jenetics-mutator' := "de.evoal.jenetics.mutators.PathMutator";
	                        };
	                }
	        ];
	    };
	
		'optimisation-function' := 'problem-function' {};
	}
}