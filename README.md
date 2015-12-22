# EmailSpamPredictDemostration

## Three Part Problem

1. Gmail Authorization and getting spam and non spam mails from Gmail.
2. Putting all of them inside HDFS.
3. Using Spark MLlib's Naive Bayes Model generator to generate spam model and classification of test mail.

## Gmail Authorization and getting spam and non spam mails from Gmail

1. com.david.utilities.gmail.GmailAuthorize.java authorizes you to use your gmail account.
2. Put your <b>client_secret.json</b> in the src/main/resources folder.
3. com.david.utilities.gmail.GmailUtility.java gets messages from gmail.

For more info about Gmail Api: <b><i>https://developers.google.com/gmail/api/?hl=en</i></b>

## Putting all of them inside HDFS

1. Start your Hadoop single node or cluster.
2. Put the core-site.xml and hdfs-site.xml in src/main/resources folder.
3. com.david.utilities.hdfs.HdfsUtility.java puts messages into HDFS.

For more info about Hadoop and how to setup your cluster go to my blog ;) : <b><i>http://daviddecoding.com/blog/tutorial/installing-hadoop/</i></b>

## Using Spark MLlib's Naive Bayes Model generator to generate spam model and classfication of test mail

1. com.david.utilities.spark.SparkUtilities.java uses Spark MLlib to generate a Naive Bayes Model.
2. Then it classifies test mail as Spam or Non-Spam.

For more info about Spark MLlib: <b><i>http://spark.apache.org/docs/latest/mllib-naive-bayes.html</i></b>

## Other References:
<b><i>https://www.mapr.com/blog/comparing-kill-mockingbird-its-sequel-with-apache-spark</i></b>
<b><i>http://www.programcreek.com/java-api-examples/index.php?api=org.apache.spark.mllib.classification.NaiveBayes</i></b>
