import "definitions" from de.evoal.routenoptimierung.definitions;

module search {
	
	
	data:
		/**
		 * Source dimension of the training data.
		 */
		real data 'id';
		quotient real data 'length';

		/**
		 * Target dimension of the training data.
		 */
		quotient real data 'fitness';
		
	
}