package com.powsybl.iidm.geodata.utils;

/**
 * @author Chamseddine Benhamed {@literal <chamseddine.benhamed at rte-france.com>}
 */
public final class DistanceCalculator {

    private DistanceCalculator() {

    }

    /**
     * Compute an approximate distance in meters between two geographical points (latitude, longitude in degrees).
     * The computation assumes that the earth is spherical and its radius is equal to 6378137 meters.
     * @param the lat and lon of the two points in degrees
     * @return the approximate distance in meters
     */
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        // source : https://geodesie.ign.fr/contenu/fichiers/Distance_longitude_latitude.pdf
        if (lat1 == lat2 && lon1 == lon2) {
            return 0;
        } else {
            double dL = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(dL));
            dist = Math.acos(dist);
            //6 378 137 is the conventional earth radius
            return dist * 6_378_137;
        }
    }
}
