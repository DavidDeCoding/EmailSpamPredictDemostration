package com.david.hdfs;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.conf.Configuration;

import java.util.List;
import java.util.ArrayList;


class HdfsApp {
	public static void main(String[] args) {
		List<String> messages = new ArrayList<String>();
		messages.add("<html>1</html>");
		messages.add("<html>2</html>");

		try{
			putMessages( messages );
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	*
	* Puts each message into Hadoop.
	*
	* @param list of messages to be stored in hdfs.
	* @throws exception.
	*/
	public static void putMessages(List<String> messages) throws IOException {
		Configuration conf = new Configuration();
		conf.set("fs.defaultFS", "hdfs://localhost:9000");
		conf.set("hadoop.tmp.dir", "/Users/davidde/HealthReveal/Hadoop/hadoop/tmp");
		conf.set("dfs.replication", "1");
		FileSystem fs = FileSystem.get( conf );
		Path path = null;
		BufferedWriter writer = null;

		int i = 1;
		for (String message: messages) {
			path = new Path("/user/davidde/gmail/" + i + ".txt");
			if (fs.exists( path )) return;

			writer = new BufferedWriter(new OutputStreamWriter(
				fs.create( path )));
			writer.write( message );
			writer.close();
			i++;
		}
	}
}