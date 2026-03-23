import 'dart:convert';

import 'package:http/http.dart' as http;

class ApiClient {
  final String baseUrl;
  const ApiClient({required this.baseUrl});

  Uri _uri(String path) => Uri.parse('$baseUrl$path');

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

class ProfileRequest {
  final double? heightCm;
  final double? heightFt;
  final double? weightKg;
  final double? weightLbs;
  final int age;
  final String gender; // "Nam" / "Nữ"

  ProfileRequest({
    required this.heightCm,
    required this.heightFt,
    required this.weightKg,
    required this.weightLbs,
    required this.age,
    required this.gender,
  });

  Map<String, dynamic> toJson() => {
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

  GoalPlanRequest({
    required this.goalType,
    required this.targetWeightKg,
    required this.timeframeType,
    required this.timeframeValue,
    required this.profileId,
  });

  Map<String, dynamic> toJson() => {
        'goalType': goalType,
        'targetWeightKg': targetWeightKg,
        'timeframeType': timeframeType,
        'timeframeValue': timeframeValue,
        'profileId': profileId,
      };
}

class PlanResponse {
  final int planId;
  final int profileId;
  final DateTime startDate;
  final DateTime endDate;
  final double caloriesPerDay;
  final MealPlanDto planJson;

  PlanResponse({
    required this.planId,
    required this.profileId,
    required this.startDate,
    required this.endDate,
    required this.caloriesPerDay,
    required this.planJson,
  });

  factory PlanResponse.fromJson(Map<String, dynamic> json) {
    return PlanResponse(
      planId: json['planId'] as int,
      profileId: json['profileId'] as int,
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

