import 'package:flutter/material.dart';
import 'package:movieratings/utils/text.dart';
import '../description.dart';

class TrendingMovies extends StatelessWidget {
  final List trending;

  const TrendingMovies({Key? key, required this.trending}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(10),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          modified_text(
            text: 'Trending Movies',
            color: Colors.white,
            size: 26,
          ),

          const SizedBox(height: 10),

          SizedBox(
            height: 270,
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              itemCount: trending.length,
              itemBuilder: (context, index) {

                // 🔹 Base image URL
                const imageBase = "https://image.tmdb.org/t/p/w500";

                // 🔹 Safe poster & banner paths
                String? posterPath = trending[index]['poster_path'];
                String? backdropPath = trending[index]['backdrop_path'];

                String posterUrl = posterPath != null
                    ? "$imageBase$posterPath"
                    : "https://via.placeholder.com/300x450";

                String bannerUrl = backdropPath != null
                    ? "$imageBase$backdropPath"
                    : "https://via.placeholder.com/500x300";

                return InkWell(
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => Description(
                          name: trending[index]['title'],
                          bannerurl: bannerUrl,
                          posterurl: posterUrl,
                          description: trending[index]['overview'],
                          vote: trending[index]['vote_average'].toString(),
                          launch_on: trending[index]['release_date'],
                        ),
                      ),
                    );
                  },

                  child: Container(
                    width: 140,
                    margin: const EdgeInsets.only(right: 8),
                    child: Column(
                      children: [
                        // 🎥 Movie Poster
                        Container(
                          height: 200,
                          decoration: BoxDecoration(
                            borderRadius: BorderRadius.circular(10),
                            image: DecorationImage(
                              image: NetworkImage(posterUrl),
                              fit: BoxFit.cover,
                            ),
                          ),
                        ),

                        const SizedBox(height: 5),

                        // 🎬 Movie Title
                        modified_text(
                          size: 14,
                          color: Colors.white,
                          text: trending[index]['title'] ?? "No Title",
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
