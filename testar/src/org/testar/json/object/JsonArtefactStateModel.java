/***************************************************************************************************
 *
 * Copyright (c) 2019 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2019 Open Universiteit - www.ou.nl
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

package org.testar.json.object;

import java.io.File;
import java.io.FileWriter;
import java.util.Set;

import org.fruit.monkey.SettingsDialog;
import org.testar.OutputStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import es.upv.staq.testar.NativeLinker;

public class JsonArtefactStateModel {
	
	private JsonArtefactStateModel() {}
	
	private static String url = "https://testar.org/images/models/";
	
	@JsonCreator
	public static void createStateModelArtefact(String outputPath, String applicationName, String applicationVersion,
			String abstractionId, boolean deterministic, int unvisitedActions) {
		
		SutJsonObject sutJson = new SutJsonObject("sutTitle", "sutName", true, "license", "sutURL", "1.X.X",
				NativeLinker.getOsName());

		ToolJsonObject toolJson = new ToolJsonObject("TESTAR", "TESTAR: Automated Robustness Testing at the GUI level",
				true, "BSD-3-Clause License", "https://github.com/TESTARtool/TESTAR_dev/tree/decoder_pkm", SettingsDialog.TESTAR_VERSION,
				NativeLinker.getOsName());
		
		StateModelJsonObject modelJson = new StateModelJsonObject(OutputStructure.startOuterLoopDateString,
				url, sutJson, toolJson, applicationName, applicationVersion, "a",
				abstractionId, deterministic, unvisitedActions,
				0, 0, 0, 0,
				false, 0, 0);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String outputFile = outputPath + File.separator +
				"ArtefactStateModel_" + applicationName + "_" + applicationVersion + ".json";
		
		try{
			FileWriter fileWriter = new FileWriter(outputFile);
			gson.toJson(modelJson, fileWriter);
			fileWriter.flush();
			fileWriter.close();
		}catch(Exception e){
			System.out.println("ERROR! Creating JSON ArtefactStateModel!");
		}
	}
	
	@JsonCreator
	public static void automaticStateModelArtefact(String applicationName, String applicationVersion, String modelIdentifier,
			String abstractionId, boolean deterministic, long unvisitedActions,
			long abstractStates, long abstractActions, long concreteStates, long concreteActions,
			boolean storeWidgets, long widgets, long testSequences, Set<StateModelTestSequenceJsonObject> testSequenceObject) {
		
		SutJsonObject sutJson = new SutJsonObject("sutTitle", "sutName", true, "license", "sutURL", "1.X.X",
				NativeLinker.getOsName());

		ToolJsonObject toolJson = new ToolJsonObject("TESTAR", "TESTAR: Automated Robustness Testing at the GUI level",
				true, "BSD-3-Clause License", "https://github.com/TESTARtool/TESTAR_dev/tree/decoder_pkm", SettingsDialog.TESTAR_VERSION,
				NativeLinker.getOsName());
		
		StateModelJsonObject modelJson = new StateModelJsonObject(OutputStructure.startOuterLoopDateString,
				url, sutJson, toolJson, applicationName, applicationVersion, modelIdentifier,
				abstractionId, deterministic, unvisitedActions,
				abstractStates, abstractActions, concreteStates, concreteActions,
				storeWidgets, widgets, testSequences);
		
		modelJson.setTestSequences(testSequenceObject);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String outputFile = OutputStructure.outerLoopOutputDir + File.separator +
				"ArtefactStateModel_" + applicationName + "_" + applicationVersion + "_" +
				modelIdentifier + "_" + OutputStructure.startOuterLoopDateString + ".json";
		
		try{
			FileWriter fileWriter = new FileWriter(outputFile);
			gson.toJson(modelJson, fileWriter);
			fileWriter.flush();
			fileWriter.close();
			System.out.println("Created JSON State Model artefact: " + outputFile);
		}catch(Exception e){
			System.out.println("ERROR! Creating JSON ArtefactStateModel!");
		}
	}

}
