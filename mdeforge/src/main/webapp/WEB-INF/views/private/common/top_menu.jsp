<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/security/tags" prefix="security" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>



<div class="navbar main">
			
			<!-- Menu Toggle Button -->
			<button type="button" class="btn btn-navbar pull-left">
				<span class="icon-bar"></span> <span class="icon-bar"></span> <span class="icon-bar"></span>
			</button>
			<!-- // Menu Toggle Button END -->
			
			
			
			<ul class="topnav pull-left">
				<li><a href="${pageContext.request.contextPath}/private/EcoreMetamodel/upload" class="glyphicons cloud-upload"><i></i> Metamodel Upload</a></li>
				
					<%-- <li><a href="${pageContext.request.contextPath}/public/search" class="glyphicons search"><i></i> Search</a></li>	 --%>
					
					<li class="glyphs hidden-tablet hidden-phone">
					<ul>
						<li><a href="${pageContext.request.contextPath}/public/browse" class="glyphicons folder_open" data-toggle="tooltip" data-title="Browse the repository" data-placement="bottom" data-original-title="" title=""><i></i></a></li>
						<li><a href="${pageContext.request.contextPath}/public/search" class="glyphicons search" data-toggle="tooltip" data-title="Search Artifacts" data-placement="bottom" data-original-title="" title=""><i></i></a></li>
						<li><a href="${pageContext.request.contextPath}/private/dashboard" class="glyphicons edit" data-toggle="tooltip" data-title="Private Area" data-placement="bottom" data-original-title="" title=""><i></i></a></li>
					</ul>
				</li>
				<li class="search open">
					<form action="${pageContext.request.contextPath}/public/search" method="get" class="dropdown dd-1">
						<input type="text" value="" placeholder="Search Artifacts.." name="search_string" data-toggle="typeahead" />
						<button type="button" class="glyphicons search"><i></i></button>
					</form>
				</li> 
				
			</ul>
			
			
			
			
			<%-- <ul class="topnav pull-left">
				<li><a href="${pageContext.request.contextPath}/private/execute_transformation.htm" class="glyphicons play"><i></i> Execute Transformation</a></li> 
				<li><a href="${pageContext.request.contextPath}/private/transformation_chain.htm" class="glyphicons random"><i></i> Transformation Chain</a></li> 
				
				<li class="dropdown dd-1">
					<a href="" data-toggle="dropdown" class="glyphicons cloud-upload"><i></i>Upload<span class="caret"></span></a>
					<ul class="dropdown-menu pull-left">
					
					<security:authorize access="hasRole('admin')">
						<li><a href="${pageContext.request.contextPath}/private/upload.htm" class="glyphicons cloud-upload"><i></i> Generico</a></li>	
					</security:authorize>	
						
						<li><a href="${pageContext.request.contextPath}/private/metamodelInsertion.htm" class="glyphicons cloud-upload"><i></i>Metamodel</a></li>						
						<li><a href="${pageContext.request.contextPath}/private/transformationInsertion.htm" class="glyphicons cloud-upload"><i></i>Transformation</a></li>
						
					
					</ul>
				</li>
			</ul> --%>
						
						<!-- Top Menu Right -->
			 <ul class="topnav pull-right hidden-phone hidden-tablet hidden-desktop-1">								
								
				<%-- <security:authorize access="hasRole('admin')">
				<li><a href="${pageContext.request.contextPath}/admin/admin_dashboard.htm" class="glyphicons settings"><i></i> Admin</a></li>
				</security:authorize> --%>
			
				<!-- Profile / Logout menu -->
				<li class="account dropdown dd-1">
					<a data-toggle="dropdown" href="#" class="glyphicons logout lock">
					<span class="hidden-tablet hidden-phone hidden-desktop-1">  
					<security:authentication property="principal.user.username"/></span><i></i></a>
					<ul class="dropdown-menu pull-right">
						<li><a href="${pageContext.request.contextPath}/private/dashboard"
												class="glyphicons edit" data-toggle="tooltip" data-title="Private Area" data-placement="bottom" data-original-title="" title="">Dashboard</a></li>					
								
						<li><a href="#" class="glyphicons cogwheel">Settings<i></i></a></li>						
						<li class="profile">
							<span>
								<span class="heading">Profile <a href="#" class="pull-right">edit</a></span>
								<span class="img"></span>
								<span class="details">
									<a href="#"><security:authentication property="principal.user.firstname"/> 
									<security:authentication property="principal.user.lastname"/></a>
									<security:authentication property="principal.user.email"/>
								</span>
								<span class="clearfix"></span>
							</span>
						</li>
						<li>
							<span>
								<a class="btn btn-default btn-mini pull-right" href="${pageContext.request.contextPath}/j_spring_security_logout"><spring:message code="common.logout"/></a>
							</span>
						</li>
					</ul>
				</li>
				<!-- // Profile / Logout menu END -->
				
			</ul> 
			<!-- // Top Menu Right END -->
						<div class="clearfix"></div>
			
		</div>