/**
 * Copyright (C) 2011, SAIM team at the MOLE research
 * group at AKSW / University of Leipzig
 *
 * This file is part of SAIM (Semi-Automatic Instance Matcher).
 *
 * SAIM is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SAIM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.autosparql.client.widget;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;


/** @author Konrad Höffner */
public class WaitDialog extends DialogBox
{	
	HTML html;
	
	public void setText(String text)
	{
		html.setHTML("<CENTER>"+text+"</CENTER>");
	}
	
	public WaitDialog(String text)
	{
		FlowPanel panel = new FlowPanel();
		this.setWidget(panel);
		html = new HTML();
		html.setText(text);
		panel.add(html);
//		Image image = new Image(ImageBundle.INSTANCE.waiting());
//		image.setStyleName("center");
//		panel.add(image);	
		this.center();
	}
}
