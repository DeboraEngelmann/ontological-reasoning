package br.pucrs.smart.ontology.mas;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
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
//				System.out.println("Rule");
//				axiom.components().forEach(c-> System.out.println(c));
				
				Collection<Term> bodyTerms = getTermsBySWRLAtoms(body);
				Collection<Term> headTerms = getTermsBySWRLAtoms(head);
				
				Literal dF = ASSyntax.createLiteral("defeasible_rule", ASSyntax.createList(headTerms));
				dF.addTerm(ASSyntax.createList(bodyTerms));
				dF.addAnnot(ASSyntax.createLiteral("as", ASSyntax.createString("Esquema1")));
//				System.out.println(dF);
			} else if(hasType(axiom, "ObjectPropertyDomain")){
				//ignore
			} else if(hasType(axiom, "DataPropertyDomain")){
//				//ignore
			} else if(hasType(axiom, "DataPropertyAssertion")){

				OWLDataPropertyAssertionAxiom dPAAxiom = (OWLDataPropertyAssertionAxiom) axiom;
				List<?> dPAAxiomComponents = dPAAxiom.components().collect(Collectors.toList());
				
				String domain = dPAAxiomComponents.get(0).toString();
				String propertie = dPAAxiomComponents.get(1).toString();
				String range = dPAAxiomComponents.get(2).toString();
				
				Literal l = ASSyntax.createLiteral(propertie.substring((propertie.indexOf("#")+1), propertie.indexOf(">")));
				l.addTerm(ASSyntax.createAtom(OntoQueryLayerLiteral.getNameForJason(domain.substring((domain.indexOf("#")+1), domain.indexOf(">")))));
				if (range.contains("xsd:integer")) {
					l.addTerm(ASSyntax.createAtom(range.substring(1, (range.indexOf("^^")-1))));
				} else {
					l.addTerm(ASSyntax.createAtom(OntoQueryLayerLiteral.getNameForJason(range.substring((range.indexOf("#")+1), range.indexOf(">")))));
				}

			} else if(hasType(axiom, "SubClassOf")){

				OWLSubClassOfAxiom sCAAxiom = (OWLSubClassOfAxiom) axiom;
				List<?> sCAAxiomComponents = sCAAxiom.components().collect(Collectors.toList());
				String domain = sCAAxiomComponents.get(0).toString();
				String range = sCAAxiomComponents.get(1).toString();
				
				Literal l = ASSyntax.createLiteral("subClassOf");
				l.addTerm(ASSyntax.createAtom(OntoQueryLayerLiteral.getNameForJason(domain.substring((domain.indexOf("#")+1), domain.indexOf(">")))));
				l.addTerm(ASSyntax.createAtom(OntoQueryLayerLiteral.getNameForJason(range.substring((range.indexOf("#")+1), range.indexOf(">")))));
				
			} else if(hasType(axiom, "ClassAssertion")){

				OWLClassAssertionAxiom cAAxiom = (OWLClassAssertionAxiom) axiom;
				List<?> cAAxiomComponents = cAAxiom.components().collect(Collectors.toList());

				String range = cAAxiomComponents.get(0).toString();
				String domain = cAAxiomComponents.get(1).toString();
				Literal l = ASSyntax.createLiteral(OntoQueryLayerLiteral.getNameForJason(domain.substring((domain.indexOf("#")+1), domain.indexOf(">"))));
				l.addTerm(ASSyntax.createAtom(OntoQueryLayerLiteral.getNameForJason(range.substring((range.indexOf("#")+1), range.indexOf(">")))));

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
							// for example [Variable(<urn:swrl:var#A>), "17"^^xsd:integer]
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
