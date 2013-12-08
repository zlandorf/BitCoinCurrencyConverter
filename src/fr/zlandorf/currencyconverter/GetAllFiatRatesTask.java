package fr.zlandorf.currencyconverter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class GetAllFiatRatesTask extends AsyncTask<Void, Integer, Boolean> {

	public static String FIAT_URL = "https://api-sandbox.oanda.com/v1/quote?instruments=EUR_USD%2CEUR_CNY%2CEUR_GBP%2CUSD_CNY%2CUSD_GBP%2CGBP_CNY";
	//EUR USD
	//EUR CNY
	//EUR GBP
	//USD CNY
	//USD GBP
	//GBP CNY
	private MainActivity activity;
	private ProgressDialog progressBar 	= null;
	
	public GetAllFiatRatesTask(MainActivity activity) {
		this.activity = activity;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		progressBar = new ProgressDialog(activity);
		
		progressBar.setCancelable(false);
		progressBar.setMessage("Retrieving fiat rates ...");
		progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressBar.show();
		progressBar.setProgress(0);
		progressBar.setMax(2);
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		progressBar.setProgress(progress[0]);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		progressBar.dismiss();
		activity.onFiatRetrieved(result);
	}
	
	@Override
	protected Boolean doInBackground(Void ... params) {
		publishProgress(0);
		int totalSize = 0;
		URL httpsUrl;
		try {
				httpsUrl = new URL(FIAT_URL);
				HttpsURLConnection conn = (HttpsURLConnection) httpsUrl.openConnection();
				
				conn.setRequestMethod("GET");
				conn.setRequestProperty("User-agent", "Java client");
				
				int responseCode = conn.getResponseCode();
				if (responseCode != 200) {
					throw new Exception("Failed to connect to "+httpsUrl);
				}
				
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String input;
				StringBuffer buffer = new StringBuffer();
				String result = null;
				
				while ((input = br.readLine()) != null) {
					buffer.append(input);
				}
				br.close();
				result = buffer.toString();
				totalSize = result.length();
				
				publishProgress(1);
				// -- Parse the result to get the currencyRate
				parseResult(result);
				
				publishProgress(2);
			
		} catch (Exception e) {
			System.out.println("EXCEPTION CAUGHT : "+e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}
		
		
		System.out.println("TOTAL SIZE : "+totalSize);
		return true;
	}

	private void parseResult(String result) throws Exception {
		
		Pattern pattern = Pattern.compile("\\{\\s*\"instrument\"\\s*:\\s*\"(\\w*)\"(.*?)\\s*\"ask\"\\s*:\\s*([0-9.]*)\\s*(.*?)\\s*\\}");
		Matcher matcher = pattern.matcher(result);
		while (matcher.find()) {
			String pair = matcher.group(1).replace("_", "");
			double rate = Double.parseDouble(matcher.group(3));
			activity.setRate(pair, rate);
			System.out.println("Found : "+rate);
		}
		
	}
}
