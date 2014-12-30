/**
 * Hilfsklassen zum übersetzen von Integer Zahlen in einen Buchstaben oder umgekehrt.
 * Verwendet für Kartendarstellungen. Wurde für Google Static Map Service benötigt,da
 * hier nur Buchstaben (keine Zahlen) als Label für Kartenmarker möglich sind.
 * (nicht mehr benötigt)
 * @author Björn Buchwald
 *
 */
public class Letter{
	
	//für max. 52 Orte (für mehr Teams lösche entsprechende Methoden und nutze Zahlen statt Buchstaben)
	/**
	 * Alphabet Großbuchstaben gefolgt von Kleinbuchstaben.
	 */
	private static final char[] alphabet = { 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z' };
	
	/**
	 * Großbuchstaben + Zahlen
	 */
	private static final char[] alphabetPlusNumeric = { 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9'};
	
	/**
	 * Übersetzen einer Zahl in einen Buchstaben. 
	 * @param number Index des alphabet arrays.
	 * @return Buchstabe als Character
	 */
	public static char getLetterByNumber(int number){
		if( number > 51 || number < 0 ) return '?';
		else return alphabet[number];
		
	}
	
	/**
	 * Übersetzen einer Zahl in Buchstaben oder Zahl. 
	 * @param number Index des alphabetPlusNumeric arrays.
	 * @return Buchstabe oder Zahl als Character
	 */
	public static char getLetterOrNumericByNumber(int number){
		if( number > 35 || number < 0 ) return '?';
		else return alphabetPlusNumeric[number]; 
	}
	
	/**
	 * Index des übergebenen Buchstabens nach alphabet array.
	 * @param letter
	 * @return Index
	 */
	public static int getNumberByLetter(char letter){
		
		int count = 0; 
		for(char let : alphabet){
			if(let == letter){
				return count;
			}
			count++;
		}
		return -1;
	}
	
	/**
	 * Index des übergebenen Buchstabens nach alphabetPlusNumeric arrays.
	 * @param letter
	 * @return Index
	 */
	public static int getNumberByLetterOrNumeric(String letter){
		
		if(letter.length() != 1) return -1;
		int count = 0; 
		for(char let : alphabetPlusNumeric){
			if(let == letter.charAt(0)){
				return count;
			}
			count++;
		}
		return -1;
	}
	
}