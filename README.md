# PopularMovies

This is the first two projects of Udacity's Android Developer Nanodegree program.

PopularMovies queries themoviedb.org for current popular movies and displays them
in a GridView with thumbnails of each poster. When a user clicks on a poster, a
new fragment presents the user with the title, release year, length, rating per
themoviedb.org, a brief description of the plot, links to youtube trailers, and
user reviews.

The user is also able to keep a list of their favorite movies. The list is
accessible via the action bar. Movies marked as a favorite will be saved to a 
local database where their details can be viewed offline.

### Generating an APK
You must add your own key for themoviedb.org's API. Add your key to the project
level gradle.properties file.

