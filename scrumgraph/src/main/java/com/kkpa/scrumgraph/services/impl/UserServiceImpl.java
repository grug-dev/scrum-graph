package com.kkpa.scrumgraph.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kkpa.scrumgraph.aop.LogExecutionTime;
import com.kkpa.scrumgraph.services.UserService;

import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.services.ServiceFactory;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceUserNode;
import co.edu.ud.scrumgraph.data.services.interfaces.IServicesFactory;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

@Service("userSrv")
public class UserServiceImpl implements UserService  {

	private IServiceUserNode userService;

	protected IServiceUserNode getUserService() {
		if (userService == null) {
			IServicesFactory srvFactory = new ServiceFactory();
			userService = srvFactory.getUserNodeService();
		}

		return userService;
	}

	@LogExecutionTime
	@Override
	public List<NodeTO> getAllUsers(String authToken) {
		try {
			return getUserService().getAllUsers(authToken);
		} catch (ScrumGraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	


}
