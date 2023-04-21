package domain;

public class Wine {

	private String id;
	private String image;
	private int classificationSum;
	private int classificationCount;
	private String avgClassification;

	public Wine(String id, String image, int classificationSum, int classificationCount) {
		this.id = id;
		this.image = image;
		this.classificationSum = classificationSum;
		this.classificationCount = classificationCount;
		this.avgClassification = initClassification(classificationSum, classificationCount);
	}

	public String getID() {
		return this.id;
	}

	public String getImage() {
		return image;
	}

	private String initClassification(int classificationSum, int classificationCount) {
		if (classificationCount != 0)
			return String.format("%.2f", Float.parseFloat(String.valueOf(this.classificationSum))
					/ Float.parseFloat(String.valueOf(this.classificationCount)));

		return "0";
	}

	public void updateClassification(int stars) {
		this.classificationSum += stars;
		this.classificationCount++;

		if (classificationCount != 0)
			this.setAvgClassification(String.format("%.2f", Float.parseFloat(String.valueOf(this.classificationSum))
					/ Float.parseFloat(String.valueOf(this.classificationCount))));
		else
			this.setAvgClassification("0");
	}

	public String getAvgClassification() {
		return avgClassification;
	}

	public void setAvgClassification(String avgClassification) {
		this.avgClassification = avgClassification;
	}

}
