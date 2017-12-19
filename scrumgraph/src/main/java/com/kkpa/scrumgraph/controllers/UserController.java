package com.kkpa.scrumgraph.controllers;

import java.util.List;

import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkpa.scrumgraph.constants.ECodeResponse;
import com.kkpa.scrumgraph.dto.ResponseDTO;
import com.kkpa.scrumgraph.dto.ScrumHeaderDTO;
import com.kkpa.scrumgraph.dto.UserCollectionDTO;
import com.kkpa.scrumgraph.dto.UserDTO;
import com.kkpa.scrumgraph.exceptionhandler.SGException;
import com.kkpa.scrumgraph.services.UserService;

@RestController
@RequestMapping("/users")
public class UserController implements ISrvSGRest {

	@Autowired
	private ApplicationContext appCtx;

	@Autowired
	@Qualifier("userSrv")
	private UserService userService;

	@GetMapping
	@Produces(value = "application/json")
	public ResponseDTO getUsers(@RequestHeader(SCRUM_GRAPH_HEADER) ScrumHeaderDTO scrumGraphHeader) throws SGException{
		ResponseDTO respDTO = (ResponseDTO) appCtx.getBean("responseDTO");
		UserCollectionDTO userCollectionDTO = (UserCollectionDTO) appCtx.getBean("userCollection");
		List<UserDTO> allUsers = null;

		allUsers = userService.getAllUsers(scrumGraphHeader.getAuthToken());

		userCollectionDTO.setLstUsers(allUsers);
		respDTO.setResponse(userCollectionDTO);
		respDTO.setCode(ECodeResponse.OK.getCode());

		return respDTO;
	}

	/** OPTIONS */
	@OPTIONS
	public Response rootCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.build();
	}

	@OPTIONS
	@Path("/{id}")
	public Response userIdCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.build();
	}

	@OPTIONS
	@Path("/{id}/projects")
	public Response idProjectsCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.build();
	}

	@OPTIONS
	@Path("/{id}/project")
	public Response idProjectCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.build();
	}

	@OPTIONS
	@Path("/login")
	public Response loginCodeCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.build();
	}

	@OPTIONS
	@Path("/verify/token")
	public Response verifyTokenCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.build();
	}

}
