/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.jboss.teiid.sizing.util.model;

import java.util.ArrayList;
import java.util.List;

public class Prompt {

	private String title;
	
	private List<String> items ;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getItems() {
		return items;
	}

	public void setItems(List<String> items) {
		this.items = items;
	}
	
	public void addItem(String item){
		if(null == items) {
			items = new ArrayList<String>();
		}
		items.add(item);
	}
	
	public void addItems(List<String> list){
		if(null == items) {
			items = new ArrayList<String>();
		}
		items.addAll(list);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(title + "\n");
		for(int i = 0 ; i < items.size() ; i ++) {
			sb.append("  " + items.get(i) + "\n");
		}
		return sb.toString();
	}
}
