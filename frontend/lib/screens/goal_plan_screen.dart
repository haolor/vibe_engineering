import 'package:flutter/material.dart';

import '../api/api_client.dart';

class GoalPlanScreen extends StatefulWidget {
  final ApiClient apiClient;
  final int? profileId;
  final PlanResponse? initialPlan;
  final void Function(PlanResponse plan) onPlanCreated;

  const GoalPlanScreen({
    super.key,
    required this.apiClient,
    required this.profileId,
    required this.initialPlan,
    required this.onPlanCreated,
  });

  @override
  State<GoalPlanScreen> createState() => _GoalPlanScreenState();
}

class _GoalPlanScreenState extends State<GoalPlanScreen> {
  final _targetWeightController = TextEditingController();
  final _timeframeValueController = TextEditingController(text: '4');

  String _goalType = 'GIAM';
  String _timeframeType = 'WEEKS';

  bool _loading = false;

  PlanResponse? _plan;

  @override
  void initState() {
    super.initState();
    _plan = widget.initialPlan;
  }

  @override
  void dispose() {
    _targetWeightController.dispose();
    _timeframeValueController.dispose();
    super.dispose();
  }

  double? _tryParseDouble(TextEditingController c) {
    final t = c.text.trim();
    if (t.isEmpty) return null;
    return double.tryParse(t);
  }

  int? _tryParseInt(TextEditingController c) {
    final t = c.text.trim();
    if (t.isEmpty) return null;
    return int.tryParse(t);
  }

  Future<void> _submit() async {
    final targetWeightKg = _tryParseDouble(_targetWeightController);
    final timeframeValue = _tryParseInt(_timeframeValueController);

    if (targetWeightKg == null || targetWeightKg <= 0) {
      _showError('Vui lòng nhập cân nặng mục tiêu (targetWeightKg) > 0');
      return;
    }
    if (timeframeValue == null || timeframeValue <= 0) {
      _showError('Vui lòng nhập timeframeValue > 0');
      return;
    }

    setState(() => _loading = true);
    try {
      final plan = await widget.apiClient.createPlan(
        GoalPlanRequest(
          goalType: _goalType,
          targetWeightKg: targetWeightKg,
          timeframeType: _timeframeType,
          timeframeValue: timeframeValue,
          profileId: widget.profileId,
        ),
      );

      setState(() => _plan = plan);
      widget.onPlanCreated(plan);
    } catch (e) {
      _showError(e.toString());
    } finally {
      setState(() => _loading = false);
    }
  }

  void _showError(String msg) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(msg)),
    );
  }

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Text('Tạo lộ trình ăn uống',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600)),
          const SizedBox(height: 12),

          DropdownButtonFormField<String>(
            value: _goalType,
            items: const [
              DropdownMenuItem(value: 'GIAM', child: Text('Giảm cân')),
              DropdownMenuItem(value: 'TANG', child: Text('Tăng cân')),
            ],
            onChanged: (v) {
              if (v == null) return;
              setState(() => _goalType = v);
            },
            decoration: const InputDecoration(labelText: 'Nhu cầu'),
          ),

          const SizedBox(height: 8),

          TextField(
            controller: _targetWeightController,
            keyboardType: TextInputType.number,
            decoration:
                const InputDecoration(labelText: 'Cân nặng mục tiêu (kg)'),
          ),

          const SizedBox(height: 8),

          Row(
            children: [
              Expanded(
                child: DropdownButtonFormField<String>(
                  value: _timeframeType,
                  items: const [
                    DropdownMenuItem(value: 'DAYS', child: Text('Ngày')),
                    DropdownMenuItem(value: 'WEEKS', child: Text('Tuần')),
                    DropdownMenuItem(value: 'MONTHS', child: Text('Tháng')),
                    DropdownMenuItem(value: 'YEARS', child: Text('Năm')),
                  ],
                  onChanged: (v) {
                    if (v == null) return;
                    setState(() => _timeframeType = v);
                  },
                  decoration:
                      const InputDecoration(labelText: 'Khoảng thời gian'),
                ),
              ),
              const SizedBox(width: 8),
              SizedBox(
                width: 100,
                child: TextField(
                  controller: _timeframeValueController,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(labelText: 'Giá trị'),
                ),
              )
            ],
          ),

          const SizedBox(height: 16),

          ElevatedButton(
            onPressed: _loading ? null : _submit,
            child: _loading
                ? const SizedBox(
                    height: 18,
                    width: 18,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Text('Tạo lộ trình'),
          ),

          if (_plan != null) ...[
            const SizedBox(height: 16),
            Text(
              'Calories / ngày: ${_plan!.caloriesPerDay.toStringAsFixed(0)} kcal',
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 8),
            Text('Ngày 1: ${_plan!.planJson.days.isNotEmpty ? _plan!.planJson.days.first.date : '-'}'),
            const SizedBox(height: 8),
            ..._plan!.planJson.days.isNotEmpty
                ? _plan!.planJson.days.first.meals.map((m) {
                    return Card(
                      margin: const EdgeInsets.only(bottom: 8),
                      child: ListTile(
                        title: Text(m.name),
                        subtitle: Text('${m.mealType} • ${m.description}'),
                        trailing: Text('${m.caloriesEstimated.toStringAsFixed(0)} kcal'),
                      ),
                    );
                  }).toList()
                : [const Text('Chưa có dữ liệu ngày 1')],
          ],

          if (widget.profileId == null) ...[
            const SizedBox(height: 16),
            Text(
              'Chưa có profileId. Bạn nên tạo Profile ở tab Profile trước.',
              style: TextStyle(color: Theme.of(context).colorScheme.error),
            ),
          ]
        ],
      ),
    );
  }
}

