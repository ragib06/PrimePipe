import java.io.*;

public class Sieve extends Thread {
	
	private final int terminationMark = 0;
	private int prime, number;
	private int sieveID;						//only for debugging purpose
	private Boolean gotPrime, sieveCreated;		//flags
	
	private InputStream parentIS;				//to read supplied numbers from parent
	private DataInputStream parentDIS;				
	private PipedInputStream childPIS;			//to read back primes from descendant
	private DataInputStream childDIS;			
	private PipedOutputStream parentPOS;		//to write back the primes to parent
	private DataOutputStream parentDOS;		
	private PipedOutputStream childPOS;			//to write the filtered numbers to descendant
	private DataOutputStream childDOS;			
	private PipedOutputStream childPOSBack;		//for descendant to write back the primes
	
	
	public Sieve(int id, InputStream is, PipedOutputStream pos){
		this.sieveID = id;		//ONLY FOR DEBUGGING PURPOSE
		this.parentIS = is;
		this.parentPOS = pos;		//pipe to write back to parent
		this.parentDOS = new DataOutputStream(parentPOS);	
		this.gotPrime = false;
		this.sieveCreated = false;
		//System.out.println ("Sieve"+sieveID+" created ...");
		this.start();
	}
	
	//creates descendant thread and prepares in & out pipes for data transfer
	public void createDescendant() throws IOException {
		childPOS = new PipedOutputStream();					//pipe to write to descendant
		childDOS = new DataOutputStream(childPOS);
		childPOSBack = new PipedOutputStream();				//pipe for descendant to write back
		childPIS = new PipedInputStream(childPOSBack);
		new Sieve(sieveID+1, new PipedInputStream(childPOS), childPOSBack);
		sieveCreated = true;
	}
	
	//reads numbers from the parent and filters them.
	//finally sends the filtered numbers to the descendant
	public void filterNumbers() throws IOException{
		parentDIS = new DataInputStream(parentIS);		//to receive numbers
		
		number = parentDIS.readInt();					//get the next number
		while(number != terminationMark){				//until termination 
			//System.out.println ("Sieve"+sieveID+" got: "+number);
			if(!gotPrime){
				prime = number;							//store the prime (once)
				gotPrime = true;
			}
			if(number%prime != 0){						//check if not number divisible by 'prime' 
				if(!sieveCreated)
					createDescendant();					//create descendant
				
				childDOS.writeInt(number);				//write the number to the descendant
			}
			number = parentDIS.readInt();				//get the next number
		}
	}
	
	//passes the primes received from descendant to parent
	public void passPrimes() throws IOException{
		childDIS = new DataInputStream(childPIS);	//to read primes back from descendant
		
		prime = childDIS.readInt();					//get the next prime
		while(prime != terminationMark){
			parentDOS.writeInt(prime);				//write the prime back to the parent
			prime = childDIS.readInt();				//get the next prime
		}
	}
	
	//closes the input streams from parent
	public void closeParentInStreams() throws  IOException{
		parentDIS.close();
		parentIS.close();
	}
	
	//closes the output streams to parent
	public void closeParentOutStreams() throws  IOException{
		parentDOS.writeInt(terminationMark);	//send termination mark before closing
		parentDOS.close();
		parentPOS.close();
	}
	
	//closes the input streams to child
	public void closeChildInStreams() throws  IOException{
		childDIS.close();	
		childPIS.close();
	}
	
	//closes the output streams to child
	public void closeChildOutStreams() throws  IOException{
		childDOS.writeInt(terminationMark);		//send termination mark before closing
		childDOS.close();
		childPOS.close();
	}
	
	public void run(){
		try{
			filterNumbers();
			//System.out.println ("SIEVE: Numbers Filtered");					
			closeParentInStreams();				//read from parent finished
			//System.out.println ("SIEVE: Parent Input Streams Closed");
			
			if(gotPrime){
				parentDOS.writeInt(prime);		//write the stored 'prime' to parent
			}
			if(sieveCreated){
				closeChildOutStreams();			
				//System.out.println ("SIEVE: Child Output Streams Closed");
				passPrimes();					
				//System.out.println ("SIEVE: Primes Passed to Parent");
				closeChildInStreams();			
				//System.out.println ("SIEVE: Child Input Streams Closed");
			}
			closeParentOutStreams();			
			//System.out.println ("SIEVE: Parent Output Streams Closed");
			
		}catch(EOFException eof){
			System.out.println ("Sieve: EOF!");
		}catch(Exception e){
			System.out.println ("ERROR in SIEVE: "+e);
		}
	}
}