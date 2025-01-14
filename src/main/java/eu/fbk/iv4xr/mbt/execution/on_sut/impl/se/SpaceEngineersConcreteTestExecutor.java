package eu.fbk.iv4xr.mbt.execution.on_sut.impl.se;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import environments.SeAgentState;
import environments.SeEnvironment;
import eu.fbk.iv4xr.mbt.concretization.TestConcretizer;
import eu.fbk.iv4xr.mbt.concretization.impl.SpaceEngineersTestConcretizer;
import eu.fbk.iv4xr.mbt.execution.on_sut.ConcreteTestExecutor;
import eu.fbk.iv4xr.mbt.execution.on_sut.TestCaseExecutionReport;
import eu.fbk.iv4xr.mbt.execution.on_sut.TestSuiteExecutionReport;
import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;
import eu.fbk.iv4xr.mbt.testsuite.SuiteChromosome;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.GoalStructure.PrimitiveGoal;
import spaceEngineers.controller.ContextControllerWrapper;
import spaceEngineers.controller.SpaceEngineers;
import spaceEngineers.controller.SpaceEngineersJavaProxyBuilder;
import spaceEngineers.controller.SpaceEngineersTestContext;

public class SpaceEngineersConcreteTestExecutor implements ConcreteTestExecutor {

	private TestConcretizer testConcretizer;
	private int maxCyclePerGoal;
	private TestSuiteExecutionReport testReporter;
	
	private String spaceEngineersExeRootDir;
	private String seGameSavePath;
	
	private SeEnvironment theEnv;
	
	private static long longSleepTime = 2000l;
	private static long shortSleepTime = 500l;
	
	
	private String agentId = SpaceEngineers.Companion.DEFAULT_AGENT_ID;
	
	
	public SpaceEngineersConcreteTestExecutor(String seExecutableDir, String seGameSavePath, Integer maxCyclePerGoal) {
		this.testConcretizer = new SpaceEngineersTestConcretizer();
		setMaxCyclePerGoal(maxCyclePerGoal);
		this.spaceEngineersExeRootDir = seExecutableDir;
		this.seGameSavePath = seGameSavePath;
		testReporter = new TestSuiteExecutionReport();

		
		// check if SE is running and eventually start it 
		if (!isRunningSpaceEngineers()) {
			// try to start Space Engineers
			if (!startSpaceEngineers()) {
				throw new RuntimeException("Cannot start Space Engineers");
			}
		}
	}
	
	private void sleep(long i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean isRunningSpaceEngineers() {
		
		String filter = "/nh /fi \"Imagename eq SpaceEngineers.exe\"";
		String cmd = System.getenv("windir") +"/system32/tasklist.exe "+filter;

		Process p;
		Boolean processFound = false;
		try {
			p = Runtime.getRuntime().exec(cmd);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

			List<String> procs = new ArrayList<String>();
			String line = null;
			while ((line = input.readLine()) != null) 
			    procs.add(line);
			input.close();

			processFound = procs.stream().filter(row -> row.indexOf("SpaceEngineers.exe") > -1).count() > 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return processFound;
	}
	
	
	private boolean startSpaceEngineers() {
		String cmd = this.spaceEngineersExeRootDir + "SpaceEngineers.exe -plugin Ivxr.SePlugin.dll";
		Process p;
		
		// start Space Engineers
		try {
			p = Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// wait some time to load the game
		sleep(10000l);
		
		// check if the game is running
		return isRunningSpaceEngineers();
	}
	
	@Override
	public boolean executeTestSuite(SuiteChromosome testSuite) throws InterruptedException {
		
		// TODO now load only random medium as load an save map needs to be fixed
		//Path pathToGameSave = Paths.get(seGameSavePath);
		String worldId = "LR_random_medium";
		
		// open connection with Space Engineer
		// load map
		
		SpaceEngineersTestContext context = new SpaceEngineersTestContext();
		SpaceEngineersJavaProxyBuilder proxyBuilder = new SpaceEngineersJavaProxyBuilder();
		SpaceEngineers se = proxyBuilder.localhost(agentId);
		ContextControllerWrapper controllerWrapper = new ContextControllerWrapper(se, context);
		// theEnv = new SeEnvironment(worldId, controllerWrapper);
		theEnv = SpaceEngineersUtils.createSeEnvWithLrRanommediumMap(controllerWrapper);
		
		boolean testSuiteResult = true;
		// cycle over the test cases
		for (int i = 0; i < testSuite.size(); i++) {
			AbstractTestSequence testcase = (AbstractTestSequence) testSuite.getTestChromosome(i).getTestcase();
			Boolean testResult = executeTestCase(testcase);
			if (!testResult) {
				testSuiteResult = false;
			} 
		}

		// close se connection
		theEnv.close();
		
		return testSuiteResult;
	}

	@Override
	public boolean executeTestCase(AbstractTestSequence testcase) throws InterruptedException {
		
		long initialTime = System.currentTimeMillis();
		
		LinkedList<TestCaseExecutionReport> goalReporter = new LinkedList<TestCaseExecutionReport>();

		
		System.out.println("Executing test case");
		System.out.println(testcase.toString());
		
		
		// load map
		theEnv.loadWorld();
		// wait the game load the map
		sleep(longSleepTime);
		
		
		var dataCollector = new TestDataCollector();
		var myAgentState = new SeAgentState(agentId);
		var testAgent = new TestAgent(agentId, "Navigator");
		testAgent.attachState(myAgentState);
		testAgent.attachEnvironment(theEnv);
		testAgent.setTestDataCollector(dataCollector);
		List<GoalStructure> concretizeTestCase = testConcretizer.concretizeTestCase(testAgent, testcase );
		//for(GoalStructure g: concretizeTestCase) {
		//	execTestingTask(g, testAgent);
		//}

		String status = "SUCCESS";
		
		for (int i = 0; i < concretizeTestCase.size(); i++) {
			//for (GoalStructure g : goals) {
				GoalStructure g = concretizeTestCase.get(i);
				testAgent.setGoal(g);
				System.err.println("Testing "+testcase.getPath().getTransitionAt(i).toString());
		
				// try to execute the test case
				int nCycle = 0;
				while (g.getStatus().inProgress()) {
					testAgent.update();
					if (testAgent.getTestDataCollector().getNumberOfFailVerdictsSeen() > 0) {
						// stop the time
						long finalTime = System.currentTimeMillis();
						long timeDuration = finalTime - initialTime;
						
						String err = "Verdict " + testAgent.getTestDataCollector().getLastFailVerdict().toString() + " failed";
						System.err.println(err);
						TestCaseExecutionReport goalRep = new TestCaseExecutionReport();
						goalRep.addReport(g, err, testcase.getPath().getTransitionAt(i), getGoalStatus(g));
						goalReporter.add(goalRep);
						testReporter.addTestCaseReport(testcase, goalReporter, Boolean.FALSE, timeDuration);
						return false;
					}
					Thread.sleep(20);
					nCycle++;
					if (nCycle > maxCyclePerGoal) {
						// stop the time
						long finalTime = System.currentTimeMillis();
						long timeDuration = finalTime - initialTime;
						
						String err = "The goal cannot be satisfied in " + maxCyclePerGoal + " cycles";
						System.err.println(err);
						TestCaseExecutionReport goalRep = new TestCaseExecutionReport();
						goalRep.addReport(g, err, testcase.getPath().getTransitionAt(i), getGoalStatus(g));
						goalReporter.add(goalRep);
						testReporter.addTestCaseReport(testcase, goalReporter, Boolean.FALSE, timeDuration);
						return false;
					}
				}
				TestCaseExecutionReport goalRep = new TestCaseExecutionReport();
				goalRep.addReport(g, "Pass", testcase.getPath().getTransitionAt(i), getGoalStatus(g));
				goalReporter.add(goalRep);
				if (!g.getStatus().success()) {
					status = "FAIL";
				}
			}
			
			// stop the time
			long finalTime = System.currentTimeMillis();
			long timeDuration = finalTime - initialTime;
			
			if (status == "SUCCESS") {
				testReporter.addTestCaseReport(testcase, goalReporter, Boolean.TRUE, timeDuration);
				return true;
			}else {
				testReporter.addTestCaseReport(testcase, goalReporter, Boolean.FALSE, timeDuration);
				return false;
			}
	}
	
	
	/**
	 * 
	 * @param testingTask
	 * @param testAgent
	 */
	private void execTestingTask(GoalStructure testingTask, TestAgent testAgent) {

		testAgent.setGoal(testingTask);
		SeAgentState state = (SeAgentState) testAgent.state();

		var i = 0;
		while (testingTask.getStatus().inProgress() && i <= getMaxCylcePerGoal()) {
			sleep(shortSleepTime);
			testAgent.update();
			System.out
					.println("Cycle " + i + ": " + testAgent.getId() + " @ " + state.worldmodel().position.toString());
			System.out.println(testingTask.showGoalStructureStatus());
			
			i++;
			// System.out.println();
		}
	}
	

	@Override
	public TestSuiteExecutionReport getReport() {
		return testReporter;
	}

	@Override
	public void setMaxCyclePerGoal(int max) {
		this.maxCyclePerGoal = max;
		
	}

	@Override
	public int getMaxCylcePerGoal() {
		return maxCyclePerGoal;
	}

	
	
	// covert the goal status of a goal structure to a string
	private String getGoalStatus(GoalStructure goal) {
		if (goal instanceof PrimitiveGoal) {
			return goal.getStatus().toString();
		}else {
			String out = "";
			for(GoalStructure g : goal.getSubgoals()) {
				out = out + getGoalStatus(g) +"; ";
			}
			return out;
		}
	}
}
