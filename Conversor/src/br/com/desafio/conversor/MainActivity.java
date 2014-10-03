package br.com.desafio.conversor;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

		adapter = ArrayAdapter.createFromResource(this, R.array.array_moedas,
				android.R.layout.simple_list_item_1);

		spMoeda.setAdapter(adapter);
		spMoeda.setOnItemSelectedListener(this);

		edtValor = (EditText) findViewById(R.id.edt_valor);
		btnConverter = (Button) findViewById(R.id.btn_converter);
		txtValorConvertido = (TextView) findViewById(R.id.txt_valor_convertido);

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		paisMoedaConversao = (String) adapter.getItem(position);

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		paisMoedaConversao = "USD";
	}

	public void onClickConverter(View v) {

		int valorParaConversao = 0;

		try {
			valorParaConversao = Integer
					.parseInt(edtValor.getText().toString());

		} catch (NumberFormatException e) {
			Toast.makeText(getApplicationContext(),
					"Erro na conversao do valor", Toast.LENGTH_LONG);
		}

		btnConverter.setEnabled(false);
		processarConversao(valorParaConversao);

	}

	/**
	 * Processa a requisicao para a API externa via internet, por causa disso eh
	 * necessario o uso de Thread. Porque assim não travara a UIThread.
	 * 
	 * @param valor
	 *            para conversao da moeda
	 */
	private void processarConversao(final int valorParaConversao) {

		new Thread(new Runnable() {

			@Override
			public void run() {

				double valorConvertido = 0;
				try {

					DefaultHttpClient httpClient = new DefaultHttpClient();
					HttpGet get = new HttpGet(gerarURL(valorParaConversao));

					HttpResponse httpResponse = httpClient.execute(get);
					String json = EntityUtils.toString(httpResponse.getEntity());

					JSONObject valorJSon = new JSONObject(json);

					valorConvertido = (Double) valorJSon.get("v");

				} catch (MalformedURLException e) {
					Log.d("URL", "MalformedURLException");

					e.printStackTrace();
				} catch (IOException e) {
					Log.d("URL", "IOException");
					e.printStackTrace();
				} catch (JSONException e) {
					Log.d("URL", "JSONException");

					e.printStackTrace();
				}

				final String novoTextoValorConvertido = valorConvertido + "";

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

	/**
	 * exemplo http://rate-exchange.appspot.com/currency?from=BRL&to=USD&q=5
	 * 
	 * @return
	 */
	public String gerarURL(int valor) {
		StringBuilder sURL = new StringBuilder();

		sURL.append("http://rate-exchange.appspot.com/currency?from=")
				.append(MOEDA_BRASIL).append("&to=").append(paisMoedaConversao)
				.append("&q=").append(valor);

		return sURL.toString();
	}

}
