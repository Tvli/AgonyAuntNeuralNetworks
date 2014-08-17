import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.back.Backpropagation;

/** This class generates the best possible net for a given problem
 * 
 * @author Abigail Lowe
 * @author Teng Li
 *
 */
public class AutomaticTrainingHarness {

	// Maximum number of neurons to have
	final int MAX_NEURONS = 10;
	// Maximum learning rate
	final double MAX_LEARNING = 1.0;
	// Maximum momentum
	final double MAX_MOMENTUM = 1.0;
	// Amount to increase learning rate and momentum by
	final double STEP = 0.1;
	// Maximum epoch 
	final int MAX_EPOCH = 1000;
	// Variables for finding best net
	double best_error = 0.0;
	int best_neurons = 0;
	double best_learning = 0;
	double best_momentum = 0;

	/** Create a net with a given number of hidden neurons
	 * 
	 * @param neurons	Number of hidden neurons
	 * @return			New network
	 */
	public BasicNetwork createNet(int neurons) {
		BasicNetwork network = new BasicNetwork();
		// First layer
		network.addLayer(new BasicLayer(null, true, InterventionSlotsNet.getInputBits()));
		// Hidden layer, with [neurons] neurons
		network.addLayer(new BasicLayer(new ActivationSigmoid(), true, neurons));
		// Output later
		network.addLayer(new BasicLayer(new ActivationSigmoid(), false, InterventionSlotsNet.getOuputBits()));
		network.getStructure().finalizeStructure();
		network.reset();
		return network;
	}

	/** Train a network with given parameters
	 * 
	 * @param network			The net
	 * @param learning_rate		Learning rate
	 * @param momentum			Momentum
	 * @return					Error
	 */
	public double train(BasicNetwork network, double learning_rate,
			double momentum) {
		// Using BackProp
		final Backpropagation train = new Backpropagation(network,
				InterventionSlotsNet.getTrainingSet(), learning_rate, momentum);
		int epoch = 1;
		// Train
		do {
			train.iteration();
			epoch++;
		} while (epoch < MAX_EPOCH);
		return train.getError();
	}

	
	/** Find the best network */
	public BasicNetwork calibrate() {
		BasicNetwork bestNet = null;
		// Iterate through a number of neurons, increasing each time
		for (int i = 1; i < MAX_NEURONS; i++) {
			// Iterate through learning rates
			for (double j = 0.0; j < MAX_LEARNING; j = j + STEP) {
				// Iterate through momentums
				for (double k = 0.0; k < MAX_MOMENTUM; k = k + STEP) {
					// Create a net with i neurons
					BasicNetwork network = createNet(i);
					// Train the net, with given learning rate and momentum
					// Get its error
					double current_error = train(network, j, k);
//					System.out.println("\nNeurons: " + i + " Learning: " + j
//							+ " Momentum: " + k);
//					System.out.println("Best: " + best_error + " Current: "
//							+ current_error);
					// Check everything went as expected
					if (!Double.isNaN(best_error) && !Double.isNaN(current_error)) {
						// If it's the first run, or if the net has the best (lowest) error yet
						if ((i == 1 && j == 0 && k == 0) || current_error < best_error) {
							// Record info
							best_error = current_error;
							bestNet = network;
							best_neurons = i;
							best_learning = j;
							best_momentum = k;
						}
					}
				}
			}
		}
		System.out.println("\nDone!");
		System.out.println("Best neurons: "+bestNet.getLayerNeuronCount(1)+
				"\nBest learning rate: "+best_learning+
				"\nBest momentum: "+best_momentum);
		
		// Return the best ever network
		return bestNet;
	}
}
