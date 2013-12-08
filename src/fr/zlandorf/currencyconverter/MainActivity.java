package fr.zlandorf.currencyconverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.app.AlertDialog;
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

	private List<String> currencyList = null;

	private boolean computeTop = false;
	private boolean computeBot = false;

	private double amountTop = 0.0;
	private double amountBot = 0.0;

	private Spinner listTop = null;
	private Spinner listBot = null;
	private ArrayAdapter<String> selectableCurrencyListAdapter = null;

	private EditText textTop = null;
	private EditText textBot = null;

	private TextChangedListener topChangeListener = null;
	private TextChangedListener botChangeListener = null;
	
	private Button refreshButton = null;

	private boolean ratesInitialised = false;

	private Map<String, Double> rateMap = null;
	
	private List<String> ratesList = null;
	private ListView ratesListView = null;
	private ArrayAdapter<String> ratesListAdapter = null;
	
	private AsyncTask<Void, Integer, Boolean> cryptoTask = null;
	private AsyncTask<Void, Integer, Boolean> fiatTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		currencyList = new ArrayList<String>();

		listTop = (Spinner) findViewById(R.id.spinnerTop);
		listBot = (Spinner) findViewById(R.id.spinnerBot);

		textTop = (EditText) findViewById(R.id.AmountTop);
		textBot = (EditText) findViewById(R.id.AmountBot);

		topChangeListener = new TextChangedListener(textTop);
		botChangeListener = new TextChangedListener(textBot);

		textTop.addTextChangedListener(topChangeListener);
		textBot.addTextChangedListener(botChangeListener);
		
		textTop.setOnFocusChangeListener(new FocusChangeListener(textTop));
		textBot.setOnFocusChangeListener(new FocusChangeListener(textBot));

		selectableCurrencyListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currencyList);
		selectableCurrencyListAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

		listTop.setAdapter(selectableCurrencyListAdapter);
		listBot.setAdapter(selectableCurrencyListAdapter);
//		listBot.setSelection(2);

		listTop.setOnItemSelectedListener(this);
		listBot.setOnItemSelectedListener(this);
		
		ratesListView = (ListView) findViewById(R.id.ratesList);
		ratesList =  Collections.synchronizedList(new ArrayList<String>());
		ratesListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ratesList);
		ratesListView.setAdapter(ratesListAdapter);
		
		rateMap = new ConcurrentHashMap<String, Double>();
		initRateMap();
		
		refreshButton = (Button) findViewById(R.id.refreshButton);
		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshRates();
			}
		});

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
	}

	public void onCryptoRetrieved(boolean isSuccess) {
		if (!isSuccess) {
			showErrorDialog("Failed to retrieve rates from Kraken. Please make sure you are connected to the internet.");
		} else {
			ratesInitialised = true;
		}
	}
	
	public void onFiatRetrieved(boolean isSuccess) {
		if (!isSuccess) {
			showErrorDialog("Failed to retrieve USD/EUR rate. Please make sure you are connected to the internet.");
		} else {
			ratesInitialised = true;
		}
	}
	
	private void showErrorDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setTitle("Error");
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void refreshRates() {
		if (cryptoTask == null || cryptoTask.getStatus() == Status.FINISHED) {
			cryptoTask = new GetAllCryptoCurrencyRatesTask(this).execute();
		}

		if (fiatTask == null || fiatTask .getStatus() == Status.FINISHED) {
			fiatTask = new GetAllFiatRatesTask(this).execute();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private String getSelectedCurrency(Spinner list) {
		return currencyList.get(list.getSelectedItemPosition());
	}

	private double getTopBotExchangeRate() {
		String key = getSelectedCurrency(listTop) + getSelectedCurrency(listBot);
		if (rateMap.containsKey(key)) {
			return rateMap.get(key);
		}
		return 1;
	}

	private String getFormattedDecimal(double amount) {
//		return  String.format(Locale.ENGLISH, "%,.05f", amount);
		return String.valueOf(amount);
	}
	
	private void update() {

		double topBotExchangeRate = getTopBotExchangeRate();
		
		if (computeTop) {

			if (topBotExchangeRate != 0) {
				amountTop = amountBot * (1 / topBotExchangeRate);
			} else {
				amountTop = 0;
			}

			textTop.removeTextChangedListener(topChangeListener);
			textTop.setText(getFormattedDecimal(amountTop));
			textTop.addTextChangedListener(topChangeListener);

			computeTop = false;
		}

		if (computeBot) {

			amountBot = amountTop * topBotExchangeRate;

			textBot.removeTextChangedListener(botChangeListener);
			textBot.setText(getFormattedDecimal(amountBot));
			textBot.addTextChangedListener(botChangeListener);

			computeBot = false;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
		if (adapterView == null) return;
		
		if (ratesInitialised) {
			if (adapterView.getId() == listTop.getId()) {
				computeTop = true;
			} else if (adapterView.getId() == listBot.getId()) {
				computeBot = true;
			}
			update();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	private class TextChangedListener implements TextWatcher {

		private EditText editText;
		private boolean editing = false;

		public TextChangedListener(EditText editText) {
			this.editText = editText;
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (ratesInitialised) {
				if (!editing) {
					editing = true;
					double amount = 0;
					String text = s.toString();
					text = text.replaceAll(",","");
					
					if (!text.equals("")) { 
						amount = Double.parseDouble(text);
					}

					if (editText.getId() == textTop.getId()) {
						amountTop = amount;
						computeBot = true;
					}

					if (editText.getId() == textBot.getId()) {
						amountBot = amount;
						computeTop = true;
					}

					update();
					editing = false;
				}
			}
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

		private EditText text = null;

		public FocusChangeListener(EditText text) {
			this.text = text;
		}

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				String textToStr = text.getText().toString();
				textToStr = textToStr.replaceAll(",","");
				if (!textToStr.equals("") && Double.parseDouble(textToStr) == 0) {
					text.setText("");
				}
			}
		}
	}

	private void initRateMap() {
		refreshRates();
		
		listTop.setClickable(true);
		listBot.setClickable(true);
		
		textTop.setEnabled(true);
		textBot.setEnabled(true);
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
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// If the currency doesn't exist in the spinner add it
				boolean selectableCurrencyAdded = false;
				if (!currencyList.contains(fromCurrency)) {
					currencyList.add(fromCurrency);
					selectableCurrencyAdded = true;
				}
				
				if (!currencyList.contains(toCurrency)) {
					currencyList.add(toCurrency);
					selectableCurrencyAdded = true;
				}
				
				if (selectableCurrencyAdded) {
					selectableCurrencyListAdapter.notifyDataSetChanged();
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
