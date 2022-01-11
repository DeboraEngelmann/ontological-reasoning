package jia;

import java.util.ArrayList;
import java.util.List;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

public class unifyRule extends DefaultInternalAction{
	@Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		
		 // Get Parameters        
	       List<Literal> rules = (List<Literal>) args[0]; //receives defeasible rules without unification
	       List<Term> preds = (List<Term>) args[1]; // receives the unified terms
	       Unifier u = new Unifier();

	      for (Literal arg : rules) {
	    	  
	    	  System.out.println("arg.toString()");
	    	  System.out.println(arg.toString());
	    	  Literal argLit = Literal.parseLiteral(arg.toString());
		      List<Term> listTerms = argLit.getTerms(); 
		      
		      // for each element of the list, check if it is possible to carry out the unification 
		      // with each unified term passed as a parameter.
		      for (Term element : listTerms) {  
		    	    if(element.isList()) {
		    	    	List<Term> list2 = (List<Term>) element;
		    	    	for (Term term : list2) {
		    	    		for (Term pred : preds) {
		    	    			System.out.println("1-pred.toString()");
		    	  	    	  	System.out.println(pred.toString());
			    	    		Literal predLit = Literal.parseLiteral(pred.toString());
			    	    		if(u.unifies(term, predLit)) {
			    	    			System.out.print("Unifier function -> ");
			    	    			System.out.println(u.toString());
			    	    		}
		    	    		}
		    	    	}
		    	    } else {
		    	    	for (Term pred : preds) {
	    	    			System.out.println("2-pred.toString()");
	    	  	    	  	System.out.println(pred.toString());
		    	    		Literal predLit = Literal.parseLiteral(pred.toString());
			    	    	if(u.unifies(element, predLit)) {
				    		}
		    	    	}
		    	    }
		      }
	      }
	      
	      List<Term> unifiedRules = getUnifiedRules(rules,u);
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
