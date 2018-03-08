package at.tuwien.ict.acona.evolutiondemo.brokeragent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Depot {
	private String owner = "";
	private String ownerType = "";
	private double liquid = 0;
	private final List<Asset> asset = new ArrayList<Asset>();

	public double getLiquid() {
		return liquid;
	}

	public void setLiquid(double liquid) {
		this.liquid = liquid;
	}

	public void addLiquid(double amount) throws Exception {
		// if (amount >= 0) {
		this.liquid += amount;
		// } else {
		// throw new Exception("Cannot add a negative amount");
		// }

	}

	public double removeLiquid(double amount) {
		double result = 0;

		if (this.liquid < amount) {
			result = this.liquid;
			this.liquid = 0;
		} else {
			result = amount;
			this.liquid -= amount;
		}

		return result;
	}

	public void sell(String stockName, int amount, double price) throws Exception {

		Optional<Asset> asset = this.asset.stream().filter(name -> name.getStockName().equals(stockName)).findFirst();

		if (asset.isPresent() == false) {
			throw new Exception("Cannot sell a stock=" + stockName + " because name does not exist");
		}

		if (asset.get().getVolume() < amount) {
			throw new Exception("Cannot sell more stocks that available. Available=" + asset.get().getVolume() + ". Amount to sell=" + amount);
		}

		asset.get().setVolume(asset.get().getVolume() - amount);
		this.addLiquid(amount * price);

		if (asset.get().getVolume() == 0) {
			this.getAssets().remove(asset.get());
		}
	}

	public void buy(String stockName, int amount, double price) throws Exception {
		Optional<Asset> asset = this.asset.stream().filter(name -> name.getStockName().equals(stockName)).findFirst();

		if (this.getLiquid() < amount * price) {
			throw new Exception("Cannot buy. Not anough money on account. Available=" + this.getLiquid() + ". Required=" + amount * price);
		}

		if (asset.isPresent()) {
			int oldVolume = asset.get().getVolume();
			double oldPrice = asset.get().getAveragePrice();

			int newVolume = amount + oldVolume;
			double newAveragePrice = ((oldPrice * oldVolume) + (amount * price)) / (amount + oldVolume);

			asset.get().setAveragePrice(newAveragePrice);
			asset.get().setVolume(newVolume);
		} else {
			this.getAssets().add(new Asset(stockName, amount, price));
		}

		this.addLiquid(-amount * price);
	}

	public List<Asset> getAssets() {
		return asset;
	}

	public double getTotalValue() {
		double result = this.getLiquid();
		for (Asset a : this.asset) {
			result += a.getAveragePrice() * a.getVolume();
		}

		return result;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Depot [owner=");
		builder.append(owner);
		builder.append(", ownerType=");
		builder.append(ownerType);
		builder.append(", liquid=");
		builder.append(liquid);
		builder.append(", asset=");
		builder.append(asset);
		builder.append("]");
		return builder.toString();
	}
}
