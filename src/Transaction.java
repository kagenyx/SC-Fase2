
public class Transaction {
	private int id;
	private int unitsSold;
	private int pricePerUnit;
	private int ownerID;
	
	public Transaction(int id, int unitsSold, int pricePerUnit, int ownerID) {
		this.id = id;
		this.unitsSold = unitsSold;
		this.pricePerUnit = pricePerUnit;
		this.ownerID = ownerID;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getUnitsSold() {
		return unitsSold;
	}
	
	public void setUnitsSold(int unitsSold) {
		this.unitsSold = unitsSold;
	}
	
	public int getPricePerUnit() {
		return pricePerUnit;
	}
	
	public void setPricePerUnit(int pricePerUnit) {
		this.pricePerUnit = pricePerUnit;
	}
	
	public int getOwnerID() {
		return ownerID;
	}
	
	public void setOwnerID(int ownerID) {
		this.ownerID = ownerID;
	}
	
	public String toString() {
		return "Transação: {\n"
				+ "           id:"+this.id
				+ "           numero de unidades:"+this.unitsSold
				+ "           preço de cada unidade:"+this.pricePerUnit
				+ "           dono:"+this.ownerID;
	}
}
