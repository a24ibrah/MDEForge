package org.mdeforge.business.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.mdeforge.business.BusinessException;
import org.mdeforge.business.GridFileMediaService;
import org.mdeforge.business.model.Artifact;
import org.mdeforge.business.model.GridFileMedia;
import org.mdeforge.integration.GridFileMediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;

@Service
public class GridFileMediaServiceImpl implements GridFileMediaService {
	@Autowired
	protected SimpleMongoDbFactory mongoDbFactory;
	@Autowired
	GridFsTemplate operations;
	@Autowired
	GridFileMediaRepository gridFileMediaRepository;

	@Override
	public void store(GridFileMedia gridFileMedia) throws BusinessException {
		GridFSFile gridFile = operations.store(new ByteArrayInputStream(
				gridFileMedia.getByteArray()), gridFileMedia.getFileName());
		gridFileMedia.setIdFile(gridFile.getId().toString());
		gridFileMediaRepository.save(gridFileMedia);
	}

	@Override
	public GridFileMedia getGridFileMedia(GridFileMedia id)
			throws BusinessException {
		Query q = new Query();
		q.addCriteria(Criteria.where("_id").is(id.getIdFile()));
		GridFSDBFile dbFile = operations.findOne(q);
		id.setByteArray(readData(dbFile));
		return id;
	}
	
	@Override 
	public GridFileMedia createObjectFromFile (String tempFilePath) throws IOException {
		GridFileMedia gfr = new GridFileMedia();
		gfr.setFileName(tempFilePath);
		java.nio.file.Path path = Paths.get(tempFilePath);
		byte[] data = Files.readAllBytes(path);
		gfr.setByteArray(data);
		return gfr;
	}

	private static byte[] readData(GridFSDBFile dbFile) {
		byte[] data = new byte[0];

		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			dbFile.writeTo(bout);
			data = bout.toByteArray();
		} catch (Exception e) {
			throw new BusinessException();	
		}
		return data;
	}

	@Override
	public void delete(GridFileMedia file) throws BusinessException {
		Query q = new Query();
		q.addCriteria(Criteria.where("id").is(file.getIdFile()));
		operations.delete(q);
		gridFileMediaRepository.delete(file);
	}

	@Value("#{cfgproperties[basePath]}")
	private String basePath;

	@Override
	public String getFilePath(Artifact artifact) throws BusinessException {
		GridFileMedia grm = null;
		if (artifact.getFile().getId()!=null)
			grm = getGridFileMedia(artifact.getFile());
		else grm = artifact.getFile();
		FileOutputStream out;
		try {
			String path = basePath + artifact.getFile().getFileName();
			out = new FileOutputStream(path);
			if (grm.getByteArray() != null && grm.getByteArray().length != 0)
				out.write(grm.getByteArray());

			else
				out.write(Base64.decode(grm.getContent().getBytes()));

			out.close();
			return path;
		} catch (FileNotFoundException e) {
			throw new BusinessException(e.getMessage());
		} catch (IOException e) {
			throw new BusinessException(e.getMessage());
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}

	}

	@Override
	public byte[] getFileByte(Artifact artifact) throws BusinessException {
		GridFileMedia grm = getGridFileMedia(artifact.getFile());
		GridFS fileStore = new GridFS(mongoDbFactory.getDb());
		GridFSDBFile found = fileStore.findOne(grm.getFileName());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			found.writeTo(baos);
		} catch (IOException e) {
			throw new BusinessException();
		}
		return baos.toByteArray();
	}

	@Override
	 public InputStream getFileInputStream(Artifact artifact) throws BusinessException {
	  GridFileMedia grm = getGridFileMedia(artifact.getFile());
	  Query q = new Query();
	  q.addCriteria(Criteria.where("_id").is(grm.getIdFile()));
	  GridFSDBFile dbFile = operations.findOne(q);
	  return dbFile.getInputStream();
	 }
	
}
