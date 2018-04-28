package app.team2.com.smartpark;

import com.google.android.gms.maps.model.LatLng;

public class Parking {

    private String nom;
    private int nb_place;
    private int nb_place_libre;
    private double lat;
    private double lon;
    private int nb_place_reserve;
    private String trf;

    public Parking() {
    }

    public Parking(String nom, int nb_place, int nb_place_vide, double lat, double lon, int nb_place_reserve,String trf) {
        this.nom = nom;
        this.nb_place = nb_place;
        this.nb_place_libre = nb_place_vide;
        this.lat = lat;
        this.lon = lon;
        this.nb_place_reserve = nb_place_reserve;
        this.trf = trf;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getNb_place() {
        return nb_place;
    }

    public void setNb_place(int nb_place) {
        this.nb_place = nb_place;
    }

    public int getNb_place_libre() {
        return nb_place_libre;
    }

    public void setNb_place_libre(int nb_place_vide) {
        this.nb_place_libre = nb_place_vide;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getNb_place_reserve() {
        return nb_place_reserve;
    }

    public void setNb_place_reserve(int nb_place_reserve) {
        this.nb_place_reserve = nb_place_reserve;
    }

    public String getTarif() {
        return trf;
    }

    public void setTarif(String trf) {
        this.trf = trf;
    }

    public LatLng getLatLng(){
        return new LatLng(this.lat, this.lon);
    }
}
