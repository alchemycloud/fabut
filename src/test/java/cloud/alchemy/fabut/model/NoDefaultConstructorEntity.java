package cloud.alchemy.fabut.model;

public class NoDefaultConstructorEntity extends NoDefaultConstructorType {

    private Integer id;

    public NoDefaultConstructorEntity(final String property, final Integer id) {
        super(property);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

}
