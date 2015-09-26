package org.mdeforge.importer;

import java.util.Date;
import java.util.List;

import org.mdeforge.business.EcoreMetamodelService;
import org.mdeforge.business.model.EcoreMetamodel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CreareEcoreSimilarityRelation {
	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"file:src/main/webapp/WEB-INF/spring/root-context.xml");
		EcoreMetamodelService ecoreMetamodelService = context.getBean(EcoreMetamodelService.class);
//		ecoreMetamodelService.createIndex();
		List<EcoreMetamodel> ecoreMMlist = ecoreMetamodelService.findAllPublic();
		EcoreMetamodel [] ecoreMMArray = ecoreMMlist.toArray(new EcoreMetamodel[ecoreMMlist.size()]);
		System.out.println("start time: " + new Date());
		for (int i =0; i < ecoreMMArray.length-1; i++) {
			System.out.println(ecoreMMArray[i].getName() + " " + i + " of 299 start time: " + new Date());
			for (int j = i+1; j <ecoreMMArray.length; j++) {
				try {
					ecoreMetamodelService.calculateSimilarity(ecoreMMArray[i], ecoreMMArray[j]);
				} catch (Exception e) {
					System.err.println("ERROR: " + ecoreMMArray[i].getName() + "_" + ecoreMMArray[j].getName());
				}
			}
		}
		System.out.println("end time: " + new Date());
	}
}