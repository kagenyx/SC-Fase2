import java.util.ArrayList;

public class Block {
	private int max;
	private ArrayList<Transaction> transactions;
	private int hash; // provavelmente bytes
	private int blk_id;
	private String ass; // maybe not string
	
	public Block(int hash, int blk_id, int max) {
		this.hash = hash;
		this.blk_id = blk_id;
		transactions = new ArrayList<>();
		this.max = max;
	}
	
	public boolean addTransaction(Transaction t) {
		if (transactions.size() < max) {
			transactions.add(t);
			return true;
		} else {
			return false;
		}
		
	}
	
	// creates file after finish block
	public void blockFile() {
		//TODO	
	}
	
	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public ArrayList<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(ArrayList<Transaction> transactions) {
		this.transactions = transactions;
	}

	public int getHash() {
		return hash;
	}

	public void setHash(int hash) {
		this.hash = hash;
	}

	public int getBlk_id() {
		return blk_id;
	}

	public void setBlk_id(int blk_id) {
		this.blk_id = blk_id;
	}

	public String getAss() {
		return ass;
	}

	public void setAss(String ass) {
		this.ass = ass;
	}
	
}
