package br.pucrs.smart.ontology.mas;

import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;

public class AxiomTranslator {
	
	static void translateAxioms(Set<OWLAxiom> explanation) {
		
		
		for (OWLAxiom axiom : explanation) {
			
			if(hasType(axiom, "Rule")){
				System.out.println("Rule");
				System.out.println(axiom);
			} else if(hasType(axiom, "ObjectPropertyDomain")){
				System.out.println("ObjectPropertyDomain");
				System.out.println(axiom);
			} else if(hasType(axiom, "DataPropertyDomain")){
				System.out.println("DataPropertyDomain");
				System.out.println(axiom);
			} else if(hasType(axiom, "DataPropertyAssertion")){
				System.out.println("DataPropertyAssertion");
				System.out.println(axiom);
			} else if(hasType(axiom, "SubClassOf")){
				System.out.println("SubClassOf");
				System.out.println(axiom);
			} else if(hasType(axiom, "ClassAssertion")){
				System.out.println("ClassAssertion");
				System.out.println(axiom);
			} else {
				System.out.println("[AxiomTranslator] Error: Type not registered: " + axiom.getAxiomType());
				System.out.println(axiom);
			}
		}
		
	}
	
	
	static boolean hasType(OWLAxiom axiom, String type) {
		AxiomType<?> aType = axiom.getAxiomType();
		return aType.equals(AxiomType.getAxiomType(type));
	}
}
