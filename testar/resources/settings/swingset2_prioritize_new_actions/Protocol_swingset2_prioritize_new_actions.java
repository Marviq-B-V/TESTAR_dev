/***************************************************************************************************
 *
 * Copyright (c) 2020 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2020 Open Universiteit - www.ou.nl
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

import java.util.Set;
import nl.ou.testar.PrioritizeNewActionsSelector;
import org.fruit.monkey.Settings;
import org.testar.protocols.JavaSwingProtocol;

import java.io.File;
import java.io.IOException;

import org.fruit.alayer.Action;
import org.fruit.alayer.exceptions.ActionBuildException;
import org.fruit.alayer.windows.WinProcess;
import org.fruit.monkey.ConfigTags;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Widget;
import org.fruit.alayer.actions.AnnotatingActionCompiler;
import org.fruit.alayer.actions.StdActionCompiler;
import org.fruit.alayer.Tags;
import org.testar.OutputStructure;
import static org.fruit.alayer.Tags.Blocked;
import static org.fruit.alayer.Tags.Enabled;

import java.io.FileWriter;

/**
 * This protocol together with the settings provides a specific behavior to test SwingSet2
 * We will use Java Access Bridge settings (AccessBridgeEnabled = true) for widget tree extraction
 *
 * It uses PrioritizeNewActionsSelector algorithm.
 */
public class Protocol_swingset2_prioritize_new_actions extends JavaSwingProtocol {
	
	private long startSequenceTime;
	private String reportTimeDir;

	// PrioritizeNewActionsSelector: Instead of random, we will prioritize new actions for action selection
	private PrioritizeNewActionsSelector selector = new PrioritizeNewActionsSelector();
	
	/**
	 * Called once during the life time of TESTAR
	 * This method can be used to perform initial setup work
	 * @param   settings  the current TESTAR settings as specified by the user.
	 */
	@Override
	protected void initialize(Settings settings){
		super.initialize(settings);
		
		// SwingSet2: Requires Java Access Bridge
		System.out.println("Are we running Java Access Bridge ? " + settings.get(ConfigTags.AccessBridgeEnabled, false));
		
		// TESTAR will execute the SUT with Java
		// We need this to add JMX parameters properly (-Dcom.sun.management.jmxremote.port=5000)
		WinProcess.java_execution = true;
		
		// Copy "bin/settings/protocolName/build.xml" file to "bin/jacoco/build.xml"
		copyJacocoBuildFile();
	}
	
	/**
	 * This method is invoked each time the TESTAR starts the SUT to generate a new sequence.
	 * This can be used for example for bypassing a login screen by filling the username and password
	 * or bringing the system into a specific start state which is identical on each start (e.g. one has to delete or restore
	 * the SUT's configuration files etc.)
	 */
	 @Override
	protected void beginSequence(SUT system, State state){
		startSequenceTime = System.currentTimeMillis();
		try{
			reportTimeDir = new File(OutputStructure.outerLoopOutputDir).getCanonicalPath();
		} catch (Exception e) {
				System.out.println("sequenceTimeUntilActions.txt can not be created " );
				e.printStackTrace();
		}
	 	super.beginSequence(system, state);
	}

	 /**
	  * This method is used by TESTAR to determine the set of currently available actions.
	  * You can use the SUT's current state, analyze the widgets and their properties to create
	  * a set of sensible actions, such as: "Click every Button which is enabled" etc.
	  * The return value is supposed to be non-null. If the returned set is empty, TESTAR
	  * will stop generation of the current action and continue with the next one.
	  * @param system the SUT
	  * @param state the SUT's current state
	  * @return  a set of actions
	  */
	 protected Set<Action> deriveActions(SUT system, State state) throws ActionBuildException{

		 //The super method returns a ONLY actions for killing unwanted processes if needed, or bringing the SUT to
		 //the foreground. You should add all other actions here yourself.
		 // These "special" actions are prioritized over the normal GUI actions in selectAction() / preSelectAction().
		 Set<Action> actions = super.deriveActions(system,state);

		 // To derive actions (such as clicks, drag&drop, typing ...) we should first create an action compiler.
		 StdActionCompiler ac = new AnnotatingActionCompiler();

		 /**
		  * Specific Action Derivation for SwingSet2 SUT
		  * To avoid deriving actions on non-desired widgets
		  * 
		  * Optional : iterate through top level widgets based on Z-index
		  * for(Widget w : getTopWidgets(state))
		  * If selected also change it for all SwingSet2 protocols
		  */

		 // iterate through all widgets
		 for(Widget w : state){

			 if(w.get(Enabled, true) && !w.get(Blocked, false)){ // only consider enabled and non-blocked widgets

				 if (!blackListed(w)){  // do not build actions for tabu widgets  

					 // left clicks
					 if(isClickable(w) && (isUnfiltered(w) || whiteListed(w))) {
						 actions.add(ac.leftClickAt(w));
					 }

					 // type into text boxes
					 // SwingSet2: isSourceCodeEditWidget feature
					 if((isTypeable(w) && (isUnfiltered(w) || whiteListed(w))) && !isSourceCodeEditWidget(w)) {
						 actions.add(ac.clickTypeInto(w, this.getRandomText(w), true));
					 }

					 // GENERIC: All swing apps
					 //Force actions on some widgets with a wrong accessibility
					 //Optional, comment this changes if your Swing applications doesn't need it
					 if(w.get(Tags.Role).toString().contains("Tree") ||
							 w.get(Tags.Role).toString().contains("ComboBox") ||
							 w.get(Tags.Role).toString().contains("List")) {
						 widgetTree(w, actions);
					 }
					 //End of Force action
				 }
			 }
		 }

		 // PrioritizeNewActionsSelector: pick prioritized actions
		 actions = selector.getPrioritizedActions(actions);

		 return actions;

	 }
	 
	 /**
	  * SwingSet2 application contains a TabElement called "SourceCode"
	  * that internally contains UIAEdit widgets that are not modifiable.
	  * Because these widgets have the property ToolTipText with the value "text/html",
	  * use this Tag to recognize and ignore.
	  */
	 private boolean isSourceCodeEditWidget(Widget w) {
		 return w.get(Tags.ToolTipText, "").contains("text/html");
	 }

	 //Force actions on Tree widgets with a wrong accessibility
	 public void widgetTree(Widget w, Set<Action> actions) {
		 StdActionCompiler ac = new AnnotatingActionCompiler();
		 actions.add(ac.leftClickAt(w));
		 w.set(Tags.ActionSet, actions);
		 for(int i = 0; i<w.childCount(); i++) {
			 widgetTree(w.child(i), actions);
		 }
	 }
	
	/**
	 * Select one of the available actions using an action selection algorithm (for example random action selection)
	 *
	 * super.selectAction(state, actions) updates information to the HTML sequence report
	 *
	 * @param state the SUT's current state
	 * @param actions the set of derived actions
	 * @return  the selected action (non-null!)
	 */
	@Override
	protected Action selectAction(State state, Set<Action> actions){
		// PrioritizeNewActionsSelector: we select randomly one of the prioritize actions
		Action action = super.selectAction(state, actions);
		return(action);
	}

	/**
	 * Execute the selected action.
	 * Extract and create JaCoCo coverage report (After each action JaCoCo report will be created).
	 */
	@Override
	protected boolean executeAction(SUT system, State state, Action action){
		boolean actionExecuted = super.executeAction(system, state, action);

		// Write sequence duration to CLI and to file
		long  sequenceDurationSoFar = System.currentTimeMillis() - startSequenceTime;
		System.out.println();
		System.out.println("Elapsed time until action " + actionCount + ": " + sequenceDurationSoFar);

		long minutes = (sequenceDurationSoFar / 1000)  / 60;
		int seconds = (int)((sequenceDurationSoFar / 1000) % 60);
		System.out.println("Elapsed time until action " + actionCount + ": " + + minutes + " minutes, "+ seconds + " seconds.");
		System.out.println();
		// Write sequence duration to file
		try {
			FileWriter myWriter = new FileWriter(reportTimeDir + "/" + OutputStructure.startInnerLoopDateString + "_" + OutputStructure.executedSUTname + "_actionTimeStamps.txt", true);
			myWriter.write(sequenceDurationSoFar + "\r\n");
			myWriter.close();
			System.out.println("Wrote time so far to file." + reportTimeDir + "/_sequenceTimeUntilAction.txt");
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

		// Extract and create JaCoCo action coverage report for Generate Mode
		if(settings.get(ConfigTags.Mode).equals(Modes.Generate)) {
			extractJacocoActionReport();
		}

		return actionExecuted;
	}
	
	/**
	 * This method is invoked each time the TESTAR has reached the stop criteria for generating a sequence.
	 * This can be used for example for graceful shutdown of the SUT, maybe pressing "Close" or "Exit" button
	 */
	@Override
	protected void finishSequence() {

		// Extract and create JaCoCo sequence coverage report for Generate Mode
		if(settings.get(ConfigTags.Mode).equals(Modes.Generate)) {
			extractJacocoSequenceReport();
		}
 
		super.finishSequence();
		
		// Write sequence duration to CLI and to file
		long  sequenceDuration = System.currentTimeMillis() - startSequenceTime;
		System.out.println();
		System.out.println("Sequence duration: " + sequenceDuration);
		long minutes = (sequenceDuration / 1000)  / 60;
		int seconds = (int)((sequenceDuration / 1000) % 60);
		System.out.println("Sequence duration: " + minutes + " minutes, "+ seconds + " seconds.");
		System.out.println();
		try {
			String reportDir = new File(OutputStructure.outerLoopOutputDir).getCanonicalPath();//  + File.separator;
			FileWriter myWriter = new FileWriter(reportDir + "/" + OutputStructure.startInnerLoopDateString + "_" + OutputStructure.executedSUTname + "_sequenceDuration.txt");
			myWriter.write("Sequence duration: " + minutes + " minutes, " + seconds + " seconds.   (" + sequenceDuration + " mili)");
			myWriter.close();
			System.out.println("Wrote time to file." + reportDir + "/_sequenceDuration.txt");
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	/**
	 * This methods stops the SUT
	 *
	 * @param system
	 */
	@Override
	protected void stopSystem(SUT system) {
		super.stopSystem(system);

		// This is the default JaCoCo generated file, we dumped our desired file with MBeanClient (finishSequence)
		// In this protocol this one is residual, so just delete
		if(new File("jacoco.exec").exists()) {
			System.out.println("Deleted residual jacoco.exec file ? " + new File("jacoco.exec").delete());
		}
	}
	
	/**
	 * This method is called after the last sequence, to allow for example handling the reporting of the session
	 */
	@Override
	protected void closeTestSession() {
		super.closeTestSession();
		// Extract and create JaCoCo run coverage report for Generate Mode
		if(settings.get(ConfigTags.Mode).equals(Modes.Generate)) {
			extractJacocoRunReport();
			compressJacocoReportFolder();
		}
	}
}