/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package eu.fbk.iv4xr.mbt.testcase.secondaryobjectives;

import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.execution.ExecutionResult;

import eu.fbk.iv4xr.mbt.testcase.MBTChromosome;

/**
 * <p>MinimizeExceptionsSecondaryObjective class.</p>
 *
 * @author Gordon Fraser
 */
public class MinimizeExceptionsSO extends SecondaryObjective<MBTChromosome> {

	private static final long serialVersionUID = -4405276303273532040L;

	private int getNumExceptions(MBTChromosome chromosome) {
		ExecutionResult result = chromosome.getLastExecutionResult();
		if (result != null)
			return result.getNumberOfThrownExceptions();
		else
			return 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SecondaryObjective#compareChromosomes(org.evosuite.ga.Chromosome,
	 * org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public int compareChromosomes(MBTChromosome chromosome1, MBTChromosome chromosome2) {
		return getNumExceptions(chromosome1) - getNumExceptions(chromosome2);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SecondaryObjective#compareGenerations(org.evosuite.ga.Chromosome,
	 * org.evosuite.ga.Chromosome, org.evosuite.ga.Chromosome, org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public int compareGenerations(MBTChromosome parent1, MBTChromosome parent2,
			MBTChromosome child1, MBTChromosome child2) {
		return Math.min(getNumExceptions(parent1), getNumExceptions(parent2))
		        - Math.min(getNumExceptions(child1), getNumExceptions(child2));
	}

}
