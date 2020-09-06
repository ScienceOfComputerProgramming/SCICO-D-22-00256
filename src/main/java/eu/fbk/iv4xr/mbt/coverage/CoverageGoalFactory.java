/**
 * 
 */
package eu.fbk.iv4xr.mbt.coverage;

import java.util.List;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

/**
 * @author kifetew
 *
 */
public interface CoverageGoalFactory<T extends FitnessFunction<Chromosome>> {
	List<T> getCoverageGoals ();
	
	public double getFitness(Chromosome test);
}