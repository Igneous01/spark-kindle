package kindle.domain;

public class KindleArguments {
    String request;
    String matcher;
    String[] appArgs;

    public KindleArguments(String request, String matcher, String[] appArgs) {
        this.request = request;
        this.matcher = matcher;
        this.appArgs = appArgs;
    }

    public final String getRequest() {
        return request;
    }

    public final String getMatcher() {
        return matcher;
    }

    public final String[] getAppArgs() {
        return appArgs;
    }
}
