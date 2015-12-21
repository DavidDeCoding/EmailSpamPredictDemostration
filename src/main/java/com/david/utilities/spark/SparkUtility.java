package com.david.utilities.spark;

import org.apache.spark.SparkConf;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.feature.IDF;
import org.apache.spark.mllib.feature.IDFModel;
import org.apache.spark.mllib.linalg.Vector;

import scala.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class SparkUtility {
	private static HashingTF hashingTF = new HashingTF();
	private static SparkConf conf = new SparkConf().setMaster("local[4]").setAppName("Naive Bayes");
	private static JavaSparkContext sc = new JavaSparkContext(conf);

	/**
	*
	* Generates the Naive Bayes Model.
	*
	* @param folder path
	* @throws exception
	* @return tuple of generated model and idfs.
	*
	*/
	public static Tuple2<NaiveBayesModel, IDFModel> naiveBayes(List<String> folders) throws Exception {
		Double[] labels = new Double[] { new Double(0), new Double(1) };

		JavaRDD<LabeledPoint> data = sc.emptyRDD();

		for (int i = 0; i < folders.size(); i++) {
			final Double label = labels[i];
			JavaRDD<String> files = sc.textFile ( folders.get(i) + "/*" );
			JavaRDD<LabeledPoint> labeledpoints = files
				.map ( poem -> new LabeledPoint (
					label,
					hashingTF.transform(
						Arrays.asList(poem.split(" "))
					))
				);
			data = data.union( labeledpoints );
		}

		JavaRDD<Vector> features = data.map ( x -> x.features() );
		IDFModel modelIDF = new IDF().fit( features );

		JavaRDD<LabeledPoint> modellingData = data.map (
			x -> new LabeledPoint(x.label(), modelIDF.transform(x.features()) )
		);

		return new Tuple2(NaiveBayes.train( modellingData.rdd(), 1.0 ), modelIDF);
	}

	/**
	*
	* Predicts based on the trained Model and returns the predicted label.
	*
	* @param test file
	* @return predicted label
	*
	*/
	public static Double predict(String file, Tuple2<NaiveBayesModel, IDFModel> trainedData) {
		JavaRDD<String> testData = sc.textFile( file );
		String textFile = "";
		for (String line: testData.toArray()) {
			textFile += " " + line;
		}
		return trainedData._1().predict(
			trainedData._2().transform( hashingTF.transform( Arrays.asList(textFile.split(" ")) ))
		);
	}
}































