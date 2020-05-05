package nl.ou.testar.temporal.guipanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import nl.ou.testar.temporal.control.TemporalController;
import nl.ou.testar.temporal.ioutils.CSVHandler;
import nl.ou.testar.temporal.ioutils.StreamConsumer;
import nl.ou.testar.temporal.oracle.TemporalOracle;
import nl.ou.testar.temporal.oracle.TemporalPattern;
import nl.ou.testar.temporal.oracle.TemporalPatternConstraint;
import nl.ou.testar.temporal.util.*;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.fruit.monkey.ConfigTags.TemporalGeneratorTactics;

public class Temporalpanel {
    //****custom
    private Process webAnalyzerProcess = null;
    private String VisualizerURL;
    private String VisualizerURLStop;
    private StreamConsumer webAnalyzerErr;
    private StreamConsumer webAnalyzerOut;
    TemporalController tcontrol;
    private String outputDir;
    boolean statemodelEnabled;
    //**** custom


    private JPanel mainTemporalPanel;
    private JTabbedPane containerTab;
    private JTextField patternFile;
    private JTextField PropositionManagerFile;
    private JButton selectFilePatterns;
    private JButton selectFileOracles;
    private JTextField oracleFile;
    private JButton selectFilePropositionManager;
    private JButton generateBtn;
    private JButton startAnalyzerBtn;
    private JButton stopAnalyzerBtn;
    private JButton modelCheckBtn;
    private JButton ModelOnlyBtn;
    private JTextField spotLTLChecker;
    private JCheckBox WSLCheckBoxLTLSpot;
    private JTextField itsCTLChecker;
    private JTextField PythonEnv_Path;
    private JButton selectFilePython_ENV;
    private JTextField PythonVisualizer_Path;
    private JButton selectFilePython_VIZ;
    private JButton testDbButton;
    //private JComboBox<String> tacticComboBox; // Test: suppress unchecked warnings at compile time css 202005
    @SuppressWarnings("rawtypes")
    private JComboBox tacticComboBox;
    private JButton defaultPropositionMgrBtn;
    private JButton sampleOracleBtn;
    private JButton graphMLBtn;
    private JCheckBox verboseCheckBox;
    private JPanel setupPanel;
    private JPanel minerPanel;
    private JPanel visualizerPanel;
    private JCheckBox enableTemporalOfflineOraclesCheckBox;
    private JCheckBox enforceAbstractionEquality;
    private JTextField patternConstraintsFile;
    private JCheckBox WSLCheckBoxCTLITS;
    private JCheckBox instrumentDeadlockStatesCheckBox;
    private JCheckBox WSLCheckBoxLTLITS;
    private JTextField itsLTLChecker;
    private JCheckBox CounterExamples;
    private JButton selectFilePatternConstraints;
    private JTextField ltsminLTLChecker;
    private JCheckBox WSLCheckBoxLTLLTSMIN;
    private JCheckBox enableSPOT_LTL;
    private JCheckBox enableITS_CTL;
    private JCheckBox enableITS_LTL;
    private JCheckBox enableLTSMIN_LTL;
    private JTextField galCTLChecker;
    private JCheckBox WSLCheckBoxCTLGAL;
    private JCheckBox enableGAL_CTL;
    private JTextField ltsminCTLChecker;
    private JCheckBox WSLCheckBoxCTLLTSMIN;
    private JCheckBox enableLTSMIN_CTL;

    public Temporalpanel() {
        $$$setupUI$$$();


        startAnalyzerBtn.addActionListener(this::startTemporalWebAnalyzer);
        stopAnalyzerBtn.addActionListener(this::stopTemporalWebAnalyzer);
        ModelOnlyBtn.addActionListener(this::exportTemporalmodel
        );
        testDbButton.addActionListener(this::testdbconnection);
        selectFilePython_ENV.addActionListener(e -> chooserHelper(PythonEnv_Path));
        selectFilePython_VIZ.addActionListener(e -> chooserHelper(PythonVisualizer_Path));
        selectFilePropositionManager.addActionListener(e -> chooserHelper(PropositionManagerFile));
        selectFileOracles.addActionListener(e -> chooserHelper(oracleFile));
        modelCheckBtn.addActionListener(e -> ModelCheck());
        defaultPropositionMgrBtn.addActionListener(e -> testSaveDefaultPropositionManagerJSON());
        sampleOracleBtn.addActionListener(e -> {
            testOracleCSV();
            testPatternCSV();
            testPatternConstraintCSV();
        });
        graphMLBtn.addActionListener(this::testgraphml);
        generateBtn.addActionListener(e -> generateOracles());
        selectFilePatterns.addActionListener(e -> chooserHelper(patternFile));
        selectFilePatternConstraints.addActionListener(e -> chooserHelper(patternConstraintsFile));

        containerTab.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
            }
        });

        mainTemporalPanel.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
//                if (statemodelEnabled) {
//                    //works crappy todo: improve
//                    JOptionPane.showMessageDialog(setupPanel,
//                            "The State Model must be enabled and setup completely before using the Temporal Features." +
//                                    "Restart TESTAR or switch settings to apply",
//                            "Warning",
//                            JOptionPane.WARNING_MESSAGE);
//                }
            }
        });
    }

    public static Temporalpanel createTemporalPanel() {
        return new Temporalpanel();
    }


    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainTemporalPanel = new JPanel();
        mainTemporalPanel.setLayout(new FormLayout("right:245px:grow,left:4dlu:noGrow,fill:45px:noGrow,left:7dlu:noGrow,fill:51px:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:195px:noGrow,fill:8px:noGrow,right:max(p;42px):noGrow,left:4dlu:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:noGrow,top:328px:noGrow"));
        mainTemporalPanel.setEnabled(true);
        mainTemporalPanel.setMinimumSize(new Dimension(621, 350));
        mainTemporalPanel.setPreferredSize(new Dimension(621, 350));
        mainTemporalPanel.setVisible(true);
        mainTemporalPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JSeparator separator1 = new JSeparator();
        CellConstraints cc = new CellConstraints();
        mainTemporalPanel.add(separator1, cc.xyw(1, 1, 3, CellConstraints.FILL, CellConstraints.FILL));
        containerTab = new JTabbedPane();
        containerTab.setMinimumSize(new Dimension(617, 350));
        containerTab.setPreferredSize(new Dimension(617, 350));
        containerTab.setRequestFocusEnabled(true);
        containerTab.setVisible(true);
        mainTemporalPanel.add(containerTab, cc.xyw(1, 2, 16));
        setupPanel = new JPanel();
        setupPanel.setLayout(new FormLayout("fill:139px:noGrow,left:91px:noGrow,left:8dlu:noGrow,fill:212px:noGrow,right:66px:noGrow,fill:132px:noGrow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:37px:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:42px:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        setupPanel.setMinimumSize(new Dimension(588, 325));
        setupPanel.setPreferredSize(new Dimension(588, 325));
        setupPanel.setVisible(false);
        containerTab.addTab("Setup", setupPanel);
        final JLabel label1 = new JLabel();
        label1.setText("SPOT LTL Checker:");
        label1.setToolTipText("Used for LTL model check. the usual command to invoke is:  spot_checker");
        setupPanel.add(label1, cc.xy(1, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        spotLTLChecker = new JTextField();
        spotLTLChecker.setToolTipText("<html>Command to invoke the SPOT-based LTL model checker<br>\n(counterexamples can be computed and visualized)");
        setupPanel.add(spotLTLChecker, cc.xyw(2, 1, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        WSLCheckBoxLTLSpot = new JCheckBox();
        WSLCheckBoxLTLSpot.setText("WSL");
        WSLCheckBoxLTLSpot.setToolTipText("<html> Does this command need a WSL path?<br> \ne.g. starting with \"/mnt/C/...\"<br>\nWhen ticked then filepath for the modelchecker are converted automatically.\n</html>");
        setupPanel.add(WSLCheckBoxLTLSpot, cc.xy(5, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));
        final JLabel label2 = new JLabel();
        label2.setText("ITS CTL Checker:");
        label2.setToolTipText("Used for CTL model check. the usual command to invoke is:  its-ctl");
        setupPanel.add(label2, cc.xy(1, 5, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        itsCTLChecker = new JTextField();
        itsCTLChecker.setToolTipText("<html>Command to invoke the ITS-based CTL model checker.<br>\n(no visualization of counterexample possible)</html>");
        setupPanel.add(itsCTLChecker, cc.xyw(2, 5, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        WSLCheckBoxCTLITS = new JCheckBox();
        WSLCheckBoxCTLITS.setText("WSL");
        WSLCheckBoxCTLITS.setToolTipText("<html> Does this command need a WSL path?<br> \ne.g. starting with \"/mnt/C/...\"<br>\nWhen ticked then filepath for the modelchecker are converted automatically.\n</html>");
        setupPanel.add(WSLCheckBoxCTLITS, cc.xy(5, 5, CellConstraints.LEFT, CellConstraints.DEFAULT));
        itsLTLChecker = new JTextField();
        itsLTLChecker.setText("");
        itsLTLChecker.setToolTipText("<html>Command to invoke the ITS-based LTL model checker<br>\n(counterexamples are computed in the raw output, but not visualized)");
        itsLTLChecker.setVisible(true);
        setupPanel.add(itsLTLChecker, cc.xyw(2, 9, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        WSLCheckBoxLTLITS = new JCheckBox();
        WSLCheckBoxLTLITS.setText("WSL");
        WSLCheckBoxLTLITS.setToolTipText("<html> Does this command need a WSL path?<br> \ne.g. starting with \"/mnt/C/...\"<br>\nWhen ticked then filepath for the modelchecker are converted automatically.\n</html>");
        WSLCheckBoxLTLITS.setVisible(true);
        setupPanel.add(WSLCheckBoxLTLITS, cc.xy(5, 9, CellConstraints.LEFT, CellConstraints.DEFAULT));
        enableSPOT_LTL = new JCheckBox();
        enableSPOT_LTL.setText("Enable");
        enableSPOT_LTL.setToolTipText("<html> Use this model-checker?<br> \nWhen ticked,then this model checker will verify<br>\nthe supplied temporal oracles that match the temporal-type.\n</html>");
        setupPanel.add(enableSPOT_LTL, cc.xy(6, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));
        enableITS_CTL = new JCheckBox();
        enableITS_CTL.setText("Enable");
        enableITS_CTL.setToolTipText("<html> Use this model-checker?<br> \nWhen ticked,then this model checker will verify<br>\nthe supplied temporal oracles that match the temporal-type.\n</html>");
        setupPanel.add(enableITS_CTL, cc.xy(6, 5, CellConstraints.LEFT, CellConstraints.DEFAULT));
        enableITS_LTL = new JCheckBox();
        enableITS_LTL.setEnabled(true);
        enableITS_LTL.setSelected(false);
        enableITS_LTL.setText("Enable");
        enableITS_LTL.setToolTipText("<html> Use this model-checker?<br> \nWhen ticked,then this model checker will verify<br>\nthe supplied temporal oracles that match the temporal-type.\n</html>");
        enableITS_LTL.setVisible(true);
        setupPanel.add(enableITS_LTL, cc.xy(6, 9, CellConstraints.LEFT, CellConstraints.DEFAULT));
        final JLabel label3 = new JLabel();
        label3.setText("LTSMIN LTL Checker:");
        label3.setToolTipText("Used for LTL model check. the usual command to invoke is:  its-ltl");
        setupPanel.add(label3, cc.xy(1, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        ltsminLTLChecker = new JTextField();
        ltsminLTLChecker.setText("");
        ltsminLTLChecker.setToolTipText("<html>Command to invoke the LTSMIN-based LTL model checker<br>\n(counterexamples cannot be computed)");
        setupPanel.add(ltsminLTLChecker, cc.xyw(2, 3, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        WSLCheckBoxLTLLTSMIN = new JCheckBox();
        WSLCheckBoxLTLLTSMIN.setText("WSL");
        WSLCheckBoxLTLLTSMIN.setToolTipText("<html> Does this command need a WSL path?<br> \ne.g. starting with \"/mnt/C/...\"<br>\nWhen ticked then filepath for the modelchecker are converted automatically.\n</html>");
        setupPanel.add(WSLCheckBoxLTLLTSMIN, cc.xy(5, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
        enableLTSMIN_LTL = new JCheckBox();
        enableLTSMIN_LTL.setText("Enable");
        enableLTSMIN_LTL.setToolTipText("<html> Use this model-checker?<br> \nWhen ticked,then this model checker will verify<br>\nthe supplied temporal oracles that match the temporal-type.\n</html>");
        setupPanel.add(enableLTSMIN_LTL, cc.xy(6, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
        final JLabel label4 = new JLabel();
        label4.setText("GAL CTL Checker:");
        label4.setToolTipText("Used for CTL model check. the usual command to invoke is:  its-ctl");
        setupPanel.add(label4, cc.xy(1, 7, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        galCTLChecker = new JTextField();
        galCTLChecker.setToolTipText("<html>Command to invoke the ITS/GAL-based CTL model checker.<br>\n(slow,counterexamples are computed in the raw output, but not visualized)</html>");
        setupPanel.add(galCTLChecker, cc.xyw(2, 7, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        WSLCheckBoxCTLGAL = new JCheckBox();
        WSLCheckBoxCTLGAL.setText("WSL");
        WSLCheckBoxCTLGAL.setToolTipText("<html> Does this command need a WSL path?<br> \ne.g. starting with \"/mnt/C/...\"<br>\nWhen ticked then filepath for the modelchecker are converted automatically.\n</html>");
        setupPanel.add(WSLCheckBoxCTLGAL, cc.xy(5, 7, CellConstraints.LEFT, CellConstraints.DEFAULT));
        enableGAL_CTL = new JCheckBox();
        enableGAL_CTL.setText("Enable");
        enableGAL_CTL.setToolTipText("<html> Use this model-checker?<br> \nWhen ticked,then this model checker will verify<br>\nthe supplied temporal oracles that match the temporal-type.\n</html>");
        setupPanel.add(enableGAL_CTL, cc.xy(6, 7, CellConstraints.LEFT, CellConstraints.DEFAULT));
        final JLabel label5 = new JLabel();
        label5.setOpaque(false);
        label5.setText("ITS LTL Checker:");
        label5.setToolTipText("Used for LTL model check. the usual command to invoke is:  its-ltl");
        label5.setVisible(true);
        setupPanel.add(label5, cc.xy(1, 9, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JLabel label6 = new JLabel();
        label6.setOpaque(false);
        label6.setText("LTSMIN CTL Checker:");
        label6.setToolTipText("Used for LTL model check. the usual command to invoke is:  its-ltl");
        label6.setVisible(true);
        setupPanel.add(label6, cc.xy(1, 11, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        ltsminCTLChecker = new JTextField();
        ltsminCTLChecker.setText("");
        ltsminCTLChecker.setToolTipText("<html>Command to invoke the ITS-based LTL model checker<br>\n(counterexamples are not available)");
        ltsminCTLChecker.setVisible(true);
        setupPanel.add(ltsminCTLChecker, cc.xyw(2, 11, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        WSLCheckBoxCTLLTSMIN = new JCheckBox();
        WSLCheckBoxCTLLTSMIN.setText("WSL");
        WSLCheckBoxCTLLTSMIN.setToolTipText("<html> Does this command need a WSL path?<br> \ne.g. starting with \"/mnt/C/...\"<br>\nWhen ticked then filepath for the modelchecker are converted automatically.\n</html>");
        WSLCheckBoxCTLLTSMIN.setVisible(true);
        setupPanel.add(WSLCheckBoxCTLLTSMIN, cc.xy(5, 11, CellConstraints.LEFT, CellConstraints.DEFAULT));
        enableLTSMIN_CTL = new JCheckBox();
        enableLTSMIN_CTL.setEnabled(true);
        enableLTSMIN_CTL.setSelected(false);
        enableLTSMIN_CTL.setText("Enable");
        enableLTSMIN_CTL.setToolTipText("<html> Use this model-checker?<br> \nWhen ticked,then this model checker will verify<br>\nthe supplied temporal oracles that match the temporal-type.\n</html>");
        enableLTSMIN_CTL.setVisible(true);
        setupPanel.add(enableLTSMIN_CTL, cc.xy(6, 11, CellConstraints.LEFT, CellConstraints.DEFAULT));
        minerPanel = new JPanel();
        minerPanel.setLayout(new FormLayout("left:132px:noGrow,fill:121px:noGrow,fill:37px:noGrow,fill:43px:noGrow,fill:19px:noGrow,fill:11px:noGrow,fill:33px:noGrow,left:111px:noGrow,left:45dlu:noGrow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:41px:noGrow,center:41px:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,center:4dlu:noGrow,center:max(d;4px):noGrow,top:5dlu:noGrow,center:42px:noGrow"));
        containerTab.addTab("Miner", minerPanel);
        final JLabel label7 = new JLabel();
        label7.setText("Oracles:");
        minerPanel.add(label7, cc.xy(1, 14, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JLabel label8 = new JLabel();
        label8.setText("Oracle Patterns:");
        minerPanel.add(label8, cc.xy(1, 12, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        oracleFile = new JTextField();
        minerPanel.add(oracleFile, cc.xyw(2, 14, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        patternFile = new JTextField();
        patternFile.setText("");
        minerPanel.add(patternFile, cc.xyw(2, 12, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label9 = new JLabel();
        label9.setText("Proposition Manager:");
        minerPanel.add(label9, cc.xy(1, 8, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        PropositionManagerFile = new JTextField();
        minerPanel.add(PropositionManagerFile, cc.xyw(2, 8, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label10 = new JLabel();
        label10.setText("Pattern Constraints:");
        minerPanel.add(label10, cc.xy(1, 10, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        patternConstraintsFile = new JTextField();
        minerPanel.add(patternConstraintsFile, cc.xyw(2, 10, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        tacticComboBox = new JComboBox();
        tacticComboBox.setEnabled(true);
        tacticComboBox.setMaximumSize(new Dimension(130, 38));
        tacticComboBox.setMinimumSize(new Dimension(130, 30));
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("1");
        defaultComboBoxModel1.addElement("5");
        defaultComboBoxModel1.addElement("10");
        defaultComboBoxModel1.addElement("50");
        defaultComboBoxModel1.addElement("100");
        defaultComboBoxModel1.addElement("500");
        defaultComboBoxModel1.addElement("1000");
        tacticComboBox.setModel(defaultComboBoxModel1);
        tacticComboBox.setPreferredSize(new Dimension(150, 30));
        tacticComboBox.setToolTipText("<HTML>tactic to generate oracles from the supplied Pattern collection.<BR>\nShows the number of potential oracles to generate per pattern\n</HTML>");
        tacticComboBox.setVerifyInputWhenFocusTarget(true);
        minerPanel.add(tacticComboBox, cc.xy(9, 12, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JLabel label11 = new JLabel();
        label11.setText("Generate:");
        minerPanel.add(label11, cc.xy(1, 7, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        defaultPropositionMgrBtn = new JButton();
        defaultPropositionMgrBtn.setMaximumSize(new Dimension(130, 38));
        defaultPropositionMgrBtn.setMinimumSize(new Dimension(130, 38));
        defaultPropositionMgrBtn.setPreferredSize(new Dimension(130, 30));
        defaultPropositionMgrBtn.setText("Proposition Mgr");
        defaultPropositionMgrBtn.setToolTipText("Generate a sample Proposition Manager");
        minerPanel.add(defaultPropositionMgrBtn, cc.xy(2, 7, CellConstraints.LEFT, CellConstraints.DEFAULT));
        sampleOracleBtn = new JButton();
        sampleOracleBtn.setPreferredSize(new Dimension(110, 30));
        sampleOracleBtn.setText("Sample Oracle");
        sampleOracleBtn.setToolTipText("Generate files with 1. a sample Oracle and 2. a sample Pattern, 3. a sample constraint");
        sampleOracleBtn.putClientProperty("html.disable", Boolean.FALSE);
        minerPanel.add(sampleOracleBtn, cc.xyw(3, 7, 4, CellConstraints.LEFT, CellConstraints.CENTER));
        generateBtn = new JButton();
        generateBtn.setEnabled(true);
        generateBtn.setHorizontalTextPosition(0);
        generateBtn.setMaximumSize(new Dimension(81, 30));
        generateBtn.setText("<html>Generate</html>");
        generateBtn.setToolTipText("<html>Instantiates the parameters in the Oracle Patterns with Atomic Propositions (AP's) from the Model to generate (Potential) Oracles. <BR>\nPattern Constraints can be applied to control the ramdom instantiation.\n<BR>\nThe list of  AP's in the model is computed by applying the Proposition Manager filters the graph DB </html>");
        minerPanel.add(generateBtn, cc.xy(8, 12));
        modelCheckBtn = new JButton();
        modelCheckBtn.setHorizontalTextPosition(0);
        modelCheckBtn.setText("Model Check");
        modelCheckBtn.setToolTipText("<html>Perform a Model** Check against the (potential) Oracles. <BR>\nrequired input: <BR>\n\t+ Proposition Manager. This file is used for filtering atomic propositions from the Model<BR>\n\t+ Oracles. This file contains the formulas to be checked. <BR>\n** Ensure that <BR>\n1. the Application name and version settings on General panel and <BR>\n2. Abstraction settings on the State model panel are saved before invoking this function!!! <BR>\nUse the Show Db Models on the Setup-tab to view the available models </html> ");
        minerPanel.add(modelCheckBtn, cc.xy(8, 14));
        enableTemporalOfflineOraclesCheckBox = new JCheckBox();
        enableTemporalOfflineOraclesCheckBox.setText("Enable Temporal Offline Oracles");
        enableTemporalOfflineOraclesCheckBox.setToolTipText("<html>Temporal oracles are automatically evaluated after each TESTAR run and use the settings in this form.<BR>\nProtocol (JAVA) modification might apply</html>");
        minerPanel.add(enableTemporalOfflineOraclesCheckBox, cc.xyw(1, 3, 2));
        CounterExamples = new JCheckBox();
        CounterExamples.setText("Counter Examples");
        CounterExamples.setToolTipText("<html>Produce traces of counter examples (when verdict=FAIL) or witness (when verdict=PASS).<BR>\n(not all implemented modelcheckers can produce such traces) </html>");
        minerPanel.add(CounterExamples, cc.xyw(3, 3, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
        verboseCheckBox = new JCheckBox();
        verboseCheckBox.setSelected(true);
        verboseCheckBox.setText("Verbose");
        verboseCheckBox.setToolTipText("<html> When checked: keeps the intermediate files on disk ,<br>\nelse: these files are deleted after the Checker has run.\n</html>");
        minerPanel.add(verboseCheckBox, cc.xy(8, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
        enforceAbstractionEquality = new JCheckBox();
        enforceAbstractionEquality.setSelected(true);
        enforceAbstractionEquality.setText("Enforce Abstraction equality");
        enforceAbstractionEquality.setToolTipText("<html>Concrete Abstraction attributes in <b>NEW</b> models will be the same as on the Abstract Layer.<br>\n(this overrules the ConcreteStateAttributes parameter in the settings file and <br>\nthe default uses <b>ALL</b> StateAttributes as ConcreteStateAttributes)<br>\nIt is advised to leave this setting enabled.</html>");
        minerPanel.add(enforceAbstractionEquality, cc.xyw(1, 5, 2));
        instrumentDeadlockStatesCheckBox = new JCheckBox();
        instrumentDeadlockStatesCheckBox.setText("Instrument terminal states");
        instrumentDeadlockStatesCheckBox.setToolTipText("<html> In case that the Graph-model has terminal states (~ no outgoing transitions):<BR>\n1. a new artificial state  with a selfloop is added<BR>\n2. forall terminal states, a transition will be added to that newly created artificial state.<BR>\n3. the transitions have a single atomic proposition that indicates that the target state is terminal.<BR>\nThis 'enriched' model will be provided to the model-checker.<BR>\n(not the original Graph-model) </html>");
        minerPanel.add(instrumentDeadlockStatesCheckBox, cc.xyw(3, 5, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
        testDbButton = new JButton();
        testDbButton.setText("Show DB ");
        testDbButton.setToolTipText("Exports the existing models in the Graph database to a file.");
        minerPanel.add(testDbButton, cc.xy(9, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
        graphMLBtn = new JButton();
        graphMLBtn.setMaximumSize(new Dimension(83, 38));
        graphMLBtn.setPreferredSize(new Dimension(90, 30));
        graphMLBtn.setText("GraphML");
        graphMLBtn.setToolTipText("<html>Exports the model** from the graphDB  into (GRAPHML.XML) format. <br>\n<BR>\n** Ensure that the correct <BR>\n1. Application name and version settings on General panel and <BR>\n2. Abstraction settings on the State model panel<BR>\nare saved before invoking this function!!! <BR>\nUse the 'Show Db' to view the available models </html> ");
        minerPanel.add(graphMLBtn, cc.xy(8, 7, CellConstraints.LEFT, CellConstraints.DEFAULT));
        ModelOnlyBtn = new JButton();
        ModelOnlyBtn.setPreferredSize(new Dimension(78, 30));
        ModelOnlyBtn.setText("Model");
        ModelOnlyBtn.setToolTipText("<html>Exports/transforms the model from the graphDB  into (JSON) format. <br>\n\n** Ensure that <BR>\n1. Proposition Manager is available. This file is used for filtering atomic propositions <BR>\n2. Application name and version settings on General panel and <BR>\n3. Abstraction settings on the State model panel<BR> \nare saved before invoking this function!!! <BR>\nUse the 'Show Db'  to view the available models </html> ");
        minerPanel.add(ModelOnlyBtn, cc.xy(9, 7, CellConstraints.LEFT, CellConstraints.DEFAULT));
        selectFilePropositionManager = new JButton();
        selectFilePropositionManager.setText("...");
        minerPanel.add(selectFilePropositionManager, cc.xy(7, 8));
        selectFilePatternConstraints = new JButton();
        selectFilePatternConstraints.setText("...");
        minerPanel.add(selectFilePatternConstraints, cc.xy(7, 10));
        selectFilePatterns = new JButton();
        selectFilePatterns.setText("...");
        minerPanel.add(selectFilePatterns, cc.xy(7, 12));
        selectFileOracles = new JButton();
        selectFileOracles.setText("...");
        minerPanel.add(selectFileOracles, cc.xy(7, 14));
        visualizerPanel = new JPanel();
        visualizerPanel.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,left:78dlu:noGrow,left:4dlu:noGrow,left:115px:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:47px:noGrow,left:27dlu:noGrow,fill:max(d;4px):noGrow", "center:49px:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        visualizerPanel.setEnabled(false);
        containerTab.addTab("Visualizer", visualizerPanel);
        PythonEnv_Path = new JTextField();
        visualizerPanel.add(PythonEnv_Path, cc.xyw(3, 1, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label12 = new JLabel();
        label12.setText("Python Env. :");
        label12.setToolTipText("Path to Active Virtual environment");
        visualizerPanel.add(label12, cc.xy(1, 1));
        selectFilePython_ENV = new JButton();
        selectFilePython_ENV.setText("...");
        visualizerPanel.add(selectFilePython_ENV, cc.xy(9, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));
        PythonVisualizer_Path = new JTextField();
        visualizerPanel.add(PythonVisualizer_Path, cc.xyw(3, 3, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label13 = new JLabel();
        label13.setText("Visualizer:");
        label13.setToolTipText("Usually this is the path to run.py");
        visualizerPanel.add(label13, cc.xy(1, 3));
        selectFilePython_VIZ = new JButton();
        selectFilePython_VIZ.setText("...");
        visualizerPanel.add(selectFilePython_VIZ, cc.xy(9, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
        startAnalyzerBtn = new JButton();
        startAnalyzerBtn.setText("Start Analyzer");
        visualizerPanel.add(startAnalyzerBtn, cc.xy(1, 5));
        stopAnalyzerBtn = new JButton();
        stopAnalyzerBtn.setText("Stop Analyzer");
        visualizerPanel.add(stopAnalyzerBtn, cc.xyw(5, 5, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainTemporalPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }


    //***********TESTAR generic panel code
    public void populateFrom(final Settings settings) {
        spotLTLChecker.setText(settings.get(ConfigTags.TemporalLTL_SPOTChecker));
        WSLCheckBoxLTLSpot.setSelected(settings.get(ConfigTags.TemporalLTL_SPOTCheckerWSL));
        enableSPOT_LTL.setSelected(settings.get(ConfigTags.TemporalLTL_SPOTChecker_Enabled));

        itsCTLChecker.setText(settings.get(ConfigTags.TemporalCTL_ITSChecker));
        WSLCheckBoxCTLITS.setSelected(settings.get(ConfigTags.TemporalCTL_ITSCheckerWSL));
        enableITS_CTL.setSelected(settings.get(ConfigTags.TemporalCTL_ITSChecker_Enabled));

        galCTLChecker.setText(settings.get(ConfigTags.TemporalCTL_GALChecker));
        WSLCheckBoxCTLGAL.setSelected(settings.get(ConfigTags.TemporalCTL_GALCheckerWSL));
        enableGAL_CTL.setSelected(settings.get(ConfigTags.TemporalCTL_GALChecker_Enabled));

        itsLTLChecker.setText(settings.get(ConfigTags.TemporalLTL_ITSChecker));
        WSLCheckBoxLTLITS.setSelected(settings.get(ConfigTags.TemporalLTL_ITSCheckerWSL));
        enableITS_LTL.setSelected(settings.get(ConfigTags.TemporalLTL_ITSChecker_Enabled));

        ltsminLTLChecker.setText(settings.get(ConfigTags.TemporalLTL_LTSMINChecker));
        WSLCheckBoxLTLLTSMIN.setSelected(settings.get(ConfigTags.TemporalLTL_LTSMINCheckerWSL));
        enableLTSMIN_LTL.setSelected(settings.get(ConfigTags.TemporalLTL_LTSMINChecker_Enabled));

        ltsminCTLChecker.setText(settings.get(ConfigTags.TemporalCTL_LTSMINChecker));
        WSLCheckBoxCTLLTSMIN.setSelected(settings.get(ConfigTags.TemporalCTL_LTSMINCheckerWSL));
        enableLTSMIN_CTL.setSelected(settings.get(ConfigTags.TemporalCTL_LTSMINChecker_Enabled));

        instrumentDeadlockStatesCheckBox.setSelected(settings.get(ConfigTags.TemporalInstrumentDeadlockState));
        verboseCheckBox.setSelected(settings.get(ConfigTags.TemporalVerbose));
        CounterExamples.setSelected(settings.get(ConfigTags.TemporalCounterExamples));
        enableTemporalOfflineOraclesCheckBox.setSelected(settings.get(ConfigTags.TemporalOffLineEnabled));
        enforceAbstractionEquality.setSelected(settings.get(ConfigTags.TemporalConcreteEqualsAbstract));
        oracleFile.setText(settings.get(ConfigTags.TemporalOracles));
        patternFile.setText(settings.get(ConfigTags.TemporalPatterns));
        PropositionManagerFile.setText(settings.get(ConfigTags.TemporalPropositionManager));
        patternConstraintsFile.setText(settings.get(ConfigTags.TemporalPatternConstraints));
        String[] comboBoxLabels = settings.get(TemporalGeneratorTactics).stream().filter(Objects::nonNull).toArray(String[]::new);
        DefaultComboBoxModel<String> cbModel = new DefaultComboBoxModel<>(comboBoxLabels); // read only
        tacticComboBox.setModel(cbModel);
        PythonEnv_Path.setText(settings.get(ConfigTags.TemporalPythonEnvironment));
        PythonVisualizer_Path.setText(settings.get(ConfigTags.TemporalVisualizerServer));
        VisualizerURL = settings.get(ConfigTags.TemporalVisualizerURL);
        VisualizerURLStop = settings.get(ConfigTags.TemporalVisualizerURLStop);
        if (outputDir != null && !outputDir.equals("")) {// when triggered by save button on the general panel
            tcontrol = new TemporalController(settings, outputDir);// look for better location
        } else
            tcontrol = new TemporalController(settings);
        outputDir = tcontrol.getOutputDir();
        statemodelEnabled = settings.get(ConfigTags.StateModelEnabled);
    }


    public void extractInformation(final Settings settings) {
        settings.set(ConfigTags.TemporalLTL_SPOTChecker, spotLTLChecker.getText());
        settings.set(ConfigTags.TemporalLTL_SPOTCheckerWSL, WSLCheckBoxLTLSpot.isSelected());
        settings.set(ConfigTags.TemporalLTL_SPOTChecker_Enabled, enableSPOT_LTL.isSelected());
        settings.set(ConfigTags.TemporalCTL_ITSChecker, itsCTLChecker.getText());
        settings.set(ConfigTags.TemporalCTL_ITSCheckerWSL, WSLCheckBoxCTLITS.isSelected());
        settings.set(ConfigTags.TemporalCTL_ITSChecker_Enabled, enableITS_CTL.isSelected());
        settings.set(ConfigTags.TemporalCTL_GALChecker, galCTLChecker.getText());
        settings.set(ConfigTags.TemporalCTL_GALCheckerWSL, WSLCheckBoxCTLGAL.isSelected());
        settings.set(ConfigTags.TemporalCTL_GALChecker_Enabled, enableGAL_CTL.isSelected());
        settings.set(ConfigTags.TemporalLTL_ITSChecker, itsLTLChecker.getText());
        settings.set(ConfigTags.TemporalLTL_ITSCheckerWSL, WSLCheckBoxLTLITS.isSelected());
        settings.set(ConfigTags.TemporalLTL_ITSChecker_Enabled, enableITS_LTL.isSelected());
        settings.set(ConfigTags.TemporalLTL_LTSMINChecker, ltsminLTLChecker.getText());
        settings.set(ConfigTags.TemporalLTL_LTSMINCheckerWSL, WSLCheckBoxLTLLTSMIN.isSelected());
        settings.set(ConfigTags.TemporalLTL_LTSMINChecker_Enabled, enableLTSMIN_LTL.isSelected());
        settings.set(ConfigTags.TemporalCTL_LTSMINChecker, ltsminCTLChecker.getText());
        settings.set(ConfigTags.TemporalCTL_LTSMINCheckerWSL, WSLCheckBoxCTLLTSMIN.isSelected());
        settings.set(ConfigTags.TemporalCTL_LTSMINChecker_Enabled, enableLTSMIN_CTL.isSelected());

        settings.set(ConfigTags.TemporalVerbose, verboseCheckBox.isSelected());
        settings.set(ConfigTags.TemporalCounterExamples, CounterExamples.isSelected());
        settings.set(ConfigTags.TemporalOffLineEnabled, enableTemporalOfflineOraclesCheckBox.isSelected());
        settings.set(ConfigTags.TemporalConcreteEqualsAbstract, enforceAbstractionEquality.isSelected());
        settings.set(ConfigTags.TemporalOracles, oracleFile.getText());
        settings.set(ConfigTags.TemporalPropositionManager, PropositionManagerFile.getText());
        settings.set(ConfigTags.TemporalPatternConstraints, patternConstraintsFile.getText());
        settings.set(ConfigTags.TemporalInstrumentDeadlockState, instrumentDeadlockStatesCheckBox.isSelected());
        settings.set(ConfigTags.TemporalPythonEnvironment, PythonEnv_Path.getText());
        settings.set(ConfigTags.TemporalVisualizerServer, PythonVisualizer_Path.getText());

    }

    //******************Eventhandlers

    private void chooserHelper(JTextField textfield) {
        JFileChooser fd = new JFileChooser();
        fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fd.setCurrentDirectory(new File(textfield.getText()).getParentFile());
        if (fd.showOpenDialog(mainTemporalPanel) == JFileChooser.APPROVE_OPTION) {
            String file = fd.getSelectedFile().getAbsolutePath();
            textfield.setText(file);
        }
    }

    private void ModelCheck() {

        tcontrol.MCheck(
                PropositionManagerFile.getText(),
                oracleFile.getText(),
                verboseCheckBox.isSelected(),
                CounterExamples.isSelected(),
                instrumentDeadlockStatesCheckBox.isSelected(),
                spotLTLChecker.getText(), WSLCheckBoxLTLSpot.isSelected(), enableSPOT_LTL.isSelected(),
                itsCTLChecker.getText(), WSLCheckBoxCTLITS.isSelected(), enableITS_CTL.isSelected(),
                itsLTLChecker.getText(), WSLCheckBoxLTLITS.isSelected(), enableITS_LTL.isSelected(),
                ltsminLTLChecker.getText(), WSLCheckBoxLTLLTSMIN.isSelected(), enableLTSMIN_LTL.isSelected(),
                galCTLChecker.getText(), WSLCheckBoxCTLGAL.isSelected(), enableGAL_CTL.isSelected(),
                ltsminCTLChecker.getText(), WSLCheckBoxCTLLTSMIN.isSelected(), enableLTSMIN_CTL.isSelected()
        );
    }

    private void generateOracles() {

        tcontrol.generateOraclesFromPatterns(
                PropositionManagerFile.getText(),
                patternFile.getText(),
                patternConstraintsFile.getText(),
                Integer.parseInt(Objects.requireNonNull(tacticComboBox.getSelectedItem()).toString())); //requirenonnull?
    }


    private void startTemporalWebAnalyzer(ActionEvent evt) {
        String cli = PythonEnv_Path.getText() + " " + PythonVisualizer_Path.getText();
        try {// call the external program
            if (webAnalyzerProcess == null) {
                webAnalyzerProcess = Runtime.getRuntime().exec(cli);
                System.out.println("Visualizer Started. goto " + VisualizerURL + "\n");


                Desktop desktop = Desktop.getDesktop();
                URI uri = new URI(VisualizerURL);
                desktop.browse(uri);
                webAnalyzerErr = new StreamConsumer(webAnalyzerProcess.getErrorStream(), "ERROR");
                webAnalyzerOut = new StreamConsumer(webAnalyzerProcess.getInputStream(), "OUTPUT");
                // kick them off
                webAnalyzerErr.start();
                webAnalyzerOut.start();

            } else {
                System.out.println("Visualizer was already running. goto " + VisualizerURL + "\n");
            }
        } catch (Exception e) {
            System.err.println("Error on starting Visualizer");
            e.printStackTrace();
        }
    }

    private void stopTemporalWebAnalyzer(ActionEvent evt) {
        try {
            assert webAnalyzerProcess != null;
            if (webAnalyzerProcess.isAlive()) Common.HTTPGet(VisualizerURLStop);

            boolean ret = false;
            webAnalyzerProcess.waitFor(1, TimeUnit.SECONDS);
            if (webAnalyzerProcess.isAlive()) {
                webAnalyzerProcess.destroyForcibly();
                System.out.println("Forcing Visualizer Server  to Stop.\n");
                ret = webAnalyzerProcess.waitFor(2, TimeUnit.SECONDS);  //gently wait
                System.out.println("Visualizer Stopped. (exitcode was : " + webAnalyzerProcess.exitValue() + ")\n");
            }
            if (ret || !webAnalyzerProcess.isAlive()) webAnalyzerProcess = null;
            webAnalyzerErr.stop();
            webAnalyzerOut.stop();
        } catch (Exception e) {
            System.err.println("Error on stopping Analyzer");
            e.printStackTrace();
        }

    }

    private void testdbconnection(ActionEvent evt) {
        tcontrol.pingDB();
    }

    private void exportTemporalmodel(ActionEvent evt) {
        tcontrol.makeTemporalModel(PropositionManagerFile.getText(), verboseCheckBox.isSelected(), true);
    }

    private void testgraphml(ActionEvent evt) {
        tcontrol.saveToGraphMLFile("GraphML.XML", false);
        tcontrol.saveToGraphMLFile("GraphML_NoWidgets.XML", true);
    }

    public void testOracleCSV() {
        System.out.println("Writing an oracle to CSV file\n");
        TemporalOracle to = TemporalOracle.getSampleLTLOracle();
        List<TemporalOracle> tocoll = new ArrayList<>();
        tocoll.add(to);
        CSVHandler.save(tocoll, outputDir + "temporalOracleSample.csv");
    }

    public void testPatternCSV() {
        System.out.println("Writing a pattern to CSV file\n");
        TemporalPattern pat = TemporalPattern.getSamplePattern();
        List<TemporalPattern> patcoll = new ArrayList<>();
        patcoll.add(pat);
        CSVHandler.save(patcoll, outputDir + "temporalPatternSample.csv");
    }

    public void testPatternConstraintCSV() {
        System.out.println("Writing a pattern-constraint to CSV file\n");
        List<TemporalPatternConstraint> patconstraintcoll = TemporalPatternConstraint.getSampleConstraints();
        CSVHandler.save(patconstraintcoll, outputDir + "temporalPatternConstraintSample.csv");
    }

    public void testSaveDefaultPropositionManagerJSON() {
        tcontrol.setDefaultPropositionManager();
        tcontrol.savePropositionManager("PropositionManager_Default.json");
    }

//*******************Eventhandlers

}
