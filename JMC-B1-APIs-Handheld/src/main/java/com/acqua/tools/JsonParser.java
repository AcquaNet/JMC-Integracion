package com.acqua.tools;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JsonParser {

	public static Object jsonToObject(String payload, String selectClass)
			throws JsonParseException, JsonMappingException, IOException, NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Object obj = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		try {
			obj = mapper.readValue(payload, Class.forName(selectClass));
			System.out.println("Datos de B1 procesados.");

		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
		
		try{
		Method miMetodo = obj.getClass().getMethod("sorted");
		miMetodo.invoke(obj);
		} catch (Exception e){
			System.out.println(e.getStackTrace());
		}
		return obj;
	}
	
}
