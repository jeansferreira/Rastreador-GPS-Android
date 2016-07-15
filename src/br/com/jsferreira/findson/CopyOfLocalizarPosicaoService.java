package br.com.jsferreira.findson;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class CopyOfLocalizarPosicaoService extends Service implements Runnable {

	static final String			TAG					= "GPS_SERVIÇO_LOCALIZACAO";

	// The vital part, it allows access to location and GPS status services
	private String				fone;
	private int					time;

	private SQLiteDatabase		db					= null;

	// //////////////////
	boolean						isGPSEnabled		= false;

	// flag for network status
	boolean						isNetworkEnabled	= false;

	// flag for GPS status
	boolean						canGetLocation		= false;

	Location					location;											// location
	double						latitude;											// latitude
	double						longitude;											// longitude
	double						latitudeOld;											// latitude

	// Declaring a Location Manager
	protected LocationManager	locationManager;

	public void onCreate() {
		// tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);

		// fone = tm.getLine1Number();
		getConfigDataBase();
		// TODO: Obtem celular cadastro para inserir no no log
		// de Localização.
		new Thread(CopyOfLocalizarPosicaoService.this).start();
	}

	/**
	 * 
	 */
	private void getConfigDataBase() {
		SQLiteDatabase dbConfiguracao = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);
		Cursor curso = dbConfiguracao.rawQuery("Select * from Configuracao", null);
		if (curso.moveToNext()) {
			fone = curso.getString(1);

			setInRastreamento("0");
		} else {
			setInRastreamento("1");
		}
		time = 1000 * 60 * Integer.valueOf(curso.getString(7)).intValue();
	}

	/**
	 * 
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 */
	@Override
	public void run() {
		Looper.prepare();
		final Looper looper = Looper.myLooper();

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "Metodo-run::Handler");

				try {
					for (int i = 0; i < 100; i++) {
						getLocation();
						Thread.sleep(time);
					}
				} catch (Exception ex) {
					Log.d(TAG, "run-ERRO:" + ex.getMessage());
				} finally {
					looper.quit();
				}
			}

		}, 100);

		Looper.loop();
	}

	/**
	 * 
	 * @return
	 */
	public Location getLocation() {
		try {
			locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				Log.d(TAG, "2-Metodo::onLocationChanged|Latitude:" + latitude);
				Log.d(TAG, "2-Metodo::onLocationChanged|Longitude:" + longitude);
			}
			
			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				this.canGetLocation = true;
				// First get location from Network Provider
				if (isNetworkEnabled) {
					Log.d("Network", "Network");
					if (locationManager != null) {
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
							//Log.d(TAG, "1-Metodo::onLocationChanged|Latitude:" + latitude);
							//Log.d(TAG, "1-Metodo::onLocationChanged|Longitude:" + longitude);
						}
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						Log.d("GPS Enabled", "GPS Enabled");
						if (locationManager != null) {
							location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
								//Log.d(TAG, "2-Metodo::onLocationChanged|Latitude:" + latitude);
								//Log.d(TAG, "2-Metodo::onLocationChanged|Longitude:" + longitude);
							}
						}
					}
				}

				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(timestamp.getTime());

				// Metodo que vai inserir os dados da posição
				insertLocalizacao(new Double(latitude).toString(), new Double(longitude).toString(), date.toString());
			}

		} catch (Exception e) {
			Log.d(TAG, "Erro-Metodo:getLocation-" + e.getMessage());
		}

		return location;
	}

	/**
	 * 
	 * @param location
	 */
	public void onLocation(Location location) {
		try {
			Log.d(TAG, "Metodo::onLocationChanged");
			double longitude = location.getLongitude();
			double latitude = location.getLatitude();

			// Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(timestamp.getTime());

			insertLocalizacao(new Double(latitude).toString(), new Double(longitude).toString(), date.toString());
		} catch (Exception e) {
			Log.d(TAG, "Metodo - onLocation:Erro: " + e.getMessage());
		}
	}

	private Location getLocation2() {            
        Location gpslocation = null;
        Location networkLocation = null;

        if(locationManager==null){
        	locationManager = (LocationManager) getApplicationContext() .getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            	//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000, 1, this);
                gpslocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            }
            if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            	//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000, 1, this);
                networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); 
            }
        } catch (IllegalArgumentException e) {
            //Log.e(ErrorCode.ILLEGALARGUMENTERROR, e.toString());
            Log.e("error", e.toString());
        }
        if(gpslocation==null && networkLocation==null)
            return null;

        if(gpslocation!=null && networkLocation!=null){
            if(gpslocation.getTime() < networkLocation.getTime()){
                gpslocation = null;
                return networkLocation;
            }else{
                networkLocation = null;
                return gpslocation;
            }
        }
        if (gpslocation == null) {
            return networkLocation;
        }
        if (networkLocation == null) {
            return gpslocation;
        }
        return null;
    }
	/**
	 * 
	 * @param start
	 */
	private void setInRastreamento(String start) {
		try {
			Log.d(TAG, "Metodo::setInRastreamento");
			db = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);

			String sql = "UPDATE Configuracao SET isRastreando='" + start + "'";
			db.execSQL(sql);
		} catch (Exception e) {
			Log.d(TAG, "setInRastreamento-ERRO:" + e.getMessage());
		}
	}

	/**
	 * 
	 * @param la
	 * @param lo
	 * @param myDate
	 */
	private void insertLocalizacao(String la, String lo, String myDate) {

		try {
			Log.d(TAG, "Metodo - insertLocalizacao");
			db = openOrCreateDatabase("Coordenadas.db", Context.MODE_PRIVATE, null);
			ContentValues ctv = new ContentValues();

			ctv.put("celular", fone);
			ctv.put("la", la);
			ctv.put("lo", lo);
			ctv.put("myDate", myDate);

			String s = "Dados da Localização: Celular[" + fone + "] \nLongitude[" + la + "] \nLatitude[" + lo + "] \nData/Hora[" + myDate
					+ "]";

			s = s + "\n-----------------------";

			if (latitude != latitudeOld){
				if (db.insert("Coordenadas", "_id", ctv) > 0) {
					try {
						Log.d(TAG, "Log:" + s);
						//sendSMS(fone,s);
						Log.d(TAG, "Sucesso em inserir localização!");
					} catch (Exception e) {
						Log.d(TAG, "Send:" + e.getMessage());
					}
				} else {
					Log.d(TAG, "Erro em inserir o localização!");
				}
			}else{
				Log.d(TAG, "Sem mudança em posição atual!");
			}
		} catch (Exception e) {
			Log.d(TAG, "ERRO-Metodo:insertLocalizacao-" + e.getMessage());
		}
	}

	/*
	public void sendSMS(String phoneNumber, String message) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

		// ---when the SMS has been sent---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
				break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
				break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
				break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
				break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
				break;
				}
			}
		}, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
				break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
				break;
				}
			}
		}, new IntentFilter(DELIVERED));

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}
	*/
}
