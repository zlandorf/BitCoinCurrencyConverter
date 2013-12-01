package fr.zlandorf.currencyconverter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import android.os.AsyncTask;

public class GetAllCryptoCurrencyRatesTask extends AsyncTask<Void, Void, Boolean> {

	public static String CLOSED_ARRAY_STRING_START = "\"c\":[\"";
	
	public static String KRAKEN_URL = "https://api.kraken.com/0/public/Ticker?pair=BTCEUR,BTCLTC,XBTUSD,LTCEUR,LTCUSD";
	
	MainActivity activity;
	Map<String, String> pairMap;
	
	public GetAllCryptoCurrencyRatesTask(MainActivity activity) {
		this.activity = activity;
		pairMap = new HashMap<String, String>();
		pairMap.put("XLTCZEUR", "LTCEUR");
		pairMap.put("XLTCZUSD", "LTCUSD");
		pairMap.put("XXBTXLTC", "BTCLTC");
		pairMap.put("XXBTZEUR", "BTCEUR");
		pairMap.put("XXBTZUSD", "BTCUSD");
	}
	
	@Override
	protected Boolean doInBackground(Void ... params) {
		
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
		Pattern pattern = Pattern.compile("\"(\\w*)\":\\{\"a\":\\[\"(.*?)\",(.*?)\\],\\s*\"(.*?)\"\\s*\\}");
		Matcher matcher = pattern.matcher(result);
		
		while (matcher.find()) {
			String pair = pairMap.get(matcher.group(1));
			double rate = Double.parseDouble(matcher.group(2));
			activity.setRate(pair, rate);
		}
		
	}
}
