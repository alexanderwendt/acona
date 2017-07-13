package at.tuwien.ict.acona.evolutiondemo.brokeragent;

public class Asset {
	
	private double volume;
	private String stockName;
	private double averagePrice;
	
	public Asset(String stockName, double volume, double averagePrice) {
		super();
		this.volume = volume;
		this.stockName = stockName;
		this.averagePrice = averagePrice;
	}
	
	public double getVolume() {
		return volume;
	}
	public void setVolume(double volume) {
		this.volume = volume;
	}
	public String getStockName() {
		return stockName;
	}
	public void setStockName(String stockName) {
		this.stockName = stockName;
	}
	public double getAveragePrice() {
		return averagePrice;
	}
	public void setAveragePrice(double averagePrice) {
		this.averagePrice = averagePrice;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Asset [volume=");
		builder.append(volume);
		builder.append(", stockName=");
		builder.append(stockName);
		builder.append(", averagePrice=");
		builder.append(averagePrice);
		builder.append("]");
		return builder.toString();
	}
	
	
}
