/**
 * Copyright (C) 2010, SAIM team at the MOLE research
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
package org.autosparql.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/** @author Konrad Höffner */
public interface ImageBundle extends ClientBundle
{
	public static final ImageBundle INSTANCE = GWT.create(ImageBundle.class);

	@Source("images/yes_crystal_clear_32.png")
	public ImageResource yes();	
	
	@Source("images/no_crystal_clear_32.png")
	public ImageResource no();	

	@Source("images/wait_animation_128.gif")
	public ImageResource waiting();	
}