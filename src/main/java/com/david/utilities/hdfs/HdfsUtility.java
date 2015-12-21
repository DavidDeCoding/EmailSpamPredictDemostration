package com.david.utilities.hdfs;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.conf.Configuration;

import java.util.List;
import java.util.ArrayList;


public class HdfsUtility {
	/**
	*
	* Puts each message into Hadoop.
	*
	* @param list of messages to be stored in hdfs.
	* @throws exception.
	*/
	public static void putMessages(List<String> messages, String filename) throws IOException {
		Configuration conf = new Configuration();
		conf.addResource( HdfsUtility.class.getResourceAsStream("/core-site.xml"));
		conf.addResource( HdfsUtility.class.getResourceAsStream("/hdfs-site.xml"));

		FileSystem fs = FileSystem.get( conf );
		Path path = null;
		BufferedWriter writer = null;

		int i = 1;
		for (String message: messages) {
			path = new Path( "/" + filename + i + ".txt" );
			if (fs.exists( path )) return;

			writer = new BufferedWriter(new OutputStreamWriter(
				fs.create( path )));
			writer.write( message );
			writer.close();
			i++;
		}
	}
}