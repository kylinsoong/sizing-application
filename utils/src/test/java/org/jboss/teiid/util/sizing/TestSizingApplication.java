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
package org.jboss.teiid.util.sizing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * This unit test used to verify sizing application on Red Hat Customer Portal
 *
 */
public class TestSizingApplication {
	
	@Test
	public void testHeapCacualtion1() {
		CaculationEntity entity = new CaculationEntity(2, 500, 100, 10000, 100, 1000, 10000, 1000, 2000, false);
		CaculationTool tool = new CaculationTool(entity);
		assertEquals(16, tool.heapCaculation());
	}
	
	@Test
	public void testHeapCacualtion2() {
		CaculationEntity entity = new CaculationEntity(10, 1000, 100, 10000, 100, 1000, 10000, 1000, 2000, false);
		CaculationTool tool = new CaculationTool(entity);
		assertEquals(50, tool.heapCaculation());
	}
	
	@Test
	public void testCoreSizeCaculation1() {
		CaculationEntity entity = new CaculationEntity(2, 500, 100, 10000, 100, 1000, 10000, 1000, 2000, false);
		CaculationTool tool = new CaculationTool(entity);
		assertEquals(27, tool.coreCaculation());
	}
	
	@Test
	public void testCoreSizeCaculation2() {
		CaculationEntity entity = new CaculationEntity(2, 500, 100, 10000, 100, 500, 10000, 1000, 2000, false);
		CaculationTool tool = new CaculationTool(entity);
		assertEquals(58, tool.coreCaculation());
	}
	
	@Test
	public void testCoreSizeCaculation3() {
		CaculationEntity entity = new CaculationEntity(2, 500, 500, 10000, 100, 500, 10000, 1000, 2000, false);
		CaculationTool tool = new CaculationTool(entity);
		assertEquals(128, tool.coreCaculation());
	}

}
