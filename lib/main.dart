 import 'package:flutter/material.dart';
import 'package:movieratings/utils/text.dart';
import 'package:movieratings/widgets/toprated.dart';
import 'package:movieratings/widgets/trending.dart';
import 'package:movieratings/widgets/tv.dart';
import 'package:tmdb_api/tmdb_api.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await dotenv.load(fileName: ".env");
  print(dotenv.env);

  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});
  
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme:ThemeData(brightness: Brightness.dark),
      home: Home(),
    );
  }
}
class Home extends StatefulWidget {
  const   Home({super.key});

   


  @override
  State<Home> createState() => _MyWidgetState();
}


class _MyWidgetState extends State<Home> {
     final  apiKey= dotenv.env['TMDB_API_KEY'];
      final readaccessToken = dotenv.env['TMDB_ACCESS_TOKEN'];

@override
void initState(){
super.initState();
  loadmovies(); // THIS HELPS TO INTITALISE THE FUNCTIONS  AT THE STARTING OF APPLCIATION
}
///ALL THIS TO SEE IN TERMINal api calling is going alright and is api key working fine?


   loadmovies() async {
  
    if (apiKey == null || readaccessToken == null) {
    print("❌ API keys not loaded from .env");
    return;
  }

  TMDB tmdbWithCustomLogs = TMDB(
    ApiKeys(apiKey!, readaccessToken!),
    logConfig: ConfigLogger(
      showLogs: true,
      showErrorLogs: true,
    ),
  );

  Map trendingresult = await tmdbWithCustomLogs.v3.trending.getTrending();
  Map topratedresultApi = await tmdbWithCustomLogs.v3.movies.getTopRated();
  Map tvresultApi = await tmdbWithCustomLogs.v3.tv.getPopular();

  setState(() {
    trendingmovies = trendingresult['results'];
    topratedmovies = topratedresultApi['results'];
    tvresult = tvresultApi['results'];
  });
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  //   TMDB tmdbWithCustomLogs = TMDB(// this is a tmddb section client which passes api key,read acess token

  //     ApiKeys(apiKey!, readaccessToken!),  
  //     logConfig: ConfigLogger(
  //       showLogs: true,      //Logging is enabled so you can see: api request,erros in the console
  //       showErrorLogs: true,
  //     ),
  //   );
  //  Map trendingresult = await tmdbWithCustomLogs.v3.trending.getTrending();
  //   Map topratedresultApi = await tmdbWithCustomLogs.v3.movies.getTopRated();
  //   Map tvresultApi= await tmdbWithCustomLogs.v3.tv.getPopular();
  //   print((trendingresult));

  //   setState(() { //updates the data regularly and screen also
  //     trendingmovies = trendingresult['results'];
  //     topratedmovies = topratedresultApi['results'];
  //     tvresult = tvresultApi['results'];
  //   });
    print(trendingmovies);
    print(tvresult);
    print(topratedmovies);



  }



List trendingmovies=[];
List topratedmovies = [];
List tvresult = [];

   
  @override
  Widget build(BuildContext context) {
    return Scaffold(
        backgroundColor: Colors.black,
        appBar: AppBar(
          title: modified_text(text: 'Flutter Movie App ❤️', color:Colors.white, size:14,),
          backgroundColor: Colors.transparent,
        ),
        body: ListView(
          children: [
            
            TrendingMovies(
              trending: trendingmovies
            ),
            TV(tv:tvresult),
            TopRatedMovies(toprated: topratedmovies,
              
            ),
          ],
        ));
  }
}