package de.lukaspanneke.masterthesis.logic;

import io.github.cvc5.Result;
import io.github.cvc5.Solver;
import io.github.cvc5.Sort;
import io.github.cvc5.Term;

import java.util.HashMap;
import java.util.Map;

import static de.lukaspanneke.masterthesis.Options.*;

public class SatSolver {

	private static boolean checkSat(Formula formula) {
		Solver solver = new Solver();
		try {
			if (SHOW_MODEL) {
				solver.setOption("produce-models", "true");
			}
			Sort integer = solver.getIntegerSort();
			Map<Variable, Term> atoms = new HashMap<>();
			Term cvc5Formula = formula.toCvc5(solver, v -> atoms.computeIfAbsent(v, variable -> solver.mkConst(integer, variable.name())));
			solver.assertFormula(cvc5Formula);
			Result result = solver.checkSat();
			if (SHOW_MODEL && result.isSat()) {
				for (Term atom : atoms.values()) {
					System.err.println(atom + " = " + solver.getValue(atom));
				}
			}
			if (result.isSat()) {
				return true;
			} else if (result.isUnsat()) {
				return false;
			} else {
				throw new AssertionError("SAT result is " + result + ". formula was " + formula + ", encoded as " + cvc5Formula);
			}
		} finally {
			solver.deletePointer();
		}
	}

	public static boolean isSatisfiable(Formula formula) {
		if (SHOW_FORMULAS) {
			System.err.println("      " + formula);
		}
		boolean result = checkSat(formula);
		if (PRINT_COLOR_CONFLICT_INFO) {
			System.err.println("      " + (result ? "SAT" : "UNSAT"));
		}
		return result;
	}

	public static boolean isTautology(Formula formula) {
		if (SHOW_FORMULAS) {
			System.err.println("    " + formula);
		}
		boolean result = !checkSat(formula.not());
		if (PRINT_COLOR_CUTOFF_INFO) {
			System.err.println("    " + (result ? "TAUTOLOGY" : "NOT A TAUTOLOGY"));
		}
		return result;
	}
}
