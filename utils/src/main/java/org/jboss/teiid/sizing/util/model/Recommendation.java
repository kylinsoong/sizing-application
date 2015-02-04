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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recommendation {
	
	private static final String KEY_HEAP = "key-heap";
	private static final String KEY_CORE = "key-core";
	private static final String KEY_SUGG = "key-suggestion";
	private static final String KEY_DISK = "key-suggestion";

	Map<String, List<String>> recommendationMap = new HashMap<String, List<String>>(3);
	
	public List<String> getHeapRecommendation() {
		return recommendationMap.get(KEY_HEAP);
	}
	
	public List<String> getCoreRecommendation() {
		return recommendationMap.get(KEY_CORE);
	}
	
	public List<String> getSuggestion() {
		return recommendationMap.get(KEY_SUGG);
	}
	
	public List<String> getDiskRecommendation() {
		return recommendationMap.get(KEY_DISK);
	}
	
	public void addHeapRecommendation(String item) {
		List<String> list = recommendationMap.get(KEY_HEAP);
		if(null == list) {
			list = new ArrayList<String>();
			recommendationMap.put(KEY_HEAP, list);
		}
		list.add(item);
	}
	
	public void addCoreRecommendation(String item) {
		List<String> list = recommendationMap.get(KEY_CORE);
		if(null == list) {
			list = new ArrayList<String>();
			recommendationMap.put(KEY_CORE, list);
		}
		list.add(item);
	}

	public void addSuggestion(String item) {
		List<String> list = recommendationMap.get(KEY_SUGG);
		if(null == list) {
			list = new ArrayList<String>();
			recommendationMap.put(KEY_SUGG, list);
		}
		list.add(item);
	}
	
	public void addDiskRecommendation(String item) {
		List<String> list = recommendationMap.get(KEY_DISK);
		if(null == list) {
			list = new ArrayList<String>();
			recommendationMap.put(KEY_DISK, list);
		}
		list.add(item);
	}
}
