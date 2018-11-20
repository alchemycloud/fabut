package cloud.alchemy.fabut.model.test;

public class Student {

    private Address address;
    private String name;
    private String lastName;
    private Faculty faculty;

    public Student() {

    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(final Faculty faculty) {
        this.faculty = faculty;
    }

}
