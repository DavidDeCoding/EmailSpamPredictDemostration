package com.david;

import com.david.utilities.hdfs.*;
import com.david.utilities.spark.*;
import com.david.utilities.gmail.*;

import java.util.List;
import java.util.ArrayList;

import java.util.stream.Collectors;

import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.feature.IDFModel;

import scala.Tuple2;

public class App {
	public static void main(String... args) throws Exception {
		// Getting the Spam Mails
		List<String> spamId = new ArrayList();
		spamId.add( "SPAM" );
		List<String> spamMails = GmailUtility.getAllMessages( "me", spamId )
											.stream().filter ( x -> !x.isEmpty() ).collect(Collectors.toList());

		// Putting Spam Mails inside HDFS
		HdfsUtility.putMessages( spamMails, "/user/davidde/train/spam" );

		// Getting the NonSpam Mails
		List<String> nonSpamId = new ArrayList();
		nonSpamId.add( "INBOX" );
		List<String> nonSpamMails = GmailUtility.getAllMessages( "me", spamId )
											.stream().filter ( x -> !x.isEmpty() ).collect(Collectors.toList());

		// Putting Non Spam Mails inside HDFS
		HdfsUtility.putMessages( nonSpamMails, "/user/davidde/train/nonspam" );

		// Using Spark to build Model
		List<String> folders = new ArrayList();
		folders.add( "/user/davidde/train/spam" );
		folders.add( "/user/davidde/train/nonspam" );
		Tuple2<NaiveBayesModel, IDFModel> trainedData = SparkUtility.naiveBayes( folders );

		// Lets use the model to see
		Double prediction = SparkUtility.predict("/user/davidde/test/*", trainedData),
		if ( prediction == (new Double(0))) System.out.println ("Its a spam.");
		else System.out.println ("Its not a spam.");
	}
}