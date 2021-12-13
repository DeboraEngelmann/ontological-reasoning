package br.pucrs.smart.ontology.mas;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLRule;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

public class AxiomTranslator {
	
	static void translateAxioms(Set<OWLAxiom> explanation) {
		
		
		for (OWLAxiom axiom : explanation) {
			
			if(hasType(axiom, "Rule")){
//				 defeasible_rule([is_of_age_group(P,adult)],[person(P),age(P,A),greaterThan(A,17)])[as(<nome_do_esquema>)]
				SWRLRule rule = (SWRLRule) axiom;
				List<SWRLAtom> body = rule.bodyList();
				List<SWRLAtom> head = rule.headList();
				System.out.println("Rule");
				System.out.println(axiom);
				
				Collection<Term> bodyTerms = getTermsBySWRLAtoms(body);
				Collection<Term> headTerms = getTermsBySWRLAtoms(head);
				
				System.out.println("*********** bodyTerms");
				bodyTerms.forEach(b->System.out.println(b.toString()));
				System.out.println("*********** headTerms");
				headTerms.forEach(entity -> System.out.println(entity));
				
				Literal dF = ASSyntax.createLiteral("defeasible_rule", ASSyntax.createList(headTerms));
				dF.addTerm(ASSyntax.createList(bodyTerms));
				dF.addAnnot(ASSyntax.createLiteral("as", ASSyntax.createString("Esquema1")));
				System.out.println("*********** Defeasible Rule");
				System.out.println(dF);
				System.out.println("-----------------");
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
	
	static Collection<Term> getTermsBySWRLAtoms(List<SWRLAtom> atoms) {
		Collection<Term> terms = new LinkedList<Term>();
		atoms.forEach(entity -> {
			String uriPred = entity.getPredicate().toString();
			String pred = uriPred.substring((uriPred.indexOf("#")+1), (uriPred.contains(">") ? uriPred.indexOf(">") : uriPred.length()));
			String jPred = OntoQueryLayerLiteral.getNameForJason(pred);
			Literal l = ASSyntax.createLiteral(jPred);
			
			List<?> var = entity.components().collect(Collectors.toList());
			var.forEach(v -> {
				String vs = v.toString();
				if (!vs.contains(pred)) {
					if (vs.contains("Variable")) {
						if(vs.contains("xsd:integer")) {
	//						for example [Variable(<urn:swrl:var#A>), "17"^^xsd:integer]
							l.addTerm(ASSyntax.createAtom(vs.substring((vs.indexOf("#")+1), vs.indexOf(">"))));
							l.addTerm(ASSyntax.createAtom(vs.substring((vs.indexOf(",")+3), (vs.indexOf("^^")-1))));
						} else {
							l.addTerm(ASSyntax.createAtom(vs.substring((vs.indexOf("#")+1), vs.indexOf(">"))));
						}
					} else {
						l.addTerm(ASSyntax.createAtom(OntoQueryLayerLiteral.getNameForJason(vs.substring((vs.indexOf("#")+1), vs.indexOf(">")))));
					}
				}
			});
			terms.add(l);
		});
		return terms;
	}
	
	
	static boolean hasType(OWLAxiom axiom, String type) {
		AxiomType<?> aType = axiom.getAxiomType();
		return aType.equals(AxiomType.getAxiomType(type));
	}
	
}
