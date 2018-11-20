package cloud.alchemy.fabut.property;


public class PropertyPath<T> {

    private static final String DOT = ".";
    private String path;

    public PropertyPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public <S> PropertyPath<S> chain(PropertyPath<S> addPath) {
        return new PropertyPath<S>(path + DOT + addPath.path);
    }
}
