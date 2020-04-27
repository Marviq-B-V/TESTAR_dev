/***************************************************************************************************
 *
 * Copyright (c) 2019, 2020 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2019, 2020 Open Universiteit - www.ou.nl
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


package org.testar.protocols;

import static org.fruit.alayer.Tags.Blocked;
import static org.fruit.alayer.Tags.Enabled;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.fruit.Environment;
import org.fruit.Util;
import org.fruit.Pair;
import org.fruit.alayer.Action;
import org.fruit.alayer.SUT;
import org.fruit.alayer.Shape;
import org.fruit.alayer.State;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Verdict;
import org.fruit.alayer.Visualizer;
import org.fruit.alayer.Widget;
import org.fruit.alayer.actions.*;
import org.fruit.alayer.exceptions.StateBuildException;
import org.fruit.alayer.exceptions.SystemStartException;
import org.fruit.alayer.visualizers.ShapeVisualizer;
import org.fruit.alayer.webdriver.WdDriver;
import org.fruit.alayer.webdriver.WdElement;
import org.fruit.alayer.webdriver.WdWidget;
import org.fruit.alayer.webdriver.enums.WdTags;
import org.fruit.alayer.windows.WinProcess;
import org.fruit.alayer.windows.Windows;
import org.fruit.monkey.ConfigTags;
import org.testar.OutputStructure;

import es.upv.staq.testar.NativeLinker;
import es.upv.staq.testar.protocols.ClickFilterLayerProtocol;
import es.upv.staq.testar.serialisation.LogSerialiser;
import nl.ou.testar.HtmlReporting.HtmlSequenceReport;

public class WebdriverProtocol extends ClickFilterLayerProtocol {
	//Attributes for adding slide actions
	protected static double SCROLL_ARROW_SIZE = 36; // sliding arrows
	protected static double SCROLL_THICK = 16; //scroll thickness
	protected HtmlSequenceReport htmlReport;
	protected State latestState;
	
	protected boolean naughtyStrings = false;

    protected static Set<String> existingCssClasses = new HashSet<>();
    
	protected Pattern actionFilterWebIdPattern = null;
	protected Pattern actionFilterWebHrefPattern = null;
	protected Map<String, Matcher> actionFilterWebIdMatchers = new WeakHashMap<>();
	protected Map<String, Matcher> actionFilterWebHrefMatchers = new WeakHashMap<>();
	
	protected Pattern suspiciousValuePattern = null;
	protected Map<String, Matcher> suspiciousValueMatchers = new WeakHashMap<>();

	// Classes that are deemed clickable by the web framework
	protected List<String> clickableClasses = new ArrayList<>();
	protected List<String> clickableLabels = new ArrayList<>();

	// Disallow links and pages with these extensions
	// Set to null to ignore this feature
	protected List<String> deniedExtensions = new ArrayList<>();

	// Define a whitelist of allowed domains for links and pages
	// An empty list will be filled with the domain from the sut connector
	// Set to null to ignore this feature
	protected List<String> domainsAllowed = null;

	// If true, follow links opened in new tabs
	// If false, stay with the original (ignore links opened in new tabs)
	protected boolean followLinks = true;

	// URL + form name, username input id + value, password input id + value
	// Set login to null to disable this feature
	protected Pair<String, String> login = Pair.from("https://login.awo.ou.nl/SSO/login", "OUinloggen");
	protected Pair<String, String> username = Pair.from("username", "");
	protected Pair<String, String> password = Pair.from("password", "");

	// List of atributes to identify and close policy popups
	// Set to null to disable this feature
	protected Map<String, String> policyAttributes =
			new HashMap<String, String>() {{
				put("id", "_cookieDisplay_WAR_corpcookieportlet_okButton");
			}};

	/**
	 * This methods is called before each test sequence, allowing for example using external profiling software on the SUT
	 */
	@Override
	protected void preSequencePreparations() {

		if(!new File(settings.get(ConfigTags.InputFileText)).exists()) {
			System.out.println("Warning: BLNS file from "+ settings.get(ConfigTags.ProtocolClass) + " settings cannot be readed, "
					+ "check if the current value is correct: " + settings.get(ConfigTags.InputFileText));
			System.out.println("Example of correct value: \"InputFileText = ./settings/webdriver_path/blns.txt\" ");
			System.out.println("If you want to use a set of advanced Strings to test the text input fields, "
					+ "please configure properly the InputFileText setting");
		}

		//initializing the HTML sequence report:
		htmlReport = new HtmlSequenceReport();
	}

	/**
	 * This method is called when TESTAR starts the System Under Test (SUT). The method should
	 * take care of
	 * 1) starting the SUT (you can use TESTAR's settings obtainable from <code>settings()</code> to find
	 * out what executable to run)
	 * 2) bringing the system into a specific start state which is identical on each start (e.g. one has to delete or restore
	 * the SUT's configuratio files etc.)
	 * 3) waiting until the system is fully loaded and ready to be tested (with large systems, you might have to wait several
	 * seconds until they have finished loading)
	 *
	 * @return a started SUT, ready to be tested.
	 */
	@Override
	protected SUT startSystem() throws SystemStartException {
		SUT sut = super.startSystem();

		// A workaround to obtain the browsers window handle, ideally this information is acquired when starting the
		// webdriver in the constructor of WdDriver.
		// A possible solution could be creating a snapshot of the running browser processes before and after
		if(System.getProperty("os.name").contains("Windows")
				&& sut.get(Tags.HWND, null) == null) {
			// Note don't place a breakpoint here since the outcome of the function call will result in the IDE pid and
			// window handle. The running browser needs to be in the foreground when we reach this part.
			long hwnd = Windows.GetForegroundWindow();
			long pid = Windows.GetWindowProcessId(Windows.GetForegroundWindow());
			// Safe to set breakpoints again.
			if (WinProcess.procName(pid).contains("chrome")) {
				sut.set(Tags.HWND, hwnd);
				sut.set(Tags.PID, pid);
				System.out.printf("INFO System PID %d and window handle %d have been set\n", pid, hwnd);
			}
		}

		double displayScale = Environment.getInstance().getDisplayScale(sut.get(Tags.HWND, (long)0));

		// See remarks in WdMouse
		mouse = sut.get(Tags.StandardMouse);
		mouse.setCursorDisplayScale(displayScale);

		return sut;
	}

	/**
	 * This method is invoked each time the TESTAR starts the SUT to generate a new sequence.
	 * This can be used for example for bypassing a login screen by filling the username and password
	 * or bringing the system into a specific start state which is identical on each start (e.g. one has to delete or restore
	 * the SUT's configuration files etc.)
	 */
	@Override
	protected void beginSequence(SUT system, State state) {
		super.beginSequence(system, state);
	}

	/**
	 * This method is called when the TESTAR requests the state of the SUT.
	 * Here you can add additional information to the SUT's state or write your
	 * own state fetching routine. The state should have attached an oracle
	 * (TagName: <code>Tags.OracleVerdict</code>) which describes whether the
	 * state is erroneous and if so why.
	 * @return  the current state of the SUT with attached oracle.
	 */
	@Override
	protected State getState(SUT system) throws StateBuildException {

		WdDriver.waitDocumentReady();

		State state = super.getState(system);

		if(settings.get(ConfigTags.ForceForeground)
				&& System.getProperty("os.name").contains("Windows")
				&& system.get(Tags.PID, (long)-1) != (long)-1 
				&& WinProcess.procName(system.get(Tags.PID)).contains("chrome") 
				&& !WinProcess.isForeground(system.get(Tags.PID))){
			WinProcess.politelyToForeground(system.get(Tags.HWND));
			LogSerialiser.log("Trying to set Chrome Browser to Foreground... " 
					+ WinProcess.procName(system.get(Tags.PID)) + "\n");
		}

		latestState = state;

		//Spy mode didn't use the html report
		if(settings.get(ConfigTags.Mode) == Modes.Spy) {

			for(Widget w : state) {
				WdElement element = ((WdWidget) w).element;
				for(String s : element.cssClasses) {
					existingCssClasses.add(s);
				}
			}

			return state;
		}

		//adding state to the HTML sequence report:
		htmlReport.addState(latestState);
		return latestState;
	}
	
	@Override
	protected Verdict getVerdict(State state){

		Verdict currentVerdict = super.getVerdict(state);
		if(!currentVerdict.equals(Verdict.OK)) {
			return currentVerdict;
		}
		
		this.suspiciousValuePattern = Pattern.compile(settings().get(ConfigTags.SuspiciousWebTitle), Pattern.UNICODE_CHARACTER_CLASS);
		this.suspiciousValueMatchers = new WeakHashMap<>();
		Verdict webTitleVerdict = suspiciousValueMatcher(WdTags.WebTitle, state);
		if(!webTitleVerdict.equals(Verdict.OK)) {
			return webTitleVerdict;
		}
		
		this.suspiciousValuePattern = Pattern.compile(settings().get(ConfigTags.SuspiciousWebName), Pattern.UNICODE_CHARACTER_CLASS);
		this.suspiciousValueMatchers = new WeakHashMap<>();
		Verdict webNameVerdict = suspiciousValueMatcher(WdTags.WebName, state);
		if(!webNameVerdict.equals(Verdict.OK)) {
			return webNameVerdict;
		}
		
		this.suspiciousValuePattern = Pattern.compile(settings().get(ConfigTags.SuspiciousWebHref), Pattern.UNICODE_CHARACTER_CLASS);
		this.suspiciousValueMatchers = new WeakHashMap<>();
		Verdict webHrefVerdict = suspiciousValueMatcher(WdTags.WebHref, state);
		if(!webHrefVerdict.equals(Verdict.OK)) {
			return webHrefVerdict;
		}

		return currentVerdict;
	}
	
	private Verdict suspiciousValueMatcher(Tag<String> tag, State state) {
		for(Widget w : state) {
			if(tag!=null && !w.get(tag, "").isEmpty()) {
				String value = w.get(tag,"");
				Matcher matcher = this.suspiciousValueMatchers.get(value);
				if (matcher == null){
					matcher = this.suspiciousValuePattern.matcher(value);
					this.suspiciousValueMatchers.put(value, matcher);
				}
				
				if (matcher.matches()){
					Visualizer visualizer = Util.NullVisualizer;
					// visualize the problematic widget, by marking it with a red box
					if(w.get(Tags.Shape, null) != null)
						visualizer = new ShapeVisualizer(RedPen, w.get(Tags.Shape), "Suspicious Title", 0.5, 0.5);
					return new Verdict(Verdict.SEVERITY_SUSPICIOUS_TITLE, 
							"Discovered suspicious widget '" + tag.name() + "' : '" + value + "'.", visualizer);
				}
			}
		}
		
		return Verdict.OK;
	}
    
	/**
	 * Check whether widget w should be filtered based on
	 * its title (matching the regular expression of the Dialog --> clickFilterPattern)
	 * that is cannot be hit
	 * @param w
	 * @return
	 */
    @Override
	protected boolean isUnfiltered(Widget w){
    	
		//Check whether the widget can be hit
		// If not, it should be filtered
		if(!Util.hitTest(w, 0.5, 0.5))
			return false;

		String webId = w.get(WdTags.WebId, "");
		if (!webId.isEmpty()) {
			//If no ActionFilterPattern exists, then create it
			//Get the ActionFilterPattern from the regular expression provided by the tester in the Dialog
			if (this.actionFilterWebIdPattern == null) {
				this.actionFilterWebIdPattern = Pattern.compile(settings().get(ConfigTags.ActionFilterWebId), Pattern.UNICODE_CHARACTER_CLASS);
			}
			//Check whether the title matches any of the ActionFilterPattern
			Matcher matcherWebId = this.actionFilterWebIdMatchers.get(webId);
			if (matcherWebId == null){
				matcherWebId = this.actionFilterWebIdPattern.matcher(webId);
				this.actionFilterWebIdMatchers.put(webId, matcherWebId);
			}
			// If we match some pattern, is not unfiltered
			if(matcherWebId.matches()) {
				return false;
			}
		}
		
		String webHref = w.get(WdTags.WebHref, "");
		if (!webHref.isEmpty()) {
			if (this.actionFilterWebHrefPattern == null) {
				this.actionFilterWebHrefPattern = Pattern.compile(settings().get(ConfigTags.ActionFilterWebHref), Pattern.UNICODE_CHARACTER_CLASS);
			}

			Matcher matcherWebHref = this.actionFilterWebHrefMatchers.get(webHref);
			if (matcherWebHref == null){
				matcherWebHref = this.actionFilterWebHrefPattern.matcher(webHref);
				this.actionFilterWebHrefMatchers.put(webHref, matcherWebHref);
			}
			// If we match some pattern, is not unfiltered
			if(matcherWebHref.matches()) {
				return false;
			}
		}
    	
		return super.isUnfiltered(w);
	}

	/**
	 * Overwriting to add HTML report writing into it
	 *
	 * @param state
	 * @param actions
	 * @return
	 */
	@Override
	protected Action preSelectAction(State state, Set<Action> actions){
		// adding available actions into the HTML report:
		htmlReport.addActions(actions);
		return(super.preSelectAction(state, actions));
	}

	/**
	 * Execute the selected action.
	 * @param system the SUT
	 * @param state the SUT's current state
	 * @param action the action to execute
	 * @return whether or not the execution succeeded
	 */
	@Override
	protected boolean executeAction(SUT system, State state, Action action){
		// adding the action that is going to be executed into HTML report:
		htmlReport.addSelectedAction(state, action);
		return super.executeAction(system, state, action);
	}

	/**
	 * This methods is called after each test sequence, allowing for example using external profiling software on the SUT
	 */
	@Override
	protected void postSequenceProcessing() {
		htmlReport.addTestVerdict(getVerdict(latestState).join(processVerdict));

		String sequencesPath = getGeneratedSequenceName();
		try {
			sequencesPath = new File(getGeneratedSequenceName()).getCanonicalPath();
		}catch (Exception e) {}

		String status = (getVerdict(latestState).join(processVerdict)).verdictSeverityTitle();
		String statusInfo = (getVerdict(latestState).join(processVerdict)).info();

		statusInfo = statusInfo.replace("\n"+Verdict.OK.info(), "");

		//Timestamp(generated by logback.xml) SUTname Mode SequenceFileObject Status "StatusInfo"
		INDEXLOG.info(OutputStructure.executedSUTname
				+ " " + settings.get(ConfigTags.Mode, mode())
				+ " " + sequencesPath
				+ " " + status + " \"" + statusInfo + "\"" );

		//Print into command line the result of the execution, useful to work with CI and timestamps
		System.out.println(OutputStructure.executedSUTname
				+ " " + settings.get(ConfigTags.Mode, mode())
				+ " " + sequencesPath
				+ " " + status + " \"" + statusInfo + "\"" );
	}

	@Override
	protected void finishSequence(){
		//With webdriver version we don't use the call SystemProcessHandling.killTestLaunchedProcesses
	}

	@Override
	protected void stopSystem(SUT system) {
		if(settings.get(ConfigTags.Mode) == Modes.Spy) {

			try {

				File folder = new File(settings.getSettingsPath());
				File file = new File(folder, "existingCssClasses.txt");
				if(!file.exists())
					file.createNewFile();

				Stream<String> stream = Files.lines(Paths.get(file.getCanonicalPath()));
				stream.forEach(line -> existingCssClasses.add(line));
				stream.close();

				PrintWriter write = new PrintWriter(new FileWriter(file.getCanonicalPath()));
				for(String s : existingCssClasses)
					write.println(s);
				write.close();

			} catch (IOException e) {System.out.println(e.getMessage());}

			//System.out.println("* " + existingCssClasses.size()+ " * Existing Css Classes: " + existingCssClasses.toString());

		}
		super.stopSystem(system);
	}

	@Override
	protected void closeTestSession() {
		super.closeTestSession();
		NativeLinker.cleanWdDriverOS();
	}

	/*
	 * Check the state if we need to force an action
	 */
	protected Set<Action> detectForcedActions(State state, StdActionCompiler ac) {
		Set<Action> actions = detectForcedDeniedUrl();
		if (actions != null && actions.size() > 0) {
			return actions;
		}

		actions = detectForcedLogin(state);
		if (actions != null && actions.size() > 0) {
			return actions;
		}

		actions = detectForcedPopupClick(state, ac);
		if (actions != null && actions.size() > 0) {
			return actions;
		}

		return null;
	}

	/*
	 * Detect and perform login if defined
	 */
	protected Set<Action> detectForcedLogin(State state) {
		if (login == null || username == null || password == null) {
			return null;
		}

		// Check if the current page is a login page
		String currentUrl = WdDriver.getCurrentUrl();
		if (currentUrl.startsWith(login.left())) {
			CompoundAction.Builder builder = new CompoundAction.Builder();
			// Set username and password
			for (Widget widget : state) {
				WdWidget wdWidget = (WdWidget) widget;
				// Only enabled, visible widgets
				if (!widget.get(Enabled, true) || widget.get(Blocked, false)) {
					continue;
				}

				if (username.left().equals(wdWidget.getAttribute("id"))) {
					builder.add(new WdAttributeAction(
							username.left(), "value", username.right()), 1);
				}
				else if (password.left().equals(wdWidget.getAttribute("id"))) {
					builder.add(new WdAttributeAction(
							password.left(), "value", password.right()), 1);
				}
			}
			// Submit form, but only if user and pass are filled
			builder.add(new WdSubmitAction(login.right()), 2);
			CompoundAction actions = builder.build();
			if (actions.getActions().size() >= 3) {
				return new HashSet<>(Collections.singletonList(actions));
			}
		}

		return null;
	}

	/*
	 * Force closing of Policies Popup
	 */
	protected Set<Action> detectForcedPopupClick(State state,
			StdActionCompiler ac) {
		if (policyAttributes == null || policyAttributes.size() == 0) {
			return null;
		}

		for (Widget widget : state) {
			// Only enabled, visible widgets
			if (!widget.get(Enabled, true) || widget.get(Blocked, false)) {
				continue;
			}

			WdElement element = ((WdWidget) widget).element;
			boolean isPopup = true;
			for (Map.Entry<String, String> entry : policyAttributes.entrySet()) {
				String attribute = element.attributeMap.get(entry.getKey());
				isPopup &= entry.getValue().equals(attribute);
			}
			if (isPopup) {
				return new HashSet<>(Collections.singletonList(ac.leftClickAt(widget)));
			}
		}

		return null;
	}

	/*
	 * Force back action due to disallowed domain or extension
	 */
	protected Set<Action> detectForcedDeniedUrl() {
		String currentUrl = WdDriver.getCurrentUrl();

		// Don't get caught in PDFs etc. and non-whitelisted domains
		if (isUrlDenied(currentUrl) || isExtensionDenied(currentUrl)) {
			// If opened in new tab, close it
			if (WdDriver.getWindowHandles().size() > 1) {
				return new HashSet<>(Collections.singletonList(new WdCloseTabAction()));
			}
			// Single tab, go back to previous page
			else {
				return new HashSet<>(Collections.singletonList(new WdHistoryBackAction()));
			}
		}

		return null;
	}

	/*
	 * Check if the current address has a denied extension (PDF etc.)
	 */
	protected boolean isExtensionDenied(String currentUrl) {
		// If the current page doesn't have an extension, always allow
		if (!currentUrl.contains(".")) {
			return false;
		}

		if (deniedExtensions == null || deniedExtensions.size() == 0) {
			return false;
		}

		// Deny if the extension is in the list
		String ext = currentUrl.substring(currentUrl.lastIndexOf(".") + 1);
		ext = ext.replace("/", "").toLowerCase();
		return deniedExtensions.contains(ext);
	}

	/*
	 * Check if the URL is denied
	 */
	protected boolean isUrlDenied(String currentUrl) {
		if (currentUrl.startsWith("mailto:")) {
			return true;
		}

		// Always allow local file
		if (currentUrl.startsWith("file:///")) {
			return false;
		}

		// User wants to allow all
		if (domainsAllowed == null) {
			return false;
		}

		// Only allow pre-approved domains
		String domain = getDomain(currentUrl);
		return !domainsAllowed.contains(domain);
	}

	/*
	 * Check if the widget has a denied URL as hyperlink
	 */
	protected boolean isLinkDenied(Widget widget) {
		String linkUrl = widget.get(Tags.ValuePattern, "");

		// Not a link or local file, allow
		if (linkUrl == null || linkUrl.startsWith("file:///")) {
			return false;
		}

		// Deny the link based on extension
		if (isExtensionDenied(linkUrl)) {
			return true;
		}

		// Mail link, deny
		if (linkUrl.startsWith("mailto:")) {
			return true;
		}

		// Not a web link (or link to the same domain), allow
		if (!(linkUrl.startsWith("https://") || linkUrl.startsWith("http://"))) {
			return false;
		}

		// User wants to allow all
		if (domainsAllowed == null) {
			return false;
		}

		// Only allow pre-approved domains if
		String domain = getDomain(linkUrl);
		return !domainsAllowed.contains(domain);
	}

	/*
	 * Get the domain from a full URL
	 */
	protected String getDomain(String url) {
		if (url == null) {
			return null;
		}

		// When serving from file, 'domain' is filesystem
		if (url.startsWith("file://")) {
			return "file://";
		}

		url = url.replace("https://", "").replace("http://", "").replace("file://", "");
		return (url.split("/")[0]).split("\\?")[0];
	}

	/*
	 * If domainsAllowed not set, allow the domain from the SUT Connector
	 */
	protected void ensureDomainsAllowed() {
		// Not required or already defined
		if (domainsAllowed == null || domainsAllowed.size() > 0) {
			return;
		}

		String[] parts = settings().get(ConfigTags.SUTConnectorValue).split(" ");
		String url = parts[parts.length - 1].replace("\"", "");
		domainsAllowed = Arrays.asList(getDomain(url));
	}
	
	/*
	 * We need to check if click position is within the canvas
	 */
	protected boolean isAtBrowserCanvas(Widget widget) {
		Shape shape = widget.get(Tags.Shape, null);
		if (shape == null) {
			return false;
		}

		// Widget must be completely visible on viewport for screenshots
		return widget.get(WdTags.WebIsFullOnScreen, false);
	}
	
	protected String[] getTextInputsFromFile(final String inputFile) {
        try (final Stream<String> lines = Files.lines(new File(inputFile).toPath())) {
            return lines.filter(line -> !line.startsWith("#") && !line.isEmpty()).toArray(String[]::new);
        } catch (IOException e) {
            return null;
        }
    }
}
