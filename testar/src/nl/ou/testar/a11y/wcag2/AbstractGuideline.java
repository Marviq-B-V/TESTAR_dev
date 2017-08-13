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
import java.util.List;
import java.util.Set;

import org.fruit.Assert;
import org.fruit.alayer.Action;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Verdict;

/**
 * An abstract WCAG guideline
 * Subclasses implement specific guideline behavior.
 * @author Davy Kager
 *
 */
public abstract class AbstractGuideline extends ItemBase {
	
	/**
	 * Constructs a new guideline
	 * @param nr The number of the guideline.
	 * @param name The name of the guideline.
	 * @param parent The principle (parent) this guideline belongs to.
	 */
	protected AbstractGuideline(int nr, String name, Principle parent) {
		super(nr, name, Assert.notNull(parent));
	}
	
	/**
	 * Evaluates the accessibility of the given state
	 * This will typically include one or more evaluation results for each success criterion that belong to this guideline.
	 * @param state The state.
	 * @return The results of the evaluation.
	 */
	protected abstract EvaluationResults evaluate(State state);
	
	/**
	 * Derives the follow-up actions from the given state
	 * This will typically include actions from all success criteria that belong to this guideline.
	 * The actions are specific to accessibility.
	 * @param state The state.
	 * @return The set of actions.
	 */
	protected abstract Set<Action> deriveActions(State state);
	
}
