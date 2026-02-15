import 'package:flutter/material.dart';
import 'package:movieratings/utils/text.dart';

class Description extends StatelessWidget {
  final String? name;
  final String? description;
  final String? bannerurl;
  final String? posterurl;
  final String? vote;
  final String? launch_on;

  const Description({
    Key? key,
    this.name,
    this.description,
    this.bannerurl,
    this.posterurl,
    this.vote,
    this.launch_on,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: ListView(
        children: [
          // 🔥 Banner Image
          Container(
            height: 250,
            child: Stack(
              children: [
                Image.network(
                  bannerurl ?? 
                      "https://via.placeholder.com/500x300?text=No+Image",
                  width: double.infinity,
                  fit: BoxFit.cover,
                ),

                // ⭐ Rating
                Positioned(
                  bottom: 10,
                  left: 10,
                  child: modified_text(
                    text: "⭐ Average Rating - ${vote ?? "N/A"}",
                    color: Colors.white,
                    size: 16,
                  ),
                ),
              ],
            ),
          ),

          SizedBox(height: 15),

          // 🎬 Movie Name
          Padding(
            padding: const EdgeInsets.all(10),
            child: modified_text(
              text: name ?? "No Title Available",
              size: 24,
              color: Colors.white,
            ),
          ),

          // 📅 Release Date
          Padding(
            padding: const EdgeInsets.only(left: 10),
            child: modified_text(
              text: "Releasing On - ${launch_on ?? "Unknown"}",
              size: 14,
              color: Colors.white,
            ),
          ),

          SizedBox(height: 10),

          // 🎥 Poster + Description Row
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                height: 200,
                width: 120,
                child: Image.network(
                  posterurl ??
                      "https://via.placeholder.com/300x450?text=No+Poster",
                  fit: BoxFit.cover,
                ),
              ),

              Flexible(
                child: Padding(
                  padding: const EdgeInsets.all(10),
                  child: modified_text(
                    text: description ?? "No description available for this movie.",
                    size: 16,
                    color: Colors.white,
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
