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

import java.util.Objects;
import java.util.Set;
import nl.ou.testar.PrioritizeNewActionsSelector;
import org.fruit.alayer.*;
import org.fruit.alayer.exceptions.*;
import org.fruit.monkey.Settings;
import org.testar.protocols.JavaSwingProtocol;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.fruit.Util;
import org.fruit.alayer.windows.AccessBridgeFetcher;
import org.fruit.alayer.windows.UIATags;
import org.fruit.alayer.windows.WinProcess;
import org.fruit.monkey.ConfigTags;
import org.fruit.alayer.actions.AnnotatingActionCompiler;
import org.fruit.alayer.actions.CompoundAction;
import org.fruit.alayer.actions.KeyDown;
import org.fruit.alayer.actions.KeyUp;
import org.fruit.alayer.actions.StdActionCompiler;
import org.fruit.alayer.devices.KBKeys;
import org.testar.OutputStructure;
import static org.fruit.alayer.Tags.Blocked;
import static org.fruit.alayer.Tags.Enabled;

import java.io.FileWriter;

/**
 * This protocol together with the settings provides a specific behavior to test rachota
 * We will use Java Access Bridge settings (AccessBridgeEnabled = true) for widget tree extraction
 *
 * It uses PrioritizeNewActionsSelector algorithm.
 */
public class Protocol_rachota_prioritize_new_actions extends JavaSwingProtocol {
	
	private long startSequenceTime;
	private String reportTimeDir;

	// rachota: sometimes SUT stop responding, we need this empty actions countdown
	private int countEmptyStateTimes = 0;

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
		
		// rachota: Requires Java Access Bridge
		System.out.println("Are we running Java Access Bridge ? " + settings.get(ConfigTags.AccessBridgeEnabled, false));
		
		// TESTAR will execute the SUT with Java
		// We need this to add JMX parameters properly (-Dcom.sun.management.jmxremote.port=5000)
		WinProcess.java_execution = true;
		
		// Enable the inspection of internal cells on Java Swing Tables
		AccessBridgeFetcher.swingJavaTableDescend = true;
		
		// Copy "bin/settings/protocolName/build.xml" file to "bin/jacoco/build.xml"
		copyJacocoBuildFile();
	}
	
	/**
	 * This methods is called before each test sequence, allowing for example using external profiling software on the SUT
	 */
	@Override
	protected void preSequencePreparations() {
		super.preSequencePreparations();
		try {
			// Create rachota settings configuration file, and disable detectInactivity feature
			File rachotaFile = new File("C:\\Users\\testar\\.rachota");
			if(!rachotaFile.exists()) {
				rachotaFile.mkdirs();
			}
			File rachotaSettings = new File("C:\\Users\\testar\\.rachota\\settings.cfg");
			if(rachotaSettings.createNewFile() || rachotaFile.exists()) {
				FileWriter settingsWriter = new FileWriter("C:\\Users\\testar\\.rachota\\settings.cfg");
				settingsWriter.write("detectInactivity = false");
				settingsWriter.close();
			}
		} catch (Exception e) {
			System.out.println("ERROR trying to disable detectInactivity configuration feature");
		}
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
		
		// wait 10 seconds, give time to rachota to start
		Util.pause(10);

		// reset values
		countEmptyStateTimes = 0;
		
		super.beginSequence(system, state);

		// rachota: predefined action to deal with initial pop-up question
		// If stopSystem configuration works (delete rachota folder) we will not need this
		for(Widget w : state) {
			if(w.get(Tags.Title,"").contains("Rachota is already running or it was not")) {
				waitAndLeftClickWidgetWithMatchingTag(Tags.Title, "Yes", state, system, 5, 1);
				// Wait and update the state
				Util.pause(10);
				state = super.getState(system);
			}
			// Dutch
			if(w.get(Tags.Title,"").contains("was de vorige keer niet normaal afgesloten")) {
				waitAndLeftClickWidgetWithMatchingTag(Tags.Title, "Ja", state, system, 5, 1);
				// Wait and update the state
				Util.pause(10);
				state = super.getState(system);
			}
		}
	 }
	 
	 /**
	  * This method is called when the TESTAR requests the state of the SUT.
	  * Here you can add additional information to the SUT's state or write your
	  * own state fetching routine. The state should have attached an oracle
	  * (TagName: <code>Tags.OracleVerdict</code>) which describes whether the
	  * state is erroneous and if so why.
	  *
	  * super.getState(system) puts the state information also to the HTML sequence report
	  *
	  * @return  the current state of the SUT with attached oracle.
	  */
	 @Override
	 protected State getState(SUT system) throws StateBuildException {
		 State state = super.getState(system);
		 // rachota: check at windows level if SUT stops responding
		 try {
			 if(WinProcess.isHungWindow(system.get(Tags.HWND, (long)0))) {
				 Verdict freezeVerdict = new Verdict(Verdict.SEVERITY_NOT_RESPONDING, "WinProcess.isHungWindow(system) Rachota stops responding");
				 state.set(Tags.OracleVerdict, freezeVerdict);
			 }
		 } catch(Exception e) {
			 System.out.println("Error trying to apply not responding verdict to system.get(Tags.HWND)");
		 }
		 return state;
	 }

	 /**
	  * The getVerdict methods implements the online state oracles that
	  * examine the SUT's current state and returns an oracle verdict.
	  * @return oracle verdict, which determines whether the state is erroneous and why.
	  */
	 @Override
	 protected Verdict getVerdict(State state){
		 // The super methods implements the implicit online state oracles for:
		 // system crashes
		 // non-responsiveness
		 // suspicious titles
		 Verdict verdict = super.getVerdict(state);

		 if(countEmptyStateTimes > 2) {
			 return new Verdict(Verdict.SEVERITY_NOT_RESPONDING, "Seems that rachota SUT is not responding");
		 }

		 //--------------------------------------------------------
		 // MORE SOPHISTICATED STATE ORACLES CAN BE PROGRAMMED HERE
		 //--------------------------------------------------------

		 return verdict;
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
	 @Override
	 protected Set<Action> deriveActions(SUT system, State state) throws ActionBuildException{

		 //The super method returns a ONLY actions for killing unwanted processes if needed, or bringing the SUT to
		 //the foreground. You should add all other actions here yourself.
		 // These "special" actions are prioritized over the normal GUI actions in selectAction() / preSelectAction().
		 Set<Action> actions = super.deriveActions(system,state);

		 // To derive actions (such as clicks, drag&drop, typing ...) we should first create an action compiler.
		 StdActionCompiler ac = new AnnotatingActionCompiler();

		 /**
		  * Specific Action Derivation for rachota SUT
		  * To avoid deriving actions on non-desired widgets
		  * 
		  * Optional : for(Widget w : state)
		  * If selected also change it for all rachota protocols
		  */

		 // iterate through top level widgets
		 for(Widget w : getTopWidgets(state)){
			 
			 // rachota: add filename report
			 if(w.get(Tags.Role, Roles.Widget).toString().equalsIgnoreCase("UIAEdit")
					 && w.get(Tags.Title,"").contains("Filename:")) {
				 addFilenameReportAction(w, actions, ac);
			 }

			 if(w.get(Enabled, true) && !w.get(Blocked, false)){ // only consider enabled and non-blocked widgets

				 if (!blackListed(w)){  // do not build actions for tabu widgets  

					 // left clicks
					 if((isClickable(w)) && (isUnfiltered(w) || whiteListed(w))) {
						 actions.add(ac.leftClickAt(w));
					 }

					 // rachota: left clicks on specific widgets
					 if((isEditToClickWidget(w) || isCalendarTextWidget(w)) && (isUnfiltered(w) || whiteListed(w))) {
						 actions.add(ac.leftClickAt(w));
					 }

					 // left click in Table Cells
					 if(isTableCell(w) && (isUnfiltered(w) || whiteListed(w))) {
						 actions.add(ac.leftClickAt(w));
					 }

					 // rachota: use spinboxes
					 if(isSpinBoxWidget(w) && (isUnfiltered(w) || whiteListed(w))) {
						 addIncreaseDecreaseActions(w, actions, ac);
					 }

					 // type into text boxes
					 // rachota: edit widgets have tooltiptext (isEditableWidget)
					 if((isTypeable(w) && (isUnfiltered(w) || whiteListed(w))) && isEditableWidget(w)) {
						 actions.add(ac.clickTypeInto(w, this.getRandomText(w), true));
					 }

					 // rachota: custom number for this generate reporting field
					 if(w.get(Tags.Role, Roles.Widget).toString().equalsIgnoreCase("UIAEdit")
							 && w.get(Tags.Title,"").contains("Price per hour")) {
						 forcePricePerHourAndFinish(w, actions, ac);
					 }
					 if(w.get(Tags.Role, Roles.Widget).toString().equalsIgnoreCase("UIAEdit")
							 && w.get(Tags.Title,"").contains("Tax:")) {
						 actions.add(ac.clickTypeInto(w, "3", true));
					 }

					 // GENERIC: All swing apps
					 /** Force actions on some widgets with a wrong accessibility **/
					 // Optional feature, comment out this changes if your Swing applications doesn't need it

					 // Tree List elements have plain "text" items have child nodes
					 // We need to derive a click action on them
					 if(w.get(Tags.Role).toString().contains("Tree")) {
						 forceWidgetTreeClickAction(w, actions);
					 }
					 // Combo Box elements also have List Elements
					 // Lists elements needs a special derivation to check widgets visibility
					 if(w.get(Tags.Role).toString().contains("List")) {
						 forceListElemetsClickAction(w, actions);
					 }
					 /** End of Force action **/
				 }
			 }
		 }

		 // PrioritizeNewActionsSelector: pick prioritized actions
		 actions = selector.getPrioritizedActions(actions);

		 // rachota: sometimes rachota freezes, TESTAR detects the SUT
		 // but cannot extract anything at JavaAccessBridge level
		 // Use this count for Verdict
		 if(actions.isEmpty()) {
			 System.out.println("Empty Actions on State!");
			 try {
				 // Windows level call to check if application is not responding.
				 if(WinProcess.isHungWindow(state.child(0).get(Tags.HWND, (long)0))) {
					 countEmptyStateTimes = countEmptyStateTimes + 5;
				 }
			 } catch (IndexOutOfBoundsException iobe) {
				 System.out.println("TESTAR State has no childs!");
				 countEmptyStateTimes = countEmptyStateTimes + 1;
				 // Wait a bit in case SUT is updating something internally
				 Util.pause(5);
			 } catch (Exception e) {
				 e.printStackTrace();
			 }
		 } else {
			 countEmptyStateTimes = 0;
		 }

		 return actions;

	 }

	 /**
	  * Rachota:
	  * This SUT have the functionality of create invoices and reports
	  * Create an action that prepares a filename to create this report
	  */
	 private void addFilenameReportAction(Widget filenameWidget, Set<Action> actions, StdActionCompiler ac) {

		 // Get Next widget
		 Widget nextButton = filenameWidget;
		 for(Widget checkWidget: filenameWidget.root()) { 
			 if(checkWidget.get(Tags.Title,"").contains("Next")) {
				 nextButton = checkWidget;
			 }
		 }

		 // type filename, use tab to complete the path, and click next
		 Action addFilename = new CompoundAction.Builder()   
				 .add(ac.clickTypeInto(filenameWidget, Util.dateString(OutputStructure.DATE_FORMAT), true), 0.5) // Click and type
				 .add(new KeyDown(KBKeys.VK_TAB),0.5) // Press TAB keyboard
				 .add(new KeyUp(KBKeys.VK_TAB),0.5) // Release Keyboard
				 .add(ac.leftClickAt(nextButton), 0.5).build(); //Click next

		 addFilename.set(Tags.Role, Roles.Button);
		 addFilename.set(Tags.OriginWidget, filenameWidget);
		 addFilename.set(Tags.Desc, "Add Filename Report");
		 actions.add(addFilename);
	 }

	 /**
	  * Rachota:
	  * Force the input of a correct price and click finish to create the invoice report
	  */
	 private void forcePricePerHourAndFinish(Widget priceWidget, Set<Action> actions, StdActionCompiler ac) {
		 // Get Finish widget
		 Widget finishButton = priceWidget;
		 for(Widget checkWidget: priceWidget.root()) { 
			 if(checkWidget.get(Tags.Title,"").contains("Finish")) {
				 finishButton = checkWidget;
			 }
		 }

		 // type price, use tab to complete the path, and click finish
		 Action forcePriceFinish = new CompoundAction.Builder()   
				 .add(ac.clickTypeInto(priceWidget, "3", true), 0.5) // Click and type
				 .add(new KeyDown(KBKeys.VK_TAB),0.5) // Press TAB keyboard
				 .add(new KeyUp(KBKeys.VK_TAB),0.5) // Release Keyboard
				 .add(ac.leftClickAt(finishButton), 0.5).build(); //Click next

		 forcePriceFinish.set(Tags.Role, Roles.Button);
		 forcePriceFinish.set(Tags.OriginWidget, priceWidget);
		 forcePriceFinish.set(Tags.Desc, "forcePriceAndFinish");
		 actions.add(forcePriceFinish);
	 }

	 /**
	  * Rachota:
	  * Some Edit widgets allow click actions
	  */
	 private boolean isEditToClickWidget(Widget w) {
		 if(w.get(Tags.Role, Roles.Widget).toString().equalsIgnoreCase("UIAEdit")){
			 return w.get(Tags.ToolTipText,"").contains("Mouse click");
		 }
		 return false;
	 }

	 /**
	  * Rachota:
	  * Seems that interactive Edit elements have tool type text with instructions
	  * Then if ToolTipText exists, the widget is interactive
	  * Disable price per hour random text, customize number
	  * Disable taxes prize, customize number
	  */
	 private boolean isEditableWidget(Widget w) {
		 String toolTipText = w.get(Tags.ToolTipText,"");
		 return !toolTipText.trim().isEmpty() && !toolTipText.contains("text/plain") 
				 && !toolTipText.contains("Mouse click") &&
				 !w.get(Tags.Title,"").contains("Price per hour") && !w.get(Tags.Title,"").contains("Tax:");
	 }

	 /**
	  * Rachota + Swing:
	  * Check if it is a Table Cell widget
	  */
	 private boolean isTableCell(Widget w) {
		 return w.get(UIATags.UIAAutomationId, "").contains("TableCell");
	 }

	 /**
	  * Rachota:
	  * Tricky way to check if current text widgets is a potential calendar number
	  */
	 private boolean isCalendarTextWidget(Widget w) {
		 if(w.parent() != null && w.parent().get(Tags.Role, Roles.Widget).toString().equalsIgnoreCase("UIAPane")) {
			 int calendarDay = getNumericInt(w.get(Tags.Title, ""));
			 if(0 < calendarDay && calendarDay < 31){
				 return true;
			 }
		 }
		 return false;
	 }

	 private int getNumericInt(String strNum) {
		 if (strNum == null) {
			 return -1;
		 }
		 try {
			 return Integer.parseInt(strNum);
		 } catch (NumberFormatException nfe) {
			 return -1;
		 }
	 }

	 /**
	  * Rachota + Swing:
	  * Check if it is a Spinner widget
	  */
	 private boolean isSpinBoxWidget(Widget w) {
		 return w.get(Tags.Role, Roles.Widget).toString().equalsIgnoreCase("UIASpinner");
	 }

	 /**
	  * Rachota + Swing:
	  * SpinBox widgets buttons seems that do not exist as unique element,
	  * derive click + keyboard action to increase or decrease
	  */
	 private void addIncreaseDecreaseActions(Widget w, Set<Action> actions, StdActionCompiler ac) {
		 Action increase = new CompoundAction.Builder()   
				 .add(ac.leftClickAt(w), 0.5) // Click for focus 
				 .add(new KeyDown(KBKeys.VK_UP),0.5) // Press Up Arrow keyboard
				 .add(new KeyUp(KBKeys.VK_UP),0.5).build(); // Release Keyboard

		 increase.set(Tags.Role, Roles.Button);
		 increase.set(Tags.OriginWidget, w);
		 increase.set(Tags.Desc, "Increase Spinner");

		 Action decrease = new CompoundAction.Builder()   
				 .add(ac.leftClickAt(w), 0.5) // Click for focus 
				 .add(new KeyDown(KBKeys.VK_DOWN),0.5) // Press Down Arrow keyboard
				 .add(new KeyUp(KBKeys.VK_DOWN),0.5).build(); // Release Keyboard

		 decrease.set(Tags.Role, Roles.Button);
		 decrease.set(Tags.OriginWidget, w);
		 decrease.set(Tags.Desc, "Decrease Spinner");

		 actions.add(increase);
		 actions.add(decrease);
	 }

	 /**
	  * Iterate through the child of the specified widget to derive a click Action
	  */
	 private void forceWidgetTreeClickAction(Widget w, Set<Action> actions) {
		 StdActionCompiler ac = new AnnotatingActionCompiler();
		 actions.add(ac.leftClickAt(w));
		 w.set(Tags.ActionSet, actions);
		 for(int i = 0; i<w.childCount(); i++) {
			 forceWidgetTreeClickAction(w.child(i), actions);
		 }
	 }

	 /**
	  * Derive a click Action on visible List dropdown elements
	  */
	 public void forceListElemetsClickAction(Widget w, Set<Action> actions) {
		 if(!Objects.isNull(w.parent())) {
			 Widget parentContainer = w.parent();
			 Rect visibleContainer = Rect.from(parentContainer.get(Tags.Shape).x(), parentContainer.get(Tags.Shape).y(),
					 parentContainer.get(Tags.Shape).width(), parentContainer.get(Tags.Shape).height());

			 forceComboBoxClickAction(w, visibleContainer, actions);
		 }
	 }

	 /**
	  * Derive a click Action if widget rect bounds are inside the visible container
	  */
	 public void forceComboBoxClickAction(Widget w, Rect visibleContainer, Set<Action> actions) {
		 StdActionCompiler ac = new AnnotatingActionCompiler();
		 try {
			 Rect widgetContainer = Rect.from(w.get(Tags.Shape).x(), w.get(Tags.Shape).y(),
					 w.get(Tags.Shape).width(), w.get(Tags.Shape).height());

			 if(Rect.contains(visibleContainer, widgetContainer)) {
				 actions.add(ac.leftClickAt(w));
				 w.set(Tags.ActionSet, actions);
			 }

			 for(int i = 0; i<w.childCount(); i++) {
				 forceComboBoxClickAction(w.child(i), visibleContainer, actions);
			 }
		 } catch(Exception e) {}
	 }

	 @Override
	 protected boolean isUnfiltered(Widget w) {
		 // Rachota: Filter Cancel button widgets if we are in the report generation window
		 if(w.get(Tags.Title,"").equals("Cancel")) {
			 for(Widget widget : w.root()) {
				 if(widget.get(Tags.Title,"").contains("Report generation wizard")) {
					 return false;
				 }
			 }
		 }
		 return super.isUnfiltered(w);
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

		String rachotaPath = "C:\\Users\\testar\\.rachota";

		// Delete rachota files then next sequence will have same initial state without tasks
		if(new File(rachotaPath).exists()) {
			try {
				FileUtils.deleteDirectory(new File(rachotaPath));
			} catch(Exception e) {System.out.println("ERROR deleting rachota folder");}
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
