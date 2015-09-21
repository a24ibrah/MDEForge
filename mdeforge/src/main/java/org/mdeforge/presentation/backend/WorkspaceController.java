package org.mdeforge.presentation.backend;

import java.beans.PropertyEditorSupport;
import java.util.List;

import org.mdeforge.business.BusinessException;
import org.mdeforge.business.ProjectService;
import org.mdeforge.business.RequestGrid;
import org.mdeforge.business.ResponseGrid;
import org.mdeforge.business.WorkspaceService;
import org.mdeforge.business.model.Project;
import org.mdeforge.business.model.User;
import org.mdeforge.business.model.Workspace;
import org.mdeforge.presentation.validators.WorkspaceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

@Controller
// TODO cambiare il mapping
@RequestMapping("/private/workspace")
public class WorkspaceController {

	@Autowired
	private WorkspaceService workspaceService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private User user;
	@Autowired
	private WorkspaceValidator workspaceValidator;

	@RequestMapping(value = "/dashboard", method = { RequestMethod.GET })
	public String dashboard(Model model, @RequestParam String id) {

		Workspace workspace = workspaceService.findById(id, user);
		model.addAttribute("workspace", workspace);
		return "workspace.dashboard";
	}

	@RequestMapping("")
	public String details(Model model, @RequestParam String id) {

		Workspace workspace = workspaceService.findById(id, user);
		model.addAttribute("workspace", workspace);
		return "private.use.workspace_details";
	}

	@RequestMapping("/list")
	public String elenco(Model model) {
		List<Workspace> workspaces_list = workspaceService
				.findAllByUsernamme(user);
		model.addAttribute("workspaces_list", workspaces_list);
		return "private.use.workspace_list";
	}

	@RequestMapping(value = "/create", method = { RequestMethod.GET })
	public String create_start(Model model) {
		Workspace workspace = new Workspace();
		model.addAttribute("workspace", workspace);
		List<Project> projects = projectService.findByUser(user);
		model.addAttribute("workspace", workspace);
		model.addAttribute("projectList", projects);
		return "private.use.workspace_insert";
	}

	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public String create(@ModelAttribute Workspace workspace, Model model,
			BindingResult bindingResult) {
		workspaceValidator.validate(workspace, bindingResult);
		if (bindingResult.hasErrors()) {
			return "redirect:/admin/dashboard";
		} else {
			try {
				workspace.setOwner(user);
				workspaceService.create(workspace);
				return "redirect:/private/workspace/list";
			} catch (BusinessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "redirect:/admin/dashboard";
			}
		}

	}

	@RequestMapping(value = "/update", method = {RequestMethod.GET })
	public String update_start(@RequestParam("id") String id, Model model) {
		Workspace workspace = workspaceService.findOne(id);
		model.addAttribute("workspace", workspace);
		return "workspace.update";
	}

	@RequestMapping(value = "/update", method = { RequestMethod.POST })
	public String update(@ModelAttribute Workspace workspace) {
		workspace.setOwner(user);
		workspaceService.update(workspace);
		return "redirect:/private/workspace/list";
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public String delete_start(@RequestParam("id") String id, Model model) {
		Workspace workspace = workspaceService.findOne(id);
		model.addAttribute("workspace", workspace);
		return "workspace.delete";
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public String delete(@ModelAttribute Workspace workspace) {
		workspaceService.delete(workspace);
		return "redirect:/private/workspace/list";
	}

	@RequestMapping("/findelementspaginated")
	public @ResponseBody ResponseGrid<Workspace> findelementspaginated(
			@ModelAttribute RequestGrid requestGrid) {
		return workspaceService.findAllPaginated(requestGrid);

	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder, WebRequest request) {

	        binder.registerCustomEditor(Project.class, "projects", new PropertyEditorSupport() {
	         @Override
	         public void setAsText(String id) {
	            setValue((id.equals(""))?null:projectService.findOne(id));
	         }
	     });
	}

}
