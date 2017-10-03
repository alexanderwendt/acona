package at.tuwien.ict.acona.demowebservice.cellfunctions.weather;

import java.util.Arrays;

public class Weather {
	public Coordinates coord;
	public WeatherDetails[] weather;
	public String base;
	public String visibility;
	public MainThing main;
	public Wind wind;
	public Clouds clouds;
	public String dt;
	public String id;
	public String name;
	public String cod;
	public System sys;
	
	public class Coordinates {
		public double lon;
		public double lat;
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Coordinates [lon=");
			builder.append(lon);
			builder.append(", lat=");
			builder.append(lat);
			builder.append("]");
			return builder.toString();
		}
	}
	
	public class WeatherDetails {
		public String id;
		public String main;
		public String description;
		public String icon;
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("WeatherDetails [id=");
			builder.append(id);
			builder.append(", main=");
			builder.append(main);
			builder.append(", description=");
			builder.append(description);
			builder.append(", icon=");
			builder.append(icon);
			builder.append("]");
			return builder.toString();
		}
	}
	
	public class MainThing {
		public double temp;
		public double pressure;
		public double humidity;
		public double temp_min;
		public double temp_max;
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("MainThing [temp=");
			builder.append(temp);
			builder.append(", pressure=");
			builder.append(pressure);
			builder.append(", humidity=");
			builder.append(humidity);
			builder.append(", temp_min=");
			builder.append(temp_min);
			builder.append(", temp_max=");
			builder.append(temp_max);
			builder.append("]");
			return builder.toString();
		}
	}
	
	public class Wind {
		public double speed;
		public double deg;
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Wind [speed=");
			builder.append(speed);
			builder.append(", deg=");
			builder.append(deg);
			builder.append("]");
			return builder.toString();
		}
	}
	
	public class Clouds {
		public double all;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Clouds [all=");
			builder.append(all);
			builder.append("]");
			return builder.toString();
		}
	}
	
	public class System {
		public String type;
		public String id;
		public String message;
		public String country;
		public long sunrise;
		public long sunset;
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("System [type=");
			builder.append(type);
			builder.append(", id=");
			builder.append(id);
			builder.append(", message=");
			builder.append(message);
			builder.append(", country=");
			builder.append(country);
			builder.append(", sunrise=");
			builder.append(sunrise);
			builder.append(", sunset=");
			builder.append(sunset);
			builder.append("]");
			return builder.toString();
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Weather [coord=");
		builder.append(coord);
		builder.append(", weather=");
		builder.append(Arrays.toString(weather));
		builder.append(", base=");
		builder.append(base);
		builder.append(", visibility=");
		builder.append(visibility);
		builder.append(", main=");
		builder.append(main);
		builder.append(", wind=");
		builder.append(wind);
		builder.append(", clouds=");
		builder.append(clouds);
		builder.append(", dt=");
		builder.append(dt);
		builder.append(", id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", cod=");
		builder.append(cod);
		builder.append(", sys=");
		builder.append(sys);
		builder.append("]");
		return builder.toString();
	}
}


//
//{
//	"coord": 
//	{
//		"lon": 16.37,
//		"lat": 48.21
//	},
//
//	"weather": 
//	[
//		{
//			"id": 800,
//			"main": "Clear",
//			"description": "clear sky",
//			"icon": "01d"
//		}
//	],
//
//	"base": "stations",
//	"main": 
//	{
//		"temp": 289.17,
//		"pressure": 1023,
//		"humidity": 51,
//		"temp_min": 288.15,
//		"temp_max": 290.15
//	},
//
//	"visibility": 10000,
//	"wind": 
//	{
//		"speed": 7.7,
//		"deg": 140
//	},
//
//	"clouds": 
//	{
//		"all": 0
//	},
//
//	"dt": 1506763800,
//	"sys": 
//	{
//		"type": 1,
//		"id": 5934,
//		"message": 0.0038,
//		"country": "AT",
//		"sunrise": 1506747179,
//		"sunset": 1506789290
//	},
//
//	"id": 2761369,
//	"name": "Vienna",
//	"cod": 200
//}
