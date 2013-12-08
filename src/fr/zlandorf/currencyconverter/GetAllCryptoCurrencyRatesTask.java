package fr.zlandorf.currencyconverter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class GetAllCryptoCurrencyRatesTask extends AsyncTask<Void, Integer, Boolean> {

	public static String CLOSED_ARRAY_STRING_START = "\"c\":[\"";
	
	public static String KRAKEN_URL = "https://api.kraken.com/0/public/Ticker?pair=BTCEUR,BTCLTC,XBTUSD,LTCEUR,LTCUSD,NMCEUR,EURXRP";
	
	/* The list of available pairs can be found here : 
	 * https://api.kraken.com/0/public/AssetPairs
	 */
	
	private MainActivity activity 		= null;
	private Map<String, String> pairMap = null;
	
	private ProgressDialog progressBar 	= null;
	
	public GetAllCryptoCurrencyRatesTask(MainActivity activity) {
		this.activity = activity;
		pairMap = new HashMap<String, String>();
		
		// This maps the pair name returned by kraken
		// with the pair name that is used in the app
		pairMap.put("XLTCZEUR", "LTCEUR");
		pairMap.put("XLTCZUSD", "LTCUSD");
		pairMap.put("XXBTXLTC", "BTCLTC");
		pairMap.put("XXBTZEUR", "BTCEUR");
		pairMap.put("XXBTZUSD", "BTCUSD");
		pairMap.put("XNMCZEUR", "NMCEUR");
		pairMap.put("ZEURXXRP", "NMCEUR");
	}
	
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		progressBar = new ProgressDialog(activity);
		
		progressBar.setCancelable(false);
		progressBar.setMessage("Retrieving crypto rates from Kraken ...");
		progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressBar.setProgress(0);
		progressBar.setMax(2);
		progressBar.show();
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
		activity.onCryptoRetrieved(result);
	}
	
	@Override
	protected Boolean doInBackground(Void ... params) {
		publishProgress(0);
		URL httpsUrl;
		try {
			httpsUrl = new URL(KRAKEN_URL);
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
			publishProgress(1);

			// -- Parse the result to get the currencyRate
			parseResult(result);
			publishProgress(2);
			
		} catch (Exception e) {
			System.out.println("EXCEPTION CAUGHT : "+e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	private void parseResult(String result) throws Exception {
		Pattern pattern = Pattern.compile("\"(\\w*)\":\\{\"a\":\\[\"(.*?)\",(.*?)\\],\\s*\"(.*?)\"\\s*\\}");
		Matcher matcher = pattern.matcher(result);
		
		while (matcher.find()) {
			String pair = pairMap.get(matcher.group(1));
			double rate = Double.parseDouble(matcher.group(2));
			activity.setRate(pair, rate);
		}
		
	}
}
