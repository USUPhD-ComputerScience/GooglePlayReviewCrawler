package ReviewsCrawler;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.ReverbType;

import Model.Review;
import Model.Review.ReviewBuilder;

import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.MarketSession.Callback;
import com.gc.android.market.api.model.Market.App;
import com.gc.android.market.api.model.Market.AppsRequest;
import com.gc.android.market.api.model.Market.AppsResponse;
import com.gc.android.market.api.model.Market.CommentsRequest;
import com.gc.android.market.api.model.Market.CommentsResponse;
import com.gc.android.market.api.model.Market.ResponseContext;

public class AndroidMarketCrawler {

	private MarketSession marketSession;
	private String currentAppID;
	private boolean converged;
	private int index;

	AndroidMarketCrawler(String login, String password, String androidID) {
		converged = false;
		marketSession = new MarketSession();
		marketSession.login(login, password);
		marketSession.getContext().setAndroidId(androidID);
	}

	public MarketSession getMarketSession() {
		return marketSession;
	}

	public void setMarketSession(MarketSession marketSession) {
		this.marketSession = marketSession;
	}

	public List<Review> getReviewsForApp(String appID) {
		this.index = 0;
		this.converged = false;
		final List<Review> reviewList = new ArrayList<>();
		CommentsRequest commentsRequest;
		while (!converged) {
			try {
				commentsRequest = CommentsRequest.newBuilder().setAppId(appID)
						.setStartIndex(index).setEntriesCount(10).build();

				marketSession.append(commentsRequest,
						new Callback<CommentsResponse>() {
							@Override
							public void onResult(ResponseContext context,
									CommentsResponse response) {
								// System.out.println("Response : " + response);
								System.out
										.println(AndroidMarketCrawler.this.index);
								if (response == null) {
									AndroidMarketCrawler.this.converged = true;
								} else {
									for (int i = 0; i < response
											.getCommentsCount(); i++) {
										Review.ReviewBuilder reviewBuilder = new ReviewBuilder();
										reviewBuilder.creationTime(response
												.getComments(i)
												.getCreationTime());
										reviewBuilder.text(response
												.getComments(i).getText());
										reviewBuilder.rating(response
												.getComments(i).getRating());
										reviewBuilder.reviewId(response
												.getComments(i).getAuthorId()
												.substring(4)); // remove "cid-"
										reviewList.add(reviewBuilder
												.createReview());
									}
									AndroidMarketCrawler.this.index += 10;
								}
							}
						});
				marketSession.flush();
				try {
					// thread to sleep for the specified number of milliseconds
					Thread.sleep(3000);
				} catch (java.lang.InterruptedException ie) {
					ie.printStackTrace();
				}
			} catch (Exception e) {
				try {
					// thread to sleep for the specified number of milliseconds
					Thread.sleep(3000);
				} catch (java.lang.InterruptedException ie) {
					ie.printStackTrace();
				}
				continue;
			}
		}
		System.out.println(reviewList.size());
		return reviewList;
	}

	// private String text;
	// private int rating;
	// private String authorName;
	// private long creationTime;
	// private String authorId;

	// public void saveCommentsToCSV(String appID, ArrayList<Comment>
	// commentList) {
	// StringBuilder stringBuilder = new StringBuilder();
	// stringBuilder.append(appID);
	// Timestamp timestamp;
	// stringBuilder.append(".csv");
	// try {
	// CSVWriter csvWriter = new CSVWriter(new FileWriter(
	// stringBuilder.toString()));
	// String[] header = "text rating authorName creationTime authorID"
	// .split(" ");
	// csvWriter.writeNext(header);
	// for (Comment aComment : commentList) {
	// header[0] = aComment.getText();
	// header[1] = String.valueOf(aComment.getRating());
	// header[2] = aComment.getAuthorName();
	// timestamp = new Timestamp(aComment.getCreationTime());
	// header[3] = timestamp.toString();
	// header[4] = aComment.getAuthorId();
	// csvWriter.writeNext(header);
	// }
	// csvWriter.close();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	public String getAppIdFromName(String name) {
		AppsRequest appsRequest = AppsRequest.newBuilder().setQuery(name)
				.setStartIndex(0).setEntriesCount(10).setWithExtendedInfo(true)
				.build();
		marketSession.append(appsRequest, new Callback<AppsResponse>() {
			@Override
			public void onResult(ResponseContext context, AppsResponse response) {
				App app = response.getApp(0);
				AndroidMarketCrawler.this.currentAppID = app.getId();
			}
		});
		marketSession.flush();
		try {
			// thread to sleep for the specified number of milliseconds
			Thread.sleep(3000);
		} catch (java.lang.InterruptedException ie) {
			ie.printStackTrace();
		}
		return this.currentAppID;
	}
}
