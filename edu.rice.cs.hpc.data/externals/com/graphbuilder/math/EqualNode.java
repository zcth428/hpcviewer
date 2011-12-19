package com.graphbuilder.math;

public class EqualNode extends OpNode {

	public EqualNode(Expression leftChild, Expression rightChild) {
		super(leftChild, rightChild);
	}

	@Override
	public String getSymbol() {
		return "==";
	}

	@Override
	public double eval(VarMap v, FuncMap f) {
		int a = (int) leftChild.eval(v, f);
		int b = (int) rightChild.eval(v, f);
		if (a==b)
			return 0.0;
		else
			return 1.0;
	}

}
