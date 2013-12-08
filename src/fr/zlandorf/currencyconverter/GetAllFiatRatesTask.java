package fr.zlandorf.currencyconverter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class GetAllFiatRatesTask extends AsyncTask<Void, Integer, Boolean> {

	//EUR USD , EUR CNY , EUR GBP , USD CNY , USD GBP , GBP CNY
	public static String FIAT_URL = "http://download.finance.yahoo.com/d/quotes.csv?s=EURUSD=X,EURGBP=X,EURCNY=X,USDGBP=X,USDCNY=X,GBPCNY=X&f=sl1&e=.csv";

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
		try {
			URL httpUrl = new URL(FIAT_URL);
			HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();

			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-agent", "Java client");

			int responseCode = conn.getResponseCode();
			if (responseCode != 200) {
				throw new Exception("Failed to connect to "+httpUrl);
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String input;
			StringBuffer buffer = new StringBuffer();
			String result = null;

			while ((input = br.readLine()) != null) {
				buffer.append(input+"\n");
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
		
		for (String line : result.replaceAll("\"", "").replaceAll("=X","").split("\n")) {
			String [] data = line.split(",");
			String pair = data[0];
			double rate = Double.parseDouble(data[1]);
			activity.setRate(pair, rate);
			System.out.println("Found : "+rate);
		}
	}
}
