/******************************************************************************************
 * COPYRIGHT:                                                                             *
 * Universitat Politecnica de Valencia 2013                                               *
 * Camino de Vera, s/n                                                                    *
 * 46022 Valencia, Spain                                                                  *
 * www.upv.es                                                                             *
 *                                                                                        * 
 * D I S C L A I M E R:                                                                   *
 * This software has been developed by the Universitat Politecnica de Valencia (UPV)      *
 * in the context of the european funded FITTEST project (contract number ICT257574)      *
 * of which the UPV is the coordinator. As the sole developer of this source code,        *
 * following the signed FITTEST Consortium Agreement, the UPV should decide upon an       *
 * appropriate license under which the source code will be distributed after termination  *
 * of the project. Until this time, this code can be used by the partners of the          *
 * FITTEST project for executing the tasks that are outlined in the Description of Work   *
 * (DoW) that is annexed to the contract with the EU.                                     *
 *                                                                                        * 
 * Although it has already been decided that this code will be distributed under an open  *
 * source license, the exact license has not been decided upon and will be announced      *
 * before the end of the project. Beware of any restrictions regarding the use of this    *
 * work that might arise from the open source license it might fall under! It is the      *
 * UPV's intention to make this work accessible, free of any charge.                      *
 *****************************************************************************************/

package nl.ou.testar.a11y.reporting;

import org.fruit.alayer.Tag;
import org.fruit.alayer.TagsBase;

/**
 * Accessibility tags
 * @author Davy Kager
 *
 */
public final class A11yTags extends TagsBase {

	private A11yTags() {}
	
	public static final Tag<EvaluationResults> A11yEvaluationResults = from("A11yEvaluationResults", EvaluationResults.class);
	public static final Tag<Integer> A11yResultCount = from("A11yResultCount", Integer.class);
	public static final Tag<Integer> A11yPassCount = from("A11yPassCount", Integer.class);
	public static final Tag<Integer> A11yWarningCount = from("A11yWarningCount", Integer.class);
	public static final Tag<Integer> A11yErrorCount = from("A11yErrorCount", Integer.class);
	public static final Tag<Boolean> A11yHasViolations = from("A11yHasViolations", Boolean.class);

}
