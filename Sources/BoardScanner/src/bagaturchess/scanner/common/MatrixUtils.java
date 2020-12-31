/**
 *  BagaturChess (UCI chess engine and tools)
 *  Copyright (C) 2005 Krasimir I. Topchiyski (k_topchiyski@yahoo.com)
 *  
 *  This file is part of BagaturChess program.
 * 
 *  BagaturChess is open software: you can redistribute it and/or modify
 *  it under the terms of the Eclipse Public License version 1.0 as published by
 *  the Eclipse Foundation.
 *
 *  BagaturChess is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Eclipse Public License for more details.
 *
 *  You should have received a copy of the Eclipse Public License version 1.0
 *  along with BagaturChess. If not, see http://www.eclipse.org/legal/epl-v10.html
 *
 */
package bagaturchess.scanner.common;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bagaturchess.bitboard.impl.utils.VarStatistic;


public class MatrixUtils {
	
	
	public static Map<Integer, int[][]> splitTo64Squares(int[][] matrix) {
		
		int size = matrix.length;
		if (matrix.length != matrix[0].length) {
			throw new IllegalStateException("matrix is not square");
		}
		
		Map<Integer, int[][]> result = new HashMap<Integer, int[][]>();
		for (int i = 0; i < size; i += size / 8) {
			for (int j = 0; j < size; j += size / 8) {
				int file = i / (size / 8);
				int rank = j / (size / 8);
				int filedID = 63 - (file + 8 * rank);
				int[][] arr = getSquarePixelsMatrix(matrix, i, j);
				result.put(filedID, arr);
			}
		}
		
		return result;
	}
	
	
	public static Map<Integer, int[][][]> splitTo64Squares(int[][][] matrix) {
		
		int size = matrix.length;
		if (matrix.length != matrix[0].length) {
			throw new IllegalStateException("matrix is not square");
		}
		
		if (matrix[0][0].length != 3) {
			throw new IllegalStateException("There is no 3 channels");
		}
		
		Map<Integer, int[][][]> result = new HashMap<Integer, int[][][]>();
		for (int i = 0; i < size; i += size / 8) {
			for (int j = 0; j < size; j += size / 8) {
				int file = i / (size / 8);
				int rank = j / (size / 8);
				int filedID = 63 - (file + 8 * rank);
				int[][][] arr = getSquarePixelsMatrix(matrix, i, j);
				result.put(filedID, arr);
			}
		}
		
		return result;
	}
	
	
	public static int[][] getSquarePixelsMatrix(int[][] matrix, int i1, int j1) {
		
		if (matrix.length % 8 != 0) {
			throw new IllegalStateException("size is not devidable by 8");
		}
		
		int size = matrix.length / 8;
		int[][] result = new int[size][size];
		
		int ic = 0;
		for (int i = i1; i < i1 + size; i++) {
			int jc = 0;
			for (int j = j1; j < j1 + size; j++) {
				result[ic][jc] = matrix[i][j];
				jc++;
			}
			ic++;
		}
		
		return result;
	}
	
	
	public static int[][][] getSquarePixelsMatrix(int[][][] matrix, int i1, int j1) {
		
		if (matrix.length % 8 != 0) {
			throw new IllegalStateException("size is not devidable by 8");
		}
		
		int size = matrix.length / 8;
		int[][][] result = new int[size][size][3];
		
		int ic = 0;
		for (int i = i1; i < i1 + size; i++) {
			int jc = 0;
			for (int j = j1; j < j1 + size; j++) {
				for (int k = 0; k < 3; k++) {
					result[ic][jc][0] = matrix[i][j][0];
					result[ic][jc][1] = matrix[i][j][1];
					result[ic][jc][2] = matrix[i][j][2];
				}
				jc++;
			}
			ic++;
		}
		
		return result;
	}
	
	
	public static List<int[][]> generateTranslations(int[][] matrix, int radius) {
		
		List<int[][]> result = new ArrayList<int[][]>();
		
		Set<Translation> translations = generateCirclePixels(radius);
		
		for (Translation translation : translations) {
			if (translation.x != 0 || translation.y != 0) {
				result.add(moveWithXY(matrix, translation.x, translation.y));
			}
		}
		
		return result;
	}
	
	
	public static final int[][] rotateMatrix(int[][] source, float angleInDegrees) {
		
		int[][] result = new int[source.length][source.length];
		
		double angle = (angleInDegrees * Math.PI / 180);
		int centerX = source.length / 2;
		int centerY = source.length / 2;
		
		for (int x = 0; x < source.length; x++) {
			for (int y = 0; y < source.length; y++) {
				int x1 = (int) (centerX + (x-centerX)*Math.cos(angle) - (y-centerY)*Math.sin(angle));
				int y1 = (int) (centerY + (x-centerX)*Math.sin(angle) + (y-centerY)*Math.cos(angle));
				if (x1 >= 0 && x1 < source.length && y1 >= 0 && y1 < source.length) {
					result[x1][y1] = source[x][y];
				}
			}
		}
		
		return result;
	}
	
	
	public static final PatternMatchingData matchImages(int[][] graySource, int[][] grayPattern) {
		
		PatternMatchingData result = null;
		
		VarStatistic stats = new VarStatistic(false);
		
		for (int x = 0; x <= graySource.length - grayPattern.length; x++ ) {
		    for (int y = 0; y <= graySource.length - grayPattern.length; y++ ) {
		        
		    	PatternMatchingData cur = new PatternMatchingData();
		    	cur.x = x;
		    	cur.y = y;
		    	cur.size = grayPattern.length;
		    	
		    	int count = 0;
		        for (int i = 0; i < grayPattern.length; i++ ) {
		            for (int j = 0; j < grayPattern.length; j++ ) {
		            	
		                int pixelSource = graySource[x+i][y+j];
		                int pixelPattern = grayPattern[i][j];
		                
		                cur.delta += Math.abs(pixelSource - pixelPattern) * Math.abs(pixelSource - pixelPattern);
		                count++;
		                
		                stats.addValue(Math.abs(pixelSource - pixelPattern), Math.abs(pixelSource - pixelPattern));
		                /*if (cur.delta > result.delta) {
		                	i = grayPattern.length;
		                	break;
		                }*/
		            }
		        }
		        cur.delta = cur.delta / (double) (count * count); 
		        
		        if (result == null || result.delta > cur.delta) { 
		        	result = cur;
		        	
		        	//printInfo(graySource, result, cur.size + "_" + cur.delta);
		        }
		    }
		}
		
		return result;
	}
	
	
	public static final PatternMatchingData matchImages(int[][][] rgbSource, int[][][] rgbPattern) {
		
		PatternMatchingData result = new PatternMatchingData();
		result.delta = Double.MAX_VALUE;
		result.size = rgbPattern.length;
		
		for (int x = 0; x <= rgbSource.length - rgbPattern.length; x++ ) {
		    for (int y = 0; y <= rgbSource.length - rgbPattern.length; y++ ) {
		        
		    	PatternMatchingData cur = new PatternMatchingData();
		    	cur.x = x;
		    	cur.y = y;
		    	cur.size = rgbPattern.length;
		    	
		    	int count = 0;
		        for (int i = 0; i < rgbPattern.length; i++ ) {
		            for (int j = 0; j < rgbPattern.length; j++ ) {
		            	
		                int pixelSource_r = rgbSource[x+i][y+j][0];
		                int pixelSource_g = rgbSource[x+i][y+j][1];
		                int pixelSource_b = rgbSource[x+i][y+j][2];
		                int pixelPattern_r = rgbPattern[i][j][0];
		                int pixelPattern_g = rgbPattern[i][j][1];
		                int pixelPattern_b = rgbPattern[i][j][2];
		                
		                cur.delta += Math.abs(pixelSource_r - pixelPattern_r);
		                cur.delta += Math.abs(pixelSource_g - pixelPattern_g);
		                cur.delta += Math.abs(pixelSource_b - pixelPattern_b);
		                
		                count++;
		                
		                /*if (cur.delta > result.delta) {
		                	i = rgbPattern.length;
		                	break;
		                }*/
		            }
		        }
		        //cur.delta = cur.delta / (double) (count * count); 
		        
		        if (result.delta > cur.delta) { 
		        	result.delta = cur.delta;
		        	result.x = x;
		        	result.y = y;
		        }
		    }
		}
		
		return result;
	}
	
	
	private static int[][] moveWithXY(int[][] matrix, int X, int Y) {
		
		if (X == 0 && Y == 0) {
			throw new IllegalStateException("X=" + X + ", Y=" + Y);
		}
		
		int[][] result = matrix;
		
		if (X > 0) {
			if (Y > 0) {
				result = moveRightWithN(result, X);
				result = moveDownWithN(result, Y);
			} else {
				result = moveRightWithN(result, X);
				result = moveUpWithN(result, Y);
			}
		} else {
			if (Y > 0) {
				result = moveLeftWithN(result, X);
				result = moveDownWithN(result, Y);
			} else {
				result = moveLeftWithN(result, X);
				result = moveUpWithN(result, Y);
			}
		}
		
		return result;
	}
	
	
	private static int[][] moveLeftWithN(int[][] matrix, int N) {
		int[][] result = matrix;
		for (int i = 0; i < N; i++) {
			result = moveLeftWith1(result);
		}
		return result;
	}
	
	
	private static int[][] moveRightWithN(int[][] matrix, int N) {
		int[][] result = matrix;
		for (int i = 0; i < N; i++) {
			result = moveRightWith1(result);
		}
		return result;
	}
	
	
	private static int[][] moveUpWithN(int[][] matrix, int N) {
		int[][] result = matrix;
		for (int i = 0; i < N; i++) {
			result = moveUpWith1(result);
		}
		return result;
	}
	
	
	private static int[][] moveDownWithN(int[][] matrix, int N) {
		int[][] result = matrix;
		for (int i = 0; i < N; i++) {
			result = moveDownWith1(result);
		}
		return result;
	}
	
	
	private static int[][] moveLeftWith1(int[][] matrix) {
		
		int[][] result = new int[matrix.length][matrix.length];
		
		for (int i = 0; i < matrix.length; i++) {
			int first = matrix[0][i];
			for(int j = 1;j < matrix.length; j++) {
				result[j - 1][i] = matrix[j][i];
			}
			result[matrix.length-1][i] = first;
		}
		
		return result;
	}
	
	
	private static int[][] moveRightWith1(int[][] matrix) {
		
		int[][] result = new int[matrix.length][matrix.length];
		
		for (int i = 0; i < matrix.length; i++) {
			int last = matrix[matrix.length - 1][i];
			for(int j = 0;j < matrix.length - 1; j++) {
				result[j + 1][i] = matrix[j][i];
			}
			result[0][i] = last;
		}
		
		return result;
	}
	
	
	private static int[][] moveUpWith1(int[][] matrix) {
		
		int[][] result = new int[matrix.length][matrix.length];
		
		for (int i = 0; i < matrix.length; i++) {
			int first = matrix[i][0];
			for(int j = 1;j < matrix.length; j++) {
				result[i][j-1] = matrix[i][j];
			}
			result[i][matrix.length-1] = first;
		}
		
		return result;
	}
	
	
	private static int[][] moveDownWith1(int[][] matrix) {
		
		int[][] result = new int[matrix.length][matrix.length];
		
		for (int i = 0; i < matrix.length; i++) {
			int last = matrix[i][matrix.length - 1];
			for(int j = 0;j < matrix.length - 1; j++) {
				result[i][j+1] = matrix[i][j];
			}
			result[i][0] = last;
		}
		
		return result;
	}
	
	
	private static Set<Translation> generateCirclePixels(int radius) {
		
		Set<Translation> result = new HashSet<Translation>();
		
	    double PI = 3.1415926535;
	    for (double angle = 0; angle < 360; angle++) {
	        double x = radius * Math.cos(angle * PI / 180);
	        double y = radius * Math.sin(angle * PI / 180);

	        result.add(new Translation((int)x, (int)y));
	    }
	    
	    return result;
	}
	
	
	private static class Translation {
		
		
		int x;
		int y;
		
		
		Translation(int _x, int _y) {
			x = _x;
			y = _y;
		}
		
		
	    @Override
	    public int hashCode() {
	        return x + y;
	    }
	    
	    
	    @Override
	    public boolean equals(final Object obj) {
	        
	    	if (this == obj)
	            return true;
	        
	        if (getClass() != obj.getClass())
	            return false;
	        
	        final Translation other = (Translation) obj;
	        if (x == other.x && y == other.y) {
	        	return true;
	        }
	        
	        return false;
	    }
	    
	    
	    @Override
	    public String toString(){  
	    	return "[" + x + ", " + y + "]";  
	    }
	}
	
	
	public static final class PatternMatchingData {
		public int x;
		public int y;
		public int size;
		public double delta;
		public int angle;
	}
	
	
	public static void main(String[] args) {
		System.out.println(generateCirclePixels(2));
	}
}