package com.kkpa.scrumgraph.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SGUtil {

	
	/**
	 * Method transforms JSON string to Map
	 * @param jsonBase String 
	 * @return Map<String,Object>
	 */
	public static Map<String,Object> transformJSON(String jsonBase) {
		try {
			return new ObjectMapper().readValue(jsonBase, HashMap.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
