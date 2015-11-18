package main;

import com.gc.android.market.api.MarketSession;

import ReviewsCrawler.Crawler;
import Word2Vec.Word2VecProcessor;

public class main {
	public static void main(String[] args) {
		// exportReviewToCSV();
		//crawReview();
		// testSentimentAnalysisSFNLP(readPhrasesFromFile());
		word2vecTesting();
	}

	private static void crawReview() {
		//Crawler crawler = new Crawler(Crawler.ANDROID_MARKET);
		//crawler.extractReviews();
		MarketSession marketSession = new MarketSession();
		marketSession.login("ususeal@gmail.com", "phdcs2014");
		marketSession.getContext().setAndroidId("dead000beef");
	}
	private static void word2vecTesting() {
		try {
            new Word2VecProcessor("E:\\EclipseWorkspace\\ReviewAnalysisJava\\text8").train();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}