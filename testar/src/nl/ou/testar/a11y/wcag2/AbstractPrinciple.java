/*****************************************************************************************
 *                                                                                       *
 * COPYRIGHT (2017):                                                                     *
 * Universitat Politecnica de Valencia                                                   *
 * Camino de Vera, s/n                                                                   *
 * 46022 Valencia, Spain                                                                 *
 * www.upv.es                                                                            *
 *                                                                                       * 
 * D I S C L A I M E R:                                                                  *
 * This software has been developed by the Universitat Politecnica de Valencia (UPV)     *
 * in the context of the TESTAR Proof of Concept project:                                *
 *               "UPV, Programa de Prueba de Concepto 2014, SP20141402"                  *
 * This sample is distributed FREE of charge under the TESTAR license, as an open        *
 * source project under the BSD3 licence (http://opensource.org/licenses/BSD-3-Clause)   *                                                                                        * 
 *                                                                                       *
 *****************************************************************************************/

package nl.ou.testar.a11y.wcag2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.Widget;

import nl.ou.testar.a11y.protocols.Evaluator;

/**
 * A WCAG principle
 * @author Davy Kager
 *
 */
public abstract class AbstractPrinciple extends ItemBase implements Evaluator {
	
	/**
	 * The list of all the guidelines in this principle
	 */
	protected final List<AbstractGuideline> guidelines = new ArrayList<>();

	/**
	 * Constructs a new principle
	 * @param nr The number of the principle.
	 * @param name The name of the principle.
	 */
	protected AbstractPrinciple(int nr, String name) {
		super(nr, name);
	}

	/**
	 * Gets all guidelines in this principle
	 * @return The list of guidelines.
	 */
	public List<AbstractGuideline> getGuidelines() {
		return Collections.unmodifiableList(guidelines);
	}
	
	/**
	 * Evaluates the accessibility of the given state
	 * This will collect evaluation results from all guidelines in this principle.
	 * @param widgets The widgets to consider.
	 * @return The results of the evaluation.
	 */
	@Override
	public EvaluationResults evaluate(List<Widget> widgets) {
		EvaluationResults results = new EvaluationResults();
		for (AbstractGuideline g : guidelines)
			for (EvaluationResult result : g.evaluate(widgets).getResults())
				results.add(result);
		return results;
	}
	
	/**
	 * Derives the follow-up actions from the given state
	 * This will collect actions from all guidelines in this principle.
	 * The actions are specific to accessibility.
	 * @param widgets The widgets to consider.
	 * @return The set of actions.
	 */
	@Override
	public Set<Action> deriveActions(List<Widget> widgets) {
		Set<Action> actions = new HashSet<>();
		for (AbstractGuideline g : guidelines)
			actions.addAll(g.deriveActions(widgets));
		return actions;
	}
		
}