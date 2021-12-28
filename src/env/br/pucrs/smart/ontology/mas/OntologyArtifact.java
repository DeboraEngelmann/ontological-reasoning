package br.pucrs.smart.ontology.mas;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.xalan.xsltc.compiler.sym;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import br.pucrs.smart.ontology.OwlOntoLayer;
import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import openllet.owlapi.OWL;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.owlapi.explanation.PelletExplanation;

public class OntologyArtifact extends Artifact {
	private Logger logger = Logger.getLogger(OntologyArtifact.class.getName());
	
	private OwlOntoLayer onto = null;
	private OntoQueryLayerLiteral queryEngine;
	private OWLOntology ontology;
	private PelletExplanation expGen;
	
	void init(String ontologyPath) {
		logger.info("Importing ontology from " + ontologyPath);
		try {
			this.onto = new OwlOntoLayer(ontologyPath);
			final OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(this.onto.getOntology());
			this.onto.setReasoner(reasoner);
			queryEngine = new OntoQueryLayerLiteral(this.onto);
			logger.info("Ontology ready!");

			PelletExplanation.setup();
			
			final OWLOntologyManager manager = OWL._manager;
			IRI iri = IRI.create((File)new File(ontologyPath));
			this.ontology = manager.loadOntology(iri);
			
			// Create an clashExplanation generator
			this.expGen = new PelletExplanation(reasoner);

		} catch (OWLOntologyCreationException e) {
			logger.info("An error occurred when loading the ontology. Error: "+e.getMessage());
		} catch (Exception e) {
			logger.info("An unexpected error occurred: "+e.getMessage());
		}
	}
	
	/**
	 * @param strDomain Name of the domain.
	 * @param strRange Name of the range.
	 * @param strobjectProperty Name of the objectProperty.
	 * @return a string with a set of axioms that explain the reasoner's inference.
	 */
	@OPERATION
	void getExplanation(String strDomain, String strRange, String strobjectProperty, OpFeedbackParam<Literal> axioms) {

			OWLDataFactory dataFactory = this.ontology.getOWLOntologyManager().getOWLDataFactory();
			IRI baseIRI = this.ontology.getOntologyID().getDefaultDocumentIRI().get();
			
			OWLNamedIndividual domain = dataFactory.getOWLNamedIndividual(IRI.create((String)((Object)baseIRI + "#" + strDomain)));
	        OWLNamedIndividual range = dataFactory.getOWLNamedIndividual(IRI.create((String)((Object)baseIRI + "#" + strRange)));
	        OWLObjectProperty objectProperty = dataFactory.getOWLObjectProperty(IRI.create((String)((Object)baseIRI + "#" + strobjectProperty)));
	        OWLObjectPropertyAssertionAxiom propertyAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom((OWLObjectPropertyExpression)objectProperty, (OWLIndividual)domain, (OWLIndividual)range);

			Set<Set<OWLAxiom>> axiomSets = this.expGen.getEntailmentExplanations((OWLAxiom)propertyAssertion);
			
			Set<OWLAxiom> explanation = chooseExplanation(axiomSets);
//			AxiomTranslator.translateAxioms(explanation);
//			ExplanationTerms explanationTerms = AxiomTranslator.translateAxioms(explanation);
//			Collection<Term> owlRulesList = AxiomTranslator.translateAxioms(explanation);
//			owlRulesList.forEach(x->System.out.println(x));
			
//			System.out.println(ASSyntax.createList(owlRulesList));
//			Literal owlAxioms = ASSyntax.createLiteral("owlAxioms", ASSyntax.createList(owlRulesList));
//			axiomList.add(owlAxioms);
		
		axioms.set(AxiomTranslator.translateAxioms(explanation));
					
	}
	
	/**
	 * @param instanceName Name of the new instance.
	 * @param conceptName Name of the concept which the new instance instances.
	 */
	@OPERATION
	void addInstance(String instanceName, String conceptName) {
		queryEngine.getQuery().addInstance(instanceName, conceptName);
	}
	
	/**
	 * @param instanceName Name of the new instance.
	 */
	@OPERATION
	void addInstance(String instanceName) {
		queryEngine.getQuery().addInstance(instanceName);
	}
	
	/**
	 * @param instanceName Name of the instance.
	 * @param conceptName Name of the concept.
	 * @return true if the <code>instanceName</code> instances <code>conceptName</code>.
	 */
	@OPERATION
	void isInstanceOf(String instanceName, String conceptName, OpFeedbackParam<Boolean> isInstance) {
		isInstance.set(queryEngine.getQuery().isInstanceOf(instanceName, conceptName));
	}
	
	/**
	 * @param conceptName Name of the concept.
	 * @param instances A free variable to receive the list of instances in the form of instances(concept,instance)
	 */
	@OPERATION
	void getInstances(String conceptName, OpFeedbackParam<Literal[]> instances){
		List<Object> individuals = queryEngine.getIndividualNames(conceptName);
		instances.set(individuals.toArray(new Literal[individuals.size()]));
	}
	
	/**
	* @return A list of ({@link OWLObjectProperty}).
	*/
	@OPERATION
	void getObjectPropertyNames(OpFeedbackParam<Literal[]> objectPropertyNames){
		List<Object> names = queryEngine.getObjectPropertyNames();
		objectPropertyNames.set(names.toArray(new Literal[names.size()]));
	}
	
	/**
	* @return A list of ({@link OWLLogicalAxiom}).
	*/	
	@OPERATION
	void getLogicalAxioms(OpFeedbackParam<Literal[]> logicalAxioms){
		System.out.println("getLogicalAxioms in Artifact");
		List<Object> axioms = queryEngine.getLogicalAxioms();
		logicalAxioms.set(axioms.toArray(new Literal[axioms.size()]));
	}
	
	/**
	 * @param domainName Name of the instance ({@link OWLNamedIndividual}} which represent the property <i>domain</i>.
	 * @param propertyName Name of the new property.
	 * @param rangeName Name of the instance ({@link OWLNamedIndividual}} which represent the property <i>range</i>.
	 */
	@OPERATION
	void addProperty(String domainName, String propertyName, String rangeName) {
		queryEngine.getQuery().addProperty(domainName, propertyName, rangeName);
	}
	
	/**
	 * @param domainName Name of the instance which represents the domain of the property.
	 * @param propertyName Name of the property.
	 * @param rangeName Name of the instance which represents the range of the property.
	 * @return true if a instance of the property was found, false otherwise.
	 */
	@OPERATION
	void isRelated(String domainName, String propertyName, String rangeName, OpFeedbackParam<Boolean> isRelated) {
		isRelated.set(queryEngine.getQuery().isRelated(domainName, propertyName, rangeName));
	}
	
	/**
	 * @param domain The name of the instance which corresponds to the domain of the property.
	 * @param propertyName Name of the property
	 * @return A list of ({@link OWLNamedIndividual}).
	 */
	@OPERATION
	void getObjectPropertyValues(String domain, String propertyName, OpFeedbackParam<String> instances) {
		List<String> individuals = new ArrayList<String>();
		for(OWLNamedIndividual individual : queryEngine.getQuery().getObjectPropertyValues(domain, propertyName)) {
			individuals.add(individual.getIRI().toString().substring(individual.getIRI().toString().indexOf('#')+1));
		}
		instances.set(individuals.toString());
	}

	/**
	* @return A list of ({@link OWLClass}).
	*/
	@OPERATION
	void getClassNames(OpFeedbackParam<Literal[]> classes){
		List<Object> classNames = queryEngine.getClassNames();
		classes.set(classNames.toArray(new Literal[classNames.size()]));
	}
	
	
	/**
	 * @param conceptName Name of the new concept.
	 */
	@OPERATION
	void addConcept(String conceptName) {
		queryEngine.getQuery().addConcept(conceptName);
	}
	
	/**
	 * @param subConceptName Name of the supposed sub-concept.
	 * @param superConceptName Name of the concept to be tested as the super-concept.
	 * @return true if <code>subConceptName</code> is a sub-concept of <code>sueperConceptName</code>, false
	 * otherwise.
	 */
	@OPERATION
	void isSubConcept(String subConceptName, String superConceptName, OpFeedbackParam<Boolean> isSubConcept) {
		isSubConcept.set(queryEngine.getQuery().isSubConceptOf(subConceptName, superConceptName));
	}
		
	/**
	 * @param outputFile Path to the new file in the structure of directories.
	 * @throws OWLOntologyStorageException
	 */
	@OPERATION
	void saveOntotogy(String outputFile) {
		try {
			queryEngine.getQuery().saveOntology(outputFile);
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
	}
	
	/**
	* @return A list of ({@link OWLAnnotationProperty}).
	*/
	@OPERATION
	void getAnnotationPropertyNames(OpFeedbackParam<Literal[]> AnnotationPropertyNames){
		List<Object> names = queryEngine.getAnnotationPropertyNames();
		AnnotationPropertyNames.set(names.toArray(new Literal[names.size()]));
	}
	
	/**
	* @return A list of ({@link OWLDataProperty}).
	*/
	@OPERATION
	void getDataPropertyNames(OpFeedbackParam<Literal[]> dataPropertyNames){
		List<Object> names = queryEngine.getDataPropertyNames();
		dataPropertyNames.set(names.toArray(new Literal[names.size()]));
	}
		

	Set<OWLAxiom> chooseExplanation(Set<Set<OWLAxiom>> axiomSets) {
		Set<OWLAxiom> mainExplanation = null;
		int mainScore = 1000;
		for (Set<OWLAxiom> axiomSet : axiomSets) {
			int score = 0;
			int numRules = 0;
			int numAxioms = 0;
			
			for (OWLAxiom axiom : axiomSet) {
				numAxioms++;
				if (AxiomTranslator.hasType(axiom, "Rule")) numRules++;
			}
			score = numRules + numAxioms;
			if (score<mainScore) {
				mainScore = score;
				mainExplanation = axiomSet;
			}
//			AxiomTranslator.translateAxioms(axiomSet);
		}
		
		System.out.println("Chosen Explanation: [");
		mainExplanation.forEach(axiom -> System.out.println(axiom));
		System.out.println("]");
		return mainExplanation;
	}
}



















