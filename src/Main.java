import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class Main {

    private static String inputPath = "input/RatingsInput.csv";
    private static String newUsersPath = "input/NewUsers.csv";
    private static String stagingPath = "output/staging1.csv";
    private static String outputPath = "output/results.csv";
    private static String separator = ",";
    private static String lineSeparator = System.lineSeparator();
    private static Boolean enclosedByQuotes = true;
    private static Integer firstLineAt = 1;


    public static void main(String[] args) throws MalformedFileException {
        File input = new File(inputPath);
        File newUsers = new File(newUsersPath);

        if (input.exists() && !input.isDirectory() &&
                newUsers.exists() && !newUsers.isDirectory()) {
            System.out.println("Reading " + input.getPath());

            MovieRatingFile inputData = readFile(input);
            List<String> rawData = inputData.getContent();
            inputData.setContent(cleanup(rawData));
            // Store data in the staging area
            saveFile(inputData, stagingPath);
            // parseContent returns the following data structure
            // HashMap<Age, HashMap<Rating, MovieList>>
            // 32 : {5: ["M1","M2","M3"], 4: ["M4". "M5"]}, 20: {5: ...}
            HashMap<Integer, HashMap> movieRatings = parseContent(inputData);

            // Now that we have our data in place we can apply the recommendation
            // system to the output file
            MovieRatingFile outputData = readFile(newUsers);
            String[] tokens;

            List<String> newUsersContent = outputData.getContent();
            // Iterate the newUsers file content to replace the ? character
            // with the list of recommended movies by age and according to
            // the number of movies to recommend
            for(int i = 0; i < newUsersContent.size(); i++){
                String row = newUsersContent.get(i);
                // Need to split the comma separated values in order
                // to get the corresponding params
                tokens = row.split(separator);
                Integer userAge = Integer.parseInt(tokens[1]);
                Integer moviesToRecommend = Integer.parseInt(tokens[2]);

                // If the target age doesn't exist in the data structure
                // then we'll need to get the closest age bucket
                if (!movieRatings.containsKey(userAge)) {
                    userAge = getClosestAge(movieRatings, userAge);
                }
                // Gets a list of recommended movies by age
                ArrayList<String> recommendedMovies = getRecommendedMoviesByAge(movieRatings, userAge);

                if (recommendedMovies.size() > 0) {
                    if (moviesToRecommend > recommendedMovies.size())
                        moviesToRecommend = recommendedMovies.size();

                    String listOfMovies = listToString(recommendedMovies.subList(0, moviesToRecommend));
                    row = row.replace("?", listOfMovies);
                }

                newUsersContent.set(i, row);
            }

            outputData.setContent(newUsersContent);
            saveFile(outputData, outputPath);
        }
    }

    static String listToString(List<String> movies) {
        String result = "\"";
        result += String.join(",", movies);
        result += "\"";
        return result;
    }

    static Integer getClosestAge(HashMap<Integer, HashMap> ratingsMap, Integer userAge) {
        // Sort the HaspMap by age
        TreeMap<Integer, HashMap> sortedAge = new TreeMap<>(ratingsMap);
        Integer delta, minDistance, minDistanceKey;
        minDistance = 100;
        minDistanceKey = userAge;
        // Iterate the map to find the closest (minimum distance) age group in the map
        for (Map.Entry<Integer, HashMap> entry : sortedAge.descendingMap().entrySet()) {
            delta = Math.abs(entry.getKey() - userAge);

            if (delta < minDistance) {
                minDistance = delta;
                minDistanceKey = entry.getKey();
            }
        }
        return minDistanceKey;
    }

    static HashMap<Integer, HashMap> parseContent(MovieRatingFile cleanData) {
        String[] tokens;
        HashMap<Integer, HashMap> ageMap = new HashMap<>();

        for (String line : cleanData.getContent()) {
            tokens = line.split(separator);
            // read params
            Integer rating = Integer.parseInt(tokens[6]);
            String movieName = tokens[5];
            Integer age = Integer.parseInt(tokens[2]);

            if (ageMap.containsKey(age)) {
                HashMap<Integer, ArrayList> ratingAgeMap = ageMap.get(age);

                if (ratingAgeMap != null &&
                        !ratingAgeMap.containsKey(rating)) {
                    ArrayList<String> movie = new ArrayList<String>();
                    movie.add(movieName);
                    ratingAgeMap.put(rating, movie);
                } else {
                    if (!ratingAgeMap.get(rating).contains(movieName))
                        ratingAgeMap.get(rating).add(movieName);
                }

            } else {
                HashMap<Integer, ArrayList> ratingAgeMap = new HashMap<>();
                ArrayList<String> movie = new ArrayList<>();
                movie.add(movieName);
                ratingAgeMap.put(rating, movie);
                ageMap.put(age, ratingAgeMap);
            }
        }
        return ageMap;
    }

    static ArrayList getRecommendedMoviesByAge(HashMap<Integer, HashMap> movieRatings, Integer age) {
        ArrayList<String> movies = new ArrayList<>();
        if (movieRatings.containsKey(age)) {
            HashMap<Integer, ArrayList> ratings = movieRatings.get(age);
            TreeMap<Integer, ArrayList> sortedRatings = new TreeMap<>(ratings);

            sortedRatings.descendingMap().forEach((key, value) -> {
                movies.addAll(value);
            });
        }

        return movies;
    }


    static MovieRatingFile readFile(File inputFile) {

        String line = "";
        Integer numberOfLines = 0;
        MovieRatingFile file = new MovieRatingFile();
        List<String> content = new ArrayList<>();

        try {
            FileReader fileReader = new FileReader(inputFile);
            BufferedReader bufferReader = new BufferedReader(fileReader);


            while ((line = bufferReader.readLine()) != null) {
                if (numberOfLines < firstLineAt) {
                    file.setHeader(line);
                } else {
                    content.add(line);
                }

                numberOfLines++;
            }
            file.setContent(content);

            bufferReader.close();
            fileReader.close();

            return file;

        } catch (FileNotFoundException fnf) {
            System.out.println("Unable to open file" + inputFile.getPath());
        } catch (IOException io) {
            System.out.println("Error reading file " + inputFile.getPath());
        }
        return null;

    }

    static void saveFile(MovieRatingFile cleanContent, String filePath) {
        try {
            FileWriter fw = new FileWriter(filePath);
            fw.write(cleanContent.getHeader() + lineSeparator);
            fw.flush();
            for (String line : cleanContent.getContent()) {
                fw.write(line + lineSeparator);
            }

            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static List<String> cleanup(List<String> raw) {
        List<String> data = new ArrayList<>();

        for (String line : raw) {
            // Helps to split MovieId from MovieName
            String newLine = line.replaceAll("\"", "");
            // Now that the comma separated values are ok we can extract
            // the movie name and capitalize it
            String[] tokens = newLine.split(separator);
            newLine = newLine.replaceAll(tokens[5], capitalizeWord(tokens[5]));
            data.add(newLine);
        }

        return data;
    }

    public static String capitalizeWord(String str) {
        String words[] = str.split("\\s");
        String capitalizeWord = "";
        for (String w : words) {
            String first = w.substring(0, 1);
            String afterfirst = w.substring(1);
            capitalizeWord += first.toUpperCase() + afterfirst + " ";
        }
        return capitalizeWord.trim();
    }
}