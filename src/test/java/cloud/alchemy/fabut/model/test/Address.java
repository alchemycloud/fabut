package cloud.alchemy.fabut.model.test;

public class Address {

    private String city;
    private String street;
    private String streetNumber;

    public Address() {}

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(final String street) {
        this.street = street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(final String streetNumber) {
        this.streetNumber = streetNumber;
    }
    
    /**
     * Returns a string representation of this Address instance.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "Address{" +
               "city='" + (city != null ? city : "null") + "'" +
               ", street='" + (street != null ? street : "null") + "'" +
               ", streetNumber='" + (streetNumber != null ? streetNumber : "null") + "'" +
               '}';
    }
}
