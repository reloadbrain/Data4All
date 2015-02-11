/*
 * Copyright (c) 2014, 2015 Data4All
 * 
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 * 
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.data4all.task;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

/**
 * AsyncTask to retrieve the username over http-connection Stores username in
 * SharedPreferences.
 * 
 * @author sb
 *
 */
public class RetrieveUsernameTask extends AsyncTask<Void, Void, Void> {

    private String tag = getClass().getSimpleName();

    private XmlPullParserFactory xmlParserFactory;

    private String url;
    private OAuthConsumer consumer;
    private SharedPreferences prefs;

    /**
     * Constructor for this task.
     * 
     * @param url
     *            The url to retrieve the data
     * @param consumer
     *            The {@link OAuthConsumer}
     * @param prefs
     *            The SharedPreferences
     */
    public RetrieveUsernameTask(String url, OAuthConsumer consumer,
            SharedPreferences prefs) {
        this.url = url;
        this.consumer = consumer;
        this.prefs = prefs;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            xmlParserFactory = XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e) {
            Log.e("Data4All", "Problem creating parser factory", e);
        }
        getUserXml(url, consumer);
        return null;
    }

    private void getUserXml(String url, OAuthConsumer consumer) {
        final DefaultHttpClient httpclient = new DefaultHttpClient();

        try {
            // make a GET request
            final HttpGet request = new HttpGet(url);
            Log.i(tag, "Requesting URL : " + url);

            // sign the request with oAuth
            consumer.sign(request);

            // execute request and get the response
            final HttpResponse response = httpclient.execute(request);
            Log.i(tag, "Statusline : " + response.getStatusLine());

            // write content of response in InputStream
            final InputStream data = response.getEntity().getContent();

            parseXmlUserInfo(data);
        } catch (OAuthMessageSignerException e) {
            // TODO Auto-generated catch block
            Log.e(tag, e.getMessage());
        } catch (OAuthExpectationFailedException e) {
            // TODO Auto-generated catch block
            Log.e(tag, e.getMessage());
        } catch (OAuthCommunicationException e) {
            // TODO Auto-generated catch block
            Log.e(tag, e.getMessage());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            Log.e(tag, e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(tag, e.getMessage());
        }

    }

    /**
     * Parse a given InputStream to retrieve the username.
     * 
     * @param inputStream
     *            an xml as InputStream
     */
    public void parseXmlUserInfo(InputStream inputStream) {
        XmlPullParser parser;
        try {
            parser = xmlParserFactory.newPullParser();
            parser.setInput(inputStream, null);
            int eventType;

            while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT) {
                final String tagName = parser.getName();
                if (eventType == XmlPullParser.START_TAG
                        && "user".equals(tagName)) {
                    prefs.edit()
                            .putString(
                                    "USERNAME",
                                    parser.getAttributeValue(null,
                                            "display_name")).apply();
                    Log.d(tag,
                            "getUserDetails display name "
                                    + prefs.getString("USERNAME", "NULL"));
                }
            }
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            Log.d(tag, e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.d(tag, e.getMessage());
        }
    }

}
