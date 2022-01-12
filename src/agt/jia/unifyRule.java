package jia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

public class unifyRule extends DefaultInternalAction {
	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

		// Get Parameters
		List<Literal> rules = (List<Literal>) args[0]; // receives defeasible rules without unification
		List<Term> preds = (List<Term>) args[1]; // receives the unified terms
		Unifier u = new Unifier();
		List<Unifier> uList = new ArrayList<Unifier>();
		List<Term> variables = new ArrayList<Term>();
		int i = 0;

//	      for (Literal arg : rules) {
//	    	  
//	    	  System.out.println("arg.toString()");
//	    	  System.out.println(arg.toString());
//	    	  Literal argLit = Literal.parseLiteral(arg.toString());
//		      List<Term> listTerms = argLit.getTerms(); 
//		      
//		      // for each element of the list, check if it is possible to carry out the unification 
//		      // with each unified term passed as a parameter.
//		      for (Term element : listTerms) {  
//		    	    if(element.isList()) {
//		    	    	List<Term> list2 = (List<Term>) element;
//		    	    	for (Term term : list2) {
//		    	    		for (Term pred : preds) {
//		    	  	    	  	System.out.println(pred.toString());
//			    	    		Literal predLit = Literal.parseLiteral(pred.toString());
//			    	    		if(u.unifies(term, predLit)) {
//			    	    			System.out.print("Unifier function -> ");
//			    	    			System.out.println(u.toString());
//			    	    		}
//		    	    		}
//		    	    	}
//		    	    } else {
//		    	    	for (Term pred : preds) {
//	    	  	    	  	System.out.println(pred.toString());
//		    	    		Literal predLit = Literal.parseLiteral(pred.toString());
//			    	    	if(u.unifies(element, predLit)) {
//				    		}
//		    	    	}
//		    	    }
//		      }
//	      }

		System.out.println("=====================================");
//		System.out.print("preds: ");
//		System.out.println(preds);
//		
//		Collections.swap(preds, preds.size()-1, 0);
//		
		System.out.print("preds: ");
		System.out.println(preds);
		System.out.println("=====================================");
		do {
			u.clear();
			variables.clear();
			i++;
			System.out.println(preds);
			for (Term pred : preds) {
				System.out.print("pred: ");
				System.out.println(pred.toString());
				Literal predLit = Literal.parseLiteral(pred.toString());

				for (Literal arg : rules) {

//					System.out.println("arg.toString()");
					System.out.println(arg.toString());
					Literal argLit = Literal.parseLiteral(arg.toString());

					// uses only the rule body to create the unify function
					List<Term> listTerms = argLit.getTerms();

					// for each element of the list, check if it is possible to carry out the
					// unification
					// with each unified term passed as a parameter.

					for (Term element : listTerms) {
						if (element.isList()) {
							List<Term> list2 = (List<Term>) element;
							for (Term term : list2) {
//								System.out.print("term: ");
//								System.out.println(term);

								// store all variables in an array for later verification
								Literal termLit = Literal.parseLiteral(term.toString());
								termLit.getTerms().forEach(t -> {
									if (!variables.contains(t))
										variables.add(t);
								});
								if (u.unifies(term, predLit)) {
									System.out.print("Unifier function -> ");
									System.out.println(u.toString());
								}

							}
						} else {
//							System.out.print("element: ");
//							System.out.println(element);
							// store all variables in an array for later verification
							Literal termLit = Literal.parseLiteral(element.toString());
							termLit.getTerms().forEach(t -> {
								if (!variables.contains(t))
									variables.add(t);
							});
							if (u.unifies(element, predLit)) {
								System.out.print("Unifier function -> ");
								System.out.println(u.toString());
							}
						}
					}
				}
			}
			Term temp = preds.remove(0);
			preds.add(temp);
			
			
			System.out.println("================================");
			System.out.print("variables.size(): ");
			System.out.println(variables.size());
			System.out.print("u.size(): ");
			System.out.println(u.size());
			System.out.print("i: ");
			System.out.println(i);
			System.out.print("preds.size(): ");
			System.out.println(preds.size());
			System.out.println("================================");
			uList.add(u.clone());
		} while (variables.size() > u.size() & i < preds.size());

		System.out.println(variables);
		uList.forEach(e -> System.out.println(e));
		List<Term> unifiedRules = getUnifiedRules(rules, u);
		return un.unifies(args[2], ASSyntax.parseTerm(unifiedRules.toString()));

	}

	List<Term> getUnifiedRules(List<Literal> rules, Unifier u) {
		List<Term> unifiedRules = new ArrayList<Term>();

		for (Term rule : rules) {
			System.out.println("rule.toString()");
			System.out.println(rule.toString());
			Term ruleLit = Literal.parseLiteral(rule.toString());
			unifiedRules.add(Literal.parseLiteral(ruleLit.capply(u).toString()));
		}

		return unifiedRules;
	}

}
