public class MovieRating {

    private Integer UserId;
    private String UserName;
    private Integer UserAge;
    private Integer MovieID;
    private String MovieName;
    private Byte Rating;
    private Integer x;

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public Integer getUserAge() {
        return UserAge;
    }

    public void setUserAge(Integer userAge) {
        UserAge = userAge;
    }

    public Integer getMovieID() {
        return MovieID;
    }

    public void setMovieID(Integer movieID) {
        MovieID = movieID;
    }

    public String getMovieName() {
        return MovieName;
    }

    public void setMovieName(String movieName) {
        MovieName = movieName;
    }

    public Byte getRating() {
        return Rating;
    }

    public void setRating(Byte rating) {
        Rating = rating;
    }

    public Integer getUserId() {
        return this.UserId;
    }

    public void setUserId(Integer userId) {
        this.UserId = userId;
    }



}
