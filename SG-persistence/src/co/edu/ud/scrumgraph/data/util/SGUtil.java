package co.edu.ud.scrumgraph.data.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.edu.ud.scrumgraph.data.enums.ECodeExceptionSG;

public class SGUtil {

	private static MessageDigest md = null;
	
	private static SimpleDateFormat dateTimeFormat;
	
	private static SimpleDateFormat dateFormat;

	private static final String PATTERN_EMAIL = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	static {
		try {
			md = MessageDigest.getInstance("MD5");
			dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	/**
	 * M�todo que valida si las propiedades pasadas c�mo par�metros son nulas o
	 * vac�as. Si son nulas o vac�as retorna una excepci�n
	 * 
	 * @param properties
	 *            Propiedades a validar.
	 * @throws ScrumGraphException
	 *             Excepci�n lanzada si las propiedades son nulas.
	 */
	public static void validateProperties(Map<String, Object> properties) throws ScrumGraphException {

		if (properties == null || properties.isEmpty()) {
			throw new ScrumGraphException("Properties of the node are null or Empty");
		}
		
		List<String> propToDeleted = new ArrayList<String>();
		for (String propKey : properties.keySet()) {
			if (properties.get(propKey) == null) {
				propToDeleted.add(propKey);
			}
		}
		for (String propKey : propToDeleted) {
			properties.remove(propKey);
		}
		
	}

	/**
	 * M�todo para encriptar una cadena de car�cteres en formato MD5
	 * 
	 * @param valueToConvert
	 *            String cadena a encriptar
	 * @return String cadena encriptada en formato MD5.
	 */
	public static String toMD5(String valueToConvert) {
		StringBuffer sb = null;

		if (valueToConvert == null || valueToConvert.length() == 0) {
			return null;
		}

		sb = new StringBuffer();

		md.update(valueToConvert.getBytes());
		byte byteData[] = md.digest();

		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x50, 16).substring(1));
		}

		return sb.toString();
	}

	/**
	 * M�todo para validar el formato de un email dado como par�metro.
	 * 
	 * @param email
	 *            Email para validar
	 * @return true email v�lido, false inv�lido.
	 */
	public static boolean validateFormatEmail(String email) {

		if (email == null || email.isEmpty()) {
			return false;
		}

		// Compiles the given regular expression into a pattern.
		Pattern pattern = Pattern.compile(PATTERN_EMAIL);

		// Match the given input against this pattern
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();

	}
	
	public static void updateDateProperty(String propKey , Map<String, Object> properties) {
		Object propValue = null;
		
		propValue = properties.get(propKey);
		
		if (propValue == null) {
			return ;
		}
		
		if (propValue instanceof Long) {
			Date fieldDate = null;
			
			fieldDate = new Date(Long.parseLong(propValue.toString()));
			
			properties.put(propKey, fieldDate);
			
		}
		
	}
	
	public static String toDateFormatSG(Object date) {
		String formatDate = null;

		formatDate = dateFormat.format(date);

		return formatDate;
	}
	
	public static Date toDate(Object objDate) throws ScrumGraphException {
		String strDate = null;
		
		if (objDate == null) {
			return null;
		}
		
		if (objDate instanceof Date) {
			return (Date) objDate;
		}
		
		if (objDate instanceof Long) {
			Date fieldDate = null;
			
			fieldDate = new Date(Long.parseLong(objDate.toString()));
			
			return fieldDate;
			
		}
		
		strDate = objDate.toString();
		strDate = strDate.replaceAll("\\s+$","");
		try {
			return dateFormat.parse(strDate);
		} catch (ParseException ex) {
			try {
				return dateTimeFormat.parse(strDate);
			}
			catch (ParseException e) {
				e.printStackTrace();
				throw new ScrumGraphException(ECodeExceptionSG.BAD_DATE);
			}
		}
	}
	
	public static String toStringDateFormat(Date date){
		String value  = dateTimeFormat.format(date);
		return value;
	}
	
	public static String toStringList(Object obj) {
		String convert = "";
		
		if (obj == null) {
			return convert;
		}
		
		if (obj instanceof List<?>) {
			List<Integer> lstInteger = (List<Integer>) obj; 
			for (Integer id : lstInteger) {
				convert += id ;
				convert += ",";
			}
			
			if (convert.lastIndexOf(",") > 0) {
				convert = convert.substring(0,convert.length()-1);
			}
		}
		else {
			convert = obj.toString();
		}
		return convert;
	}

}
