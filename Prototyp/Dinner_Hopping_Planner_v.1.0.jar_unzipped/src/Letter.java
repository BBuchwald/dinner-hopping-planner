/**
 * Hilfsklassen zum �bersetzen von Integer Zahlen in einen Buchstaben oder umgekehrt.
 * Verwendet f�r Kartendarstellungen. Wurde f�r Google Static Map Service ben�tigt,da
 * hier nur Buchstaben (keine Zahlen) als Label f�r Kartenmarker m�glich sind.
 * (nicht mehr ben�tigt)
 * @author Bj�rn Buchwald
 *
 */
public class Letter{
	
	//f�r max. 52 Orte (f�r mehr Teams l�sche entsprechende Methoden und nutze Zahlen statt Buchstaben)
	/**
	 * Alphabet Gro�buchstaben gefolgt von Kleinbuchstaben.
	 */
	private static final char[] alphabet = { 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z' };
	
	/**
	 * Gro�buchstaben + Zahlen
	 */
	private static final char[] alphabetPlusNumeric = { 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9'};
	
	/**
	 * �bersetzen einer Zahl in einen Buchstaben. 
	 * @param number Index des alphabet arrays.
	 * @return Buchstabe als Character
	 */
	public static char getLetterByNumber(int number){
		if( number > 51 || number < 0 ) return '?';
		else return alphabet[number];
		
	}
	
	/**
	 * �bersetzen einer Zahl in Buchstaben oder Zahl. 
	 * @param number Index des alphabetPlusNumeric arrays.
	 * @return Buchstabe oder Zahl als Character
	 */
	public static char getLetterOrNumericByNumber(int number){
		if( number > 35 || number < 0 ) return '?';
		else return alphabetPlusNumeric[number]; 
	}
	
	/**
	 * Index des �bergebenen Buchstabens nach alphabet array.
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
	 * Index des �bergebenen Buchstabens nach alphabetPlusNumeric arrays.
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