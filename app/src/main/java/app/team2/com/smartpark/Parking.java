package app.team2.com.smartpark;

public class Parking {

    private String nom;
    private int nb_place;
    private int nb_place_libre;
    private double lat;
    private double lon;

    public Parking() {
    }

    public Parking(String nom, int nb_place, int nb_place_libre, double lat, double lon) {
        this.nom = nom;
        this.nb_place = nb_place;
        this.nb_place_libre = nb_place_libre;
        this.lat = lat;
        this.lon = lon;
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

    public void setNb_place_libre(int nb_place_libre) {
        this.nb_place_libre = nb_place_libre;
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
}
