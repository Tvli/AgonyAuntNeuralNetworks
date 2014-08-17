import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.encog.ml.data.MLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.csv.CSVFormat;
import org.encog.util.simple.TrainingSetUtil;


public class FirstLevelQuestionNet {
	public static final String METHOD_URL = "http://tl29.host.cs.st-andrews.ac.uk/AndroidApp/control_level_to_CSV.php";
	public static final String DATA_URL = "http://tl29.host.cs.st-andrews.ac.uk/AndroidApp/firstLevelQuestion.csv";
	
	// Number of bits representing input
	static int NUM_INPUT_BITS = 3;
	// Number of bits representing output 
	static int NUM_OUTPUT_BITS = 1;
	
	// Training data
	static MLDataSet trainingSet;
	static MLDataSet normalizedTrainingSet;
	
	static boolean ready_to_parse = false;
	static File neuralNetfile = new File("neuralNetFirstLevelQuestion.eg");
	
	
	public static void main(String[] args) {
		createTrainingSetOnServer();
////		
		getTrainingSetFromServer();
		getNet();
//		testoutput();
		
//		test();
		
	}
	
	public static void test(){
//		boolean[] slots = {false, false, false};
//		boolean b_temp = false;
//		for(boolean slot : slots){
//			b_temp|= slot;
//			
//		}
//		
//		
//
//		System.out.println(b_temp);
		
		Boolean aBoolean = false;
		System.out.println(aBoolean.toString());
		
		String a = "true";
		System.out.println(a=="true");
		
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
				fos = new FileOutputStream("firstLevelQuestion.csv");
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
		
		trainingSet = TrainingSetUtil.loadCSVTOMemory(CSVFormat.ENGLISH, "firstLevelQuestion.csv", true, NUM_INPUT_BITS, NUM_OUTPUT_BITS);
		
		// Retrieve net
		BasicNetwork network = ath.calibrate();
		writeFile(network);
	}
	
	
	public static void testoutput(){
		
		BasicNetwork net = (BasicNetwork) EncogDirectoryPersistence.loadObject(new File("neuralNetFirstLevelQuestion.eg"));
		double[] input =  {0.7, 2.72, 2.0};
		double[] input2 =  { 0.3, 3.2, 1.0};
		double[] input3 =  { 0.3, 3.7, 2.0};
		double[] input4 =  { 0.1, 4.8, 2.0};
		double[] input5 =  { 0.5, 1.549, 1.0};
		


	    
	    double[] output = new double[1];
	    double[] output2 = new double[1];
	    double[] output3 = new double[1];
	    double[] output4 = new double[1];
	    double[] output5 = new double[1];
	    
	    net.compute(input, output);
	    net.compute(input2, output2);
	    net.compute(input3, output3);
	    net.compute(input4, output4);
	    net.compute(input5, output5);
	    
	    System.out.println("The output question is: " + output[0] +" Hey " +  (int) Math.floor(output[0]*10 + 0.5));
	    System.out.println(output2[0]+" " + output3[0] +" " + output4[0] + " " + output5[0]);
	    System.out.println();
	    
	    double[] out = new double[1];
	    for(int i = 0; i<100; i++){
	    	double[] in =  {0.3, i, 2.0};
	    	net.compute(in, out);
	    	System.out.println(Math.floor(out[0]*10 + 0.5) + " " + out[0]);
	    }
	    
	    
	    

	    


	    
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
