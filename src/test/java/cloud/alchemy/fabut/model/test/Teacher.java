package cloud.alchemy.fabut.model.test;

public class Teacher {

    private String name;
    private Student student;
    private Address address;

    public Teacher() {}

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(final Student student) {
        this.student = student;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }
    
    /**
     * Returns a string representation of this Teacher instance.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "Teacher{" +
               "name='" + (name != null ? name : "null") + "'" +
               ", student=" + (student != null ? student : "null") +
               ", address=" + (address != null ? address : "null") +
               '}';
    }
}
