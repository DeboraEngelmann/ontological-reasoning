// Internal action code for project data_access_control

package jia;

import java.util.ArrayList;
import java.util.List;

import jason.asSemantics.*;
import jason.asSyntax.*;

public class get_unifier2 extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
    	
       // Get Parameters        
       List<Literal> rules = (List<Literal>) args[0]; //recebe a regra defeasible sem unificação
       List<Term> preds = (List<Term>) args[1]; // recebe o termo unificado
       Unifier u = new Unifier();
       
      //System.out.println(args[0].toString() + "\n" + args[1].toString() + "\n" + args[2].toString());

      for (Literal arg : rules) {
       
	      List<Term> listTerms = arg.getTerms(); //pela formatação atual, a regra defeasible é uma lista 
	      
	      for (Term element : listTerms) {  						//para cada elemento da lista verifica se é possivel realizar a unificação com o termo unificado passado como parâmetro
	    	    if(element.isList()) {                              // a lista pode ainda ser uma lista ... (segunda a estrutura que passou)
	    	    	List<Term> list2 = (List<Term>) element;
	    	    	for (Term term : list2) {
	    	    		System.out.print("** term -> ");
	    	    		System.out.println(term);
	    	    		for (Term pred : preds) {
	    	    			System.out.print("pred -> ");
		    	    		System.out.println(pred);
		    	    		if(u.unifies(term, pred)) {
		    	    			System.out.print("u.toString() -> ");// detalhe.. se tivermos uma lista de termos unificados (nesse exemplo temos apenas age(fulano,24)), precisa um novo laço de iteração       
		    	    			System.out.println(u.toString());       // e é necessário testar quando algumas variáveis estão em termos diferentes, se o "unifies" complementa a função de unificação ou substitui
		    	    		} else {
		    	    			System.out.println("não unifica ");
		    	    		}
	    	    		}
	    	    	}
	    	    } else {
	    	    	for (Term pred : preds) {
		    	    	if(u.unifies(element, pred)) {
		    	    		System.out.print("u.toString() -> ");
			    			System.out.println(u.toString());
			    		}
	    	    	}
	    	    }
	      }
	      System.out.println("Regra unificada, caso necessário ->>  " + Literal.parseLiteral(arg.capply(u).toString())); // caso queira retornar a regra unificada, basta aplicar a função de unificação
      }
      
      System.out.println("Regras unificadas: ");
      rules.forEach(rule -> System.out.println(Literal.parseLiteral(rule.capply(u).toString())));
      return un.unifies(args[2], new StringTermImpl(u.toString()));  // u contem a função de unificação, é o que está sendo retornado
    
    }

}
