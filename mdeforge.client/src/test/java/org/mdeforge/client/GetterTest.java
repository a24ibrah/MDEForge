package org.mdeforge.client;

import static org.junit.Assert.assertNotNull;

import java.io.PrintWriter;
import java.util.List;
import java.util.Stack;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mdeforge.business.model.ATLTransformation;
import org.mdeforge.business.model.ETLTransformation;
import org.mdeforge.business.model.EcoreMetamodel;
import org.mdeforge.business.model.Model;

public class GetterTest {

	private static MDEForgeClient c;

	@BeforeClass
	public static void setup() throws Exception {
		c = new MDEForgeClient("http://localhost:8080/mdeforge/", "test123", "test123");
	}
	
	@Ignore
	@Test
	public void getModelsTest() throws Exception {
		System.out.println("###Model");
		for (Model iterable_element : c.getModels()) {
			System.out.println(iterable_element.getName());
		}
		assertNotNull(c);
	}
	@Ignore
	@Test
	public void getEcoreMetamodelsTest() throws Exception {
		System.out.println("###Ecore");
		for (EcoreMetamodel iterable_element : c.getEcoreMetamodels()) {
			System.out.println(iterable_element.getName());
		}
		assertNotNull(c);
	}
	@Ignore
	@Test
	public void getETLTransformationsTest() throws Exception {
		System.out.println("###ETL");
		for (ETLTransformation iterable_element : c.getETLTransformations()) {
			System.out.println(iterable_element.getName());
		}
		assertNotNull(c);
	}
	@Ignore
	@Test
	public void getATLTransformationsTest() throws Exception {
		System.out.println("###ATL");
		for (ATLTransformation iterable_element : c.getATLTransformations()) {
			System.out.println(iterable_element.getName());
		}
		assertNotNull(c);
	}
	@Ignore
	@Test
	public void getEcoreMetanmodelInWorkspaceTest() throws Exception {
		System.out.println("###EcoreWorkspace");
		for (EcoreMetamodel iterable_element : c.getEcoreMetamodelsInWorkspace("5514aa53d4c67eee3e2c1b12")) {
			System.out.println(iterable_element.getName());
		}
		assertNotNull(c);
	}
	@Ignore
	@Test
	public void getEcoreMetamodelsPublicTest() throws Exception {
		System.out.println("###START");
		List<EcoreMetamodel> ecoreMMlist = c.getEcoreMetamodelsPublic();
		EcoreMetamodel [] ecoreMMArray = ecoreMMlist.toArray(new EcoreMetamodel[ecoreMMlist.size()]);
		String [][] matrix = new String[ecoreMMArray.length][ecoreMMArray.length];
		for (int i = 0; i < ecoreMMArray.length-1; i++) {
			for (int j = i+1; j <ecoreMMArray.length; j++){
				String s = c.getEcoreMetamodelSimilarity(ecoreMMArray[i].getId(), ecoreMMArray[j].getId());
				matrix[i][j] = s; 
				double d = Double.parseDouble(s);
				if(d>0.5)
					System.out.println("#######Iteration: " + i + "_" + ecoreMMArray[i].getName() + " : " + j + "_" + ecoreMMArray[j].getName() + " = " + d);
				else
					System.out.println("Iteration: " + i + "_" + ecoreMMArray[i].getName() + " : " + j + "_" + ecoreMMArray[j].getName() + " = " + d);
				
			}
		}
		String printString = "";
		PrintWriter p = new PrintWriter("/Users/juridirocco/Desktop/result.txt");
		for (int i = 86; i < matrix.length; i++){
			for (int j = 0; j <matrix[i].length; j++)
				printString +=(matrix[i][j] == null)?";":";";
			printString+="\n";
		}
		p.println(printString);
		p.close();
		System.out.println("FINITO!!!");
		assertNotNull(c);
	}
	
}