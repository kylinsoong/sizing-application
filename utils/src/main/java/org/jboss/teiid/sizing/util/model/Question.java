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

import org.jboss.teiid.sizing.util.validation.IValidation;

public class Question {
	
	private String id;

	private String question;
	
	private String answer;
	
	private String defAnswer;
	
	private IValidation validation;

	public Question(String id, String question, String answer, String defAnswer, IValidation validation) {
		super();
		this.id = id;
		this.question = question;
		this.answer = answer;
		this.defAnswer = defAnswer;
		this.validation = validation;
	}
	
	public Question(String id, String question, String defAnswer, IValidation validation) {
		super();
		this.id = id;
		this.question = question;
		this.defAnswer = defAnswer;
		this.validation = validation;
	}
	
	public boolean isValidAnswer(String answer){
		return validation.isValid(answer);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getDefAnswer() {
		return defAnswer;
	}

	public void setDefAnswer(String defAnswer) {
		this.defAnswer = defAnswer;
	}
	
	public IValidation getValidation() {
		return validation;
	}

	public void setValidation(IValidation validation) {
		this.validation = validation;
	}

	
	
}
