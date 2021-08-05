package nz.pumbas.utilities.io;

public enum ImageType
{
    PNG("png"),
    JPG("jpg");

    private final String type;

    ImageType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
