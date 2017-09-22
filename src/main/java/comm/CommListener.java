package comm;

public interface CommListener {

    void commEvent(final CommStatusEvents event,final String status,final String shortDesc);

}
