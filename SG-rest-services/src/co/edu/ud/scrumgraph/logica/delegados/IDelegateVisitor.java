package co.edu.ud.scrumgraph.logica.delegados;

import co.edu.ud.scrumgraph.logica.serviciosrest.SrvRestPBI;

public interface IDelegateVisitor {

	IDelegateService visit(SrvRestPBI serviceRest);
	
}
