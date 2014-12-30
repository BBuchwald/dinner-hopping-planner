import java.io.*;

/**
 * Klasse zum einlesen von Nutzereingaben, Eingaben als String, Double, Flout oder Integer.
 * @author Björn Buchwald
 *
 */
public class Input{

	/**
	 * false, wenn ein Fehler beim Einlesen auftrat, true sonst.
	 */
	private static boolean fail;
	
	/**
	 * Private Konstruktor, da Statische Klasse.
	 */
	private Input(){}
	
	/*
	public static void main(String args[]){
	
		System.out.println("Input cast");
		System.out.print("String: ");
		String s = readString();
		System.out.println(s);
		System.out.print("Double: ");
		double d = readDouble();
		System.out.println(d);
	}
	*/
	
	/**
	 * @return boolen false falls Einlesen fehlgeschlagen, true sonst.
	 */
	public static boolean getFail(){
		
		return fail;
	}
	
	/**
	 * Einlesen einer Zeichenkette.
	 * @return String Nutzereingabe
	 */
	public static String readString(){
		
		fail = false;
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String s = in.readLine();
			return s;
		}
		catch(IOException e){
			fail = true;
		}
		return "";
	 }
	
	/**
	 * Einlesen eines Double-Wertes.
	 * @return double Nutzereingabe
	 */
	public static double readDouble(){
		
		fail = false;
		try{
			double d = Double.valueOf(readString()).doubleValue();
			return d;
		}
		catch(NumberFormatException e){
			
			fail = true;
		}
		return 0;
	}
	
	/**
	 * Einlesen eines Float-Wertes.
	 * @return float Nutzereingabe
	 */
	public static float readFloat(){
		
		fail = false;
		try{
			
			float f = Float.valueOf(readString()).floatValue();
			return f;
		}
		catch(NumberFormatException e){
			
			fail = true;
		}
		return 0;
	}
	
	/**
	 * Einlesen eines Integer-Wertes.
	 * @return int Nutzereingabe
	 */
	public static int readInt()
	{
		fail = false;
		try
		{
			int i = Integer.valueOf(readString()).intValue();
			return i;
		}
		catch(NumberFormatException e){
			
			fail = true;
		}
		return 0;
	}
	
	/**
	 * Konvertieren von String zu Integer.
	 * @param s String
	 * @return int
	 */
	public static int StrToInt(String s){
		
		fail = false;
		try{
			
			int i = Integer.valueOf(s).intValue();
			return i;
		}
		catch(NumberFormatException e){
			
			fail = true;
		}
		return 0;
	}
	
	/**
	 * Konvertieren von String zu float.
	 * @param s String
	 * @return float
	 */
	public static float StrToFloat(String s){
		
		fail = false;
		try{
			
			float f = Float.valueOf(s).floatValue();
			return f;
		}
		catch(NumberFormatException e){
			
			fail = true;
		}
		return 0;
	}
	
	/**
	 * Konvertieren von String zu double.
	 * @param s String
	 * @return double
	 */
	public static double StrToDouble(String s){
		
		fail = false;
		try{
			
			double i = Double.valueOf(s).doubleValue();
			return i;
		}
		catch(NumberFormatException e){
			
			fail = true;
		}
		return 0;
	}

}


