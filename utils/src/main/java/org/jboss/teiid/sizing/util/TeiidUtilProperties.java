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
package org.jboss.teiid.sizing.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jboss.teiid.sizing.util.model.CaculationEntity;
import org.jboss.teiid.sizing.util.model.Question;
import org.jboss.teiid.sizing.util.validation.ExpectValueValidation;
import org.jboss.teiid.sizing.util.validation.IValidation;
import org.jboss.teiid.sizing.util.validation.ReturnIntegerValidation;

public class TeiidUtilProperties extends Properties {

	private static final long serialVersionUID = -7779228843988784157L;
	
	public CaculationEntity getCaculationEntity() {
		CaculationEntity entity = new CaculationEntity();
		entity.setSource_count(Integer.parseInt(getProperty("sizeRecommendation.datasources.count")));
		entity.setQueries_concurrent(Integer.parseInt(getProperty("sizeRecommendation.queries.concurrent")));
		entity.setQueries_per_sec(Integer.parseInt(getProperty("sizeRecommendation.queries.per.second")));
		entity.setRow_count_each(Integer.parseInt(getProperty("sizeRecommendation.row.count.each")));
		entity.setRow_size_each(Integer.parseInt(getProperty("sizeRecommendation.row.size.each")));
		entity.setAvg_time_each(Integer.parseInt(getProperty("sizeRecommendation.avg.time.each")));
		entity.setRow_count_federated(Integer.parseInt(getProperty("sizeRecommendation.row.count.federated")));
		entity.setRow_size_federated(Integer.parseInt(getProperty("sizeRecommendation.row.size.federated")));
		entity.setAvg_time_sample(Integer.parseInt(getProperty("sizeRecommendation.avg.time.sample")));
		if(getProperty("sizeRecommendation.sorts.aggregations").equals("Yes")){
			entity.setAggregation(true);
		} else {
			entity.setAggregation(false);
		}
		
		return entity;
	}
	
	public List<Question> sizeRecommendationQuestions() {
		
		List<Question> list = new ArrayList<Question>();
		IValidation validation = new ReturnIntegerValidation();
		
		list.add(new Question("sizeRecommendation.datasources.count", TeiidUtilPlugin.Util.getString("sizeRecommendation.datasources.count"), getProperty("sizeRecommendation.datasources.count", "2"), validation));
		list.add(new Question("sizeRecommendation.queries.concurrent", TeiidUtilPlugin.Util.getString("sizeRecommendation.queries.concurrent"), getProperty("sizeRecommendation.queries.concurrent", "500"), validation));
		list.add(new Question("sizeRecommendation.queries.per.second", TeiidUtilPlugin.Util.getString("sizeRecommendation.queries.per.second"), getProperty("sizeRecommendation.queries.per.second", "200"), validation));
		list.add(new Question("sizeRecommendation.row.count.each", TeiidUtilPlugin.Util.getString("sizeRecommendation.row.count.each"), getProperty("sizeRecommendation.row.count.each", "1000"), validation));
		list.add(new Question("sizeRecommendation.row.size.each", TeiidUtilPlugin.Util.getString("sizeRecommendation.row.size.each"), getProperty("sizeRecommendation.row.size.each", "100"), validation));
		list.add(new Question("sizeRecommendation.avg.time.each", TeiidUtilPlugin.Util.getString("sizeRecommendation.avg.time.each"), getProperty("sizeRecommendation.avg.time.each", "200"), validation));
		list.add(new Question("sizeRecommendation.row.count.federated", TeiidUtilPlugin.Util.getString("sizeRecommendation.row.count.federated"), getProperty("sizeRecommendation.row.count.federated", "1000"), validation));
		list.add(new Question("sizeRecommendation.row.size.federated", TeiidUtilPlugin.Util.getString("sizeRecommendation.row.size.federated"), getProperty("sizeRecommendation.row.size.federated", "200"), validation));	
		list.add(new Question("sizeRecommendation.avg.time.sample", TeiidUtilPlugin.Util.getString("sizeRecommendation.avg.time.sample"), getProperty("sizeRecommendation.avg.time.sample", "2000"), validation));
		list.add(new Question("sizeRecommendation.sorts.aggregations", TeiidUtilPlugin.Util.getString("sizeRecommendation.sorts.aggregations"), getProperty("sizeRecommendation.sorts.aggregations", "No"), new ExpectValueValidation("Yes", "No")));
		
		return list;
	}

	public String getToolClass() {
		
		String mode = getProperty("mode", "sizeRecommendation");
		
		if(mode.equals("sizeRecommendation")){
			return "org.jboss.teiid.util.SizeRecommendation";
		} else if(mode.equals("deserialization")){
			return "org.jboss.teiid.util.DeserializationTool";
		} else if(mode.equals("serialization")){
			return "org.jboss.teiid.util.SerializationTool";
		}
		
		return "org.jboss.teiid.util.SizeRecommendation";
	}

	public void updateProperties(List<Question> list) {

		for(int i = 0 ; i < list.size() ; i ++){
			Question question = list.get(i);
			setProperty(question.getId(), question.getAnswer());
		}
	}

}
