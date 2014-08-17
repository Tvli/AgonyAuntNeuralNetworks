import java.awt.List;
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
import java.util.ArrayList;
import java.util.Arrays;

import org.encog.ConsoleStatusReportable;
import org.encog.Encog;
import org.encog.app.analyst.AnalystFileFormat;
import org.encog.app.analyst.EncogAnalyst;
import org.encog.app.analyst.csv.normalize.AnalystNormalizeCSV;
import org.encog.app.analyst.script.normalize.AnalystField;
import org.encog.app.analyst.wizard.AnalystWizard;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.csv.CSVFormat;
import org.encog.util.normalize.DataNormalization;
import org.encog.util.normalize.input.InputField;
import org.encog.util.normalize.input.InputFieldCSV;
import org.encog.util.normalize.input.InputFieldCSVText;
import org.encog.util.normalize.output.OutputFieldRangeMapped;
import org.encog.util.normalize.output.nominal.OutputOneOf;
import org.encog.util.normalize.target.NormalizationStorageCSV;
import org.encog.util.simple.TrainingSetUtil;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;



public class InterventionNet {
	public static final String METHOD_URL = "http://tl29.host.cs.st-andrews.ac.uk/AndroidApp/intervention_frequency_to_CSV.php";
	public static final String DATA_URL = "http://tl29.host.cs.st-andrews.ac.uk/AndroidApp/intervention.csv";
	
	// Number of bits representing input
	static int NUM_INPUT_BITS = 3;
	// Number of bits representing output 
	static int NUM_OUTPUT_BITS = 1;
	
	// Training data
	static MLDataSet trainingSet;
	static MLDataSet normalizedTrainingSet;

	
	
	// Whether the net can be parsed for sending to the app
	static boolean ready_to_parse = false;
	static File neuralNetfile = new File("neuralNetIntervention.eg");
	
	
	
	public static void main(String[] args) {
		createTrainingSetOnServer();
		
		getTrainingSetFromServer();
		refineCSV();
		getNet();
//		testoutput();
		
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
				fos = new FileOutputStream("intervention.csv");
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
	
	public static void getNet() {
		AutomaticTrainingHarness ath = new AutomaticTrainingHarness();
		// Create training data
		
		trainingSet = TrainingSetUtil.loadCSVTOMemory(CSVFormat.ENGLISH, "intervention_double_value.csv", true, NUM_INPUT_BITS, NUM_OUTPUT_BITS);
		
		// Retrieve net
		BasicNetwork network = ath.calibrate();
		writeFile(network);
	}
	
	

	public static void refineCSV(){
		
		
		try {
			CSVReader reader = new CSVReader(new FileReader("intervention.csv"));
			CSVWriter writer = null;
			try {
				writer = new CSVWriter(new FileWriter("intervention_double_value.csv"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			String [] nextLine;
			int loopCouter = 0;
			try {
				while ((nextLine = reader.readNext()) != null) {
					if (loopCouter > 0) {
						System.out.println(nextLine[0]+nextLine[1]+nextLine[2]+nextLine[3]);
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
						
						nextLine[3] = "0." + nextLine[3];
						
						System.out.println(ageString);	
						System.out.println(nextLine[0]);	
						
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
	
//	public static void normalizeData(){
//		File sourceFile = new File("intervention_double_value.csv");
//		File targetFile = new File("normalized_intervention.csv");
//
//		EncogAnalyst analyst = new EncogAnalyst();
//		AnalystWizard wizard = new AnalystWizard(analyst);
//		wizard.setTargetField("frequency");
//		wizard.wizard(sourceFile, true, AnalystFileFormat.DECPNT_COMMA);
//		
//		
//
//		dumpFieldInfo(analyst);
//
//		final AnalystNormalizeCSV norm = new AnalystNormalizeCSV();
//		
//		norm.analyze(sourceFile, true, CSVFormat.ENGLISH, analyst);
//		norm.setProduceOutputHeaders(true);
//		
//		norm.normalize(targetFile);
//		Encog.getInstance().shutdown();
//	}
//	
//	
//	public static void dumpFieldInfo(EncogAnalyst analyst) {
//		System.out.println("Fields found in file:");
//		for (AnalystField field : analyst.getScript().getNormalize()
//				.getNormalizedFields()) {
//
//			StringBuilder line = new StringBuilder();
//			line.append(field.getName());
//			line.append(",action=");
//			line.append(field.getAction());
//			line.append(",min=");
//			line.append(field.getActualLow());
//			line.append(",max=");
//			line.append(field.getActualHigh());
//			System.out.println(line.toString());
//		}
//	}
//	
//	
	public static void testoutput(){
		BasicNetwork net = (BasicNetwork) EncogDirectoryPersistence.loadObject(new File("NeuralNetIntervention.eg"));
		double[] input =  {0.10, 0.0, 1.0};
		double[] input2 =  { 0.1};
		

	    
	    double[] output = new double[1];
	    
	    net.compute(input, output);
	    
	    System.out.println("The output Frequency is: " + output[0] +" Hey " +  (int) Math.floor(output[0]*10 + 0.5));
	    double[] in = {Double.parseDouble("0." + "20"), Double.parseDouble("0." + "20")};
	    System.out.println(Double.parseDouble("0." + "20") + " in[0]->" + in[0] + " in[1]->" + in[1]);
	    
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


