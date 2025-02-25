/**
 * 
 */
package eu.fbk.iv4xr.mbt.coverage;

import java.util.List;

/**
 * @author kifetew
 *
 */
public interface CoverageGoalFactory<T extends CoverageGoal> {
	List<T> getCoverageGoals ();
	
	public boolean isMaximizationFunction();
}
