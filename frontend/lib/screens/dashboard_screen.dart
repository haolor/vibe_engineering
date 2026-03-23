import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:image_picker/image_picker.dart';

import '../api/api_client.dart';

class DashboardScreen extends StatefulWidget {
  final ApiClient apiClient;
  final int? profileId;
  final PlanResponse? currentPlan;

  const DashboardScreen({
    super.key,
    required this.apiClient,
    required this.profileId,
    required this.currentPlan,
  });

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  final _weightController = TextEditingController();
  final _caloriesController = TextEditingController();
  final _mealNameController = TextEditingController();
  final _ingredientsController = TextEditingController();
  final _originalCaloriesController = TextEditingController();
  final _subMealNameController = TextEditingController();
  final _subIngredientsController = TextEditingController();

  DateTime _logDate = DateTime.now();

  bool _loading = false;
  DashboardResponse? _dashboard;
  MealAnalyzeResponse? _lastMealAnalyze;
  AutoSubstituteResponse? _autoSubstitute;
  ManualSubstituteResponse? _manualSubstitute;
  final ImagePicker _imagePicker = ImagePicker();
  XFile? _pickedImage;
  String? _uploadedImageUrl;
  List<MealDto> _planMealsForDate = [];
  MealDto? _selectedPlanMeal;
  String? _error;

  @override
  void initState() {
    super.initState();
    _syncPlanMealForDate();
    _refresh();
  }

  @override
  void didUpdateWidget(covariant DashboardScreen oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.currentPlan != widget.currentPlan) {
      _syncPlanMealForDate();
    }
  }

  @override
  void dispose() {
    _weightController.dispose();
    _caloriesController.dispose();
    _mealNameController.dispose();
    _ingredientsController.dispose();
    _originalCaloriesController.dispose();
    _subMealNameController.dispose();
    _subIngredientsController.dispose();
    super.dispose();
  }

  double? _tryParseDouble(TextEditingController c) {
    final t = c.text.trim();
    if (t.isEmpty) return null;
    return double.tryParse(t);
  }

  void _refresh() async {
    setState(() => _loading = true);
    try {
      final now = DateTime.now();
      final from = now.subtract(const Duration(days: 30));
      final data = await widget.apiClient.getDashboard(
        profileId: widget.profileId,
        from: from,
        to: now,
      );
      setState(() {
        _dashboard = data;
        _error = null;
      });
    } catch (e) {
      setState(() => _error = e.toString());
    } finally {
      setState(() => _loading = false);
    }
  }

  Future<void> _addWeight() async {
    final w = _tryParseDouble(_weightController);
    if (w == null || w <= 0) {
      _showError('Vui lòng nhập cân nặng (kg) > 0');
      return;
    }

    setState(() => _loading = true);
    try {
      await widget.apiClient.addWeight(
        WeightLogRequest(
          weightKg: w,
          weightLbs: null,
          logDate: _logDate,
          profileId: widget.profileId,
        ),
      );
      _weightController.clear();
      await Future<void>.delayed(const Duration(milliseconds: 50));
      _refresh();
    } catch (e) {
      _showError(e.toString());
      setState(() => _loading = false);
    }
  }

  List<MealIngredientInput> _parseIngredients(String rawText) {
    final lines = rawText
        .split('\n')
        .map((e) => e.trim())
        .where((e) => e.isNotEmpty)
        .toList();
    return lines.map((line) {
      final parts = line.split(':');
      final name = parts.first.trim();
      final amount = parts.length > 1 ? double.tryParse(parts[1].trim()) : null;
      return MealIngredientInput(name: name, quantityText: amount == null ? '1 phan' : parts[1].trim(), amount: amount);
    }).toList();
  }

  Future<void> _analyzeMeal() async {
    final mealName = _mealNameController.text.trim();
    if (mealName.isEmpty) {
      _showError('Nhap ten mon an');
      return;
    }
    setState(() => _loading = true);
    try {
      final resp = await widget.apiClient.analyzeMeal(
        MealAnalyzeRequest(
          profileId: widget.profileId,
          mealName: mealName,
          imageUrl: _uploadedImageUrl,
          ingredients: _parseIngredients(_ingredientsController.text),
        ),
      );
      setState(() => _lastMealAnalyze = resp);
    } catch (e) {
      _showError(e.toString());
    } finally {
      setState(() => _loading = false);
    }
  }

  Future<void> _pickAndUploadImage() async {
    try {
      final picked = await _imagePicker.pickImage(source: ImageSource.gallery);
      if (picked == null) return;
      setState(() {
        _pickedImage = picked;
        _loading = true;
      });
      final uploaded = await widget.apiClient.uploadImage(picked);
      setState(() {
        _uploadedImageUrl = uploaded.secureUrl;
      });
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Upload ảnh thành công')));
    } catch (e) {
      _showError(e.toString());
    } finally {
      if (mounted) {
        setState(() => _loading = false);
      }
    }
  }

  Future<void> _addCalories() async {
    final calories = _tryParseDouble(_caloriesController) ?? _lastMealAnalyze?.totalCalories;
    if (calories == null || calories <= 0) {
      _showError('Nhap calories > 0 hoac phan tich mon an truoc');
      return;
    }
    setState(() => _loading = true);
    try {
      await widget.apiClient.addCalories(
        CalorieLogRequest(
          profileId: widget.profileId,
          caloriesIn: calories,
          logDate: _logDate,
          mealAnalysisId: _lastMealAnalyze?.mealAnalysisId,
          note: _lastMealAnalyze?.mealName,
        ),
      );
      _caloriesController.clear();
      _refresh();
    } catch (e) {
      _showError(e.toString());
      setState(() => _loading = false);
    }
  }

  Future<void> _autoSubstituteCall() async {
    final originalCalories = _tryParseDouble(_originalCaloriesController) ?? _lastMealAnalyze?.totalCalories;
    if (originalCalories == null || originalCalories <= 0) {
      _showError('Nhap original calories hoac phan tich mon an truoc');
      return;
    }
    setState(() => _loading = true);
    try {
      final resp = await widget.apiClient.autoSubstitute(
        AutoSubstituteRequest(
          profileId: widget.profileId,
          originalCalories: originalCalories,
          originalMealAnalysisId: _lastMealAnalyze?.mealAnalysisId,
        ),
      );
      setState(() => _autoSubstitute = resp);
    } catch (e) {
      _showError(e.toString());
    } finally {
      setState(() => _loading = false);
    }
  }

  Future<void> _manualSubstituteCall() async {
    final originalCalories = _tryParseDouble(_originalCaloriesController) ?? _lastMealAnalyze?.totalCalories;
    if (originalCalories == null || originalCalories <= 0) {
      _showError('Nhap original calories hoac phan tich mon an truoc');
      return;
    }
    final name = _subMealNameController.text.trim();
    if (name.isEmpty) {
      _showError('Nhap ten mon thay the');
      return;
    }
    setState(() => _loading = true);
    try {
      final resp = await widget.apiClient.manualSubstitute(
        ManualSubstituteRequest(
          profileId: widget.profileId,
          originalCalories: originalCalories,
          originalMealAnalysisId: _lastMealAnalyze?.mealAnalysisId,
          substituteMealName: name,
          ingredients: _parseIngredients(_subIngredientsController.text),
        ),
      );
      setState(() => _manualSubstitute = resp);
    } catch (e) {
      _showError(e.toString());
    } finally {
      setState(() => _loading = false);
    }
  }

  String _dateOnly(DateTime d) =>
      '${d.year.toString().padLeft(4, '0')}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

  void _syncPlanMealForDate() {
    final plan = widget.currentPlan;
    if (plan == null) {
      setState(() {
        _planMealsForDate = [];
        _selectedPlanMeal = null;
      });
      return;
    }

    final day = plan.planJson.days.where((d) => d.date == _dateOnly(_logDate)).toList();
    if (day.isEmpty || day.first.meals.isEmpty) {
      setState(() {
        _planMealsForDate = [];
        _selectedPlanMeal = null;
        _originalCaloriesController.clear();
      });
      return;
    }

    final meals = day.first.meals;
    final currentSelected = _selectedPlanMeal;
    MealDto? selected;
    if (currentSelected != null) {
      for (final m in meals) {
        if (m.name == currentSelected.name && m.mealType == currentSelected.mealType) {
          selected = m;
          break;
        }
      }
    }
    selected ??= meals.first;

    setState(() {
      _planMealsForDate = meals;
      _selectedPlanMeal = selected ?? meals.first;
      _originalCaloriesController.text = _selectedPlanMeal!.caloriesEstimated.toStringAsFixed(0);
    });
  }

  void _showError(String msg) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg)));
  }

  List<FlSpot> _toSpots(List<WeightPointDto> points) {
    final sorted = [...points];
    // points trả về asc theo date, nhưng cứ giữ an toàn.
    // x = index
    return sorted.asMap().entries.map((e) {
      return FlSpot(e.key.toDouble(), e.value.weightKg);
    }).toList();
  }

  LineChartData _lineData(List<FlSpot> spots) {
    return LineChartData(
      minY: spots.isEmpty ? 0 : spots.map((s) => s.y).reduce((a, b) => a < b ? a : b) - 0.5,
      maxY: spots.isEmpty ? 1 : spots.map((s) => s.y).reduce((a, b) => a > b ? a : b) + 0.5,
      lineBarsData: [
        LineChartBarData(
          spots: spots,
          isCurved: true,
          barWidth: 3,
          dotData: FlDotData(show: true),
        ),
      ],
      gridData: FlGridData(show: true),
      titlesData: FlTitlesData(show: false),
      borderData: FlBorderData(show: false),
    );
  }

  @override
  Widget build(BuildContext context) {
    final weightSpots = _dashboard == null ? <FlSpot>[] : _toSpots(_dashboard!.weightHistory);

    final caloriesPoints = _dashboard?.plannedCaloriesHistory ?? [];
    final caloriesSpots = caloriesPoints.asMap().entries.map((e) {
      return FlSpot(e.key.toDouble(), e.value.caloriesPerDay);
    }).toList();

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Text('Dashboard',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600)),
          const SizedBox(height: 12),

          if (_loading && _dashboard == null) ...[
            const Center(child: Padding(padding: EdgeInsets.all(16), child: CircularProgressIndicator())),
          ] else if (_error != null) ...[
            Text(
              _error!,
              style: TextStyle(color: Theme.of(context).colorScheme.error),
            ),
            const SizedBox(height: 12),
          ],

          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Cân nặng (30 ngày gần đây)', style: TextStyle(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 8),
                  SizedBox(height: 220, child: LineChart(_lineData(weightSpots))),
                  const SizedBox(height: 8),
                  if (_dashboard != null)
                    Text(
                      'Số điểm: ${_dashboard!.weightHistory.length}',
                      style: const TextStyle(color: Colors.grey),
                    ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 12),

          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Calo theo lộ trình (nếu có)', style: TextStyle(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 8),
                  SizedBox(height: 220, child: LineChart(_lineData(caloriesSpots))),
                  const SizedBox(height: 8),
                  if (_dashboard != null)
                    Text(
                      'Số điểm: ${_dashboard!.plannedCaloriesHistory.length}',
                      style: const TextStyle(color: Colors.grey),
                    ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 16),

          const Text('Phan tich mon an (anh URL hoac thanh phan)', style: TextStyle(fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),
          TextField(
            controller: _mealNameController,
            decoration: const InputDecoration(labelText: 'Ten mon an'),
          ),
          const SizedBox(height: 8),
          OutlinedButton.icon(
            onPressed: _loading ? null : _pickAndUploadImage,
            icon: const Icon(Icons.photo_library),
            label: Text(_pickedImage == null ? 'Chon anh mon an' : 'Doi anh: ${_pickedImage!.name}'),
          ),
          if (_uploadedImageUrl != null) ...[
            const SizedBox(height: 6),
            Text(
              'Da upload Cloudinary',
              style: TextStyle(color: Colors.green.shade700),
            ),
          ],
          const SizedBox(height: 8),
          TextField(
            controller: _ingredientsController,
            minLines: 3,
            maxLines: 5,
            decoration: const InputDecoration(
              labelText: 'Thanh phan (moi dong: ten:so_luong)',
              hintText: 'com:1\ntrung:2\nrau:1',
            ),
          ),
          const SizedBox(height: 8),
          ElevatedButton(
            onPressed: _loading ? null : _analyzeMeal,
            child: const Text('Tinh calories mon an'),
          ),
          if (_lastMealAnalyze != null) ...[
            const SizedBox(height: 8),
            Text('Tong: ${_lastMealAnalyze!.totalCalories.toStringAsFixed(0)} kcal'),
            ..._lastMealAnalyze!.ingredients.map(
              (i) => Text('- ${i.name}: ${i.caloriesEstimated.toStringAsFixed(0)} kcal'),
            ),
          ],

          const SizedBox(height: 16),
          const Text('Doi mon tuong tuong', style: TextStyle(fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),
          if (_planMealsForDate.isNotEmpty) ...[
            DropdownButtonFormField<String>(
              value: _selectedPlanMeal == null ? null : '${_selectedPlanMeal!.mealType}|${_selectedPlanMeal!.name}',
              items: _planMealsForDate.map((m) {
                final key = '${m.mealType}|${m.name}';
                return DropdownMenuItem<String>(
                  value: key,
                  child: Text('${m.mealType} - ${m.name}'),
                );
              }).toList(),
              onChanged: (v) {
                if (v == null) return;
                final meal = _planMealsForDate.firstWhere((m) => '${m.mealType}|${m.name}' == v);
                setState(() {
                  _selectedPlanMeal = meal;
                  _originalCaloriesController.text = meal.caloriesEstimated.toStringAsFixed(0);
                });
              },
              decoration: const InputDecoration(labelText: 'Mon an trong lo trinh cua ngay da chon'),
            ),
            const SizedBox(height: 8),
          ],
          TextField(
            controller: _originalCaloriesController,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(labelText: 'Original calories (bo trong neu da phan tich)'),
          ),
          const SizedBox(height: 8),
          ElevatedButton(
            onPressed: _loading ? null : _autoSubstituteCall,
            child: const Text('Goi y doi mon tu dong'),
          ),
          if (_autoSubstitute != null)
            ..._autoSubstitute!.options.map((o) => Text('- ${o.mealName}: ${o.calories.toStringAsFixed(0)} kcal')),
          const SizedBox(height: 8),
          TextField(
            controller: _subMealNameController,
            decoration: const InputDecoration(labelText: 'Ten mon thay the do ban nhap'),
          ),
          const SizedBox(height: 8),
          TextField(
            controller: _subIngredientsController,
            minLines: 2,
            maxLines: 4,
            decoration: const InputDecoration(labelText: 'Thanh phan mon thay the (ten:so_luong)'),
          ),
          const SizedBox(height: 8),
          ElevatedButton(
            onPressed: _loading ? null : _manualSubstituteCall,
            child: const Text('Kiem tra mon thay the'),
          ),
          if (_manualSubstitute != null) ...[
            const SizedBox(height: 8),
            Text(
              '${_manualSubstitute!.substituteMealName}: ${_manualSubstitute!.substituteCalories.toStringAsFixed(0)} kcal',
            ),
            Text(
              _manualSubstitute!.acceptable ? 'Co the thay the' : 'Khong nen thay the',
              style: TextStyle(
                color: _manualSubstitute!.acceptable ? Colors.green : Theme.of(context).colorScheme.error,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],

          const SizedBox(height: 16),
          const Text('Nhap can nang + calo de ve bieu do', style: TextStyle(fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),

          TextField(
            controller: _weightController,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(labelText: 'Cân nặng (kg)'),
          ),
          const SizedBox(height: 8),
          TextField(
            controller: _caloriesController,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(labelText: 'Calo nap vao (kcal)'),
          ),
          const SizedBox(height: 8),

          Row(
            children: [
              Expanded(
                child: Text('Ngày: ${_logDate.toLocal().toString().split(' ')[0]}'),
              ),
              TextButton(
                onPressed: () async {
                  final picked = await showDatePicker(
                    context: context,
                    initialDate: _logDate,
                    firstDate: DateTime(2000),
                    lastDate: DateTime(2100),
                  );
                  if (picked == null) return;
                  setState(() => _logDate = picked);
                  _syncPlanMealForDate();
                },
                child: const Text('Chọn ngày'),
              ),
            ],
          ),

          const SizedBox(height: 8),

          ElevatedButton(
            onPressed: _loading ? null : _addWeight,
            child: _loading
                ? const SizedBox(
                    height: 18,
                    width: 18,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Text('Lưu cân nặng'),
          ),
          const SizedBox(height: 8),
          ElevatedButton(
            onPressed: _loading ? null : _addCalories,
            child: const Text('Luu calories'),
          ),
        ],
      ),
    );
  }
}

