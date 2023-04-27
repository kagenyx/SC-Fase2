import java.util.ArrayList;



public class Blockchain {
	private static final int MAX = 5;
	private ArrayList<Block> blocks;
	
	public Blockchain() {
		this.blocks = new ArrayList<>();
		blocks.add(new Block(0,0,MAX));
	}
	
	public void createBlock() {
		blocks.add(new Block(
				blocks.get(blocks.size()-1).hashCode(),
				blocks.size(),
				MAX));
	}
	
	public void newTransaction(Transaction t) {
		//coisas a acontecer q criam o transaction ou ja vem criado como parametro
		
		if (blocks.get(blocks.size()-1).addTransaction(t)) {
			//assinar block
		} else {
			createBlock();
			blocks.get(blocks.size()-1).addTransaction(t);
		}
		
	}
	
}
