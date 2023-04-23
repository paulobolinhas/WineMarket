package enums;

public enum TransactionType {
	SELL("Sell"),
    BUY("Buy");

    private final String label;

    private TransactionType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
