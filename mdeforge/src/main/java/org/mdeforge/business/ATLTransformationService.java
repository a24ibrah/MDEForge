package org.mdeforge.business;

import java.util.List;

import org.mdeforge.business.model.ATLTransformation;
import org.mdeforge.business.model.Model;

public interface ATLTransformationService extends CRUDArtifactService<ATLTransformation>, MetricProvider{	
	void execute(ATLTransformation transformation, List<Model> models);
	List<ATLTransformation> findTransformationsBySourceMetamodels(ATLTransformation metamodel);
	List<ATLTransformation> findTransformationsByTargetMetamodels(ATLTransformation metamodel);
	ResponseGrid<ATLTransformation> findAllPaginated(RequestGrid requestGrid)  throws BusinessException;
	String inject(ATLTransformation atlTransformation) throws BusinessException;
}
