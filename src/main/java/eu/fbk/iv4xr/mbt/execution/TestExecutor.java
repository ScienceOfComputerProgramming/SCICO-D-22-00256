/**
 * 
 */
package eu.fbk.iv4xr.mbt.execution;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.fbk.iv4xr.mbt.efsm.EFSM;
//import eu.fbk.iv4xr.mbt.efsm4j.EFSM;
//import eu.fbk.iv4xr.mbt.efsm4j.EFSMParameter;
//import eu.fbk.iv4xr.mbt.efsm4j.EFSMState;
//import eu.fbk.iv4xr.mbt.efsm4j.IEFSMContext;
import eu.fbk.iv4xr.mbt.testcase.Testcase;

/**
 * @author kifetew
 *
 */
public abstract class TestExecutor {

	protected EFSM efsm;
	protected transient Set<ExecutionListener> listners =  new HashSet<ExecutionListener>();
	/**
	 * 
	 */
	public TestExecutor() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Execute the given test case (sequence of transitions) on the model
	 * @param testcase
	 * @return execution trace
	 */
	public abstract ExecutionResult executeTestcase (Testcase testcase);
	
	/**
	 * Execute the given test suite (set of test cases) on the model
	 * @param testSuite
	 * @return execution trace
	 */
	public abstract ExecutionResult executeTestSuite (List<Testcase> testSuite);
	
	/**
	 * reset the model to the initial state
	 * @return
	 */
	public abstract boolean reset ();
	
	
	public void addListner (ExecutionListener listner) {
		listners.add(listner);
	}
	
	public void removeListner (ExecutionListener listner) {
		listners.remove(listner);
	}
	
}
