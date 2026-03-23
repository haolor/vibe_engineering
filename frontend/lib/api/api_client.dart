import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:image_picker/image_picker.dart';

class ApiClient {
  final String baseUrl;
  const ApiClient({required this.baseUrl});

  Uri _uri(String path) => Uri.parse('$baseUrl$path');

  Future<AuthResponse> register(RegisterRequest req) async {
    final resp = await http.post(
      _uri('/api/auth/register'),
      headers: const {'Content-Type': 'application/json'},
      body: jsonEncode(req.toJson()),
    );
    _throwIfNotOk(resp);
    return AuthResponse.fromJson(jsonDecode(resp.body) as Map<String, dynamic>);
  }

  Future<AuthResponse> login(LoginRequest req) async {
    final resp = await http.post(
      _uri('/api/auth/login'),
      headers: const {'Content-Type': 'application/json'},
      body: jsonEncode(req.toJson()),
    );
    _throwIfNotOk(resp);
    return AuthResponse.fromJson(jsonDecode(resp.body) as Map<String, dynamic>);
  }

  Future<ProfileResponse> upsertProfile(ProfileRequest req) async {
    final resp = await http.post(
      _uri('/api/profile'),
      headers: const {'Content-Type': 'application/json'},
      body: jsonEncode(req.toJson()),
    );
    _throwIfNotOk(resp);
    return ProfileResponse.fromJson(jsonDecode(resp.body) as Map<String, dynamic>);
  }

  Future<PlanResponse> createPlan(GoalPlanRequest req) async {
    final resp = await http.post(
      _uri('/api/plan'),
      headers: const {'Content-Type': 'application/json'},
      body: jsonEncode(req.toJson()),
    );
    _throwIfNotOk(resp);
    return PlanResponse.fromJson(jsonDecode(resp.body) as Map<String, dynamic>);
  }

  Future<WeightLogResponse> addWeight(WeightLogRequest req) async {
    final resp = await http.post(
      _uri('/api/weights'),
      headers: const {'Content-Type': 'application/json'},
      body: jsonEncode(req.toJson()),
    );
    _throwIfNotOk(resp);
    return WeightLogResponse.fromJson(jsonDecode(resp.body) as Map<String, dynamic>);
  }

  Future<DashboardResponse> getDashboard({
    int? profileId,
    DateTime? from,
    DateTime? to,
  }) async {
    final query = <String, String>{};
    if (profileId != null) query['profileId'] = profileId.toString();
    if (from != null) query['from'] = _dateOnly(from);
    if (to != null) query['to'] = _dateOnly(to);

    final resp = await http.get(
      _uri('/api/dashboard').replace(queryParameters: query.isEmpty ? null : query),
      headers: const {'Content-Type': 'application/json'},
    );
    _throwIfNotOk(resp);
    return DashboardResponse.fromJson(jsonDecode(resp.body) as Map<String, dynamic>);
  }

  Future<BootstrapResponse> getBootstrap(int userId) async {
    final resp = await http.get(
      _uri('/api/bootstrap').replace(queryParameters: {'userId': userId.toString()}),
      headers: const {'Content-Type': 'application/json'},
    );
    _throwIfNotOk(resp);
    return BootstrapResponse.fromJson(jsonDecode(resp.body) as Map<String, dynamic>);
  }

  Future<MealAnalyzeResponse> analyzeMeal(MealAnalyzeRequest req) async {
    final resp = await http.post(
      _uri('/api/meals/analyze'),
      headers: const {'Content-Type': 'application/json'},
      body: jsonEncode(req.toJson()),
    );
    _throwIfNotOk(resp);
    return MealAnalyzeResponse.fromJson(jsonDecode(resp.body) as Map<String, dynamic>);
  }

  Future<AutoSubstituteResponse> autoSubstitute(AutoSubstituteRequest req) async {
    final resp = await http.post(
      _uri('/api/meals/substitute/auto'),
      headers: const {'Content-Type': 'application/json'},
      body: jsonEncode(req.toJson()),
    );
    _throwIfNotOk(resp);
    return AutoSubstituteResponse.fromJson(jsonDecode(resp.body) as Map<String, dynamic>);
  }

  Future<ManualSubstituteResponse> manualSubstitute(ManualSubstituteRequest req) async {
    final resp = await http.post(
      _uri('/api/meals/substitute/manual'),
      headers: const {'Content-Type': 'application/json'},
      body: jsonEncode(req.toJson()),
    );
    _throwIfNotOk(resp);
    return ManualSubstituteResponse.fromJson(jsonDecode(resp.body) as Map<String, dynamic>);
  }

  Future<CalorieLogResponse> addCalories(CalorieLogRequest req) async {
    final resp = await http.post(
      _uri('/api/calories'),
      headers: const {'Content-Type': 'application/json'},
      body: jsonEncode(req.toJson()),
    );
    _throwIfNotOk(resp);
    return CalorieLogResponse.fromJson(jsonDecode(resp.body) as Map<String, dynamic>);
  }

  Future<ImageUploadResponse> uploadImage(XFile file) async {
    final bytes = await file.readAsBytes();
    final request = http.MultipartRequest('POST', _uri('/api/images/upload'))
      ..files.add(http.MultipartFile.fromBytes('file', bytes, filename: file.name));

    final streamed = await request.send();
    final resp = await http.Response.fromStream(streamed);
    _throwIfNotOk(resp);
    return ImageUploadResponse.fromJson(jsonDecode(resp.body) as Map<String, dynamic>);
  }

  void _throwIfNotOk(http.Response resp) {
    if (resp.statusCode >= 200 && resp.statusCode < 300) return;
    throw ApiException(
      'API error ${resp.statusCode}: ${resp.body}',
    );
  }

  String _dateOnly(DateTime d) => '${d.year.toString().padLeft(4, '0')}-'
      '${d.month.toString().padLeft(2, '0')}-'
      '${d.day.toString().padLeft(2, '0')}';
}

class ApiException implements Exception {
  final String message;
  ApiException(this.message);

  @override
  String toString() => message;
}

class RegisterRequest {
  final String email;
  final String password;
  final String? fullName;

  RegisterRequest({
    required this.email,
    required this.password,
    required this.fullName,
  });

  Map<String, dynamic> toJson() => {
        'email': email,
        'password': password,
        'fullName': fullName,
      };
}

class LoginRequest {
  final String email;
  final String password;

  LoginRequest({
    required this.email,
    required this.password,
  });

  Map<String, dynamic> toJson() => {
        'email': email,
        'password': password,
      };
}

class AuthResponse {
  final int userId;
  final String email;
  final String? fullName;
  final String? message;

  AuthResponse({
    required this.userId,
    required this.email,
    required this.fullName,
    required this.message,
  });

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      userId: json['userId'] as int,
      email: json['email'] as String,
      fullName: json['fullName'] as String?,
      message: json['message'] as String?,
    );
  }
}

class BootstrapResponse {
  final ProfileResponse? latestProfile;
  final PlanResponse? latestPlan;

  BootstrapResponse({
    required this.latestProfile,
    required this.latestPlan,
  });

  factory BootstrapResponse.fromJson(Map<String, dynamic> json) {
    return BootstrapResponse(
      latestProfile: json['latestProfile'] == null
          ? null
          : ProfileResponse.fromJson(json['latestProfile'] as Map<String, dynamic>),
      latestPlan: json['latestPlan'] == null
          ? null
          : PlanResponse.fromJson(json['latestPlan'] as Map<String, dynamic>),
    );
  }
}

class ProfileRequest {
  final int? userId;
  final double? heightCm;
  final double? heightFt;
  final double? weightKg;
  final double? weightLbs;
  final int age;
  final String gender; // "Nam" / "Nữ"

  ProfileRequest({
    required this.userId,
    required this.heightCm,
    required this.heightFt,
    required this.weightKg,
    required this.weightLbs,
    required this.age,
    required this.gender,
  });

  Map<String, dynamic> toJson() => {
        'userId': userId,
        'heightCm': heightCm,
        'heightFt': heightFt,
        'weightKg': weightKg,
        'weightLbs': weightLbs,
        'age': age,
        'gender': gender,
      };
}

class ProfileResponse {
  final int profileId;
  final double heightCm;
  final double weightKg;
  final int age;
  final String gender;
  final double bmi;

  ProfileResponse({
    required this.profileId,
    required this.heightCm,
    required this.weightKg,
    required this.age,
    required this.gender,
    required this.bmi,
  });

  factory ProfileResponse.fromJson(Map<String, dynamic> json) {
    return ProfileResponse(
      profileId: json['profileId'] as int,
      heightCm: (json['heightCm'] as num).toDouble(),
      weightKg: (json['weightKg'] as num).toDouble(),
      age: json['age'] as int,
      gender: json['gender'] as String,
      bmi: (json['bmi'] as num).toDouble(),
    );
  }
}

class GoalPlanRequest {
  final String goalType; // "GIAM"/"TANG"
  final double targetWeightKg;
  final String timeframeType; // "DAYS"/"WEEKS"/"MONTHS"/"YEARS"
  final int timeframeValue;
  final int? profileId;
  final int? userId;

  GoalPlanRequest({
    required this.goalType,
    required this.targetWeightKg,
    required this.timeframeType,
    required this.timeframeValue,
    required this.profileId,
    required this.userId,
  });

  Map<String, dynamic> toJson() => {
        'goalType': goalType,
        'targetWeightKg': targetWeightKg,
        'timeframeType': timeframeType,
        'timeframeValue': timeframeValue,
        'profileId': profileId,
        'userId': userId,
      };
}

class PlanResponse {
  final int planId;
  final int profileId;
  final String? goalType;
  final double? targetWeightKg;
  final DateTime startDate;
  final DateTime endDate;
  final double caloriesPerDay;
  final MealPlanDto planJson;

  PlanResponse({
    required this.planId,
    required this.profileId,
    required this.goalType,
    required this.targetWeightKg,
    required this.startDate,
    required this.endDate,
    required this.caloriesPerDay,
    required this.planJson,
  });

  factory PlanResponse.fromJson(Map<String, dynamic> json) {
    return PlanResponse(
      planId: json['planId'] as int,
      profileId: json['profileId'] as int,
      goalType: json['goalType'] as String?,
      targetWeightKg: (json['targetWeightKg'] as num?)?.toDouble(),
      startDate: DateTime.parse(json['startDate'] as String),
      endDate: DateTime.parse(json['endDate'] as String),
      caloriesPerDay: (json['caloriesPerDay'] as num).toDouble(),
      planJson: MealPlanDto.fromJson(json['planJson'] as Map<String, dynamic>),
    );
  }
}

class WeightLogRequest {
  final double? weightKg;
  final double? weightLbs;
  final DateTime? logDate;
  final int? profileId;

  WeightLogRequest({
    required this.weightKg,
    required this.weightLbs,
    required this.logDate,
    required this.profileId,
  });

  Map<String, dynamic> toJson() => {
        'weightKg': weightKg,
        'weightLbs': weightLbs,
        'logDate': logDate == null
            ? null
            : '${logDate!.year.toString().padLeft(4, '0')}-'
                '${logDate!.month.toString().padLeft(2, '0')}-'
                '${logDate!.day.toString().padLeft(2, '0')}',
        'profileId': profileId,
      };
}

class WeightLogResponse {
  final int logId;
  final DateTime logDate;
  final double weightKg;

  WeightLogResponse({
    required this.logId,
    required this.logDate,
    required this.weightKg,
  });

  factory WeightLogResponse.fromJson(Map<String, dynamic> json) {
    return WeightLogResponse(
      logId: json['logId'] as int,
      logDate: DateTime.parse(json['logDate'] as String),
      weightKg: (json['weightKg'] as num).toDouble(),
    );
  }
}

class CalorieLogRequest {
  final int? profileId;
  final double caloriesIn;
  final DateTime? logDate;
  final int? mealAnalysisId;
  final String? note;

  CalorieLogRequest({
    required this.profileId,
    required this.caloriesIn,
    required this.logDate,
    required this.mealAnalysisId,
    required this.note,
  });

  Map<String, dynamic> toJson() => {
        'profileId': profileId,
        'caloriesIn': caloriesIn,
        'logDate': logDate == null ? null : _dateOnlyStatic(logDate!),
        'mealAnalysisId': mealAnalysisId,
        'note': note,
      };
}

class CalorieLogResponse {
  final int logId;
  final DateTime logDate;
  final double caloriesIn;

  CalorieLogResponse({
    required this.logId,
    required this.logDate,
    required this.caloriesIn,
  });

  factory CalorieLogResponse.fromJson(Map<String, dynamic> json) {
    return CalorieLogResponse(
      logId: json['logId'] as int,
      logDate: DateTime.parse(json['logDate'] as String),
      caloriesIn: (json['caloriesIn'] as num).toDouble(),
    );
  }
}

class ImageUploadResponse {
  final String secureUrl;
  final String publicId;

  ImageUploadResponse({
    required this.secureUrl,
    required this.publicId,
  });

  factory ImageUploadResponse.fromJson(Map<String, dynamic> json) {
    return ImageUploadResponse(
      secureUrl: json['secureUrl'] as String,
      publicId: json['publicId'] as String,
    );
  }
}

class MealIngredientInput {
  final String name;
  final String? quantityText;
  final double? amount;

  MealIngredientInput({
    required this.name,
    required this.quantityText,
    required this.amount,
  });

  Map<String, dynamic> toJson() => {
        'name': name,
        'quantityText': quantityText,
        'amount': amount,
      };
}

class MealAnalyzeRequest {
  final int? profileId;
  final String mealName;
  final String? imageUrl;
  final List<MealIngredientInput> ingredients;

  MealAnalyzeRequest({
    required this.profileId,
    required this.mealName,
    required this.imageUrl,
    required this.ingredients,
  });

  Map<String, dynamic> toJson() => {
        'profileId': profileId,
        'mealName': mealName,
        'imageUrl': imageUrl,
        'ingredients': ingredients.map((e) => e.toJson()).toList(),
      };
}

class MealIngredientResult {
  final String name;
  final String? quantityText;
  final double caloriesEstimated;

  MealIngredientResult({
    required this.name,
    required this.quantityText,
    required this.caloriesEstimated,
  });

  factory MealIngredientResult.fromJson(Map<String, dynamic> json) {
    return MealIngredientResult(
      name: json['name'] as String,
      quantityText: json['quantityText'] as String?,
      caloriesEstimated: (json['caloriesEstimated'] as num).toDouble(),
    );
  }
}

class MealAnalyzeResponse {
  final int mealAnalysisId;
  final String mealName;
  final double totalCalories;
  final List<MealIngredientResult> ingredients;

  MealAnalyzeResponse({
    required this.mealAnalysisId,
    required this.mealName,
    required this.totalCalories,
    required this.ingredients,
  });

  factory MealAnalyzeResponse.fromJson(Map<String, dynamic> json) {
    return MealAnalyzeResponse(
      mealAnalysisId: json['mealAnalysisId'] as int,
      mealName: json['mealName'] as String,
      totalCalories: (json['totalCalories'] as num).toDouble(),
      ingredients: (json['ingredients'] as List<dynamic>? ?? [])
          .map((e) => MealIngredientResult.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
  }
}

class AutoSubstituteRequest {
  final int? profileId;
  final double? originalCalories;
  final int? originalMealAnalysisId;

  AutoSubstituteRequest({
    required this.profileId,
    required this.originalCalories,
    required this.originalMealAnalysisId,
  });

  Map<String, dynamic> toJson() => {
        'profileId': profileId,
        'originalCalories': originalCalories,
        'originalMealAnalysisId': originalMealAnalysisId,
      };
}

class SubstituteOption {
  final String mealName;
  final double calories;
  final double deltaCalories;

  SubstituteOption({
    required this.mealName,
    required this.calories,
    required this.deltaCalories,
  });

  factory SubstituteOption.fromJson(Map<String, dynamic> json) {
    return SubstituteOption(
      mealName: json['mealName'] as String,
      calories: (json['calories'] as num).toDouble(),
      deltaCalories: (json['deltaCalories'] as num).toDouble(),
    );
  }
}

class AutoSubstituteResponse {
  final double originalCalories;
  final List<SubstituteOption> options;

  AutoSubstituteResponse({
    required this.originalCalories,
    required this.options,
  });

  factory AutoSubstituteResponse.fromJson(Map<String, dynamic> json) {
    return AutoSubstituteResponse(
      originalCalories: (json['originalCalories'] as num).toDouble(),
      options: (json['options'] as List<dynamic>? ?? [])
          .map((e) => SubstituteOption.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
  }
}

class ManualSubstituteRequest {
  final int? profileId;
  final double? originalCalories;
  final int? originalMealAnalysisId;
  final String substituteMealName;
  final List<MealIngredientInput> ingredients;

  ManualSubstituteRequest({
    required this.profileId,
    required this.originalCalories,
    required this.originalMealAnalysisId,
    required this.substituteMealName,
    required this.ingredients,
  });

  Map<String, dynamic> toJson() => {
        'profileId': profileId,
        'originalCalories': originalCalories,
        'originalMealAnalysisId': originalMealAnalysisId,
        'substituteMealName': substituteMealName,
        'ingredients': ingredients.map((e) => e.toJson()).toList(),
      };
}

class ManualSubstituteResponse {
  final String substituteMealName;
  final double originalCalories;
  final double substituteCalories;
  final bool acceptable;
  final String note;

  ManualSubstituteResponse({
    required this.substituteMealName,
    required this.originalCalories,
    required this.substituteCalories,
    required this.acceptable,
    required this.note,
  });

  factory ManualSubstituteResponse.fromJson(Map<String, dynamic> json) {
    return ManualSubstituteResponse(
      substituteMealName: json['substituteMealName'] as String,
      originalCalories: (json['originalCalories'] as num).toDouble(),
      substituteCalories: (json['substituteCalories'] as num).toDouble(),
      acceptable: json['acceptable'] as bool,
      note: json['note'] as String,
    );
  }
}

class DashboardResponse {
  final int profileId;
  final List<WeightPointDto> weightHistory;
  final List<CaloriesPointDto> plannedCaloriesHistory;

  DashboardResponse({
    required this.profileId,
    required this.weightHistory,
    required this.plannedCaloriesHistory,
  });

  factory DashboardResponse.fromJson(Map<String, dynamic> json) {
    final weight = (json['weightHistory'] as List<dynamic>? ?? [])
        .map((e) => WeightPointDto.fromJson(e as Map<String, dynamic>))
        .toList();
    final calories = (json['plannedCaloriesHistory'] as List<dynamic>? ?? [])
        .map((e) => CaloriesPointDto.fromJson(e as Map<String, dynamic>))
        .toList();

    return DashboardResponse(
      profileId: json['profileId'] as int,
      weightHistory: weight,
      plannedCaloriesHistory: calories,
    );
  }
}

class WeightPointDto {
  final String date; // yyyy-MM-dd
  final double weightKg;
  WeightPointDto({required this.date, required this.weightKg});

  factory WeightPointDto.fromJson(Map<String, dynamic> json) {
    return WeightPointDto(
      date: json['date'] as String,
      weightKg: (json['weightKg'] as num).toDouble(),
    );
  }
}

class CaloriesPointDto {
  final String date;
  final double caloriesPerDay;
  CaloriesPointDto({required this.date, required this.caloriesPerDay});

  factory CaloriesPointDto.fromJson(Map<String, dynamic> json) {
    return CaloriesPointDto(
      date: json['date'] as String,
      caloriesPerDay: (json['caloriesPerDay'] as num).toDouble(),
    );
  }
}

class MealPlanDto {
  final String planTitle;
  final double caloriesPerDay;
  final List<MealPlanDayDto> days;

  MealPlanDto({
    required this.planTitle,
    required this.caloriesPerDay,
    required this.days,
  });

  factory MealPlanDto.fromJson(Map<String, dynamic> json) {
    return MealPlanDto(
      planTitle: json['planTitle'] as String,
      caloriesPerDay: (json['caloriesPerDay'] as num).toDouble(),
      days: (json['days'] as List<dynamic>? ?? [])
          .map((e) => MealPlanDayDto.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
  }
}

class MealPlanDayDto {
  final int dayIndex;
  final String date;
  final List<MealDto> meals;

  MealPlanDayDto({
    required this.dayIndex,
    required this.date,
    required this.meals,
  });

  factory MealPlanDayDto.fromJson(Map<String, dynamic> json) {
    return MealPlanDayDto(
      dayIndex: json['dayIndex'] as int,
      date: json['date'] as String,
      meals: (json['meals'] as List<dynamic>? ?? [])
          .map((e) => MealDto.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
  }
}

class MealDto {
  final String mealType;
  final String name;
  final String description;
  final double caloriesEstimated;

  MealDto({
    required this.mealType,
    required this.name,
    required this.description,
    required this.caloriesEstimated,
  });

  factory MealDto.fromJson(Map<String, dynamic> json) {
    return MealDto(
      mealType: json['mealType'] as String,
      name: json['name'] as String,
      description: json['description'] as String,
      caloriesEstimated: (json['caloriesEstimated'] as num).toDouble(),
    );
  }
}

String _dateOnlyStatic(DateTime d) => '${d.year.toString().padLeft(4, '0')}-'
    '${d.month.toString().padLeft(2, '0')}-'
    '${d.day.toString().padLeft(2, '0')}';

