package cloud.alchemy.fabut.model.test;

public class Faculty {

    private String name;
    private Teacher teacher;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(final Teacher teacher) {
        this.teacher = teacher;
    }
    
    /**
     * Returns a string representation of this Faculty instance.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "Faculty{" +
               "name='" + (name != null ? name : "null") + "'" +
               ", teacher=" + (teacher != null ? teacher : "null") +
               '}';
    }
}
