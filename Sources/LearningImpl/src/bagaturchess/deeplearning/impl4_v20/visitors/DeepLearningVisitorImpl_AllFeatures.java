/*
 *  BagaturChess (UCI chess engine and tools)
 *  Copyright (C) 2005 Krasimir I. Topchiyski (k_topchiyski@yahoo.com)
 *  
 *  Open Source project location: http://sourceforge.net/projects/bagaturchess/develop
 *  SVN repository https://bagaturchess.svn.sourceforge.net/svnroot/bagaturchess
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
 *  along with BagaturChess. If not, see <http://www.eclipse.org/legal/epl-v10.html/>.
 *
 */
package bagaturchess.deeplearning.impl4_v20.visitors;


import java.io.File;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;

import bagaturchess.bitboard.api.IBitBoard;
import bagaturchess.bitboard.api.IGameStatus;
import bagaturchess.deeplearning.api.NeuralNetworkUtils;
import bagaturchess.deeplearning.impl4_v20.NeuralNetworkUtils_AllFeatures;
import bagaturchess.learning.goldmiddle.impl4.filler.Bagatur_ALL_SignalFiller_InArray;
import bagaturchess.ucitracker.api.PositionsVisitor;


public class DeepLearningVisitorImpl_AllFeatures implements PositionsVisitor {
	
	
	private int iteration = 0;
	
	private int counter;
	
	private static final String NET_FILE = "net.bin";
	private MultiLayerPerceptron network;
	
	
	private double sumDiffs1;
	private double sumDiffs2;
	
	private long startTime;
	
	private Bagatur_ALL_SignalFiller_InArray filler;
	private double[] inputs;
	
	private DataSet trainingSet;
	
	private double prevNetworkError = Double.MAX_VALUE;
	
	
	public DeepLearningVisitorImpl_AllFeatures() throws Exception {
		
		if ((new File(NET_FILE)).exists() ){
			network = NeuralNetworkUtils.loadNetwork(NET_FILE);
		} else {
			network = NeuralNetworkUtils_AllFeatures.buildNetwork();
		}
        
        inputs = new double[NeuralNetworkUtils_AllFeatures.getInputsSize()];
        trainingSet = new DataSet(NeuralNetworkUtils_AllFeatures.getInputsSize(), 1);
	}
	
	
	@Override
	public void visitPosition(IBitBoard bitboard, IGameStatus status, int expectedWhitePlayerEval) {
		
		if (status != IGameStatus.NONE) {
			throw new IllegalStateException("status=" + status);
		}
		
		NeuralNetworkUtils.clearInputsArray(inputs);
		filler.fillSignals(inputs, 0);
		network.setInput(inputs);
		NeuralNetworkUtils.calculate(network);
		double actualWhitePlayerEval = NeuralNetworkUtils.getOutput(network);
		
		
		sumDiffs1 += Math.abs(0 - expectedWhitePlayerEval);
		sumDiffs2 += Math.abs(expectedWhitePlayerEval - actualWhitePlayerEval);
		
		
        trainingSet.addRow(new DataSetRow(createCopy(inputs), new double[]{expectedWhitePlayerEval}));
        
        
		counter++;
		if ((counter % 1000000) == 0) {
			
			System.out.println("Iteration " + iteration + ": Time " + (System.currentTimeMillis() - startTime) + "ms, " + "Success: " + (100 * (1 - (sumDiffs2 / sumDiffs1))) + "%");
			
			network.getLearningRule().doLearningEpoch(trainingSet);
			
			trainingSet.clear();
		}
	}
	
	
	public void begin(IBitBoard bitboard) throws Exception {
		
		filler = new Bagatur_ALL_SignalFiller_InArray(bitboard);
		
		startTime = System.currentTimeMillis();
		
		counter = 0;
		iteration++;
		sumDiffs1 = 0;
		sumDiffs2 = 0;
	}
	
	
	public void end() {
		
		//System.out.println("***************************************************************************************************");
		//System.out.println("End iteration " + iteration + ", Total evaluated positions count is " + counter);
		System.out.println("END Iteration " + iteration + ": Time " + (System.currentTimeMillis() - startTime) + "ms, " + "Success: " + (100 * (1 - (sumDiffs2 / sumDiffs1))) + "%, Error: " + network.getLearningRule().getTotalNetworkError());
		
		if (prevNetworkError < network.getLearningRule().getTotalNetworkError()) {
			System.exit(0);
		}
				
		network.save(NET_FILE);
		
		prevNetworkError = network.getLearningRule().getTotalNetworkError();
	}
	
	
	private double[] createCopy(double[] inputs) {
		double[] result = new double[inputs.length];
		for (int i = 0; i < inputs.length; i++) {
			result[i] = inputs[i];
		}
		return result;
	}
}
