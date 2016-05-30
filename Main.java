import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main {


	public static List<String> charger_dictionnaire(FileInputStream fStream){
		List<String> list = new ArrayList<String>();

		try {

			DataInputStream in = new DataInputStream(fStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null)   {
				if(line.length() >= 3){
					list.add(line.toUpperCase());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
	}




	
	public static void main(String ... args) throws FileNotFoundException{
		FileInputStream d = new FileInputStream("dictionnaire1000en.txt");
		List<String> dico = charger_dictionnaire(d);

		String baseAppPath = "baseapp";
		
		if(args.length == 3){
			String baseTestPath = args[0];
			ClassifBayes classif = new ClassifBayes(dico);
			classif.learning(baseAppPath);
			classif.test(baseTestPath, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		}
		else{
			
		}
	}
	
	


}
