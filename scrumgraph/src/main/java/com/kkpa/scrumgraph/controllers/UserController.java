package com.kkpa.scrumgraph.controllers;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestHeader;

import com.kkpa.scrumgraph.aop.LogExecutionTime;
import com.kkpa.scrumgraph.dto.ResponseDTO;
import com.kkpa.scrumgraph.dto.ScrumHeaderDTO;
import com.kkpa.scrumgraph.services.UserService;

import co.edu.ud.scrumgraph.data.dto.NodeTO;



@Path("users")
public class UserController implements ISrvSGRest {

	@Autowired
	private ApplicationContext appCtx;
	
	@Autowired
	@Qualifier("userSrv")
	private UserService userService;
	
	@LogExecutionTime
	@GET()
	@Produces(value="application/json")	
	public ResponseDTO getUsers(@HeaderParam(SCRUM_GRAPH_HEADER) @RequestHeader ScrumHeaderDTO scrumGraphHeader) {
		ResponseDTO respDTO = (ResponseDTO) appCtx.getBean("responseDTO");
		List<NodeTO> allUsers = null;
		
		respDTO.setErrorCode("100");
		respDTO.setErrorMsg("Rt");
				
		// Obtener todos los usuarios
		allUsers = userService.getAllUsers(scrumGraphHeader.getAuthToken());
		
		respDTO.setResponse(allUsers);
		
		return respDTO;
	}

	

	/** OPTIONS */
	@OPTIONS 
	public Response rootCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	
	@OPTIONS 
	@Path("/{id}")
	public Response userIdCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	

	@OPTIONS 
	@Path("/{id}/projects")
	public Response idProjectsCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	
	
	@OPTIONS 
	@Path("/{id}/project")
	public Response idProjectCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	
	
	@OPTIONS 
	@Path("/login")
	public Response loginCodeCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	
	@OPTIONS 
	@Path("/verify/token")
	public Response verifyTokenCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	

}
