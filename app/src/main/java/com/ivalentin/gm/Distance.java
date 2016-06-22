package com.ivalentin.gm;

/**
 * Static class that allows to calculate the distance between tho 
 * sets of GPS coordinates.
 * 
 * @author Iñigo Valentin
 *
 */
public final class Distance {
	
	/**
	 * Calculates the distance between two points (given the latitude/longitude of those points). 
	 * 
	 * Official Web site: http://www.geodatasource.com 
	 * GeoDataSource.com (C) All Rights Reserved 2015
	 * 
	 * @param lat1 Latitude of the first point
	 * @param lon1 Longitude of the first point
	 * @param lat2 Latitude of the second point
	 * @param lon2 Longitude of the second point
	 * @param unit The result unit. 'M' for statute miles (default), 'K' for kilometers and 'N' for nautical miles.
	 * 
	 * @return The distance between the points
	 */
	public static double calculateDistance(double lat1, double lon1, double lat2, double lon2, char unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == 'K') {
			dist = dist * 1.609344;
		}
		else if (unit == 'N') {
			dist = dist * 0.8684;
		}
		return (dist);
	}
	
	/**
	 * Converts decimal degrees to radians.
	 * 
	 * @param deg Decimal degrees.
	 * 
	 * @return Radians.
	 */
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}
	
	/**
	 * Converts radians to decimal degrees.
	 * 
	 * @param rad Radans.
	 * 
	 * @return Decimal degrees.
	 */
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}
	
}
