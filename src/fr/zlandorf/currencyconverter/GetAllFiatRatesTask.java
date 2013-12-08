package fr.zlandorf.currencyconverter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class GetAllFiatRatesTask extends AsyncTask<Void, Integer, Boolean> {

	public static String USDEUR_URL = "https://www.google.com/finance/converter?";
	
	private MainActivity activity;
	private ProgressDialog progressBar 	= null;
	private List<RatePair> ratesToRetrieve = null;
	
	public GetAllFiatRatesTask(MainActivity activity) {
		this.activity = activity;
		ratesToRetrieve = new ArrayList<RatePair>();
		
		ratesToRetrieve.add(new RatePair("USD", "EUR"));
		ratesToRetrieve.add(new RatePair("USD", "GBP"));
		ratesToRetrieve.add(new RatePair("USD", "CNY"));
		
		ratesToRetrieve.add(new RatePair("EUR", "GBP"));
		ratesToRetrieve.add(new RatePair("EUR", "CNY"));
		
		ratesToRetrieve.add(new RatePair("GBP", "CNY"));
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
		progressBar.setMax(ratesToRetrieve.size());
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
		URL httpsUrl;
		try {
			int progress = 0;
			for (RatePair rateToRetrieve : ratesToRetrieve)
			{
				httpsUrl = new URL(USDEUR_URL+rateToRetrieve.getURLParameters());
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
				parseResult(rateToRetrieve, result);
				
				publishProgress(progress);
				progress++;
			}
			
		} catch (Exception e) {
			System.out.println("EXCEPTION CAUGHT : "+e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	private void parseResult(RatePair pair, String result) throws Exception {
		
		Pattern pattern = Pattern.compile("<span class=bld>([0-9.]*)\\s+\\w+</span>");
		Matcher matcher = pattern.matcher(result);
		System.out.println("For : "+pair);
		while (matcher.find()) {
			double rate = Double.parseDouble(matcher.group(1));
			activity.setRate(pair.toString(), rate);
			System.out.println("Found : "+rate);
		}
		
	}
	
	private class RatePair {
		private String from = null;
		private String to = null;
		
		public RatePair(String from, String to) {
			this.from = from;
			this.to = to;
		}
		
		String getURLParameters() {
			return "a=1&from="+from+"&to="+to;
		}
		
		@Override
		public String toString() {
			return from+to;
		}
	}
}
