package org.mdeforge.client;

import org.junit.Test;
import org.mdeforge.business.model.ConformToRelation;
import org.mdeforge.business.model.EcoreMetamodel;
import org.mdeforge.business.model.Model;
import org.mdeforge.business.model.Project;

import junit.framework.TestCase;

public class TestClient extends TestCase {

	private MDEForgeClient c;

//	@Test
//	public void testAddEcoreMetamodel () {
//		try {
//			c = new MDEForgeClient("http://localhost:8080/mdeforge/", "test123", "test123");
//			Model model = new Model();
//			model.setName("android2.model");
//			model.setOpen(true);
//			Project p = new Project();
//			p.setId("5514b9a6d4c6c379396fe8b9");
//			//p.getArtifacts().add(emm);
//			EcoreMetamodel emm = new EcoreMetamodel();
//			emm.setId("551baa6545686c2b30f1351f");
//			ConformToRelation c2 = new ConformToRelation();
//			c2.setFromArtifact(model);
//			c2.setToArtifact(emm);
//			model.getRelations().add(c2);
//			model.getProjects().add(p);
//			c.addModel(model, "temp/android.model");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	@Test	
//	public void testSimilarityEcoreMetamodel () {
//		try {
//			c = new MDEForgeClient("http://localhost:8080/mdeforge/", "test123", "test123");
//			System.out.println(c.getEcoreMetamodelSimilarity("551baa6545686c2b30f1351f", "551baa6545686c2b30f1351f"));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}		
	@Test	
	public void testValidateEcoreMetamodel () {
		try {
			c = new MDEForgeClient("http://localhost:8080/mdeforge/", "test123", "test123");
			System.out.println(c.validateModels("551d16574568e6d8551fc5af"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
