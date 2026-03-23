import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';

import '../api/api_client.dart';

class DashboardScreen extends StatefulWidget {
  final ApiClient apiClient;
  final int? profileId;

  const DashboardScreen({
    super.key,
    required this.apiClient,
    required this.profileId,
  });

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  final _weightController = TextEditingController();

  DateTime _logDate = DateTime.now();

  bool _loading = false;
  DashboardResponse? _dashboard;
  String? _error;

  @override
  void initState() {
    super.initState();
    _refresh();
  }

  @override
  void dispose() {
    _weightController.dispose();
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

          const Text('Nhập cân nặng hôm nay', style: TextStyle(fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),

          TextField(
            controller: _weightController,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(labelText: 'Cân nặng (kg)'),
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
        ],
      ),
    );
  }
}

