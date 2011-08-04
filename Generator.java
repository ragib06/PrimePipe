import java.io.*;

public class Generator extends Thread {
	
	private final int limit;					//the max limit of numbers
	private final int terminationMark = 0;		//termination mark to end pipe transfer
	private int number, primes;
	
	private PipedInputStream sievePIS;			//to read back from the sieve 
	private PipedOutputStream myPOS;			//to write to sieve
	private PipedOutputStream sievePOS;			//for the sieve to write back
	private DataOutputStream myDOS;				//to write to sieve
	private DataInputStream sieveDIS;			//to read back from the sieve

	public Generator(int n){
		this.myPOS = new PipedOutputStream();		//creating pipe to write to sieve
		this.myDOS = new DataOutputStream(myPOS);
		this.sievePOS = new PipedOutputStream();	//creating pipe for reading back from sieve
		this.limit = n;
		this.primes = 0;
		this.start();
	}
	
	//creates sieve and prepares the pipe to read back in from sieve 
	public void createSieve() throws IOException {
		this.sievePIS = new PipedInputStream(this.sievePOS);
		new Sieve(1, new PipedInputStream(myPOS), sievePOS);
	}
	
	//generates natural numbers(>1) upto the 'limit' and writes them to sieve
	public void generateNumbers() throws IOException{
		//System.out.println ("Generation Started ...");
		number = 2;
		while(number <= limit){
			//System.out.println ("next number: "+number);
			myDOS.writeInt(number);					//write to sieve							
			number++;
		}
	}
	
	//closes the streams that used to write to sieve
	public void closeMyStreams() throws IOException{
		this.myDOS.writeInt(terminationMark);		//send termination mark before closing
		this.myDOS.close();
		this.myPOS.close();
	}
	
	//closes the streams that used to read back in from sieve
	public void closeSieveStreams() throws IOException{
		this.sieveDIS.close();
		this.sievePIS.close();
	}
	
	//gets back the primes from sieve and displays the primes to the output
	public void getPrimes() throws IOException{
		sieveDIS = new DataInputStream(sievePIS);
		primes = sieveDIS.readInt();
		//System.out.println ("primes upto " + limit + " are: ");
		while(primes != terminationMark){
			System.out.println (primes);
			primes = sieveDIS.readInt();
		}
	}
	
	public void run(){
		try{
			createSieve();
			//System.out.println ("GEN: Sieve Created");
			generateNumbers();
			//System.out.println ("GEN: numbers generated");
			closeMyStreams();
			//System.out.println ("GEN: my-streams closed");
			getPrimes();
			closeSieveStreams();
			//System.out.println ("GEN: back-streams closed");
		}catch(Exception e){
			System.out.println ("ERROR in GEN: "+e);
		}
	}
	
	public static void main (String[] args) {
		try{
			new Generator(Integer.parseInt(args[0]));
			//new Generator(1000);
		}catch( ArrayIndexOutOfBoundsException aioobe ){
			System.out.println ("ERROR: argument missing!");
		}
		
	}
}