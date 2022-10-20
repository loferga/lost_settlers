package fr.loferga.lost_settlers.map.geometry;

public class Matrix {
	
	public Matrix(double[][] v) {
		this.v = v;
	}
	
	private double[][] v;
	
	public Point toPoint() {
		if (v.length == 3 && v[0].length == 1)
			return new Point(v[0][0], v[1][0], v[2][0]);
		else if (v.length == 1 && v[0].length == 3)
			return new Point(v[0][0], v[0][1], v[0][2]);
		return null;
	}
	
	public Matrix multiply(Matrix b) {
		
		double[][] matrix = new double[v.length][b.v[0].length];
		for (int i = 0; i < v.length; i++) {
			for (int j = 0; j < b.v[0].length; j++) {
				double sum = 0;
				for (int k = 0; k < v[i].length; k++)
					sum += v[i][k] * b.v[k][j];
				matrix[i][j] = sum;
			}
		}
		
		return new Matrix(matrix);
	}
	
	public Matrix inverse() {
		double[][] inverse = new double[v.length][v.length];
		
		// minors and cofactors
		for (int i = 0; i < v.length; i++)
			for (int j = 0; j < v[i].length; j++)
				inverse[i][j] = Math.pow(-1, (double) i + j)
						* submatrix(i, j).determinant();
		
		// adjugate and determinant
		double det = 1.0 / determinant();
		for (int i = 0; i < inverse.length; i++) {
			for (int j = 0; j <= i; j++) {
				double temp = inverse[i][j];
				inverse[i][j] = inverse[j][i] * det;
				inverse[j][i] = temp * det;
			}
		}
		
		return new Matrix(inverse);
	}
	
	private Matrix submatrix(int row, int column) {
		double[][] submatrix = new double[v.length - 1][v.length - 1];
		
		for (int i = 0; i < v.length; i++)
			for (int j = 0; i != row && j < v[i].length; j++)
				if (j != column)
					submatrix[i < row ? i : i - 1][j < column ? j : j - 1] = v[i][j];
		return new Matrix(submatrix);
	}
	
	private double determinant() {
		if (v.length == 2)
			return v[0][0] * v[1][1] - v[0][1] * v[1][0];
		
		double det = 0;
		for (int i = 0; i < v[0].length; i++)
			det += Math.pow(-1, i) * v[0][i]
					* this.submatrix(0, i).determinant();
		return det;
	}
	
}
