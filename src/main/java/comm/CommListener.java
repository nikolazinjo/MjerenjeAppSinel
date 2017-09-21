package comm;

public interface CommListener {

    void commEvent(CommStatusEvents event, String status, String shortDesc);

}
