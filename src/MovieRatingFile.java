import java.util.List;

public class MovieRatingFile {
    private String Header;
    private List<String> Content;

    public String getHeader() {
        return Header;
    }

    public void setHeader(String header) {
        Header = header;
    }

    public List<String> getContent() {
        return Content;
    }

    public void setContent(List<String> content) {
        Content = content;
    }
}
