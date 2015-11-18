package ReviewsCrawler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Model.Review;
import Util.PostgreSQLConnector;

public class Crawler {
	private AndroidMarketCrawler AMcrawler = null;
	private static final String LOGIN = "ususeal@gmail.com";
	private static final String PASSWORD = "phdcs2014";
	// private static final String LOGIN = "rio.app.test1@gmail.com";
	// private static final String PASSWORD = "abc13579";

	// private static final String ANDROID = "3FA8A9EFF6CA06E0";
	private static final String ANDROID = "32F52476388F20DE";
	// private static final String ANDROID = "dead000beef";
	public static final int ANDROID_MARKET = 0;;
	public static final int GOOGLE_PLAY = 1;

	public Crawler(int option) {
		if (option == ANDROID_MARKET) {
			// run Android Market Crawler
			AMcrawler = new AndroidMarketCrawler(LOGIN, PASSWORD, ANDROID);
		} else {

		}
	}

	private HashMap<Integer, String> getAppId(int option) {
		HashMap<Integer, String> appIDs = new HashMap<>();
		PostgreSQLConnector db = null;
		try {
			db = new PostgreSQLConnector(PostgreSQLConnector.DBLOGIN,
					PostgreSQLConnector.DBPASSWORD,
					PostgreSQLConnector.REVIEWDB);

			String fields[] = { "ID", "name", "gplay", "amarket" };
			ResultSet results = db.select(PostgreSQLConnector.APPID_TABLE,
					fields, null);
			while (results.next()) {
				if (option == ANDROID_MARKET) {
					if (!results.getBoolean("armarket"))
						appIDs.put(results.getInt("ID"),
								results.getString("name"));
				} else {
					if (!results.getBoolean("gplay"))
						appIDs.put(results.getInt("ID"),
								results.getString("name"));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		return appIDs;
	}

	public void extractReviews() {
		List<Review> reviewList = new ArrayList<>();
		if (AMcrawler != null) {
			System.out
					.println(">>Start extracting reviews from Android Market.");
			HashMap<Integer, String> appIDs = getAppId(0);
			for (int key : appIDs.keySet()) {
				reviewList = AMcrawler.getReviewsForApp(appIDs.get(key));
				int count = writeReviewsIntoDB(reviewList, key, ANDROID_MARKET);
				System.out.println("...extracted " + count + " reviews from "
						+ appIDs.get(key));
			}
		} else {

		}
	}

	private int writeReviewsIntoDB(List<Review> reviewList, int key, int option) {
		PostgreSQLConnector db = null;
		int count = 0;
		try {
			db = new PostgreSQLConnector(PostgreSQLConnector.DBLOGIN,
					PostgreSQLConnector.DBPASSWORD,
					PostgreSQLConnector.REVIEWDB);
			String fields[] = { "reviewID" };
			String condition = "appid=" + key;
			ResultSet results = db.select(PostgreSQLConnector.REVIEWS_TABLE,
					fields, condition);
			while (results.next()) {
				for (Review rev : reviewList) {
					if (rev.getReviewId().equalsIgnoreCase(
							results.getString("reviewID"))) {
						rev.setDuplicate(true);
					}
				}
			}
			String values[] = new String[8];
			for (Review rev : reviewList) {
				if (rev.isDuplicate() == false) {
					try {
						values[0] = String.valueOf(key); // appid
						values[1] = rev.getTitle();
						values[2] = rev.getText();
						values[3] = String.valueOf(rev.getRating());
						values[4] = String.valueOf(rev.getCreationTime());
						values[5] = rev.getDocument_version() + "_";
						values[6] = rev.getReviewId();
						values[7] = rev.getDevice_name();
						db.insert(PostgreSQLConnector.REVIEWS_TABLE, values);
						count++;
					} catch (SQLException e) {
						e.printStackTrace();
						System.err.println("UPDATE_STATE: Only extracted "
								+ count + " reviews so far");
					}
				}
			}
			condition = "ID=" + key;
			if (option == ANDROID_MARKET)
				db.update(PostgreSQLConnector.APPID_TABLE, "amarket=TRUE",
						condition);
			else
				db.update(PostgreSQLConnector.APPID_TABLE, "gplay=TRUE",
						condition);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("UPDATE_STATE: Only extracted " + count
					+ " reviews so far");
		} finally {
			if (db != null)
				db.close();
		}
		return count;
	}
}
