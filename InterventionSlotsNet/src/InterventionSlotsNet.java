import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.encog.ml.data.MLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.csv.CSVFormat;
import org.encog.util.simple.TrainingSetUtil;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


public class InterventionSlotsNet {
	public static final String METHOD_URL = "http://tl29.host.cs.st-andrews.ac.uk/AndroidApp/intervention_slots_to_CSV.php";
	public static final String DATA_URL = "http://tl29.host.cs.st-andrews.ac.uk/AndroidApp/interventionSlots.csv";
	
	// Number of bits representing input
	static int NUM_INPUT_BITS = 3;
	// Number of bits representing output 
	static int NUM_OUTPUT_BITS = 6;
	
	// Training data
	static MLDataSet trainingSet;
	static MLDataSet normalizedTrainingSet;
	
	static boolean ready_to_parse = false;
	static File neuralNetfile = new File("neuralNetInterventionSlots.eg");
	
	
	public static void main(String[] args) {
//		createTrainingSetOnServer();
//		
//		getTrainingSetFromServer();
//		refineCSV();
//		
//		getNet();
		testoutput();
		

		
	}
	
	public static void createTrainingSetOnServer(){
		try {
			URL url = new URL(METHOD_URL);
			try {
				
				 BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
					        in.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	
	public static void getTrainingSetFromServer(){
		try {
			URL url = new URL(DATA_URL);
			ReadableByteChannel rbc = null;
			FileOutputStream fos = null;
			
			try {
				 rbc = Channels.newChannel(url.openStream());
				 
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				fos = new FileOutputStream("interventionSlots.csv");
				System.out.println("Created the csv file?");
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static void refineCSV(){
		
		
		try {
			CSVReader reader = new CSVReader(new FileReader("interventionSlots.csv"));
			CSVWriter writer = null;
			try {
				writer = new CSVWriter(new FileWriter("interventionSlots_double_value.csv"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			String [] nextLine;
			int loopCouter = 0;
			try {
				while ((nextLine = reader.readNext()) != null) {
					if (loopCouter > 0) {
						
//						Change the age value to double so net can recognize 
						String ageString = nextLine[0];
						nextLine[0] = "0."+ageString;
						
//						Change the gender value
						if (nextLine[1].equals("Male")) {
							nextLine[1] = "0.0";
						}else if (nextLine[1].equals("Female")) {
							nextLine[1] = "1.0";
						}
						
//						Change the occupation value
						if (nextLine[2].equals("Writer")) {
							nextLine[2] = "0.0";
						}else if (nextLine[2].equals("Student")) {
							nextLine[2] = "1.0";
						}else if (nextLine[2].equals("Freelancer")) {
							nextLine[2] = "2.0";
						}else if (nextLine[2].equals("Not hired")) {
							nextLine[2] = "3.0";
						}else {
							nextLine[2] = "4.0";
						}
						
						
	
						
					}
				

					writer.writeNext(nextLine);
				
					loopCouter++;
					
				}
				
				writer.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	public static void getNet() {
		AutomaticTrainingHarness ath = new AutomaticTrainingHarness();
		// Create training data
		
		trainingSet = TrainingSetUtil.loadCSVTOMemory(CSVFormat.ENGLISH, "interventionSlots_double_value.csv", true, NUM_INPUT_BITS, NUM_OUTPUT_BITS);
		
		// Retrieve net
		BasicNetwork network = ath.calibrate();
		writeFile(network);
	}
	
	
	public static void testoutput(){


		BasicNetwork net = (BasicNetwork) EncogDirectoryPersistence.loadObject(new File("neuralNetInterventionSlots.eg"));
		double[] input =  {0.3, 0.0, 0.0};
		double[] input2 =  { 0.13, 1.0, 1.0};
		double[] input3 =  { 0.2, 0.0, 2.0};
		double[] input4 =  { 0.45, 0.1, 3.0};
		
		


		double[] output = new double[6];
		double[] output2 = new double[6];
		double[] output3 = new double[6];
		double[] output4 = new double[6];
		

		net.compute(input, output);
		net.compute(input2, output2);
		net.compute(input3, output3);
		net.compute(input4, output4);
		
		for (int i = 0; i < output.length; i++) {
			System.out.print(Math.floor(output[i]+0.5) + " ");	
			
		}
		System.out.println();
		for (int i = 0; i < output.length; i++) {
			System.out.print(Math.floor(output2[i]+0.5) + " ");	
		}
		System.out.println();
		for (int i = 0; i < output.length; i++) {
			System.out.print(Math.floor(output3[i]+0.5) + " ");	
		}
		System.out.println();
		for (int i = 0; i < output.length; i++) {
			System.out.print(Math.floor(output4[i]+0.5) + " ");	
		}
		
		for (int i = 0; i < output4.length; i++) {
			output4[i] = Math.floor(output4[i]+0.5);
		}
		
		System.out.println();
		for (int i = 0; i < output.length; i++) {
			System.out.print(output4[i] + " ");	
		}
		
		
		
		
//		System.out.println(Math.floor(output2[0]+0.5)+" " +Math.floor( output3[0] + 0.5) +" " + output4[0] + " ");




	}
	
	
	
	/**  Write out adult net to file
	 * 
	 * @param network	Fully trained net
	 */
	public static void writeFile(BasicNetwork network) {
		EncogDirectoryPersistence.saveObject(neuralNetfile, network);
		ready_to_parse = true;
	}
	
	public static MLDataSet getTrainingSet() {
		return trainingSet;
	}
	
	public static int getInputBits() {
		return NUM_INPUT_BITS;
	}

	public static int getOuputBits() {
		return NUM_OUTPUT_BITS;
	}

}
