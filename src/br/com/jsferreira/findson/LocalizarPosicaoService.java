package br.com.jsferreira.findson;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class LocalizarPosicaoService extends Service implements Runnable, LocationListener {

	static final String			TAG								= "GPS_SERVIÇO_LOCALIZACAO";
	private static final long	MIN_DISTANCE_CHANGE_FOR_UPDATES	= 50;							// 10 meters

	// The minimum time between updates in milliseconds
	private static final long	MIN_TIME_BW_UPDATES				= 1000 * 60 * 1;				// 1 minute

	// The vital part, it allows access to location and GPS status services
	private int					time;

	// //////////////////
	boolean						isGPSEnabled					= false;

	// flag for network status
	boolean						isNetworkEnabled				= false;

	// flag for GPS status
	boolean						canGetLocation					= false;

	Location					location;
	double						latitude;
	double						longitude;
	double						latitudeOld;
	double						longitudeOld;

	// Declaring a Location Manager
	protected LocationManager	locationManager;
	boolean						executar						= true;

	public void onCreate() {
		setConfigDataBase();
		new Thread(LocalizarPosicaoService.this).start();
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
				//Log.d(TAG, "1-Metodo-run::Handler");
				//Log.d(TAG, "2-Metodo::run|Executando:" + executar);
				try {
					for (;;) {
						boolean condicao = getConfigDataBase();
						//Log.d(TAG, "4-Metodo::run|Executando:" + condicao);
						if (condicao) {
							getLocation();
						}
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
	 */
	private void setConfigDataBase() {
		SQLiteDatabase dbConfiguracao = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);
		Cursor curso = dbConfiguracao.rawQuery("Select * from Configuracao", null);
		if (curso.moveToNext()) {
			setInRastreamento("0");
		} else {
			setInRastreamento("1");
		}
		dbConfiguracao.close();
	}

	private boolean getConfigDataBase() {

		boolean canConfigDataBase = false;
		try {

			SQLiteDatabase dbConfiguracao = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);
			Cursor curso = dbConfiguracao.rawQuery("Select * from Configuracao", null);
			if (curso.moveToNext()) {
				time = 1000 * 60 * Integer.valueOf(curso.getString(7)).intValue();
				String rast = curso.getString(6).toString();
				//Log.d(TAG, "3-Metodo::run|Executando:" + rast);
				if (rast.equalsIgnoreCase("0")) {
					//Log.d(TAG, "3-Metodo::run|Executando:IF");
					canConfigDataBase = true;
				} else {
					//Log.d(TAG, "3-Metodo::run|Executando:ELSE");
					canConfigDataBase = false;
				}
			}
			dbConfiguracao.close();

		} catch (Exception e) {
			Log.d(TAG, "Metodo::getConfigDataBase-Erro:" + e.getMessage());
		}
		return canConfigDataBase;

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
	 * @return
	 */
	public void getLocation() {
		try {
			locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			//locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (isNetworkEnabled) {
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
				location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (location != null) {
					latitude = location.getLatitude();
					longitude = location.getLongitude();
					Log.d(TAG, "0-Metodo::onLocationChanged|Latitude:" + latitude);
					Log.d(TAG, "0-Metodo::onLocationChanged|Longitude:" + longitude);
				}
			}
			if (isGPSEnabled) {
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
				location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (location != null) {
					latitude = location.getLatitude();
					longitude = location.getLongitude();
					Log.d(TAG, "1-Metodo::onLocationChanged|Latitude:" + latitude);
					Log.d(TAG, "1-Metodo::onLocationChanged|Longitude:" + longitude);
				}
			}

			if ((latitude != latitudeOld) && longitude != longitudeOld) {
				Log.d(TAG, "SEM ALTERAÇÃO DE POSIÇÃO!");
				latitudeOld = latitude;
				longitudeOld = longitude;
				Log.d(TAG, "2-Metodo::onLocationChanged|Latitude:" + latitude);
				Log.d(TAG, "2-Metodo::onLocationChanged|Longitude:" + longitude);

				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(timestamp.getTime());

				// Metodo que vai inserir os dados da posição
				insertLocalizacao(new Double(latitude).toString(), new Double(longitude).toString(), date.toString());
			} else {
				Log.d(TAG, "SEM ALTERAÇÃO DE POSIÇÃO!");
			}
		} catch (Exception e) {
			Log.d(TAG, "Erro-Metodo:getLocation-" + e.getMessage());
		}
	}

	/**
	public void onLocation(Location location) {
		try {
			// Log.d(TAG, "Metodo::onLocationChanged");
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
	*/

	/**
	 * 
	 * @param start
	 */
	private void setInRastreamento(String start) {
		try {
			SQLiteDatabase dbConfiguracao = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);
			//Log.d(TAG, "Metodo::setInRastreamento-1");
			//Log.d(TAG, "Metodo::setInRastreamento-1"+dbConfiguracao);
			dbConfiguracao = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);
			//Log.d(TAG, "Metodo::setInRastreamento-2");

			String sql = "UPDATE Configuracao SET isRastreando='" + start + "'";
			//Log.d(TAG, "Metodo::setInRastreamento-3");
			dbConfiguracao.execSQL(sql);
			//Log.d(TAG, "Metodo::setInRastreamento-4");
			dbConfiguracao.close();
			//Log.d(TAG, "Metodo::setInRastreamento-5");
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
			// Log.d(TAG, "Metodo - insertLocalizacao");
			SQLiteDatabase dbCoordenadas = openOrCreateDatabase("Coordenadas.db", Context.MODE_PRIVATE, null);
			dbCoordenadas = openOrCreateDatabase("Coordenadas.db", Context.MODE_PRIVATE, null);
			ContentValues ctv = new ContentValues();

			ctv.put("address", getAddress());
			ctv.put("la", la);
			ctv.put("lo", lo);
			ctv.put("myDate", myDate);

			if (dbCoordenadas.insert("Coordenadas", "_id", ctv) > 0) {
				try {
					Log.d(TAG, "Sucesso em inserir localização!");
				} catch (Exception e) {
					Log.d(TAG, "Send:" + e.getMessage());
				}
			} else {
				Log.d(TAG, "Erro em inserir o localização!");
			}
			dbCoordenadas.close();
		} catch (Exception e) {
			Log.d(TAG, "ERRO-Metodo:insertLocalizacao-" + e.getMessage());
		}
	}

	private String getAddress() {
		Geocoder gc = new Geocoder(this, Locale.getDefault());
		String addressString = "Sem deslocamento!";
		try {

			List<Address> addresses = gc.getFromLocation(latitude, longitude, 1);
			StringBuilder sb = new StringBuilder();

			if (addresses.size() > 0) {
				Address address = addresses.get(0);

				for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
					sb.append(address.getAddressLine(i));

				sb.append(address.getCountryName());

				addressString = address.getAddressLine(0);
				addressString += "\nCidade:" + address.getLocality();
				// addressString += "\nPais:" + address.getCountryName();
				addressString += "\nCEP:" + address.getPostalCode();
			}
			Log.d(TAG, addressString);
		} catch (IOException e) {
			Log.d(TAG, "getAddress-Erro:" + e.getMessage());
		}
		return addressString;
	}

	public void onFinalizar() {
		try {
			//Log.d(TAG, "Thread - FINALIZADA");
			executar = false;
			//Log.d(TAG, "Thread - FINALIZADA:" + executar);
			setInRastreamento("1");
			Thread.interrupted();
		} catch (Exception e) {
			Log.d(TAG, "onFinalizar - Erro:" + e.getMessage());
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
