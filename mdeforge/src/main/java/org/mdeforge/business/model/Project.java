package org.mdeforge.business.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mdeforge.business.model.serializer.json.ArtifactListSerializer;
import org.mdeforge.business.model.serializer.json.UserListSerializer;
import org.mdeforge.business.model.serializer.json.UserSerializer;
import org.mdeforge.business.model.serializer.json.WorkspaceListSerializer;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A representation of the model object '<em><b>Project</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Document(collection="Projects")
public class Project implements java.io.Serializable{

	
	private static final long serialVersionUID = -717518242205317774L;
	private boolean open;
	private Date createdDate;
	private String Description;
	private Date modifiedDate;
	@DBRef(lazy = true)
	@JsonSerialize(using = ArtifactListSerializer.class)
	private List<Artifact> artifacts = new ArrayList<Artifact>();
	@DBRef
	@JsonSerialize(using = UserSerializer.class)
	private User owner;
	@DBRef
	@JsonSerialize(using = UserListSerializer.class)
	private List<User> users = new ArrayList<User>();

	@DBRef
	@JsonSerialize(using = WorkspaceListSerializer.class)
	private List<Workspace> workspaces = new ArrayList<Workspace>();;

	private String name = null;

	private String id = null;

	public List<Artifact> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<Artifact> newArtifacts) {
		artifacts = newArtifacts;
	}

	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> newShared) {
		users = newShared;
	}

	public List<Workspace> getWorkspaces() {
		return workspaces;
	}

	public void setWorkspaces(List<Workspace> newWorkspaces) {
		workspaces = newWorkspaces;
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public String getId() {
		return id;
	}

	
	public void setId(String newId) {
		id = newId;
	}

	@Override
	public String toString() {
		return "Project " + " [name: " + getName() + "]" + " [id: " + getId()
				+ "]";
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Project) 
			return this.id.equals(((Project)obj).getId());
		else return false;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean _open) {
		this.open = _open;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}
}
