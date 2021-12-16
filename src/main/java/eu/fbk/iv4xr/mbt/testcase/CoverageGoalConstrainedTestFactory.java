/**
 * 
 */
package eu.fbk.iv4xr.mbt.testcase;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fbk.iv4xr.mbt.MBTProperties;
import eu.fbk.iv4xr.mbt.coverage.CoverageGoal;
import eu.fbk.iv4xr.mbt.coverage.StateCoverageGoal;
import eu.fbk.iv4xr.mbt.efsm.EFSM;
import eu.fbk.iv4xr.mbt.efsm.EFSMConfiguration;
import eu.fbk.iv4xr.mbt.efsm.EFSMContext;
import eu.fbk.iv4xr.mbt.efsm.EFSMGuard;
import eu.fbk.iv4xr.mbt.efsm.EFSMOperation;
import eu.fbk.iv4xr.mbt.efsm.EFSMParameter;
import eu.fbk.iv4xr.mbt.efsm.EFSMState;
import eu.fbk.iv4xr.mbt.efsm.EFSMTransition;
import eu.fbk.iv4xr.mbt.utils.RandomnessMBT;

//import eu.fbk.iv4xr.mbt.efsm4j.Configuration;
//import eu.fbk.iv4xr.mbt.efsm4j.EFSM;
//import eu.fbk.iv4xr.mbt.efsm4j.EFSMParameter;
//import eu.fbk.iv4xr.mbt.efsm4j.EFSMState;
//import eu.fbk.iv4xr.mbt.efsm4j.IEFSMContext;
//import eu.fbk.iv4xr.mbt.efsm4j.Transition;
//import eu.fbk.iv4xr.mbt.utils.Randomness;
//import eu.fbk.se.labrecruits.LabRecruitsState;
import org.evosuite.utils.Randomness;


/**
 * 
 * This factory generates paths in the model constrained by a given {@link CoverageGoal}.
 * The nature of the constraint is controlled by the parameter {@link MBTProperties.GOAL_CONSTRAINT_ON_TEST_FACTORY}.
 * 
 * @author kifetew
 *
 */
public class CoverageGoalConstrainedTestFactory<
	State extends EFSMState,
	InParameter extends EFSMParameter,
	OutParameter extends EFSMParameter,
	Context extends EFSMContext,
	Operation extends EFSMOperation,
	Guard extends EFSMGuard,
	Transition extends EFSMTransition<State, InParameter, OutParameter, Context, Operation, Guard>> implements TestFactory {
	
	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(CoverageGoalConstrainedTestFactory.class);
	
	private int maxLength = 100;
	EFSM<State, InParameter, OutParameter, Context, Operation, Guard, Transition> model = null;
	CoverageGoal<State, InParameter, OutParameter, Context, Operation, Guard, Transition> constrainingGoal = null;
	
	/**
	 * 
	 */
	public CoverageGoalConstrainedTestFactory(EFSM<State, InParameter, OutParameter, Context, Operation, Guard, Transition> efsm, CoverageGoal<State, InParameter, OutParameter, Context, Operation, Guard, Transition> constrainingGoal) {
		model = efsm;
		this.constrainingGoal = constrainingGoal;
	}

	
	/**
	 * 
	 */
	public CoverageGoalConstrainedTestFactory(EFSM<State, InParameter, OutParameter, Context, Operation, Guard, Transition> efsm, int max) {
		model = efsm;
		maxLength = max;
	}
	
	@Override
	public Testcase getTestcase() {
		int randomLength = Randomness.nextInt(maxLength) + 1;
		EFSMConfiguration<State, Context> initialConfiguration = model.getInitialConfiguration();
		State currentState = (State)initialConfiguration.getState();
		
		
		List<Transition> transitions = new LinkedList<Transition>();
		int len = 0;
		
		// loop until random length reached or current state has not outgoing transitions (finalInParameter?)
		while (len < randomLength && !model.transitionsOutOf(currentState).isEmpty()) {
			Set<EFSMTransition> outgoingTransitions = model.transitionsOutOf(currentState);
			
			// pick one transition at random and add it to path
			Transition transition = (Transition) Randomness.choice(outgoingTransitions);
			transitions.add(transition);
			
			// take the state at the end of the chosen transition, and repeat
			currentState = transition.getTgt();
			
			
			// check if goal constraint is satisfied, if so quit the loop
			EFSMState goal = null;
			switch(MBTProperties.GOAL_CONSTRAINT_ON_TEST_FACTORY) {
			case ENDS_WITH:
				if (constrainingGoal instanceof StateCoverageGoal) {
					goal = ((StateCoverageGoal)constrainingGoal).getState();
				}
				break;
			}
			if (goal != null && goal.equals(currentState) && RandomnessMBT.nextBoolean()) {
				break;
			}
			
			// until maxLength is reached or final state is reached
			len++;
		}
		//model.reset();
		
		// build the test case
		Testcase testcase = new AbstractTestSequence<State, InParameter, OutParameter, Context, Operation, Guard, Transition>();
		Path path = new Path (transitions);
		((AbstractTestSequence)testcase).setPath(path);
		assert path.isConnected();
		assert path.getTransitionAt(0).getSrc().getId().equalsIgnoreCase(model.getInitialConfiguration().getState().getId());
		return testcase;
	}

}
