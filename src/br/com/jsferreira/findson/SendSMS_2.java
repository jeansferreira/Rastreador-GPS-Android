package br.com.jsferreira.findson;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class SendSMS_2 extends Service implements Runnable {

	static final String	TAG			= "SMS_SERVIÇO";

	// The vital part, it allows access to location and GPS status services
	private String		phoneNumber;
	private String		isMail		= "1";
	private String		emailAdd;
	private int			time;
	private boolean		executar	= true;
	Thread				myThread;
	boolean				running;
	int					cnt;

	public void onCreate() {
		// new Thread(SendSMS_2.this).start();
		startThread();
	}

	public void startThread() {
		Log.d(TAG, "Log: " + "A");
		running = true;
		Log.d(TAG, "Log: " + "B");
		myThread = new Thread(this);
		Log.d(TAG, "Log: " + "C");
		myThread.start();
		Log.d(TAG, "Log: " + "D");
		cnt = 0;
		Log.d(TAG, "Log: " + "E");
	}

	public void stopThread() {
		Log.d(TAG, "Log: " + "F");
		running = false;
		Log.d(TAG, "Log: " + "G");
		boolean retry = true;
		Log.d(TAG, "Log: " + "H");
		while (retry) {
			Log.d(TAG, "Log: " + "I");
			try {
				Log.d(TAG, "Log: " + "J");
				myThread.join();
				Log.d(TAG, "Log: " + "K");
				retry = false;
				Log.d(TAG, "Log: " + "L");
			} catch (InterruptedException e) {
				Log.d(TAG, "Log: " + "M");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		Log.d(TAG, "Log: " + "N");
		// TODO Auto-generated method stub
		while (running) {
			Log.d(TAG, "Log: " + "O");
			try {
				cnt++;
				Log.d(TAG, "Log: " + "P");
				Log.d(TAG, "Log: " + "Envio!!!!!!");
				getConfigDataBase();
				Log.d(TAG, "Log: " + "Q");
				Thread.sleep(time);

			} catch (InterruptedException e) {
				Log.d(TAG, "Log: " + "R");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getConfigDataBase() {
		Log.d(TAG, "Log: " + "S");
		SQLiteDatabase dbConfiguracao = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);
		Cursor curso = dbConfiguracao.rawQuery("Select celular,sms,mail,timeRequet,email,timeSend from Configuracao", null);
		if (curso.moveToNext()) {

			phoneNumber = curso.getString(0);
			isMail = curso.getString(2).toString();
			time = 1000 * 60 * Integer.valueOf(curso.getString(5)).intValue();

			// Log.d(TAG, "QUAL OPÇÃO DE ENVIO::" + isMail.toString());
			// Log.d(TAG, "QUAL OPÇÃO DE ENVIO::" + curso.getString(1).toString());
			// Log.d(TAG, "QUAL OPÇÃO DE ENVIO::" + isMail.getClass());
			// Log.d(TAG, "QUAL OPÇÃO DE ENVIO::" + isMail.equalsIgnoreCase("1"));

			if (isMail.equalsIgnoreCase("0")) {
				Log.d(TAG, "getConfigDataBase::EMAIL");
				emailAdd = curso.getString(4);
				String message = getCoordenadas();
				EMAIL(phoneNumber, emailAdd, message);
			} else {
				Log.d(TAG, "getConfigDataBase::SMS");
				String message = getCoordenadas();
				if (message != null) {
					// Log.d(TAG, "getConfigDataBase::SMS>>" + message.trim().length());
					SMS(phoneNumber, message.substring(0, message.toString().length()));
				}
			}

		} else {

		}
	}

	@SuppressLint("NewApi")
	private String getCoordenadas() {
		String addressString = null;
		try {
			// addressString = null;
			SQLiteDatabase dbCoordenadas = openOrCreateDatabase("Coordenadas.db", Context.MODE_PRIVATE, null);
			Cursor curso = dbCoordenadas.rawQuery("Select la,lo from Coordenadas", null);
			if (curso.moveToFirst()) {

				String coordinates[] = { curso.getString(0).toString(), curso.getString(1).toString() };
				double lat = Double.parseDouble(coordinates[0]);
				double lng = Double.parseDouble(coordinates[1]);

				Geocoder gc = new Geocoder(this, Locale.getDefault());

				try {

					List<Address> addresses = gc.getFromLocation(lat, lng, 1);
					StringBuilder sb = new StringBuilder();

					if (addresses.size() > 0) {
						Address address = addresses.get(0);

						for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
							sb.append(address.getAddressLine(i));

							sb.append(address.getCountryName());

							addressString = address.getAddressLine(0);
							addressString += "\nCidade:" + address.getLocality();
							// addressString += "\nPais:" + address.getCountryName();
							addressString += "\nCEP:" + address.getPostalCode();
						}
					}
					Log.d(TAG, addressString);
				} catch (IOException e) {
					Log.d(TAG, "Erro:" + e.getMessage());
				}

				// dbCoordenadas.close();
			} else {
				// Toast.makeText(getBaseContext(), "Aguarde! O sistema ainda está em processamento a conexão GPS!",
				// Toast.LENGTH_LONG).show();
			}
		} catch (NumberFormatException e) {
			Log.d(TAG, "Erro:" + e.getMessage());
		}
		return addressString;
	}

	@SuppressWarnings("deprecation")
	private void SMS(String phoneNumber, String message) {
		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, SendSMS_2.class), 0);

		android.telephony.gsm.SmsManager sms = android.telephony.gsm.SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, pi, null);
	}

	private void EMAIL(String phoneNumber, String emailAdd, String mensagemEmail) {
		String emailaddress[] = { emailAdd };
		// Intent emailIntent = new Intent(android.content.Intent.ACTION_SENDTO);
		Intent emailIntent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("mailto", "jeansferreira@gmail.com", null));
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, emailaddress.toString());
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "FindSon - Notificação do Rastreamento!");
		emailIntent.setType("plain/text");
		emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, mensagemEmail.toString());
		// startActivity(emailIntent);
		startService(emailIntent);
		// LocalBroadcastManager.getInstance(this).sendBroadcast(emailIntent);
		// startActivity(Intent.createChooser(emailIntent, "Send mail..."));

	}

	public void onStop() {
		Log.d(TAG, "Thread - FINALIZADA");
		Thread.interrupted();
		// onStop();
		onDestroy();
		executar = false;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
