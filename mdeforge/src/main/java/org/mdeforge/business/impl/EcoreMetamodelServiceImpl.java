package org.mdeforge.business.impl;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.bson.types.ObjectId;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.m2m.atl.core.ATLCoreException;
import org.eclipse.m2m.atl.core.IExtractor;
import org.eclipse.m2m.atl.core.IInjector;
import org.eclipse.m2m.atl.core.IModel;
import org.eclipse.m2m.atl.core.IReferenceModel;
import org.eclipse.m2m.atl.core.ModelFactory;
import org.eclipse.m2m.atl.core.emf.EMFExtractor;
import org.eclipse.m2m.atl.core.emf.EMFInjector;
import org.eclipse.m2m.atl.core.emf.EMFModelFactory;
import org.eclipse.m2m.atl.core.emf.EMFReferenceModel;
import org.eclipse.m2m.atl.core.launch.ILauncher;
import org.eclipse.m2m.atl.engine.emfvm.launch.EMFVMLauncher;
import org.mdeforge.business.BusinessException;
import org.mdeforge.business.ContainmentRelationService;
import org.mdeforge.business.CosineSimilarityRelationService;
import org.mdeforge.business.DiceSimilarityRelationService;
import org.mdeforge.business.DuplicateNameException;
import org.mdeforge.business.EcoreMetamodelService;
import org.mdeforge.business.ProjectService;
import org.mdeforge.business.RequestGrid;
import org.mdeforge.business.ResponseGrid;
import org.mdeforge.business.SimilarityRelationService;
import org.mdeforge.business.ValuedRelationService;
import org.mdeforge.business.WorkspaceService;
import org.mdeforge.business.model.AggregatedIntegerMetric;
import org.mdeforge.business.model.AggregatedRealMetric;
import org.mdeforge.business.model.Artifact;
import org.mdeforge.business.model.Cluster;
import org.mdeforge.business.model.ContainmentRelation;
import org.mdeforge.business.model.CosineSimilarityRelation;
import org.mdeforge.business.model.DiceSimilarityRelation;
import org.mdeforge.business.model.EcoreMetamodel;
import org.mdeforge.business.model.Metric;
import org.mdeforge.business.model.Property;
import org.mdeforge.business.model.Relation;
import org.mdeforge.business.model.SimilarityRelation;
import org.mdeforge.business.model.SimpleMetric;
import org.mdeforge.business.model.ValuedRelation;
import org.mdeforge.business.search.ResourceSerializer;
import org.mdeforge.business.search.SimilarityMethods;
import org.mdeforge.business.search.jsonMongoUtils.JsonMongoResourceSet;
import org.mdeforge.emf.metric.Container;
import org.mdeforge.emf.metric.MetricFactory;
import org.mdeforge.emf.metric.MetricPackage;
import org.mdeforge.integration.EcoreMetamodelRepository;
import org.mdeforge.integration.MetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.stereotype.Service;

import uk.ac.shef.wit.simmetrics.similaritymetrics.DiceSimilarity;

import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.apporiented.algorithm.clustering.SingleLinkageStrategy;
import com.apporiented.algorithm.clustering.visualization.DendrogramPanel;
import com.google.common.collect.Lists;
import com.mongodb.Mongo;

@Service
public class EcoreMetamodelServiceImpl extends
		CRUDArtifactServiceImpl<EcoreMetamodel> implements
		EcoreMetamodelService {
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private WorkspaceService workspaceService;
	@Autowired
	private Mongo mongo;
	@Autowired
	private SimpleMongoDbFactory mongoDbFactory;
	@Autowired
	private JsonMongoResourceSet jsonMongoResourceSet;
	@Autowired
	private EcoreMetamodelRepository ecoreMetamodelRepository;
	@Autowired
	private MetricRepository metricRepository;
	@Autowired
	private RelationService relationService;
	@Autowired
	private SimilarityRelationService similarityRelationService;
	@Autowired
	private DiceSimilarityRelationService diceSimilarityRelationService;
	@Autowired
	private ContainmentRelationService containmentRelationService;
	@Autowired
	private CosineSimilarityRelationService cosineSimilarityRelationService;
	@Value("#{cfgproperties[basePath]}")
	protected String basePath;

	@Value("#{cfgproperties[mongoPrefix]}")
	private String mongoPrefix;
	@Value("#{cfgproperties[jsonArtifactCollection]}")
	private String jsonArtifactCollection;


	@Override
	public EcoreMetamodel create(EcoreMetamodel artifact) {
		if(findOneByName(artifact.getName())!=null)
			throw new DuplicateNameException();

		artifact.setValid(isValid(artifact));
//		String path = gridFileMediaService.getFilePath(artifact);
		EcoreMetamodel result = super.create(artifact);
//		String jsonMongoUriBase = mongoPrefix + mongo.getAddress().toString() + "/"+mongoDbFactory.getDb().getName() + "/" + jsonArtifactCollection + "/";
//		artifact.setExtractedContents(this.saveJsonMetamodel(path, jsonMongoUriBase + artifact.getId()));
//		artifactRepository.save(artifact);
		return result;
	}
	
	public String saveJsonMetamodel(String sourceURI, String mongoURI) throws BusinessException {
		String contentsString = "";
		ResourceSet load_resourceSet = new ResourceSetImpl();
		load_resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		Resource load_resource = load_resourceSet.getResource(URI.createURI(sourceURI),true);
		
		EList<EObject> contents = load_resource.getContents();
		ResourceSerializer.serialize(load_resource);
		Resource res = jsonMongoResourceSet.getResourceSet().createResource(URI.createURI(mongoURI));

		res.getContents().addAll(contents);

		try {
			res.save(null);
		} catch (IOException e) {
			throw new BusinessException();
		}
		return contentsString;
	}

	@Override
	public EcoreMetamodel findOnePublic(String id) {
		EcoreMetamodel a = super.findOnePublic(id);
		a.setMetrics(getMetrics(a));
		if (a.getExtractedContents()==null)
			a.setExtractedContents(serializeContent(a));
		a.getRelations().addAll(
				similarityRelationService.findTopProximity(a, 5));
		a.getRelations().addAll(
				containmentRelationService.findTopProximity(a, 5));
		a.getRelations().addAll(
				diceSimilarityRelationService.findTopProximity(a, 5));
		a.getRelations().addAll(
				cosineSimilarityRelationService.findTopProximity(a, 5));

		return a;
	}

	@Override
	public List<EcoreMetamodel> findEcoreMetamodelByURI(String URI) {
		return null;
	}

	@Override
	public ResponseGrid<EcoreMetamodel> findAllEcorePaginated(
			RequestGrid requestGrid) throws BusinessException {
		Page<EcoreMetamodel> rows = null;
		if (requestGrid.getSortDir().compareTo("asc") == 0) {
			rows = ecoreMetamodelRepository.findByOpen(
					true,
					new PageRequest(requestGrid.getiDisplayStart()
							/ requestGrid.getiDisplayLength(), requestGrid
							.getiDisplayLength(), Direction.ASC, requestGrid
							.getSortCol()));
		} else {
			rows = ecoreMetamodelRepository.findByOpen(
					true,
					new PageRequest(requestGrid.getiDisplayStart()
							/ requestGrid.getiDisplayLength(), requestGrid
							.getiDisplayLength(), Direction.DESC, requestGrid
							.getSortCol()));
		}
		return new ResponseGrid<EcoreMetamodel>(requestGrid.getsEcho(),
				rows.getNumberOfElements(), rows.getTotalElements(),
				rows.getContent());
	}

	@Override
	public List<Metric> calculateMetrics(Artifact emm) throws BusinessException {
		ILauncher transformationLauncher = new EMFVMLauncher();
		ModelFactory modelFactory = new EMFModelFactory();
		IInjector injector = new EMFInjector();
		IExtractor extractor = new EMFExtractor();
		/*
		 * Load metamodels
		 */
		try {
			IReferenceModel outputMetamodel = modelFactory.newReferenceModel();
			injector.inject(outputMetamodel, basePath + "Metric.ecore");
			IReferenceModel inputMetamodel = modelFactory.newReferenceModel();
			injector.inject(inputMetamodel,
					org.eclipse.emf.ecore.EcorePackage.eNS_URI);
			IModel inputModel = modelFactory.newModel(inputMetamodel);
			IModel outModel = modelFactory.newModel(outputMetamodel);
			String path = gridFileMediaService.getFilePath(emm);
			injector.inject(inputModel, new FileInputStream(new File(path)),
					null);
			transformationLauncher.initialize(new HashMap<String, Object>());
			transformationLauncher.addInModel(inputModel, "IN", "Ecore");
			transformationLauncher.addOutModel(outModel, "OUT", "Metric");
			transformationLauncher.launch(ILauncher.RUN_MODE, null,
					new HashMap<String, Object>(),
					(Object[]) getModulesList(basePath + "EcoreMetric.asm"));
			extractor.extract(outModel, basePath + "sampleCompany_Cut.xmi");
			EMFModelFactory emfModelFactory = (EMFModelFactory) modelFactory;
			emfModelFactory.unload((EMFReferenceModel) inputMetamodel);
			emfModelFactory.unload((EMFReferenceModel) outputMetamodel);
			List<Metric> result = getMetricList(basePath
					+ "sampleCompany_Cut.xmi", emm);
			File temp2 = new File(basePath + "sampleCompany_Cut.xmi");
			temp2.delete();
			metricRepository.save(result);
			return result;
		} catch (ATLCoreException e) {
			throw new BusinessException();
		} catch (IOException e) {
			throw new BusinessException();
		}
//		List<Metric> a = new ArrayList();
//		return a;
	}

	private InputStream[] getModulesList(String modules_input)
			throws IOException {
		InputStream[] modules = null;
		String[] moduleNames = modules_input.split(",");
		modules = new InputStream[moduleNames.length];
		for (int i = 0; i < moduleNames.length; i++) {
			String asmModulePath = new Path(moduleNames[i].trim())
					.removeFileExtension().addFileExtension("asm").toString();
			modules[i] = new FileInputStream(asmModulePath);
		}
		return modules;
	}

	private List<Metric> getMetricList(String path, Artifact art) {
		MetricPackage.eINSTANCE.eClass();
		// Retrieve the default factory singleton
		@SuppressWarnings("unused")
		MetricFactory factory = MetricFactory.eINSTANCE;
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("xmi", new XMIResourceFactoryImpl());
		// Obtain a new resource set
		ResourceSet resSet = new ResourceSetImpl();
		// Create a resource
		Resource resource = resSet.createResource(URI.createURI(path));
		try {
			resource.load(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Container myForge = (Container) resource.getContents().get(0);
		List<Metric> result = new ArrayList<Metric>();
		Iterator<org.mdeforge.emf.metric.Metric> it = myForge.getMetrics()
				.iterator();
		while (it.hasNext()) {
			org.mdeforge.emf.metric.Metric at2 = (org.mdeforge.emf.metric.Metric) it
					.next();
			Metric metric = null;
			if (at2 instanceof org.mdeforge.emf.metric.impl.SimpleMetricImpl) {
				SimpleMetric metric2 = new SimpleMetric();
				metric2.setName(at2.getName());
				metric2.setDescription(at2.getDescription());
				metric2.setValue(((org.mdeforge.emf.metric.impl.SimpleMetricImpl) at2)
						.getValue());
				metric = metric2;
			}
			if (at2 instanceof org.mdeforge.emf.metric.impl.AggregatedIntegerMetricImpl) {
				AggregatedIntegerMetric metric2 = new AggregatedIntegerMetric();
				metric2.setAverage(((org.mdeforge.emf.metric.impl.AggregatedIntegerMetricImpl) at2)
						.getAverage());
				metric2.setMaximum(((org.mdeforge.emf.metric.impl.AggregatedIntegerMetricImpl) at2)
						.getMaximum());
				metric2.setMedian(((org.mdeforge.emf.metric.impl.AggregatedIntegerMetricImpl) at2)
						.getMedian());
				metric2.setMinimum(((org.mdeforge.emf.metric.impl.AggregatedIntegerMetricImpl) at2)
						.getMinimum());
				metric2.setName(at2.getName());
				metric2.setDescription(at2.getDescription());
				metric = metric2;
			}
			if (at2 instanceof org.mdeforge.emf.metric.impl.AggregatedRealMetricImpl) {
				AggregatedRealMetric metric2 = new AggregatedRealMetric();
				metric2.setAverage(((org.mdeforge.emf.metric.impl.AggregatedRealMetricImpl) at2)
						.getAverage());
				metric2.setMaximum(((org.mdeforge.emf.metric.impl.AggregatedRealMetricImpl) at2)
						.getMaximum());
				metric2.setMedian(((org.mdeforge.emf.metric.impl.AggregatedRealMetricImpl) at2)
						.getMedian());
				metric2.setMinimum(((org.mdeforge.emf.metric.impl.AggregatedRealMetricImpl) at2)
						.getMinimum());
				metric2.setName(at2.getName());
				metric2.setDescription(at2.getDescription());
				metric = metric2;
			}
			metric.setArtifact(art);
			// metricRepository.save(metric);
			result.add(metric);
		}
		return result;
	}

	@Override
	public void registerMetamodel(EcoreMetamodel ecoreMetamodel)
			throws BusinessException {
		ecoreMetamodel = ecoreMetamodelRepository.findOne(ecoreMetamodel.getId());
		ecoreMetamodel.getFile();
		ecoreMetamodel.setFile(gridFileMediaService.getGridFileMedia(ecoreMetamodel.getFile()));
		String path = gridFileMediaService.getFilePath(ecoreMetamodel);
		File fileName = new File(path);
		URI uri = URI.createFileURI(fileName.getAbsolutePath());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		ResourceSet rs = new ResourceSetImpl();
		// enable extended metadata
		final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(rs.getPackageRegistry());
		rs.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetaData);
		Resource r = rs.getResource(uri, true);
		List<EObject> eObject = r.getContents();
		for (EObject eObject2 : eObject) {
			if (eObject2 instanceof EPackage) {
				EPackage p = (EPackage) eObject2;
				registerSubPackage(p);
			}
		}
	}

	@Override
	public List<String> getNSUris(EcoreMetamodel ecoreMetamodel)
			throws BusinessException {
		List<String> result = new ArrayList<String>();
		ecoreMetamodel = ecoreMetamodelRepository.findOne(ecoreMetamodel
				.getId());
		ecoreMetamodel.setFile(gridFileMediaService
				.getGridFileMedia(ecoreMetamodel.getFile()));
		String path = gridFileMediaService.getFilePath(ecoreMetamodel);
		File fileName = new File(path);
		URI uri = URI.createFileURI(fileName.getAbsolutePath());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"ecore", new EcoreResourceFactoryImpl());
		ResourceSet rs = new ResourceSetImpl();
		// enable extended metadata
		final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(
				rs.getPackageRegistry());
		rs.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA,
				extendedMetaData);
		Resource r = rs.getResource(uri, true);
		List<EObject> eObject = r.getContents();
		for (EObject eObject2 : eObject) {
			if (eObject2 instanceof EPackage) {
				EPackage p = (EPackage) eObject2;
				String s = p.getNsURI();
				if (s != null && !s.isEmpty())
					result.add(p.getNsURI());
				result.addAll(getNSUris(p));
			}
		}
		return result;
	}

	private List<String> getNSUris(EPackage p) {
		List<String> result = new ArrayList<String>();
		EPackage.Registry.INSTANCE.put(p.getNsURI(), p);
		for (EPackage pack : p.getESubpackages()) {
			String s = pack.getNsURI();
			if (s != null && !s.isEmpty())
				result.add(pack.getNsURI());
			result.addAll(getNSUris(pack));
		}
		return result;
	}

	private void registerSubPackage(EPackage p) {
		EPackage.Registry.INSTANCE.put(p.getNsURI(), p);
		for (EPackage pack : p.getESubpackages()) {
			registerSubPackage(pack);
		}
	}

	@Override
	public boolean isValid(Artifact art) {
		if (art instanceof EcoreMetamodel) {
			try {
				@SuppressWarnings("unused")
				EcoreFactory factory = EcoreFactory.eINSTANCE;
				ResourceSet resourceSet = new ResourceSetImpl();
				resourceSet.getResourceFactoryRegistry()
						.getExtensionToFactoryMap()
						.put("ecore", new EcoreResourceFactoryImpl());
				File temp = new File(gridFileMediaService.getFilePath(art));
				Resource resource = resourceSet.createResource(URI
						.createFileURI(temp.getAbsolutePath()));
				resource.load(null);
				EcoreUtil.resolveAll(resourceSet);
				EObject eo = resource.getContents().get(0);
				Diagnostic diagnostic = Diagnostician.INSTANCE.validate(eo);
				if (diagnostic.getSeverity() == Diagnostic.ERROR)
					return false;
				else
					return true;
			} catch (Exception e) {
				return false;
			}
		} else
			return false;
	}

	@Override
	public String serializeContent(EcoreMetamodel emm) {

		ResourceSet load_resourceSet = new ResourceSetImpl();
		load_resourceSet.getResourceFactoryRegistry()
				.getExtensionToFactoryMap()
				.put("*", new XMIResourceFactoryImpl());
		Resource load_resource = load_resourceSet.getResource(
				URI.createURI(gridFileMediaService.getFilePath(emm)), true);
		String test = ResourceSerializer.serialize(load_resource);
		return test;
	}
	
	@Override
	public double calculateSimilarity(Artifact art1, Artifact art2) {
//		try {
		URI uri1 = URI.createFileURI(gridFileMediaService.getFilePath(art1));
		URI uri2 = URI.createFileURI(gridFileMediaService.getFilePath(art2));
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"ecore", new XMIResourceFactoryImpl());
		ResourceSet resourceSet1 = new ResourceSetImpl();
		ResourceSet resourceSet2 = new ResourceSetImpl();
		resourceSet1.getResource(uri1, true);
		resourceSet2.getResource(uri2, true);
		IComparisonScope scope = new DefaultComparisonScope(resourceSet1,
				resourceSet2, null);
		Comparison comparison = EMFCompare.builder().build().compare(scope);
		List<Match> matches = comparison.getMatches();
		int total = matches.size();
		int counter = 0;
		int counterLeft = 0;
		int counterRight = 0;
		for (Match match : matches) {
			List<Match> lm = Lists.newArrayList(match.getAllSubmatches());
			total += lm.size();
			for (Match match2 : lm){
				if(match2.getLeft()!=null)
					counterLeft++;
				if(match2.getRight()!=null)
					counterRight++;
				if (match2.getLeft() != null && match2.getRight() != null)
					counter++;
			}
			if (match.getLeft() != null && match.getRight() != null)
				counter++;
		}
		//to save diff file
//		List<Diff> differences = comparison.getDifferences();
//		// Let's merge every single diff
//		// IMerger.Registry mergerRegistry = new IMerger.RegistryImpl();
//		IMerger.Registry mergerRegistry = IMerger.RegistryImpl
//				.createStandaloneInstance();
//		IBatchMerger merger = new BatchMerger(mergerRegistry);
//		merger.copyAllLeftToRight(differences, new BasicMonitor());
		double containmentValue = (counter * 1.0) / ((counterLeft<counterRight)?counterLeft:counterRight);
		double simValue = (counter * 1.0) / total;
		//Used to save Diff model
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("xmi", new XMIResourceFactoryImpl());
		ResourceSet resSet = new ResourceSetImpl();
		// create a resource
		Resource resource = resSet.createResource(URI.createURI(basePath
				+ "/compare.xmi"));
		resource.getContents().add(comparison);
//		try {
//			resource.save(Collections.EMPTY_MAP);
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new BusinessException();
//		}
		SimilarityRelation sr = new SimilarityRelation();
		sr.setFromArtifact(art1);
		sr.setToArtifact(art2);
		sr.setValue(simValue);
		relationRepository.save(sr);
		ContainmentRelation cr = new ContainmentRelation();
		cr.setFromArtifact(art1);
		cr.setToArtifact(art2);
		cr.setValue(containmentValue);
		relationRepository.save(cr);
		EcoreMetamodel emm1 = (EcoreMetamodel) art1;
		EcoreMetamodel emm2 = (EcoreMetamodel) art2;

		String test = serializeContent(emm1);
		String test2 = serializeContent(emm2);

		double cosineSimScore = new SimilarityMethods().cosineSimilarityScore(test,
				test2);
		CosineSimilarityRelation csr = new CosineSimilarityRelation();
		csr.setFromArtifact(art1);
		csr.setToArtifact(art2);
		csr.setValue(cosineSimScore);
		relationService.save(csr);
		
		DiceSimilarity ds = new DiceSimilarity();
		double diceSimScore = ds.getSimilarity(test, test2);
		DiceSimilarityRelation dsr = new DiceSimilarityRelation();
		dsr.setFromArtifact(art1);
		dsr.setToArtifact(art2);
		dsr.setValue(diceSimScore);
		relationService.save(dsr);
		
		return simValue;
//		}catch(Exception e) {
//			System.out.println("ERROR from" + art1.getName() + "_" + art1.getId() + " to " + art2.getName() + "_" + art2.getId());
//			return 0;
//		}
	}
	//region Cluster
	@Override
	public String getSimilarityGraph(double threshold,
			ValuedRelationService valuedRelationService)
			throws BusinessException {
		try {

			String result = "nodes = [\n";
			List<Cluster> clusterList = getSimilarityClusters(threshold,
					valuedRelationService);
			HashMap<String, Integer> hm = new HashMap<String, Integer>();
			AtomicInteger i = new AtomicInteger();
			AtomicInteger j = new AtomicInteger();

			for (Cluster cluster : clusterList) {
				int groupId = j.incrementAndGet();
				for (Artifact ecoreMetamodel : cluster.getArtifacts()) {
					if (!hm.containsKey(ecoreMetamodel.getId())) {
						int unique = i.incrementAndGet();
						hm.put(ecoreMetamodel.getId(), unique);
					}
					result += "\t{id: " + hm.get(ecoreMetamodel.getId())
							+ ", label:'" + ecoreMetamodel.getName()
							+ "', group:" + groupId + "},\n";
				}
			}

			result = result.substring(0, result.length() - 2);
			result += "];\n";
			result += "edges = [\n";
			List<ValuedRelation> relations = valuedRelationService
					.findAll(threshold);
			int size = relations.size();
			for (ValuedRelation relation : relations) {
				Double d = (relation.getValue() * 10);
				String s = relation.getValue() + "";
				s = (s.length() < 5) ? s : s.substring(0, 5);
				result += "{from:" + hm.get(relation.getFromArtifact().getId())
						+ ", to: " + hm.get(relation.getToArtifact().getId())
						+ ", value: " + d.intValue() + ", label:" + s + "}";
				if (--size != 0)
					result += ",\n";
				else
					result += "\n";
			}
			result += "];\n";
			return result;
		} catch (Exception e) {
			throw new BusinessException();
		}
	}
	
	@Override
	public List<Cluster> getSimilarityClusters(double threshold,
			ValuedRelationService valuedRelationService)
			throws BusinessException {
		List<Cluster> clusterList = new ArrayList<Cluster>();
		List<ValuedRelation> similarityRelations = valuedRelationService
				.findAll(threshold);
		Map<String, Cluster> tempHash = new HashMap<String, Cluster>();
		for (ValuedRelation similarityRelation : similarityRelations) {
			String fromId = similarityRelation.getFromArtifact().getId();
			String toId = similarityRelation.getToArtifact().getId();
			if (!tempHash.containsKey(fromId) && !tempHash.containsKey(toId)) {
				Cluster c = new Cluster();
				c.getRelations().add(similarityRelation);
				c.getArtifacts().add(similarityRelation.getFromArtifact());
				c.getArtifacts().add(similarityRelation.getToArtifact());
				List<Property> propertyList = similarityRelation
						.getFromArtifact().getProperties();
				propertyList.addAll(similarityRelation.getToArtifact()
						.getProperties());
				for (Property property : propertyList)
					if (property.getName().toLowerCase().contains("domain")
							|| property.getName().toLowerCase()
									.contains("domains"))
						c.getDomains().add(property.getValue());
				tempHash.put(fromId, c);
				tempHash.put(toId, c);
				clusterList.add(c);
			}
			if (tempHash.containsKey(fromId) && !tempHash.containsKey(toId)) {
				Cluster c = tempHash.get(fromId);
				c.getArtifacts().add(similarityRelation.getToArtifact());
				c.getRelations().add(similarityRelation);
				tempHash.put(toId, c);
				tempHash.put(fromId, c);
				List<Property> propertyList = similarityRelation
						.getToArtifact().getProperties();
				for (Property property : propertyList)
					if (property.getName().toLowerCase().contains("domain")
							|| property.getName().toLowerCase()
									.contains("domains"))
						c.getDomains().add(property.getValue());
			}
			if (!tempHash.containsKey(fromId) && tempHash.containsKey(toId)) {
				Cluster c = tempHash.get(similarityRelation.getToArtifact()
						.getId());
				c.getArtifacts().add(similarityRelation.getFromArtifact());
				c.getRelations().add(similarityRelation);
				tempHash.put(fromId, c);
				tempHash.put(toId, c);
				List<Property> propertyList = similarityRelation
						.getFromArtifact().getProperties();
				for (Property property : propertyList)
					if (property.getName().toLowerCase().contains("domain")
							|| property.getName().toLowerCase()
									.contains("domains"))
						c.getDomains().add(property.getValue());
			}
			if (tempHash.containsKey(fromId) && tempHash.containsKey(toId)
					&& tempHash.get(fromId) != tempHash.get(toId)) {
				Cluster fromCluster = tempHash.get(fromId);
				Cluster toCluster = tempHash.get(toId);
				clusterList.remove(toCluster);
				clusterList.remove(fromCluster);
				fromCluster.getRelations().addAll(toCluster.getRelations());
				fromCluster.getArtifacts().addAll(toCluster.getArtifacts());
				fromCluster.getDomains().addAll(toCluster.getDomains());
				for (Artifact art : fromCluster.getArtifacts()) {
					Cluster cc = tempHash.get(art.getId());
					clusterList.remove(cc);
					tempHash.put(art.getId(), fromCluster);
				}
				clusterList.add(fromCluster);
				tempHash.put(toId, fromCluster);
				tempHash.put(fromId, fromCluster);
			}
		}

		List<EcoreMetamodel> ecoreMetamodels = findAllPublic();
		for (EcoreMetamodel ecoreMetamodel : ecoreMetamodels) {
			if (tempHash.get(ecoreMetamodel.getId()) == null) {
				Cluster c = new Cluster();
				c.setMostRepresentive(ecoreMetamodel);
				c.getArtifacts().add(ecoreMetamodel);
				List<Property> propertyList = ecoreMetamodel.getProperties();
				for (Property property : propertyList)
					if (property.getName().toLowerCase().contains("domain")
							|| property.getName().toLowerCase()
									.contains("domains"))
						c.getDomains().add(property.getValue());
				clusterList.add(c);
			}

		}

		for (Cluster cluster : clusterList) {
			int kMax = 0;
			int kMin = Integer.MAX_VALUE;
			int countRelation = 0;
			Artifact mostRepresentive = null;
			for (Artifact elem : cluster.getArtifacts()) {
				List<Relation> srl = findInCluster(elem, cluster);// similarityRelationService.findByEcoreMetamodel((EcoreMetamodel)elem,
																	// threshold,1);
				if (srl.size() > kMax) {
					kMax = srl.size();
					mostRepresentive = elem;
				}
				if (srl.size() < kMin)
					kMin = srl.size();
				countRelation += srl.size();
			}
			if (cluster.getArtifacts().size() == 1) {
				Iterator<Artifact> iterator = cluster.getArtifacts().iterator();
				mostRepresentive = iterator.next();
			} else

				cluster.setkMax(kMax);
			cluster.setkMin(kMin);
			cluster.setkAvg(countRelation
					/ (cluster.getArtifacts().size() * 1.0));
			cluster.setMostRepresentive(mostRepresentive);
		}

		Collections.sort(clusterList, new Comparator<Cluster>() {
	        @Override
	        public int compare(Cluster  cluster1, Cluster  cluster2)
	        {

	            return  cluster1.getArtifacts().size() > cluster2.getArtifacts().size() ? 
	            				-1 : 
	            					cluster1.getArtifacts().size() < cluster2.getArtifacts().size() ? +1 : 0;
	            		//cluster1.getArtifacts().size().compareTo(cluster2.getArtifacts().size());
	        }
	    });
		
		return clusterList;
	}

	private List<Relation> findInCluster(Artifact elem, Cluster cluster) {
		List<Relation> result = new ArrayList<Relation>();
		for (Relation rel : cluster.getRelations()) {
			if (rel.getToArtifact().getId().equals(elem.getId())
					|| rel.getFromArtifact().getId().equals(elem.getId()))
				result.add(rel);
		}
		return result;
	}

	
	@Override
	public com.apporiented.algorithm.clustering.Cluster getHierarchicalCluster(
			ValuedRelationService valuedRelationService)
			throws BusinessException {
		String[] names = getNames();
		double[][] distances = getSimilarityMatrix(valuedRelationService);
		// try {
		// PrintWriter pw = new PrintWriter(new File (basePath + "j.txt"));
		// for (double[] ds : distances) {
		// for (double d : ds) {
		// pw.print(d + ";");
		// }
		// pw.println();
		// }
		// pw.close();
		// } catch (FileNotFoundException e1) {
		// e1.printStackTrace();
		// }
		ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
		com.apporiented.algorithm.clustering.Cluster cluster = alg
				.performClustering(distances, names,
						new SingleLinkageStrategy());
		return cluster;
	}

	private String[] getNames() {
		List<EcoreMetamodel> emms = findAllPublic();
		String[] result = new String[emms.size()];
		for (int i = 0; i < emms.size(); i++)
			result[i] = emms.get(i).getName();
		return result;
	}

	private double[][] getSimilarityMatrix(
			ValuedRelationService valuedRelationService) {
		List<EcoreMetamodel> emms1 = findAllPublic();
		List<EcoreMetamodel> emms2 = emms1;
		List<ValuedRelation> sr = valuedRelationService.findAll();
		HashMap<String, ValuedRelation> map = new HashMap<String, ValuedRelation>();
		for (ValuedRelation similarityRelation : sr) {
			map.put(similarityRelation.getToArtifact().getId()
					+ similarityRelation.getFromArtifact().getId(),
					similarityRelation);
			map.put(similarityRelation.getFromArtifact().getId()
					+ similarityRelation.getToArtifact().getId(),
					similarityRelation);
		}
		double[][] result = new double[emms1.size()][emms2.size()];
		for (int i = 0; i < emms1.size(); i++) {
			for (int j = 0; j < emms2.size(); j++) {
				ValuedRelation srel = map.get(emms1.get(i).getId()
						+ emms2.get(j).getId());
				if (srel != null)
					result[i][j] = 1 - srel.getValue();
				else {
					result[i][j] = 0;
				}
			}
		}
		return result;
	}

	@Override
	public double[][] getProximityMatrix(
			ValuedRelationService valuedRelationService) {
		List<EcoreMetamodel> emms1 = findAllPublic();
		List<EcoreMetamodel> emms2 = emms1;
		List<ValuedRelation> sr = valuedRelationService.findAll();
		HashMap<String, ValuedRelation> map = new HashMap<String, ValuedRelation>();
		for (ValuedRelation similarityRelation : sr) {
			map.put(similarityRelation.getToArtifact().getId()
					+ similarityRelation.getFromArtifact().getId(),
					similarityRelation);
			map.put(similarityRelation.getFromArtifact().getId()
					+ similarityRelation.getToArtifact().getId(),
					similarityRelation);
		}
		double[][] result = new double[emms1.size()][emms2.size()];
		for (int i = 0; i < emms1.size(); i++) {
			for (int j = 0; j < emms2.size(); j++) {
				ValuedRelation srel = map.get(emms1.get(i).getId()
						+ emms2.get(j).getId());
				if (srel != null)
					result[i][j] = srel.getValue();
				else {
					result[i][j] = 1;
				}
			}
		}
		return result;
	}

	@Override
	public void printHierarchicalCluster(
			com.apporiented.algorithm.clustering.Cluster cluster,
			ValuedRelationService valuedRelationService)
			throws BusinessException {
		DendrogramPanel dp = new DendrogramPanel();
		dp.setModel(cluster);
		int w = 10000;
		int h = 10000;
		dp.setSize(w, h);
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		dp.paint(g);
		dp.print(g);
		File outputfile = new File(
				"/Users/juridirocco/Desktop/tempForge/JJJJ.jpg");
		try {
			ImageIO.write(bi, "jpg", outputfile);
		} catch (IOException e) {
			throw new BusinessException();
		}

	}

	@Override
	public List<Cluster> getRealClustersFromHierarchicalCluster(
			List<com.apporiented.algorithm.clustering.Cluster> clusterList,
			ValuedRelationService valuedRelationService) {
		List<Cluster> result = new ArrayList<Cluster>();
		for (com.apporiented.algorithm.clustering.Cluster cluster : clusterList) {
			Cluster myCluster = new Cluster();
			List<com.apporiented.algorithm.clustering.Cluster> leaves = getClusterLeaf(cluster);
			for (com.apporiented.algorithm.clustering.Cluster leaf : leaves) {
				EcoreMetamodel emm = findOneByName(leaf.getName());
				myCluster.getArtifacts().add(emm);
				for (Property property : emm.getProperties())
					if (property.getName().toLowerCase().contains("domain")
							|| property.getName().toLowerCase()
									.contains("domains"))
						myCluster.getDomains().add(property.getValue());
				// myCluster.getRelations().addAll(similarityRelationService.findByEcoreMetamodel(emm,
				// (cluster.getDistance()!=null)?cluster.getDistance():0));
			}
			result.add(myCluster);
		}
		return result;
	}

	private List<com.apporiented.algorithm.clustering.Cluster> getClusterLeaf(
			com.apporiented.algorithm.clustering.Cluster cluster) {
		List<com.apporiented.algorithm.clustering.Cluster> result = new ArrayList<com.apporiented.algorithm.clustering.Cluster>();
		if (cluster.isLeaf())
			result.add(cluster);
		else
			for (com.apporiented.algorithm.clustering.Cluster c : cluster
					.getChildren()) {
				result.addAll(getClusterLeaf(c));
			}
		return result;
	}

	@Override
	public List<com.apporiented.algorithm.clustering.Cluster> getClustersWithThreshold(
			com.apporiented.algorithm.clustering.Cluster c, double threshold,
			ValuedRelationService valuedRelationService)
			throws BusinessException {
		List<com.apporiented.algorithm.clustering.Cluster> result = new ArrayList<com.apporiented.algorithm.clustering.Cluster>();
		if (c.getDistance() != null && c.getDistance() <= threshold)
			result.add(c);
		else if (c.isLeaf())
			result.add(c);
		else
			for (com.apporiented.algorithm.clustering.Cluster cluster : c
					.getChildren()) {
				result.addAll(getClustersWithThreshold(cluster, threshold,
						valuedRelationService));
			}
		return result;
	}
	//endregion
	@Override
	public List<Metric> getMetrics(Artifact emm) throws BusinessException {
		List<Metric> metricList = metricRepository
				.findByArtifactId(new ObjectId(emm.getId()));
		if (metricList.size() == 0)
			metricList = calculateMetrics(emm);
		return metricList;
	}

	@Override
	public List<EcoreMetamodel> searchByExample(EcoreMetamodel searchSample)
			throws BusinessException {
		Comparator<Double> c = new Comparator<Double>() {
			public int compare(Double a, Double b) {
		        if (a >= b) {
		            return -1;
		        } else {
		            return 1;
		        } // returning 0 would merge keys
		    }
		};
		
		List<EcoreMetamodel> repository = findAll();
		Map<Double, EcoreMetamodel> list = new TreeMap<Double, EcoreMetamodel>(c);
		for (EcoreMetamodel ecoreMetamodel : repository) {
			double d = calculateContainment(ecoreMetamodel, searchSample);
			list.put(d, ecoreMetamodel);
		}
		System.out.println(list.size());
		List<EcoreMetamodel> result = new ArrayList<EcoreMetamodel>();
		int i = 0;
		for(Entry<Double, EcoreMetamodel> entry : list.entrySet()) {
			  EcoreMetamodel value = entry.getValue();
			  System.out.println("score: " + entry.getKey());
			  System.out.println("metamodel" + entry.getValue().getName());
			  result.add(value);
			  if(i++ > 10)
				  break;
			}
		return result;
	}
	
	private double calculateContainment(EcoreMetamodel art1, EcoreMetamodel art2) {
		try {
		URI uri1 = URI.createFileURI(gridFileMediaService.getFilePath(art1));
		URI uri2 = URI.createFileURI(gridFileMediaService.getFilePath(art2));
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"ecore", new XMIResourceFactoryImpl());
		ResourceSet resourceSet1 = new ResourceSetImpl();
		ResourceSet resourceSet2 = new ResourceSetImpl();
		resourceSet1.getResource(uri1, true);
		resourceSet2.getResource(uri2, true);
		IComparisonScope scope = new DefaultComparisonScope(resourceSet1,
				resourceSet2, null);
		Comparison comparison = EMFCompare.builder().build().compare(scope);
		List<Match> matches = comparison.getMatches();
		int counter = 0;
		int counterLeft = 0;
		int counterRight = 0;
		for (Match match : matches) {
			List<Match> lm = Lists.newArrayList(match.getAllSubmatches());
			for (Match match2 : lm) {
				if(match2.getLeft()!=null)
					counterLeft++;
				if(match2.getRight()!=null)
					counterRight++;
				if (match2.getLeft() != null && match2.getRight() != null)
					counter++;
			}
				
			if (match.getLeft() != null && match.getRight() != null)
				counter++;
		}
		double resultValue = (counter * 1.0) / ((counterLeft<counterRight)?counterLeft:counterRight);
		return resultValue;
		}catch(Exception e) {
			return 0;
		}
	}
}