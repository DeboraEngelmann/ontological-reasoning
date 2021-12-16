// Agent ontology_assistant in project ontological_reasoning

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start 
	: true 
<- 
	.print("Ontology assistant agent enabled.")
	.print("Let's use an ontology");
	
	!fillTheBeliefBase;	
	
//	getExplanation("Fulano", "Adult", "is-of-age-group", Axioms);
	getExplanation("Fulano", "University", "is-in", Axioms);
	.print(Axioms);
	+Axioms;
	!addToBB(Axioms);
	.


+!instantiateArgumentScheme(Axioms)
<-
	
	.print(Axioms);
	 .

+!addToBB(owlAxioms(List))
<-
	!addToBB(List)	
	.
+!addToBB([H])
<-
	+H;
	.print(H);
	.
+!addToBB([H|T])
<-
	+H;
	.print(H);
	!addToBB(T);
	.
	
	
+!print(_,[])
<-
	.print("End of list");	
	.
+!print(Type,[H|T])
<-
	.print(Type," : ", H);
	!print(Type, T);
	.
	
+!fillTheBeliefBase
<- 
	.print("Getting classes in ontology");
	getClassNames(ClassNames);
	.print("Adding Classes to the belief base");
	!addToTheBeliefBase(ClassNames);
	.print("Getting ObjectProperties in ontology");
	getObjectPropertyNames(ObjectPropertyNames);
	.print("Adding ObjectProperties to the belief base");
	!addToTheBeliefBase(ObjectPropertyNames);
	.

+!addToTheBeliefBase([]).	
+!addToTheBeliefBase([H|T])
<-
	+H;
	!addToTheBeliefBase(T)
	.

+!isRelated(Domain, Property, Range, IsRelated)
 	: objectProperty(PropertyName,Property)
<-
	isRelated(Domain, PropertyName, Range, IsRelated);
	.print("Domain: ", Domain, " PropertyName: ", PropertyName, " Range: ", Range, " IsRelated: ", IsRelated);
	.
	
+!addInstance(InstanceName, Concept)
	: concept(ClassName,Concept)
<- 
	.print("Adding a new ", ClassName, " named ", InstanceName);
	addInstance(InstanceName, ClassName);
	!getInstances(Concept, Instances);
	!print("Instances", Instances);
	.

+!isInstanceOf(InstanceName, Concept, Result)
	: concept(ClassName,Concept)
<- 
	.print("Checking if ", InstanceName, " is an instance of ", ClassName);
	isInstanceOf(InstanceName, ClassName, Result);
	.print("The result is ", Result);
	!getInstances(Concept, Instances);
	!print("Instances", Instances);
	.

+!getInstances(Concept, Instances)
	: concept(ClassName,Concept)
<-
	.print("Getting instances of ", ClassName);
	getInstances(ClassName, Instances);
	.
	
+!getObjectPropertyValues(Domain, Property, Range)
 	: objectProperty(PropertyName,Property)
<-
	getObjectPropertyValues(Domain, PropertyName, Range);
	.print("Domain: ", Domain, " PropertyName: ", PropertyName, " Range: ", Range);
	.
	
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
