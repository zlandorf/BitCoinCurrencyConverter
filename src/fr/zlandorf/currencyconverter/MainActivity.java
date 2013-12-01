package fr.zlandorf.currencyconverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

public class MainActivity extends Activity implements OnItemSelectedListener {

	private List<String> currencyList = null;

	private boolean computeTop = false;
	private boolean computeBot = false;

	private double amountTop = 0.0;
	private double amountBot = 0.0;

	private Spinner listTop = null;
	private Spinner listBot = null;

	private EditText textTop = null;
	private EditText textBot = null;

	private TextChangedListener topChangeListener = null;
	private TextChangedListener botChangeListener = null;
	
	private Button refreshButton = null;

	private boolean ratesInitialised = false;

	Map<String, Double> rateMap = null;
	
	List<String> ratesList = null;
	private ListView ratesListView = null;
	private ArrayAdapter<String> ratesListAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		currencyList = new ArrayList<String>();
		currencyList.add("EUR");
		currencyList.add("USD");
		currencyList.add("BTC");
		currencyList.add("LTC");

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

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currencyList);
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

		listTop.setAdapter(adapter);
		listBot.setAdapter(adapter);
		listBot.setSelection(2);

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

		textTop.clearFocus();
		textBot.clearFocus();
		refreshButton.requestFocus();
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	private void refreshRates() {
		
		final ProgressDialog progressBar = new ProgressDialog(this);
		final MainActivity activity = this;
		
		progressBar.setCancelable(false);
		progressBar.setMessage("Retrieving rates from Kraken ...");
		progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressBar.show();
		progressBar.setProgress(0);
		progressBar.setMax(100);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					AsyncTask<Void, Void, Boolean> cryptoTask = new GetAllCryptoCurrencyRatesTask(activity).execute();
					AsyncTask<Void, Void, Boolean> fiatTask = new GetAllFiatRatesTask(activity).execute();
					
					cryptoTask.get();
					progressBar.setProgress(70);
					fiatTask.get();
					progressBar.setProgress(100);
					progressBar.dismiss();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		rateMap.put("BTCBTC", 1d);
		rateMap.put("LTCLTC", 1d);
		rateMap.put("EUREUR", 1d);
		rateMap.put("USDUSD", 1d);
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
		
		ratesInitialised = true;
		
		listTop.setClickable(true);
		listBot.setClickable(true);
		
		textTop.setEnabled(true);
		textBot.setEnabled(true);
	}
	
	public void setRate(final String pair, final double rate) {
		rateMap.put(pair, rate);
		
		String inversedPair = pair.substring(3, 6) + pair.substring(0, 3);
		double inversedRate = 0;
		if (rate != 0) {
			inversedRate = 1 / rate;
		}
		rateMap.put(inversedPair, inversedRate);
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				
				String newString = pair.substring(0, 3) + "/" + pair.substring(3, 6) + " : " + rate;
				
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
