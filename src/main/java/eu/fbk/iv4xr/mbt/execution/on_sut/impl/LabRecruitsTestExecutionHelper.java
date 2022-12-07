package eu.fbk.iv4xr.mbt.execution.on_sut.impl;

import java.io.File;
import java.util.LinkedHashMap;

import eu.fbk.iv4xr.mbt.execution.on_sut.TestExecutionHelper;
import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;

/**
 * A class that loads tests from disk and executs them on a given LabRecruits binary
 * @author kifetew
 *
 */
public class LabRecruitsTestExecutionHelper extends TestExecutionHelper {

	public LabRecruitsTestExecutionHelper(String lrExecutableDir, String lrLevelPath, String agentName, String testsDir, Integer maxCyclePerGoal) {
		testExecutor = new LabRecruitsConcreteTestExecutor(lrExecutableDir, lrLevelPath, agentName, maxCyclePerGoal);
		testToFileMap = new LinkedHashMap<AbstractTestSequence,File>();
		testSuite = parseTests (testsDir);
		testsFolder = testsDir;
		
	}
	
}
