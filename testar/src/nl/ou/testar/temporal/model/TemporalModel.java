package nl.ou.testar.temporal.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.collect.HashBiMap;
import nl.ou.testar.temporal.foundation.ValStatus;
import nl.ou.testar.temporal.foundation.TemporalBean;
import nl.ou.testar.temporal.oracle.TemporalFormalism;
import nl.ou.testar.temporal.oracle.TemporalOracle;
import org.apache.commons.lang3.StringUtils;
import java.util.*;

//@JsonRootName(value="TemporalProperties")
public class TemporalModel extends TemporalBean {

    private List<StateEncoding> stateEncodings; //Integer:  to concretstateID
    private Set<String> InitialStates;
    private List<TemporalTrace> traces; //
    private List<String> stateList;
    private List<String> transitionList;
    private Set<String> modelAPs; //AP<digits> to widget property map:
    private String formatVersion = "20200402";
    private static String APPrefix = "ap";
    private static String deadProposition = "dead";
    //private String APSeparator;

    public TemporalModel() {
        super(); // needed ?
        this.stateEncodings = new ArrayList<>();
        this.stateList = new ArrayList<>();
        this.transitionList = new ArrayList<>();
        this.modelAPs = new LinkedHashSet<>();  //must maintain order
    }

    @SuppressWarnings("unused")
    public Set<String> getInitialStates() {
        return InitialStates;
    }

    public void setInitialStates(Set<String> initialStates) {
        InitialStates = initialStates;
    }

    public Set<String> getModelAPs() {
        return modelAPs;
    }

    public void setModelAPs(Set<String> modelAPs) {
        this.modelAPs = modelAPs;
    }
    @SuppressWarnings("unused")
    public List<String> getStateList() {
        return stateList;
    }
    @SuppressWarnings("unused")
    public void setStateList(List<String> stateList) {
        this.stateList = stateList;
    }
    @SuppressWarnings("unused")
    public List<String> getTransitionList() {
        return transitionList;
    }
    @SuppressWarnings("unused")
    public void setTransitionList(List<String> transitionList) {
        this.transitionList = transitionList;
    }

    //public String getAPSeparator() {        return APSeparamtor;    }

    //public void setAPSeparator(String APSeperator) {
    //    APSeparator = APSeperator;
    //}

    public static String getDeadProposition() {
        return deadProposition;
    }

    public List<TemporalTrace> getTraces() {
        return traces;
    }

    public void setTraces(List<TemporalTrace> stateTransistionSequence) {
        this.traces = stateTransistionSequence;
    }

    public List<StateEncoding> getStateEncodings() {
        return stateEncodings;
    }

    public void setStateEncodings(List<StateEncoding> stateEncodings) {

        this.stateEncodings = stateEncodings;
        stateList.clear();
        for (StateEncoding stateEnc : stateEncodings) {
            this.modelAPs.addAll(stateEnc.getStateAPs());
            this.modelAPs.addAll(stateEnc.retrieveAllTransitionAPs());
            stateList.add(stateEnc.getState());
            for (TransitionEncoding trenc : stateEnc.getTransitionColl()
            ) {
                transitionList.add(trenc.getTransition());
            }
        }
        finalizeTransitions();
    }

    @SuppressWarnings("unused")
    public String get_formatVersion() {
        return formatVersion;
    }
    @SuppressWarnings("unused")
    public void set_formatVersion(String _formatVersion) {
        this.formatVersion = _formatVersion;
    }


    //custom
    public void addStateEncoding(StateEncoding stateEncoding, boolean updateTransitionsImmediate) {

        stateEncodings.add(stateEncoding);
        this.modelAPs.addAll(stateEncoding.getStateAPs());
        this.modelAPs.addAll(stateEncoding.retrieveAllTransitionAPs());
        stateList.add(stateEncoding.getState());
        for (TransitionEncoding trenc : stateEncoding.getTransitionColl()
        ) {
            transitionList.add(trenc.getTransition());
        }

        if (updateTransitionsImmediate) {
            finalizeTransitions();
        }
    }

    public void finalizeTransitions() {
        for (StateEncoding stateEnc : stateEncodings) {// observer pattern?
            stateEnc.updateAPConjuncts(modelAPs);
        }

    }


    public String makeHOAOutput() {
        //see http://adl.github.io/hoaf/
        StringBuilder result = new StringBuilder();
        result.append("HOA: v1\n");
        result.append("tool: \"TESTAR-CSS20190914\"\n");
        result.append("name: \"" + "app= ").append(this.getApplicationName()).append(", ver=").
                append(this.getApplicationVersion()).append(", modelid= ").append(this.getApplication_ModelIdentifier()).
                append(", abstraction= ").append(this.getApplication_AbstractionAttributes()).append("\"\n");
        result.append("States: ");
        result.append(stateEncodings.size());
        result.append("\n");

        int stateindex;
        for (String initialState : InitialStates
        ) {
            stateindex = stateList.indexOf(initialState);
            assert stateindex == -1 : "initial state not in statelist";
            result.append("Start: ").append(stateindex).append("\n");
        }
        result.append("Acceptance: 1 Inf(0)\n");  //==Buchi
        result.append("AP: ");
        result.append(modelAPs.size());
        int i = 0;
        for (String ignored : modelAPs) {
            result.append(" \"").append(APPrefix);
            result.append(i);
            result.append("\"");
            i++;
        }
        result.append("\n");
        result.append("--BODY--\n");


        int s = 0;
        for (StateEncoding stateenc : stateEncodings) {
            result.append("State: ");
            result.append(s);
            result.append("\n");
            for (TransitionEncoding trans : stateenc.getTransitionColl()) {
                result.append("[");
                result.append(trans.getEncodedTransitionAPConjunct());
                result.append("]");
                String targetState = trans.getTargetState();
                int targetStateindex = stateList.indexOf(targetState);
                assert targetStateindex == -1 : "target  state not in statelist";
                result.append(" ").append(targetStateindex);
                result.append(" {0}\n");  //all are in the same buchi acceptance set
            }
            s++;
        }
        result.append("--END--\n");
        result.append("EOF_HOA");
        return result.toString();
    }

    public String makeETFOutput() {
       return makeETFOutput(true);
    }
    public String makeETFOutput(boolean needArtificialInitialState) {
        //see https://ltsmin.utwente.nl/assets/man/etf.html
        StringBuilder result = new StringBuilder();
        StringBuilder temptransresult = new StringBuilder();

        result.append("%tool: \"TESTAR-CSS20200126\"\n");
        result.append("%name: \"" + "app= ").
                append(this.getApplicationName()).
                append(", ver=").
                append(this.getApplicationVersion()).
                append(", modelid= ").
                append(this.getApplication_ModelIdentifier()).
                append(", abstraction= ").
                append(this.getApplication_AbstractionAttributes()).
                append("\"\n");
        result.append("%modified: ").append(get_modifieddate()).append("\n");
        result.append("%\n");
        result.append("begin state\n");
        int chunk = 25;
        int i = 0;
        //   result.append("stateid:stateid\n");
        for (String ignored : modelAPs) {
            result.append(APPrefix).append(i).append(":bool ");
            if (i > 0 && (i % chunk) == 0) {
                result.append("\n");
            }
            i++;
        }
        result.append("\n");
        result.append("end state\n");
        result.append("begin edge\n");
        //result.append("transition:transition\n");
        result.append("end edge\n");

        result.append("begin init\n");

        if (needArtificialInitialState && InitialStates.size()>1) {
            result.append("%Multiple Initial States found: Adding Artificial start-state that forks to original initial-states\n");
            result.append("%Requires that formulas need to be modified: F(f) => X(f), where X is the next Operator\n");
            result.append("%To always satisfy the new artificial initial state and maintaining the semantics of the original model\n");
            //make new initial state

            for (String ignore :modelAPs) {
                result.append("0 ");
            }
            result.append("\n");

        }
        else{
            for (String initstate : InitialStates  //max one !!
            ) {
                for (StateEncoding stenc : stateEncodings
                ) {
                    if (stenc.getState().equals(initstate)) {
                        String[] stateaps = stenc.getEncodedStateAPConjunct().split("&");
                        for (String ap : stateaps
                        ) {
                            if (ap.startsWith("!")) {
                                result.append("0 ");
                            } else {
                                result.append("1 ");
                            }
                        }
                        result.append("\n");
                        break;
                    }

                }
            }
            }

        result.append("end init\n");


        if (needArtificialInitialState && InitialStates.size()>1) {
            //add artificial transitions
            result.append("begin trans\n");
            result.append("%artificial transitions to original initial states\n");
            for (String initstate : InitialStates
            ) {
                for (StateEncoding stenc : stateEncodings
                ) {
                    if (stenc.getState().equals(initstate)) {
                        String[] stateaps = stenc.getEncodedStateAPConjunct().split("&");
                        for (String ap : stateaps
                        ) {
                            if (ap.startsWith("!")) {
                                result.append("0/0 ");  //first zero aligns with the artificial initial state values
                            } else {
                                result.append("0/1 ");
                            }
                        }
                        result.append("\n");
                    }
                }
            }
            result.append("end trans\n");

        }
        result.append("begin trans\n");
        for (StateEncoding stenc : stateEncodings
        ) {
            String[] stateaps = stenc.getEncodedStateAPConjunct().split("&");

            Set<StateEncoding> doneState=new HashSet<>();
            for (TransitionEncoding trenc : stenc.getTransitionColl()
            ) {
                //       result.append("" + stateid + " ");
                String targetstate = trenc.getTargetState();
                StateEncoding targetenc = null;
                for (StateEncoding stenc1 : stateEncodings
                ) {
                    if (targetstate.equals(stenc1.getState())) {
                        targetenc = stenc1;
                        break;
                    }
                }
                if (!doneState.contains(targetenc)) {
                    String[] targetaps = targetenc.getEncodedStateAPConjunct().split("&");
                    int idex = 0;
                    for (String ap : stateaps
                    ) {


                        if (ap.startsWith("!")) {
                            result.append("0/");
                        } else {
                            result.append("1/");
                        }
                        if (targetaps[idex].startsWith("!")) {
                            result.append("0 ");
                        } else {
                            result.append("1 ");
                        }


                        idex++;
                    }
                    //   result.append(" " + transindex).append("\n");
                    result.append("\n");
                    doneState.add(targetenc);
                }
            }
        }
        result.append("\n");
        result.append("end trans\n");
//        result.append("begin sort transition\n");
//
//        for (String transid : transitionList) {
//            result.append("\"").append(transid).append("\"\n");
//        }
//
//        result.append("end sort\n");
//        result.append("begin sort stateid\n");
//        for (StateEncoding stenc : stateEncodings) {
//            result.append("\"").append(stenc.getState()).append("\"\n");
//        }
//        result.append("end sort\n");
        result.append("begin sort bool\n");
        result.append("\"0\"\n"); //"false" an d"true are common alternatives
        result.append("\"1\"\n");
        result.append("end sort\n");
        return result.toString();
    }

    public String makeGALOutput() {
        //see https://lip6.github.io/ITSTools-web/galmm.html
        final String edgeprefix = "trans"; // also used for parsing counter examples
        StringBuilder result = new StringBuilder();
        result.append("//tool: \"TESTAR-CSS20200126\"\n");
        result.append("//name: \"" + "app= ").
                append(this.getApplicationName()).
                append(", ver=").
                append(this.getApplicationVersion()).
                append(", modelid= ").
                append(this.getApplication_ModelIdentifier()).
                append(", abstraction= ").
                append(this.getApplication_AbstractionAttributes()).
                append("\"\n");
        result.append("//modified: ").append(get_modifieddate()).append("\n");
        result.append("//\n");

        result.append("gal TESTAR {\n");
        int chunk = 25;
        int i = 0;


        if (InitialStates.size()>1) {
            result.append("int ");
            //out of memory error on ITS above 30.000
            String artifical_StartState=""+ (stateList.size()+1); //statecount plus 1
            result.append("stateindex = ").append(artifical_StartState).append(" ;\n");

            for (String ignored : modelAPs) {
                result.append("int ");
                result.append(APPrefix).append(i).append( " = 0 ; ");
                if (i > 0 && (i % chunk) == 0) {
                    result.append("\n");
                }
                i++;
            }
            result.append("\n");



            result.append("// BEGIN artificial initial states\n");
            for (String initstate : InitialStates
            ) {
                for (StateEncoding stenc : stateEncodings
                ) {
                    if (stenc.getState().equals(initstate)) {

                        String artificalEdge = "artificial_init_transition_to_" + stateList.indexOf(stenc.getState());
                        result.append("    transition ").append(artificalEdge).
                                append(" [ stateindex == ").append(artifical_StartState).append(" ]  {\n");
                        result.append("        stateindex = ").
                                append(stateList.indexOf(stenc.getState())).append(" ;\n");

                        String[] stateaps = stenc.getEncodedStateAPConjunct().split("&");
                        int j = 0;
                        int chunk1 = 25;
                        result.append("        ");
                        for (String ap : stateaps
                        )
                        {

                            if (ap.startsWith("!")) {
                                result.append(APPrefix).append(j).append(" = 0 ; ");
                            } else {
                                result.append(APPrefix).append(j).append(" = 1 ; ");
                            }
                            if (j > 0 && (j % chunk1) == 0) {
                                result.append("\n");
                                result.append("        ");
                            }
                            j++;

                        }
                        result.append("    }\n");
                    }
                }
            }
            result.append("// END artificial initial states\n");
        }
        else{
            result.append("// BEGIN initial state\n");
            result.append("int ");
            for (String initstate : InitialStates
            ) {

                result.append("stateindex = ").append(stateList.indexOf(initstate)).append(" ;\n");
                for (StateEncoding stenc : stateEncodings
                ) {
                    if (stenc.getState().equals(initstate)) {

                        String[] stateaps = stenc.getEncodedStateAPConjunct().split("&");
                        int j = 0;
                        int chunk1 = 25;
                        result.append("        ");
                        for (String ap : stateaps
                        ) {
                            result.append("int ");
                            if (ap.startsWith("!")) {
                                result.append(APPrefix).append(j).append(" = 0 ; ");
                            } else {
                                result.append(APPrefix).append(j).append(" = 1 ; ");
                            }
                            if (j > 0 && (j % chunk1) == 0) {
                                result.append("\n");
                                result.append("        ");
                            }
                            j++;

                        }
                        result.append("\n");
                        break; // there is only one initalState in this scenario
                    }
                }
            }
        }


        result.append("//BEGIN explicit transitions\n");
        for (StateEncoding stenc : stateEncodings
        ) {
            String[] stateaps = stenc.getEncodedStateAPConjunct().split("&");

            Set<StateEncoding> doneState= new HashSet<>();
            for (TransitionEncoding trenc : stenc.getTransitionColl()
            ) {
                //       result.append("" + stateid + " ");
                String targetstate = trenc.getTargetState();
                StateEncoding targetenc = null;
                for (StateEncoding stenc1 : stateEncodings
                ) {
                    if (targetstate.equals(stenc1.getState())) {
                            targetenc = stenc1;
                        break;
                    }
                }
                if (!doneState.contains(targetenc)) {


                    String edge = edgeprefix + trenc.getTransition().replace("#", "_").replace(":", "_");
                    StringBuilder condition = new StringBuilder();
                    StringBuilder assignment = new StringBuilder();

                    condition.append("[ stateindex == ").append(stateList.indexOf(stenc.getState())).append(" ");


                    String[] targetaps = targetenc.getEncodedStateAPConjunct().split("&");
                    int idex = 0;
                    int chunk2 = 25;
                    assignment.append("        stateindex = ").append(stateList.indexOf(targetenc.getState())).append(" ;\n");
                    assignment.append("        ");

                    for (String ap : stateaps
                    ) {
                        if (ap.startsWith("!")) {
                            condition.append(" &&  ").append(APPrefix).append(idex).append(" == 0").append(" ");
                        } else {
                            condition.append(" &&  ").append(APPrefix).append(idex).append(" == 1").append(" ");
                        }
                        if (idex > 0 && (idex % chunk2) == 0) {
                            condition.append("\n");
                            condition.append("        ");
                        }


                        if (targetaps[idex].startsWith("!")) {
                            assignment.append(APPrefix).append(idex).append(" = 0").append(" ; ");
                        } else {
                            assignment.append(APPrefix).append(idex).append(" = 1").append(" ; ");
                        }
                        if (idex > 0 && (idex % chunk2) == 0) {
                            assignment.append("\n");
                            assignment.append("        ");
                        }

                        idex++;
                    }
                    condition.append(" ]\n");
                    // result.append("    transition ").append(artificalEdge).append(" ").append(condition.toString()).append( "label "+"\""+trenc.getTransition()+"\""+" {\n");
                    result.append("    transition ").append(edge).append(" ").append(condition.toString()).append(" {\n");

                    result.append(assignment.toString());
                    result.append("    }\n");


                    //result.append(" " + transindex).append("\n");
                    doneState.add(targetenc);
                }
            }
        }

        result.append("}\n\n");
        result.append("main TESTAR;\n");
        return result.toString();
    }


    public String validateAndMakeFormulas(List<TemporalOracle> oracleColl, boolean doTransformation) {

        StringBuilder Formulas = new StringBuilder();
        String rawFormula;
        for (TemporalOracle candidateOracle : oracleColl) {
            String formula;
            List<String> sortedparameters = new ArrayList<>(candidateOracle.getPatternBase().getPattern_Parameters());//clone list
            Collections.sort(sortedparameters);
            List<String> sortedsubstitionvalues = new ArrayList<>(candidateOracle.getSortedPattern_Substitutions().values());
            sortedsubstitionvalues.removeAll(Collections.singletonList(""));  // discard empty substitutions
            TemporalFormalism tFormalism = TemporalFormalism.valueOf(candidateOracle.getPatternTemporalType().name());

            boolean importStatus;
            rawFormula = candidateOracle.getPatternBase().getPattern_Formula();
            if (rawFormula.toUpperCase().equals("FALSE")){
                importStatus=false; // MS Excel converts 'false' to 'FALSE'. this is interpreted as eventually F(ALSE)
            }
            importStatus = sortedparameters.size() == sortedsubstitionvalues.size();
            if (!importStatus) {
                candidateOracle.addLog("inconsistent number of parameter <-> substitutions");
            }
            if (importStatus)
                importStatus = getModelAPs().containsAll(sortedsubstitionvalues);

            if (!importStatus) {
                candidateOracle.addLog("not all propositions (parameter-substitutions) are found in the Model:");
                for (String subst : sortedsubstitionvalues
                ) {
                    if (!getModelAPs().contains(subst)) candidateOracle.addLog("not found: " + subst);
                }
            }
            if (!importStatus) {
                String falseFormula="false";
                candidateOracle.addLog("setting formula to 'false'");
                String  formulalvl6= tFormalism.line_prepend+StringUtils.replace(falseFormula,tFormalism.false_replace.getLeft(), tFormalism.false_replace.getRight()) + tFormalism.line_append;
                Formulas.append(formulalvl6);
                candidateOracle.setOracle_validationstatus(ValStatus.ERROR);
            } else {

                HashBiMap<Integer, String> aplookup = HashBiMap.create();
                aplookup.putAll(getSimpleModelMap());
                ArrayList<String> apindex = new ArrayList<>();
                if (doTransformation) {

                    String deadprop = getPropositionIndex(deadProposition, true);
                    if (!deadprop.equals("")) { // model has 'dead' as an atomic  property
                        sortedsubstitionvalues.add(deadProposition);
                        sortedparameters.add(deadProposition); // consider 'dead' as a kind of parameter
                    }

                    for (String v : sortedsubstitionvalues
                    ) {
                        if (aplookup.inverse().containsKey(v)) {
                            apindex.add(APPrefix + aplookup.inverse().get(v));
                        } else
                            apindex.add(APPrefix + "_indexNotFound");
                        // will certainly fail if during model-check, because parameters are not prefixed with 'ap'

                    }

                    {
                      //  String rawFormula = candidateOracle.getPatternBase().getPattern_Formula();
                        String formulalvl0 = rawFormula;

                        if( !tFormalism.supportsMultiInitialStates && InitialStates.size()>1){
                            // ETF models for ITS can only have  1 initial state. ETF by LTSMIN can handle multiple
                            //when there are initial states added to the model, the formula alter:
                            //satisfaction of the formula starts after the artificial state, hence the X-operator.
                            if (tFormalism == TemporalFormalism.CTL ||
                                tFormalism == TemporalFormalism.CTL_ITS){ //||tFormalism == TemporalFormalism.CTL_LTSMIN){
                                formulalvl0="AX("+rawFormula+")";
                            }
                            if (tFormalism == TemporalFormalism.LTL_ITS){ //|| tFormalism == TemporalFormalism.LTL_LTSMIN){
                                formulalvl0="X("+rawFormula+")"; //@todo test ltsmin on set of initial states
                            }

                        }
                        String formulalvl1a =  tFormalism.line_prepend+formulalvl0;
                        String formulalvl1 = formulalvl1a + tFormalism.line_append;
                        String formulalvl2 = StringUtils.replace(formulalvl1,
                                tFormalism.finally_replace.getLeft(), tFormalism.finally_replace.getRight());
                        String formulalvl3 = StringUtils.replace(formulalvl2,
                                tFormalism.globally_replace.getLeft(), tFormalism.globally_replace.getRight());
                        String formulalvl4 = StringUtils.replace(formulalvl3,
                                tFormalism.and_replace.getLeft(), tFormalism.and_replace.getRight());
                        String formulalvl5 = StringUtils.replace(formulalvl4,
                                tFormalism.or_replace.getLeft(), tFormalism.or_replace.getRight());
                        String formulalvl6 = StringUtils.replace(formulalvl5,
                                tFormalism.false_replace.getLeft(), tFormalism.false_replace.getRight());

                        apindex.replaceAll(s -> tFormalism.ap_prepend + s + tFormalism.ap_append);

                        formula = StringUtils.replaceEach(formulalvl6,
                                sortedparameters.toArray(new String[0]), apindex.toArray(new String[0]));

                    }
                } else {
                    formula = candidateOracle.getPatternBase().getPattern_Formula();
                }
                Formulas.append(formula);
            }
            Formulas.append("\n"); //always a new line . if formula is not validated a blank line appears

        }

        return Formulas.toString();

    }

    public String getPropositionIndex(String proposition) {

        return getPropositionIndex(proposition, false);
    }

    public String getPropositionIndex(String proposition, boolean raw) {
        HashBiMap<Integer, String> aplookup = HashBiMap.create();
        aplookup.putAll(getSimpleModelMap());
        String encodedAP = "";
        // we encode alive as not dead "!dead"
        // so we strip the negation from the alive property, by default: "!dead"
        if (proposition.startsWith("!") && aplookup.inverse().containsKey(proposition.toLowerCase().substring(1))) {
            encodedAP = "!" + (!raw ? APPrefix : "") + aplookup.inverse().get(proposition.toLowerCase().substring(1));
        }
        if (!proposition.startsWith("!") && aplookup.inverse().containsKey(proposition.toLowerCase())) {
            encodedAP = (!raw ? APPrefix : "") + aplookup.inverse().get(proposition.toLowerCase());
        }
        return encodedAP;
    }


    @JsonGetter("modelAPs")
    private LinkedHashMap<Integer, String> getSimpleModelMap() {
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();

        int i = 0;
        for (String ap : modelAPs) {
            map.put(i, ap);
            i++;
        }
        return map;
    }

    @JsonSetter("modelAPs")
    @SuppressWarnings("unused")
    private void setFromSimpleModelMap(LinkedHashMap<Integer, String> map) {
        this.modelAPs.addAll(map.values());
    }
}