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

package org.testar.ios.actions;

import org.fruit.Util;
import org.fruit.alayer.AbsolutePosition;
import org.fruit.alayer.Action;
import org.fruit.alayer.Color;
import org.fruit.alayer.FillPattern;
import org.fruit.alayer.Pen;
import org.fruit.alayer.Position;
import org.fruit.alayer.Role;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.TaggableBase;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionFailedException;
import org.fruit.alayer.visualizers.TextVisualizer;
import org.testar.ios.IOSAppiumFramework;
import org.testar.ios.enums.IOSRoles;

public class IOSActionType extends TaggableBase implements Action {

	private static final long serialVersionUID = 6685918140970666660L;

	private String type;
	private String resourceId;
	
	private static final Pen TypePen = Pen.newPen().setColor(Color.Blue)
	        .setFillPattern(FillPattern.None).setStrokeWidth(3).build(); // use default font size
	private final int DISPLAY_TEXT_MAX_LENGTH = 16;

	public IOSActionType(State state, Widget w, String type, String resourceId) {
	    this.set(Tags.Role, IOSRoles.iosWidget);
	    this.set(Tags.OriginWidget, w);
	    this.type = type;
	    this.resourceId = resourceId;
	    this.set(Tags.Desc, toShortString());
	    double relX = w.get(Tags.Shape).x() + w.get(Tags.Shape).width()/2;
	    double relY = w.get(Tags.Shape).y() + w.get(Tags.Shape).height()/2;
	    Position position = new AbsolutePosition(relX, relY);
	    this.set(Tags.Visualizer, new TextVisualizer(position, Util.abbreviate(type, DISPLAY_TEXT_MAX_LENGTH, "..."), TypePen));
	}

	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		try {
		    IOSAppiumFramework.setValueElementById(this.resourceId, this.type);
		} catch(Exception e) {
			System.out.println("Exception trying to Type " + this.type + " in the Element with Id : " + this.resourceId);
			System.out.println(e.getMessage());
			throw new ActionFailedException(toShortString());
		}
	}

	@Override
	public String toShortString() {
		return "Execute IOS Type: " + this.type + " with Id: " + this.resourceId;
	}

	@Override
	public String toParametersString() {
		return "";
	}

	@Override
	public String toString(Role... discardParameters) {
		return "";
	}

}
