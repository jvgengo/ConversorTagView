package br.com.desafio.conversor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.json.*;

public class MainActivity extends Activity implements OnItemSelectedListener {

	private EditText edtValor;
	private TextView txtValorConvertido;
	private Spinner spMoeda;
	private Button btnConverter;
	
	private String paisMoedaConversao;
	
	private Handler handlerAlteraValorConvertido = new Handler();
	private ArrayAdapter<CharSequence> adapter;

	private static final String MOEDA_BRASIL = "BRL";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		spMoeda = (Spinner) findViewById(R.id.spinner_moedas);
		
		adapter = ArrayAdapter.createFromResource(this, R.array.array_moedas, android.R.layout.simple_list_item_1);
		
		spMoeda.setAdapter(adapter);
		
		edtValor = (EditText) findViewById(R.id.edt_valor);
		btnConverter = (Button) findViewById(R.id.btn_converter);
		txtValorConvertido = (TextView) findViewById(R.id.txt_valor_convertido);

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
		paisMoedaConversao = (String) adapter.getItem(position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	public void onClickConverter(View v) {
		
		int valorParaConversao = 0;
		
		try {
			valorParaConversao = Integer.parseInt(edtValor.getText().toString());
			
		} catch (NumberFormatException e) {
			Toast.makeText(getApplicationContext(),"Erro na conversao do valor" , Toast.LENGTH_LONG);
		}

		btnConverter.setEnabled(false);
		processarConversao(valorParaConversao);

	}

	
	/**
	 * Processa a requisicao para a API externa via internet, por causa disso eh necessario o uso de Thread.
	 * Porque assim não travara a UIThread.
	 * 
	 * @param valor para conversao da moeda
	 */
	private void processarConversao(int valorParaConversao) {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				int valorConvertido = 0;
				try {
					URL urlRateExchange = new URL("");
					HttpURLConnection conexaoURL = (HttpURLConnection) urlRateExchange.openConnection();
					
					conexaoURL.setRequestProperty("Request-Method", "GET");
					conexaoURL.setDoInput(true);  
					conexaoURL.setDoOutput(true); 
					
					conexaoURL.connect();
					
					BufferedReader brValor = new BufferedReader(new InputStreamReader(conexaoURL.getInputStream())); 
					String stringJson = "";
					
					if (brValor.ready()) {
						stringJson = brValor.readLine();
					}
					
					JSONObject valorJSon = new JSONObject(stringJson);
					
					valorConvertido = (Integer) valorJSon.get("rate");
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				
				final String novoTextoValorConvertido = valorConvertido+"";
				
				handlerAlteraValorConvertido.post(new Runnable() {
					
					@Override
					public void run() {
						
						txtValorConvertido.setText(novoTextoValorConvertido);
						btnConverter.setEnabled(true);
						
					}
				});
				
			}
		}).start();
	}
	
	

}
