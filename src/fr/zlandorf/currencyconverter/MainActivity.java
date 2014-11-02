package fr.zlandorf.currencyconverter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

public class MainActivity extends Activity implements OnItemSelectedListener {

	private List<String> topCurrencyList = null;
	private List<String> bottomCurrencyList = null;

	private Spinner listTop = null;
	private Spinner listBot = null;
	private ArrayAdapter<String> bottomSelectableCurrencyListAdapter = null;
	private ArrayAdapter<String> topSelectableCurrencyListAdapter = null;

	private EditText textTop = null;
	private EditText textBot = null;

	private TextChangedListener topChangeListener = null;
	
	private Button refreshButton = null;

	private boolean ratesInitialised = false;

	private Map<String, Double> rateMap = null;
	private Map<String, List<String>> availableConversionsMap = null;
	
	private List<String> ratesList = null;
	private ListView ratesListView = null;
	private ArrayAdapter<String> ratesListAdapter = null;
	
	private AsyncTask<Void, Integer, Boolean> cryptoTask = null;
	private AsyncTask<Void, Integer, Boolean> fiatTask = null;
	
	private DecimalFormat decimalFormatter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		topCurrencyList = new ArrayList<String>();
		bottomCurrencyList = new ArrayList<String>();
		
		decimalFormatter = new DecimalFormat("#,##0.0####", new DecimalFormatSymbols(Locale.ENGLISH));

		listTop = (Spinner) findViewById(R.id.spinnerTop);
		listBot = (Spinner) findViewById(R.id.spinnerBot);

		textTop = (EditText) findViewById(R.id.AmountTop);
		textBot = (EditText) findViewById(R.id.AmountBot);
//		textBot.setEnabled(false);
		textBot.setFocusable(false);
		textBot.setClickable(false);

		topChangeListener = new TextChangedListener();
		textTop.addTextChangedListener(topChangeListener);
		textTop.setOnFocusChangeListener(new FocusChangeListener());

		topSelectableCurrencyListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, topCurrencyList);
		topSelectableCurrencyListAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		
		bottomSelectableCurrencyListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, bottomCurrencyList);
		bottomSelectableCurrencyListAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

		listTop.setAdapter(topSelectableCurrencyListAdapter);
		listBot.setAdapter(bottomSelectableCurrencyListAdapter);

		listTop.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int selectedIndex, long l) {
				if (adapterView == null) return;
				
				// Get the previous selected currency from the bottom spinner
				int botSelectionPosition = listBot.getSelectedItemPosition();
				String botSelected = "";
				
				if (botSelectionPosition != Spinner.INVALID_POSITION) {
					botSelected = bottomCurrencyList.get(botSelectionPosition);
				}
					
				String topSelectedCurrency = topCurrencyList.get(selectedIndex);
				
				bottomCurrencyList.clear();
				bottomCurrencyList.addAll(availableConversionsMap.get(topSelectedCurrency));
				bottomSelectableCurrencyListAdapter.notifyDataSetChanged();
				
				listBot.setSelection(0);
				// If the new top currency has a possible conversion towards
				// the previous bottom selected currency, then we keep that conversion
				for (int i = 0; i < bottomCurrencyList.size(); i++) {
					if (bottomCurrencyList.get(i).equals(botSelected)) {
						listBot.setSelection(i);
					}
				}
				update();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		listBot.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int selectedIndex, long l) {
				update();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		ratesListView = (ListView) findViewById(R.id.ratesList);
		ratesList =  Collections.synchronizedList(new ArrayList<String>());
		ratesListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ratesList);
		ratesListView.setAdapter(ratesListAdapter);
		
		rateMap = new ConcurrentHashMap<String, Double>();
		availableConversionsMap = new ConcurrentHashMap<String, List<String>>();
		
		refreshButton = (Button) findViewById(R.id.refreshButton);
		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshRates();
				update();
			}
		});

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		refreshRates();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	public void onCryptoRetrieved(boolean isSuccess) {
		if (!isSuccess) {
			showErrorDialog("Failed to retrieve rates from Kraken. Please make sure you are connected to the internet.");
		}

		if (fiatTask == null || fiatTask .getStatus() == Status.FINISHED) {
			fiatTask = new GetAllFiatRatesTask(this).execute();
		}
	}
	
	public void onFiatRetrieved(boolean isSuccess) {
		if (!isSuccess) {
			showErrorDialog("Failed to retrieve fiat rates. Please make sure you are connected to the internet.");
		}
		ratesInitialised = true;
		update();
	}
	
	private void showErrorDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setTitle("Error");
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void refreshRates() {
		// The refresh rate first calls the cryptoTask
		// which calls the fiat task once it's 
		
		if (cryptoTask == null || cryptoTask.getStatus() == Status.FINISHED) {
			cryptoTask = new GetAllCryptoCurrencyRatesTask(this).execute();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private double getTopBotExchangeRate() {
		String from = "";
		String to 	= "";
		
		int selectedTop = listTop.getSelectedItemPosition();
		if (selectedTop != Spinner.INVALID_POSITION) {
			from = topCurrencyList.get(selectedTop);
		}
		
		int selectedBot = listBot.getSelectedItemPosition();
		if (selectedBot != Spinner.INVALID_POSITION) {
			to = bottomCurrencyList.get(selectedBot);
		}
		
		if (!from.equals("") && !to.equals("")) {
			String key = from + to;
			if (rateMap.containsKey(key)) {
				return rateMap.get(key);
			}
		}
		return 0;
	}

	private String getFormattedDecimal(double value) {
		return  decimalFormatter.format(value);
	}
	
	private void update() {
		double topBotExchangeRate = getTopBotExchangeRate();
		double amountTop = 0.0;
		String amountTopString = textTop.getText().toString();
		if (amountTopString != null && !amountTopString.equals("")) {
			try {
				amountTop = Double.parseDouble(amountTopString);
			} catch (NumberFormatException e) {
			}
		}
		double amountBot = amountTop * topBotExchangeRate;
		textBot.setText(getFormattedDecimal(amountBot));
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
		if (adapterView == null) return;
		
		if (ratesInitialised) {
			update();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	private class TextChangedListener implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
			update();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
	}

	private class FocusChangeListener implements OnFocusChangeListener {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				String textToStr = textTop.getText().toString();
				textToStr = textToStr.replaceAll(",","");
				if (!textToStr.equals("") && Double.parseDouble(textToStr) == 0) {
					textTop.removeTextChangedListener(topChangeListener);
					textTop.setText("");
					textTop.addTextChangedListener(topChangeListener);
				}
			}
		}
	}

	private void addPossibleConversion(String from, String to) {
		List<String> possibleConversions = null;
		if (!availableConversionsMap.containsKey(from)) {
			availableConversionsMap.put(from, new ArrayList<String>());
		}
		possibleConversions = availableConversionsMap.get(from);
		
		if (!possibleConversions.contains(to)) {
			possibleConversions.add(to);
		}
	}
	
	public void setRate(final String pair, final double rate) {
		rateMap.put(pair, rate);
		
		final String fromCurrency = pair.substring(0, 3);
		final String toCurrency = pair.substring(3, 6);
		
		String inversedPair = toCurrency + fromCurrency;
		double inversedRate = 0;
		if (rate != 0) {
			inversedRate = 1 / rate;
		}
		rateMap.put(inversedPair, inversedRate);
		rateMap.put(fromCurrency+fromCurrency, 1d);
		rateMap.put(toCurrency+toCurrency, 1d);
		
		addPossibleConversion(fromCurrency, toCurrency);
		addPossibleConversion(toCurrency, fromCurrency);
		addPossibleConversion(fromCurrency, fromCurrency);
		addPossibleConversion(toCurrency, toCurrency);
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// If the currency doesn't exist in the spinner add it
				if (!topCurrencyList.contains(fromCurrency)) {
					topCurrencyList.add(fromCurrency);
					topSelectableCurrencyListAdapter.notifyDataSetChanged();
				}
				
				if (!topCurrencyList.contains(toCurrency)) {
					topCurrencyList.add(toCurrency);
					topSelectableCurrencyListAdapter.notifyDataSetChanged();
				}
				
				// Add the new rate to the rates List
				String newString = fromCurrency + "/" + toCurrency + " : " + rate;
				
				boolean found = false;
				for (int i = 0; i < ratesList.size(); i++) {
					if (ratesList.get(i).replaceAll("/", "").startsWith(pair)) {
						found = true;
						ratesList.set(i, newString);
					}
				}
				
				if (!found) {
					ratesList.add(0, newString);
				}
				
				ratesListAdapter.notifyDataSetChanged();
			}
		});
	}
	
}
