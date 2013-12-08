package fr.zlandorf.currencyconverter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import android.os.AsyncTask;

public class GetAllFiatRatesTask extends AsyncTask<Void, Void, Boolean> {

	public static String USDEUR_URL = "https://www.google.com/finance/converter?a=1&from=USD&to=EUR";
	
	MainActivity activity;
	
	public GetAllFiatRatesTask(MainActivity activity) {
		this.activity = activity;
	}
	
	@Override
	protected Boolean doInBackground(Void ... params) {

		URL httpsUrl;
		try {
			httpsUrl = new URL(USDEUR_URL);
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


			// -- Parse the result to get the currencyRate
			parseResult(result);
		} catch (Exception e) {
			System.out.println("EXCEPTION CAUGHT : "+e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	private void parseResult(String result) throws Exception {
		
		Pattern pattern = Pattern.compile("<span class=bld>([0-9.]*)\\s+\\w+</span>");
		Matcher matcher = pattern.matcher(result);
		
		while (matcher.find()) {
			double rate = Double.parseDouble(matcher.group(1));
			activity.setRate("USDEUR", rate);
		}
		
	}
}
