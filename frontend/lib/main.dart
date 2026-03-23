import 'package:flutter/material.dart';

import 'api/api_client.dart';
import 'screens/auth_screen.dart';
import 'screens/dashboard_screen.dart';
import 'screens/goal_plan_screen.dart';
import 'screens/profile_screen.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // TRY THIS: Try running your application with "flutter run". You'll see
        // the application has a purple toolbar. Then, without quitting the app,
        // try changing the seedColor in the colorScheme below to Colors.green
        // and then invoke "hot reload" (save your changes or press the "hot
        // reload" button in a Flutter-supported IDE, or press "r" if you used
        // the command line to start the app).
        //
        // Notice that the counter didn't reset back to zero; the application
        // state is not lost during the reload. To reset the state, use hot
        // restart instead.
        //
        // This works for code too, not just values: Most code changes can be
        // tested with just a hot reload.
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  // Nếu bạn chạy backend trên cùng máy:
  // - Android emulator: cân nhắc đổi thành `http://10.0.2.2:8080`
  static const String _baseUrl = 'http://localhost:8080';

  final ApiClient _apiClient = const ApiClient(baseUrl: _baseUrl);

  AuthResponse? _auth;
  bool _bootstrapping = false;
  int _tabIndex = 0;
  int? _profileId;
  ProfileResponse? _latestProfile;
  PlanResponse? _currentPlan;

  Future<void> _bootstrapAfterLogin(AuthResponse auth) async {
    setState(() {
      _auth = auth;
      _bootstrapping = true;
    });
    try {
      final bootstrap = await _apiClient.getBootstrap(auth.userId);
      if (!mounted) return;
      setState(() {
        _latestProfile = bootstrap.latestProfile;
        _profileId = bootstrap.latestProfile?.profileId;
        _currentPlan = bootstrap.latestPlan;
      });
    } catch (_) {
      // Allow user continue even when no data exists yet.
    } finally {
      if (mounted) {
        setState(() => _bootstrapping = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_auth == null) {
      return AuthScreen(
        apiClient: _apiClient,
        onAuthenticated: (auth) {
          _bootstrapAfterLogin(auth);
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(auth.message ?? 'Xac thuc thanh cong')),
          );
        },
      );
    }

    if (_bootstrapping) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    final pages = <Widget>[
      ProfileScreen(
        apiClient: _apiClient,
        userId: _auth!.userId,
        initialProfile: _latestProfile,
        onProfileSaved: (id) {
          setState(() {
            _profileId = id;
            _latestProfile = null;
          });
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Lưu profile BMI thành công')),
          );
        },
      ),
      GoalPlanScreen(
        apiClient: _apiClient,
        userId: _auth!.userId,
        profileId: _profileId,
        initialPlan: _currentPlan,
        onPlanCreated: (plan) {
          setState(() => _currentPlan = plan);
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Tạo lộ trình thành công')),
          );
        },
      ),
      DashboardScreen(
        apiClient: _apiClient,
        profileId: _profileId,
        currentPlan: _currentPlan,
      ),
    ];

    return Scaffold(
      appBar: AppBar(
        title: Text('${widget.title} • ${_auth!.email}'),
        actions: [
          IconButton(
            tooltip: 'Dang xuat',
            onPressed: () {
              setState(() {
                _auth = null;
                _latestProfile = null;
                _profileId = null;
                _currentPlan = null;
                _tabIndex = 0;
              });
            },
            icon: const Icon(Icons.logout),
          ),
        ],
      ),
      body: pages[_tabIndex],
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _tabIndex,
        onTap: (i) => setState(() => _tabIndex = i),
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.person), label: 'Profile'),
          BottomNavigationBarItem(
              icon: Icon(Icons.restaurant_menu), label: 'Plan'),
          BottomNavigationBarItem(
              icon: Icon(Icons.dashboard), label: 'Dashboard'),
        ],
      ),
    );
  }
}
