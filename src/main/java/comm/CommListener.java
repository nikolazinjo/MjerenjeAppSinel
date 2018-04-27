package comm;

public interface CommListener {

    void commEvent(final CommStatusEvents event, String status, String shortDesc);

}
