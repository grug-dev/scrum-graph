package co.edu.ud.scrumgraph.data.enums;

/**
 * Enumerado para administrar los c�digos de respuesta del response de los
 * servicios REST.
 * 
 * @author RaspuWIN7
 */
public enum ECodeExceptionSG {

	// GLOBAL CODES
	BAD_DATE(98, "Bad date format"),
	CONSTRAINT_VIOLATION(99, "Constraint violation"),
	OPERATION_FAILED(102, "Operation failed"),
	INVALID_PROPERTIES(203, "Invalid Fields"),
	ERROR_CODE(500, "Error"),

	// USER CODES
	INVALID_EMAIL(300, "Invalid Email"),
	UNAUTHENTICATED_USER(301, "Unauthenticated user"),
	UNAUTHORIZED_USER(302, "Unauthorized user"),
	EMAIL_ALREADY_EXISTS(303, "Email already exists"),
	USER_DOESNT_EXISTS(304, "User doesn't exists"),
	USER_UNAVAILABLE(305, "User unavailable "),

	// PROJECT CODES
	PROJECT_ALREADY_EXISTS(401, "Project already exists"),
	PROJECT_DOESNT_EXISTS(402, "Project doesn't exists"),
	PROJECT_CODE_INVALID(403, "Project Code is Invalid"),

	// PBI CODES
	PBI_ALREADY_EXISTS(501, "PBI already exists"),
	PBI_DOESNT_EXISTS(502, "PBI doesn't exists"),
	PBI_CODE_INVALID(503, "PBI Code is Invalid"),

	// PBI CODES
	SPRINT_ALREADY_EXISTS(601, "Sprint already exists"),
	SPRINT_DOESNT_EXISTS(602, "Sprint doesn't exists"),
	SPRINT_CODE_INVALID(603, "Sprint Code is Invalid"),

	// PBI CODES
	TASK_ALREADY_EXISTS(701, "Task already exists"),
	TASK_DOESNT_EXISTS(702, "Task doesn't exists"),
	TASK_CODE_INVALID(703, "Task Code is Invalid"),

	;

	private int code;

	private String message;

	private ECodeExceptionSG(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
