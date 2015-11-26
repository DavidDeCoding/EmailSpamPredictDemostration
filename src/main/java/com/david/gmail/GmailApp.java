package com.david.gmail;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.Gmail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
// For message body retrieve
import java.io.ByteArrayInputStream;

import java.net.URLDecoder;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.Multipart;
import javax.mail.Part;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.googleapis.json.GoogleJsonError;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.batch.BatchRequest;

public class GmailApp {
	public static void main(String[] args) throws IOException {
		// Build a new authorized API client service.
		Gmail service = GmailAuthorize.getGmailService();

		// Print the labels in the user's account.
		String user = "me";

		/* Listing out all labels
		ListLabelsResponse listResponse = service.users().labels().list(user).execute();
		List<Label> labels = listResponse.getLabels();
		if (labels.size() == 0) {
			System.out.println("No labels found.");
		} else {
			System.out.println("Labels:");
			for (Label label: labels) {
				System.out.printf("- %s\n", label.getName());
			}
		}

		// Listing out all messages
		List<String> labelIds = new ArrayList<String>();
		labelIds.add("SPAM");
		List<Message> messages = listMessagesWithLabels(service, user, labelIds);

		// Opening a message.
		Message message = getMessage(service, user, "14062572cbf8c042");
		try{
			MimeMessage email = getMimeMessage(service, user, "15141eeace56449b");
			String message = getContent(email);
			System.out.println(message);
		} catch(Exception ex) {
			ex.printStackTrace();
		} */

		// Making batch request for all email contents.
		List<String> labelIds = new ArrayList<String>();
		labelIds.add("SPAM");
		try{
			List<MimeMessage> mimeMessages = getMimeMessages(service, user, labelIds);
			List<String> contents = getContents( mimeMessages );
			for (String content: contents) {
				System.out.println(content);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	* List all Messages of the user's mailbox matching the query.
	*
	* @param service Authorized Gmail API instance.
	* @param userId User's email address. The special value "me".
	* can be used to indicate the authenticated user.
	* @param query String used to filter the Messages listed.
	* @throws IOException
	*/
	private static List<Message> listMessagesMatchingQuery(Gmail service, String userId, String query)
		throws IOException {

		ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();

		List<Message> messages = new ArrayList<Message>();
		while (response.getMessages() != null) {
			messages.addAll(response.getMessages());
			if (response.getNextPageToken() != null) {
				String pageToken = response.getNextPageToken();
				response = service.users().messages().list(userId).setQ(query).setPageToken(pageToken).execute();
			} else {
				break;
			}
		}

		for (Message message: messages) {
			System.out.println(message.toPrettyString());
		}

		return messages;
	}

	/**
	* List all Messages of the user's mailbox with labelIds applied.
	* @param service Authorized Gmail API instance.
	* @param userId User's email address. The special value "me"
	* can be used to indicate the authenticated user.
	* @param labelIds Only return Messages with these labelIds applied.
	* @throws IOException
	*/
	private static List<Message> listMessagesWithLabels(Gmail service, String userId, List<String> labelIds)
		throws IOException {

		ListMessagesResponse response = service.users().messages().list(userId).setLabelIds(labelIds).execute();

		List<Message> messages = new ArrayList<Message>();
		while (response.getMessages() != null) {
			messages.addAll(response.getMessages());
			if (response.getNextPageToken() != null) {
				String pageToken = response.getNextPageToken();
				response = service.users().messages().list(userId).setLabelIds(labelIds).setPageToken(pageToken).execute();
			} else {
				break;
			}
		}

		/*for (Message message: messages) {
			System.out.println(message.toPrettyString());
		}*/

		return messages;
	}

	/**
	* Get Message with given ID.
	*
	* @param service Authorized Gmail API instance.
	* @param userId User's email address. The special value "me"
	* can be used to indicate the authenticated user.
	* @param messageId ID of Message to retrieve.
	* @return Message Retrieved Message.
	* @throws IOException
	*/
	private static Message getMessage(Gmail service, String userId, String messageId)
		throws IOException {

		Message message = service.users().messages().get(userId, messageId).execute();

		System.out.println("Message snippet: " + message.getSnippet());

		return message;
	}

	/**
	* Get a Message and use it to create a MimeMessage.
	*
	* @param service Authorized Gmail API instance.
	* @param userId User's email address. The special value "me"
	* can be used to indicate the authenticated user.
	* @return MimeMessage MimeMessage populated from retrieved Message.
	* @throws IOException
	* @throws MessagingException
	*/
	private static MimeMessage getMimeMessage(Gmail service, String userId, String messageId)
		throws IOException, MessagingException {

		Message message = service.users().messages().get(userId, messageId).setFormat("raw").execute();

		byte[] emailBytes = Base64.decodeBase64(message.getRaw());

		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));

		return email;
	}

	/**
	*
	* Getting the body of the messsage out.
	*
	* @param parts of the message
	* @return message body
	*/
	private static String getContent(Part p) throws
		MessagingException, IOException {

		if (p.isMimeType("text/*")) {
			String s = (String) p.getContent();
			return s;
		}

		if (p.isMimeType("multipart/alternative")) {
			Multipart mp = (Multipart) p.getContent();
			String text = null;
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);
				if (bp.isMimeType("text/plain")) {
					if (text == null) {
						text = getContent(bp);
						continue;
					} else if (bp.isMimeType("text/html")) {
						String s = getContent(bp);
						if (s != null) {
							return s;
						}
					}
				}
			}
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				String s = getContent(mp.getBodyPart(i));
				if (s != null) {
					return s;
				}
			}
		}

		return null;
	}

	/**
	*
	* Getting contents of all spam messages using batchrequests.
	*
	* @param service Authorized Gmail API instance.
	* @param userId User's email address. The special value "me"
	*/
	private static List<MimeMessage> getMimeMessages(Gmail service, String userId, List<String> labelIds)
		throws IOException, MessagingException {

		List<MimeMessage> mimeMessages = new ArrayList<MimeMessage>();

		JsonBatchCallback<Message> callback = new JsonBatchCallback<Message>() {

			@Override
			public void onSuccess(Message message, HttpHeaders responseHeaders) {
				byte[] emailBytes = Base64.decodeBase64(message.getRaw());

				Properties props = new Properties();
				Session session = Session.getDefaultInstance(props, null);
				MimeMessage email = null;
				try {
					email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				mimeMessages.add( email );
			}

			@Override
			public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
				System.out.println("Error Message: " + e.getMessage());
			}
		};

		BatchRequest batch = service.batch();

		List<Message> messages = listMessagesWithLabels(service, userId, labelIds);
		for (Message message: messages) {
			String messageId = message.getId();
			service.users().messages().get(userId, messageId).setFormat("raw").queue(batch, callback);
		}
		batch.execute();

		return mimeMessages;
	}

	/**
	*
	* Getting contents out of all batch response emails.
	*
	* @param List of MimeMessages.
	* @return Contents of MimeMessages.
	*/
	private static List<String> getContents(List<MimeMessage> mimeMessages)
		throws IOException, MessagingException {

		List<String> contents = new ArrayList<String>();
		for(MimeMessage mimeMessage: mimeMessages) {
			contents.add( getContent(mimeMessage) );
		}
		return contents;
	}

}































