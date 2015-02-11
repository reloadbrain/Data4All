package io.github.data4all.task;

import io.github.data4all.Constants;
import io.github.data4all.R;
import io.github.data4all.network.OscUploadHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * Task to request a changeSetID from OpenStreetMap
 * 
 * @author Richard
 *
 */

public class RequestChangesetIDFromOpenStreetMapTask extends
		AsyncTask<Void, Void, Void> {

	private final String TAG = getClass().getSimpleName();
	private Context context;

	private HttpPut request;

	private OAuthConsumer consumer;
	private File changeSetXML;
	private OscUploadHelper helper;

	/**
	 * HTTP result code or -1 in case of internal error
	 */
	private int statusCode = -1;

	/**
	 * Constructor of the RequestChangesetIDFromOpenStreetMapTask
	 * 
	 * Sets the Parameter values to the local attributes
	 * 
	 * @param context
	 *            the Context of the Application
	 * @param consumer
	 *            the OAuthConusmer used for authentication
	 * @param comment
	 *            the Comment for this ChangeSet, should include what is
	 *            uploaded
	 * @param helper
	 *            the OscUploadHelper which started the task
	 */
	public RequestChangesetIDFromOpenStreetMapTask(Context context,
			OAuthConsumer consumer, String comment, OscUploadHelper helper) {

		this.helper = helper;
		this.context = context;
		this.consumer = consumer;
		changeSetXML = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/getChangesetID.xml");

		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new FileWriter(changeSetXML)));

			// writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.println("<osm>");
			writer.println("<changeset>");
			writer.println("<tag k=\"created_by\" v=\"Data4All\"/>");
			writer.println("<tag k=\"comment\" v=\"" + comment + "\"/>");
			writer.println("</changeset>");
			writer.println("</osm>");

			writer.flush();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Forms the HttpRequest
	 */

	@Override
	protected void onPreExecute() {

		try {
			// Prepare request
			request = new HttpPut(Constants.SCOPE + "api/0.6/changeset/create");

			// Sign the request with oAuth
			consumer.sign(request);

			// Adding the Entity to the Request
			HttpEntity entity = new FileEntity(changeSetXML, "text/plain");
			request.setEntity(entity);

		} catch (OAuthMessageSignerException e) {

			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {

			e.printStackTrace();
		} catch (OAuthCommunicationException e) {

			e.printStackTrace();
		}
	}

	/**
	 * Displays what happened
	 */
	@Override
	protected void onPostExecute(Void result) {
		switch (statusCode) {
		case -1:
			// Internal error, the request didn't start at all
			// Toast.makeText(context,
			// R.string.uploadToOsmRequestInternalTaskError,
			// Toast.LENGTH_LONG).show();
			Log.d(TAG, "Internal error, the request did not start at all");
			break;
		case HttpStatus.SC_OK:
			// Success ! Update database and close activity
			// Toast.makeText(context, R.string.success,
			// Toast.LENGTH_LONG).show();
			Log.d(TAG, "Success");
			break;
		case HttpStatus.SC_UNAUTHORIZED:
			// Does not have authorization
			// Toast.makeText(context, R.string.uploadToOsmRequestTaskAuthError,
			// Toast.LENGTH_LONG).show();
			Log.d(TAG, "Does not have authorization");
			break;
		case HttpStatus.SC_INTERNAL_SERVER_ERROR:
			Toast.makeText(context, "INTERNAL SERVER ERROR", Toast.LENGTH_LONG)
					.show();
			Log.d(TAG, "INTERNAL SERVER ERROR");
			break;

		default:
			// unknown error
			// Toast.makeText(context, R.string.unkownError, Toast.LENGTH_LONG)
			// .show();
			Log.d(TAG, "Unknown error");
		}
	}

	/**
	 * Sending the Request and analyzes the response and starts the Uploading
	 * Task through the helper if the response was positive
	 */
	@Override
	protected Void doInBackground(Void... params) {
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();

			// Sending the Request
			HttpResponse response = httpClient.execute(request);
			statusCode = response.getStatusLine().getStatusCode();
			HttpEntity responseEntity = response.getEntity();
			Integer changesetId = 0;

			// Looking if the Request was successful and then starting the
			// Upload Task through the helper
			if (statusCode >= 200 && statusCode < 299) {
				InputStream is = responseEntity.getContent();
				BufferedReader in = new BufferedReader(
						new InputStreamReader(is));
				String line = in.readLine();
				changesetId = Integer.parseInt(line);
				helper.parseAndUpload(changesetId);
			}
			Log.d(TAG, "OSM ChangeSetID response Entity: " + responseEntity);
			Log.d(TAG, "OSM ChangeSetID response: " + changesetId);
			Log.d(TAG, "OSM ChangeSetID statusCode: " + statusCode);
		} catch (Exception e) {
			Log.e(TAG, "doInBackground failed", e);
			e.printStackTrace();
		}

		return null;
	}

}
