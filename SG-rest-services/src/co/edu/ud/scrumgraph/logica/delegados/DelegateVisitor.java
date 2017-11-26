package co.edu.ud.scrumgraph.logica.delegados;

import co.edu.ud.scrumgraph.logica.serviciosrest.SrvRestPBI;

public class DelegateVisitor implements IDelegateVisitor {

	@Override
	public IDelegateService visit(SrvRestPBI serviceRest) {
		return new DelegatePBI(serviceRest);
	}

}
